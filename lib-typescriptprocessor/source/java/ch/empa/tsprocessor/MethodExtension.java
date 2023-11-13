

/*
 *
 *
 * Copyright 2023 Simone Baffelli (simone.baffelli@empa.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.empa.tsprocessor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.habarta.typescript.generator.*;
import cz.habarta.typescript.generator.compiler.ModelCompiler;
import cz.habarta.typescript.generator.compiler.SymbolTable;
import cz.habarta.typescript.generator.compiler.TsModelTransformer;
import cz.habarta.typescript.generator.emitter.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Functional interface to specify the type of method that makes a function. This is needed because we have different
 * implementations depending on whether the method returns a promise or not.
 */
interface MethodProcessor {
    TsPropertyModel makeFunction(Method method, TsModel model, ProcessingContext processingContext);
}

/**
 * This class is used to pass the symbol table and the type processor to the method processor. It was meant for easier refactoring
 * of the methods below.
 *
 * @author Simone Baffelli
 */
class ProcessingContext {
    private final SymbolTable symbolTable;
    private final TypeProcessor localProcessor;

    ProcessingContext(SymbolTable symbolTable, TypeProcessor localProcessor) {
        this.symbolTable = symbolTable;
        this.localProcessor = localProcessor;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }


    public TypeProcessor getLocalProcessor() {
        return localProcessor;
    }
}

/**
 * This extension for the typescript-generator ({@link cz.habarta.typescript.generator}) Gradle <a href="URL#https://github.com/vojtechhabarta/typescript-generator">plugin</a> adds method and constructor signatures to the generated typescript interfaces.
 * The methods are extracted from the java classes using reflection. Currently, it can only create interface signatures to be exported in a d.ts. file, not the implementation.
 * The extensions can be configured to process certain classes as RPC classes, i.e. the methods of these classes will return a promise instead of a value.
 *
 * @author Simone Baffelli
 */
public class MethodExtension extends Extension {

    //Classes whose methods should return a promise instead of a value. This is useful for methods that are called through a REST API/RPC
    //Perhaps an alternative would be to use an annotation to mark the methods that should return a promise
    public static final String CFG_ASYNC_CLASSES = "asyncClasses";
    static final ObjectMapper mapper = new ObjectMapper();
    private static final String excludedMethods = "hashCode|toString|equals";
    private static final Logger logger = TypeScriptGenerator.getLogger();

    private List<String> asnycClasses = new ArrayList<>();

    public MethodExtension() {
    }

    /**
     * Constructor used by the gradle plugin to pass the configuration of the extension.
     *
     * @param asyncClasses a json string with the list of classes whose methods should return a promise
     */
    public MethodExtension(List<String> asyncClasses) {
        this.asnycClasses = asyncClasses;
    }

    /**
     * This method is used to filter out the methods that should not be added to the typescript interface.
     *
     * @param method the method to be filtered
     * @return true if the method should be added to the typescript interface, false otherwise
     */
    private static boolean filterMethods(Method method) {
        return !((method.getDeclaringClass() == Object.class) || (method.getName().matches(excludedMethods)));
    }


    private static List<TsParameter> getMethodParameters(Executable method, TsModel model, ProcessingContext processingContext) {
        return Arrays.stream(method.getParameters()).map(parameter -> new TsParameter(parameter.getName(), resolveGenericType(parameter.getParameterizedType(), model, processingContext))).collect(Collectors.toList());
    }


    private static TsType resolveGenericType(Type type, TsModel model, ProcessingContext processingContext) {
        //This is not very elegant because we are calling again the type processor. Maybe later on we can find a way to get the types from the model
        TypeProcessor.Context context = new TypeProcessor.Context(processingContext.getSymbolTable(), processingContext.getLocalProcessor(), null);
        return context.processType(type).getTsType();

    }

    private static TsType getReturnType(Method method, TsModel model, ProcessingContext processingContext) {
        return resolveGenericType(method.getGenericReturnType(), model, processingContext);

    }

    private static TsType.FunctionType makeFunctionType(Method method, TsModel model, ProcessingContext processingContext) {
        logger.info(String.format("Processing method %s, with params %s and return type %s", method, method.getParameters(), method.getGenericReturnType()));
        List<TsParameter> params = getMethodParameters(method, model, processingContext);
        TsType returnType = getReturnType(method, model, processingContext);
        return new TsType.FunctionType(params, returnType);
    }

    private static TsPropertyModel makeFunction(Method method, TsModel model, ProcessingContext processingContext) {
        return new TsPropertyModel(method.getName(), makeFunctionType(method, model, processingContext), TsModifierFlags.None, false, null);
    }

    private static TsPropertyModel makePromiseReturningFunction(Method method, TsModel model, ProcessingContext processingContext) {
        TsType.FunctionType syncFunction = makeFunctionType(method, model, processingContext);
        TsType promiseType = new TsType.GenericBasicType("Promise", List.of(syncFunction.type));
        return new TsPropertyModel(method.getName(), new TsType.FunctionType(syncFunction.parameters, promiseType), TsModifierFlags.None, false, null);
    }

    private static Optional<TsMethodModel> propertyModelToMethodModel(TsPropertyModel propertyModel) {
        TsType value = propertyModel.getTsType();
        if (value instanceof TsType.FunctionType) {
            TsType.FunctionType functionType = (TsType.FunctionType) value;
            List<TsParameterModel> params = functionType.parameters.stream().map(param -> new TsParameterModel(param.name, param.getTsType())).collect(Collectors.toList());
            return Optional.of(new TsMethodModel(propertyModel.getName(), propertyModel.getModifiers(), null, params, functionType.type, null, null));
        } else {
            return Optional.empty();
        }
    }


    /**
     * Constructs a typescript constructor signature from a java constructor
     *
     * @param constructor       the java constructor
     * @param beanModel         the bean model that contains the constructor
     * @param model
     * @param processingContext the context of the processing used for type resolution
     * @return the typescript constructor signature or null if the constructor is not public
     */
    private static TsMethodModel makeConstructor(Constructor<?> constructor, TsBeanModel beanModel, TsModel model, ProcessingContext processingContext) {
        List<TsParameter> params = getMethodParameters(constructor, model, processingContext);
        List<TsParameter> paramsWithoutDeclaringClass = params.stream().collect(Collectors.toList());
        TsType returnType = resolveGenericType(constructor.getDeclaringClass(), model, processingContext);
        logger.info(String.format("Processing constructor %s, with params %s and return type %s", constructor, params, returnType));
        TsType.FunctionType functionType = new TsType.FunctionType(paramsWithoutDeclaringClass, returnType);
        TsPropertyModel propertyModel = new TsPropertyModel("new ", functionType, TsModifierFlags.None, false, null);
        return propertyModelToMethodModel(propertyModel).orElse(null);
    }

    private static TsBeanModel addFunctions(TsBeanModel bean, TsModel model, ProcessingContext processingContext, MethodProcessor processor) {
        Class<?> origin = bean.getOrigin();
        Stream<TsMethodModel> params = Arrays.stream(origin.getMethods()).filter(MethodExtension::filterMethods).map(method -> propertyModelToMethodModel(processor.makeFunction(method, model, processingContext))).flatMap(it -> it.map(Stream::of).orElse(Stream.empty()));
        Stream<TsMethodModel> constructors = Arrays.stream(origin.getDeclaredConstructors()).map(constructor -> makeConstructor(constructor, bean, model, processingContext));
        List<TsMethodModel> allMethods = Stream.of(params, constructors).flatMap(it -> it).collect(Collectors.toList());
        return bean.withProperties(bean.getProperties()).withMethods(allMethods);
    }


    @Override
    public void setConfiguration(Map<String, String> configuration) throws RuntimeException {

        if (configuration.containsKey(CFG_ASYNC_CLASSES)) {

            String classString = configuration.get(CFG_ASYNC_CLASSES);
            try {
                logger.info(String.format("MethodExtension: setConfiguration, %s, %s", configuration, classString));
                asnycClasses = mapper.readValue(classString, new TypeReference<ArrayList<String>>() {
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public EmitterExtensionFeatures getFeatures() {
        final EmitterExtensionFeatures features = new EmitterExtensionFeatures();
        features.generatesRuntimeCode = false;
        features.generatesModuleCode = true;
        features.worksWithPackagesMappedToNamespaces = true;
        features.generatesJaxrsApplicationClient = false;
        return features;
    }


    @Override
    public List<TransformerDefinition> getTransformers() {
        return List.of(new TransformerDefinition(ModelCompiler.TransformationPhase.AfterDeclarationSorting, (TsModelTransformer) (context, model) -> {
            logger.info("Started processing methods");
            //Extract all types that were mapped by the bean model to reuse them
            TypeProcessor localProcessor = new DefaultTypeProcessor();
            ProcessingContext processingContext = new ProcessingContext(context.getSymbolTable(), localProcessor);
            //Add table of mapped types
            Stream<TsBeanModel> processedBeans = model.getBeans().stream().map(bean -> {
                if (asnycClasses.contains(bean.getOrigin().getName())) {
                    return addFunctions(bean, model, processingContext, MethodExtension::makePromiseReturningFunction);
                } else {
                    return addFunctions(bean, model, processingContext, MethodExtension::makeFunction);
                }
            });
            return model.withBeans(processedBeans.collect(Collectors.toList()));
        }));
    }


}
