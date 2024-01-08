

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

package ch.ethz.sis.openbis.generic.typescript;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.reflect.TypeToken;

import ch.ethz.sis.openbis.generic.OpenBIS;
import ch.systemsx.cisd.base.annotation.JsonObject;
import cz.habarta.typescript.generator.DefaultTypeProcessor;
import cz.habarta.typescript.generator.Extension;
import cz.habarta.typescript.generator.Logger;
import cz.habarta.typescript.generator.Settings;
import cz.habarta.typescript.generator.TsParameter;
import cz.habarta.typescript.generator.TsType;
import cz.habarta.typescript.generator.TypeProcessor;
import cz.habarta.typescript.generator.TypeScriptGenerator;
import cz.habarta.typescript.generator.compiler.ModelCompiler;
import cz.habarta.typescript.generator.compiler.Symbol;
import cz.habarta.typescript.generator.compiler.SymbolTable;
import cz.habarta.typescript.generator.compiler.TsModelTransformer;
import cz.habarta.typescript.generator.emitter.EmitterExtensionFeatures;
import cz.habarta.typescript.generator.emitter.TsBeanCategory;
import cz.habarta.typescript.generator.emitter.TsBeanModel;
import cz.habarta.typescript.generator.emitter.TsMethodModel;
import cz.habarta.typescript.generator.emitter.TsModel;
import cz.habarta.typescript.generator.emitter.TsModifierFlags;
import cz.habarta.typescript.generator.emitter.TsParameterModel;
import cz.habarta.typescript.generator.emitter.TsPropertyModel;

/**
 * Functional interface to specify the type of method that makes a function. This is needed because we have different
 * implementations depending on whether the method returns a promise or not.
 */
interface MethodProcessor
{
    TsPropertyModel makeFunction(TsBeanModel bean, Method method, TsModel model, ProcessingContext processingContext);
}

/**
 * This class is used to pass the symbol table and the type processor to the method processor. It was meant for easier refactoring
 * of the methods below.
 *
 * @author Simone Baffelli
 */
class ProcessingContext
{
    private final SymbolTable symbolTable;

    private final TypeProcessor localProcessor;

    ProcessingContext(SymbolTable symbolTable, TypeProcessor localProcessor)
    {
        this.symbolTable = symbolTable;
        this.localProcessor = localProcessor;
    }

    public SymbolTable getSymbolTable()
    {
        return symbolTable;
    }

    public TypeProcessor getLocalProcessor()
    {
        return localProcessor;
    }
}

/**
 * This extension for the typescript-generator ({@link cz.habarta.typescript.generator}) Gradle <a href="URL#https://github.com/vojtechhabarta/typescript-generator">plugin</a> adds method and constructor signatures to the generated typescript interfaces.
 * The methods are extracted from the java classes using reflection. Currently, it can only create interface signatures to be exported in a d.ts. file, not the implementation.
 * The extensions can be configured to process certain classes as RPC classes, i.e. the methods of these classes will return a promise instead of a value.
 *
 * @author Simone Baffelli
 * @author pkupczyk
 */
public class OpenBISExtension extends Extension
{

    private static final Logger logger = TypeScriptGenerator.getLogger();

    private static final String excludedMethods = "hashCode|toString|equals";

    public OpenBISExtension()
    {
    }

    /**
     * This method is used to filter out the methods that should not be added to the typescript interface.
     *
     * @param method the method to be filtered
     * @return true if the method should be added to the typescript interface, false otherwise
     */
    private static boolean filterMethods(Method method)
    {
        return !(method.isBridge() || (method.getDeclaringClass() == Object.class) || (method.getName().matches(excludedMethods)));
    }

    private static List<TsParameter> getMethodParameters(TsBeanModel bean, Executable method, TsModel model, ProcessingContext processingContext)
    {
        return Arrays.stream(method.getParameters()).map(parameter -> new TsParameter(parameter.getName(),
                resolveGenericType(bean.getOrigin(), parameter.getParameterizedType(), model, processingContext))).collect(Collectors.toList());
    }

    private static TsType resolveGenericType(Class<?> clazz, Type type, TsModel model, ProcessingContext processingContext)
    {
        TypeToken<?> typeToken = TypeToken.of(clazz).resolveType(type);
        //This is not very elegant because we are calling again the type processor. Maybe later on we can find a way to get the types from the model
        TypeProcessor.Context context = new TypeProcessor.Context(processingContext.getSymbolTable(), processingContext.getLocalProcessor(), null);
        TsType tsType = context.processType(typeToken.getType()).getTsType();

        if (tsType instanceof TsType.ReferenceType)
        {
            if (!((TsType.ReferenceType) tsType).symbol.isResolved())
            {
                throw new UnresolvedTypeException(clazz, type);
            }
        }

        return tsType;
    }

    private static List<TsType.GenericVariableType> resolveTypeParameters(TsBeanModel bean, TsModel model, ProcessingContext processingContext,
            boolean withBounds)
    {
        TypeVariable<? extends Class<?>>[] typeParameters = bean.getOrigin().getTypeParameters();
        return Arrays.stream(typeParameters).map(t ->
        {
            Type[] boundsTypes = t.getBounds();
            if (withBounds && boundsTypes.length > 0)
            {
                try
                {
                    List<String> boundsStrings = new ArrayList<>();
                    for (Type boundType : boundsTypes)
                    {
                        TsType tsBoundType = resolveGenericType(bean.getOrigin(), boundType, model, processingContext);
                        boundsStrings.add(tsBoundType.toString());
                    }

                    return new TsType.GenericVariableType(t.getName() + " extends " + boundsStrings.get(0));
                } catch (UnresolvedTypeException e)
                {
                    return new TsType.GenericVariableType(t.getName());
                }
            } else
            {
                return new TsType.GenericVariableType(t.getName());
            }
        }).collect(Collectors.toList());
    }

    private static TsType getReturnType(TsBeanModel bean, Method method, TsModel model, ProcessingContext processingContext)
    {
        return resolveGenericType(bean.getOrigin(), method.getGenericReturnType(), model, processingContext);
    }

    private static TsType.FunctionType makeFunctionType(TsBeanModel bean, Method method, TsModel model, ProcessingContext processingContext)
    {
        logger.info(String.format("Processing method %s, with params %s and return type %s", method, method.getParameters(),
                method.getGenericReturnType()));
        List<TsParameter> params = getMethodParameters(bean, method, model, processingContext);
        TsType returnType = getReturnType(bean, method, model, processingContext);
        return new TsType.FunctionType(params, returnType);
    }

    private static TsPropertyModel makeFunction(TsBeanModel bean, Method method, TsModel model, ProcessingContext processingContext)
    {
        try
        {
            return new TsPropertyModel(method.getName(), makeFunctionType(bean, method, model, processingContext), TsModifierFlags.None, false, null);
        } catch (UnresolvedTypeException e)
        {
            logger.warning(
                    "Skipping method " + method.getDeclaringClass() + "." + method.getName() + " as it contains unresolved type: " + e.getType());
            return null;
        }
    }

    private static TsPropertyModel makePromiseReturningFunction(TsBeanModel bean, Method method, TsModel model, ProcessingContext processingContext)
    {
        try
        {
            TsType.FunctionType syncFunction = makeFunctionType(bean, method, model, processingContext);
            TsType promiseType = new TsType.GenericBasicType("Promise", List.of(syncFunction.type));
            return new TsPropertyModel(method.getName(), new TsType.FunctionType(syncFunction.parameters, promiseType), TsModifierFlags.None, false,
                    null);
        } catch (UnresolvedTypeException e)
        {
            logger.warning(
                    "Skipping method " + method.getDeclaringClass() + "." + method.getName() + " as it contains unresolved type: " + e.getType());
            return null;
        }
    }

    private static Optional<TsMethodModel> propertyModelToMethodModel(TsPropertyModel propertyModel)
    {
        if (propertyModel == null)
        {
            return Optional.empty();
        }
        TsType value = propertyModel.getTsType();
        if (value instanceof TsType.FunctionType)
        {
            TsType.FunctionType functionType = (TsType.FunctionType) value;
            List<TsParameterModel> params =
                    functionType.parameters.stream().map(param -> new TsParameterModel(param.name, param.getTsType())).collect(Collectors.toList());
            return Optional.of(new TsMethodModel(propertyModel.getName(), propertyModel.getModifiers(), null, params, functionType.type, null, null));
        } else
        {
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
    private static TsMethodModel makeConstructor(Constructor<?> constructor, TsBeanModel beanModel, TsModel model,
            ProcessingContext processingContext)
    {
        List<TsParameter> params = getMethodParameters(beanModel, constructor, model, processingContext);
        List<TsParameter> paramsWithoutDeclaringClass = params.stream().collect(Collectors.toList());
        List<TsParameterModel> paramsModel =
                paramsWithoutDeclaringClass.stream().map(param -> new TsParameterModel(param.name, param.getTsType())).collect(Collectors.toList());
        TsType returnType = null;

        List<TsType.GenericVariableType> typeParameters = resolveTypeParameters(beanModel, model, processingContext, false);

        if (!typeParameters.isEmpty())
        {
            returnType = new TsType.GenericReferenceType(beanModel.getName(), typeParameters);
        } else
        {
            returnType = new TsType.ReferenceType(beanModel.getName());
        }

        logger.info(String.format("Processing constructor %s, with params %s and return type %s", constructor, params, returnType));
        return new TsMethodModel("new ", TsModifierFlags.None, beanModel.getTypeParameters(), paramsModel, returnType, null, null);
    }

    private static TsBeanModel addFunctions(TsBeanModel bean, TsModel model, ProcessingContext processingContext, MethodProcessor processor)
    {
        Class<?> origin = bean.getOrigin();
        Stream<TsMethodModel> params = Arrays.stream(origin.getMethods()).filter(OpenBISExtension::filterMethods)
                .map(method -> propertyModelToMethodModel(processor.makeFunction(bean, method, model, processingContext)))
                .flatMap(it -> it.map(Stream::of).orElse(Stream.empty()));
        Stream<TsMethodModel> constructors = Arrays.stream(origin.getDeclaredConstructors()).map(constructor ->
        {
            try
            {
                return makeConstructor(constructor, bean, model, processingContext);
            } catch (UnresolvedTypeException e)
            {
                logger.warning("Skipping constructor of " + constructor.getDeclaringClass() + " as it contains unresolved type: " + e.getType());
                return null;
            }
        }).filter(Objects::nonNull);
        List<TsMethodModel> allMethods = Stream.of(params, constructors).flatMap(it -> it).collect(Collectors.toList());
        return bean.withProperties(Collections.emptyList()).withMethods(allMethods);
    }

    @Override
    public EmitterExtensionFeatures getFeatures()
    {
        final EmitterExtensionFeatures features = new EmitterExtensionFeatures();
        features.generatesRuntimeCode = false;
        features.generatesModuleCode = true;
        features.worksWithPackagesMappedToNamespaces = true;
        features.generatesJaxrsApplicationClient = false;
        return features;
    }

    @Override
    public List<TransformerDefinition> getTransformers()
    {
        return List.of(new TransformerDefinition(ModelCompiler.TransformationPhase.AfterDeclarationSorting, (TsModelTransformer) (context, model) ->
        {
            logger.info("Started processing methods");
            //Extract all types that were mapped by the bean model to reuse them
            TypeProcessor localProcessor = new DefaultTypeProcessor();
            ProcessingContext processingContext = new ProcessingContext(context.getSymbolTable(), localProcessor);
            //Add table of mapped types

            List<TsBeanModel> processedBeans = model.getBeans().stream().map(bean ->
            {
                List<TsType.GenericVariableType> tsTypeParameters = resolveTypeParameters(bean, model, processingContext, true);
                return new TsBeanModel(bean.getOrigin(), bean.getCategory(), bean.isClass(), bean.getName(), tsTypeParameters,
                        bean.getParent(), bean.getExtendsList(), bean.getImplementsList(), Collections.emptyList(), bean.getConstructor(),
                        bean.getMethods(), bean.getComments());
            }).collect(Collectors.toList());

            processedBeans = processedBeans.stream().map(bean ->
            {
                if (OpenBIS.class.equals(bean.getOrigin()))
                {
                    return addFunctions(bean, model, processingContext, OpenBISExtension::makePromiseReturningFunction);
                } else
                {
                    return addFunctions(bean, model, processingContext, OpenBISExtension::makeFunction);
                }
            }).collect(Collectors.toList());

            List<TsBeanModel> processedBeansWithSeparatedConstructors = new LinkedList<>();

            processedBeans.forEach(bean ->
            {
                List<TsMethodModel> regularMethods =
                        bean.getMethods().stream().filter(method -> !method.getName().equals("new ")).collect(Collectors.toList());
                List<TsMethodModel> constructors =
                        bean.getMethods().stream().filter(method -> method.getName().equals("new ")).collect(Collectors.toList());

                if (!constructors.isEmpty())
                {
                    TsBeanModel constructorBean =
                            new TsBeanModel(bean.getOrigin(), bean.getCategory(), bean.isClass(),
                                    new Symbol(bean.getName().getSimpleName() + "Constructor"),
                                    Collections.emptyList(), null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null,
                                    constructors, Collections.emptyList());
                    processedBeansWithSeparatedConstructors.add(constructorBean);
                }

                TsBeanModel beanWithoutConstructors =
                        new TsBeanModel(bean.getOrigin(), bean.getCategory(), bean.isClass(), bean.getName(), bean.getTypeParameters(),
                                bean.getParent(), bean.getExtendsList(), bean.getImplementsList(), bean.getProperties(), bean.getConstructor(),
                                regularMethods, bean.getComments());
                processedBeansWithSeparatedConstructors.add(beanWithoutConstructors);
            });

            return model.withBeans(processedBeansWithSeparatedConstructors);
        }), new TransformerDefinition(ModelCompiler.TransformationPhase.AfterDeclarationSorting, (TsModelTransformer) (context, model) ->{
            return addOpenBISModule(model);
        }));
    }

    private TsModel addOpenBISModule(final TsModel model){
        List<TsBeanModel> beans = model.getBeans();
        List<TsPropertyModel> properties = new ArrayList<>();
        Set<String> constructors = new HashSet<>();

        for (TsBeanModel bean : beans)
        {
            if (bean.getName().getSimpleName().endsWith("Constructor"))
            {
                constructors.add(bean.getName().getSimpleName());
            }
        }

        for (TsBeanModel bean : beans)
        {
            if (!bean.getName().getSimpleName().endsWith("Constructor"))
            {
                boolean hasConstructor = constructors.contains(bean.getName().getSimpleName() + "Constructor");

                if (hasConstructor)
                {
                    JsonObject jsonObjectAnnotation = bean.getOrigin().getAnnotation(JsonObject.class);

                    properties.add(new TsPropertyModel(bean.getName().getSimpleName(),
                            new TsType.ReferenceType(new Symbol(bean.getName().getSimpleName() + "Constructor")), null, true, null));

                    if (jsonObjectAnnotation != null)
                    {
                        String jsonName = jsonObjectAnnotation.value().replaceAll("\\.", "_");

                        if(!jsonName.equals(bean.getName().getSimpleName()))
                        {
                            properties.add(new TsPropertyModel(jsonName,
                                    new TsType.ReferenceType(new Symbol(bean.getName().getSimpleName() + "Constructor")), null, true, null));
                        }
                    }
                }
            }
        }

        beans.add(new TsBeanModel(null, TsBeanCategory.Data, false, new Symbol("bundle"), null, null, null, null, properties, null, null, null));

        return model.withBeans(beans);
    }

    @Override public void emitElements(final Writer writer, final Settings settings, final boolean exportKeyword, final TsModel model)
    {
        Set<String> constructors = new HashSet<>();

        model.getBeans().forEach(bean ->
        {
            if (bean.getName().getSimpleName().endsWith("Constructor"))
            {
                String originalBeanName =
                        bean.getName().getSimpleName().substring(0, bean.getName().getSimpleName().lastIndexOf("Constructor"));

                writer.writeIndentedLine("export const " + originalBeanName + ":" + bean.getName().getSimpleName());

                JsonObject jsonObjectAnnotation = bean.getOrigin().getAnnotation(JsonObject.class);

                if (jsonObjectAnnotation != null)
                {
                    String jsonName = jsonObjectAnnotation.value().replaceAll("\\.", "_");

                    if(!jsonName.equals(originalBeanName))
                    {
                        writer.writeIndentedLine(
                                "export const " + jsonName + ":" + bean.getName().getSimpleName());
                    }
                }

                constructors.add(bean.getName().getSimpleName());
            }
        });

        model.getBeans().forEach(bean ->
        {
            if (!bean.getName().getSimpleName().endsWith("Constructor") && constructors.contains(bean.getName().getSimpleName() + "Constructor"))
            {
                JsonObject jsonObjectAnnotation = bean.getOrigin().getAnnotation(JsonObject.class);

                if (jsonObjectAnnotation != null)
                {
                    String jsonName = jsonObjectAnnotation.value().replaceAll("\\.", "_");

                    if(!jsonName.equals(bean.getName().getSimpleName()))
                    {
                        if (bean.getTypeParameters() == null || bean.getTypeParameters().isEmpty())
                        {
                            writer.writeIndentedLine(
                                    "type " + jsonName + " = " + bean.getName().getSimpleName());
                        } else
                        {
                            List<String> typeNames = bean.getTypeParameters().stream().map(p -> p.name.split(" ")[0]).collect(Collectors.toList());
                            List<String> typeParameters = bean.getTypeParameters().stream().map(TsType::toString).collect(Collectors.toList());
                            writer.writeIndentedLine("type " + jsonName + "<" + String.join(", ", typeParameters) + "> = "
                                    + bean.getName().getSimpleName() + "<" + String.join(", ", typeNames) + ">");
                        }
                    }
                }
            }
        });
    }

    private static class UnresolvedTypeException extends RuntimeException
    {
        private Class<?> clazz;

        private Type type;

        public UnresolvedTypeException(final Class<?> clazz, final Type type)
        {
            this.clazz = clazz;
            this.type = type;
        }

        public Class<?> getClazz()
        {
            return clazz;
        }

        public Type getType()
        {
            return type;
        }
    }

}
