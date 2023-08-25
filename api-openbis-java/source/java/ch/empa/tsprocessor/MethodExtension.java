package ch.empa.tsprocessor;

import com.fasterxml.jackson.core.type.TypeReference;
import cz.habarta.typescript.generator.*;
import cz.habarta.typescript.generator.compiler.ModelCompiler;
import cz.habarta.typescript.generator.compiler.Symbol;
import cz.habarta.typescript.generator.compiler.SymbolTable;
import cz.habarta.typescript.generator.compiler.TsModelTransformer;
import cz.habarta.typescript.generator.emitter.*;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;// in play 2.3

interface MethodProcessor {
    TsPropertyModel makeFunction(Method method, TsModel model, ProcessingContext processingContext);
}

interface MakeConstructor {
    TsConstructorModel makeConstructor(Constructor<?> constructor, TsModel model, ProcessingContext processingContext);
}

class ProcessingContext {
    private final SymbolTable symbolTable;
    private final MappedTypeExtractor typeExtractor;
    private final TypeProcessor localProcessor;

    ProcessingContext(SymbolTable symbolTable, MappedTypeExtractor typeExtractor, TypeProcessor localProcessor) {
        this.symbolTable = symbolTable;
        this.typeExtractor = typeExtractor;
        this.localProcessor = localProcessor;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public MappedTypeExtractor getTypeExtractor() {
        return typeExtractor;
    }

    public TypeProcessor getLocalProcessor() {
        return localProcessor;
    }
}

class MappedTypeExtractor {

    private final HashMap<Class<?>, TsType> mappedTypes = new HashMap<>();

    public void extractMappedTypes(TsModel model) {
        model.getBeans().forEach(bean -> {
            System.out.printf("Extracting mapped types for bean %s\n", bean.getOrigin().getName());
            Class<?> origin = bean.getOrigin();
            Field[] fields = origin.getDeclaredFields();
            System.out.printf("Extracting mapped types for bean %s, the fields are %s\n", bean.getOrigin().getName(), Arrays.toString(fields));
            bean.getProperties().forEach(prop -> {
                TsType propType = prop.tsType;

                Optional<Field> originalField = Arrays.stream(fields).filter(field -> field.getName().equals(prop.getName())).findFirst();
                Class<?> originalClass = originalField.flatMap(field -> Optional.ofNullable(field.getType())).orElse(null);
                TsType tsType = originalClass != null ? mappedTypes.put(originalClass, propType) : null;
            });
            mappedTypes.put(origin, new TsType.ReferenceType(bean.getName()));
        });
        model.getTypeAliases().stream().forEach(tsAliasModel -> {
            mappedTypes.put(tsAliasModel.getOrigin(), tsAliasModel.getDefinition());
        });
        System.out.printf("Mapped types %s\n", mappedTypes);

    }

    public HashMap<Class<?>, TsType> getMappedTypes() {
        return mappedTypes;
    }

    public Optional<TsType> getMappedType(Class<?> clz) {
        return Optional.ofNullable(mappedTypes.get(clz));
    }
}

public class MethodExtension extends Extension {

    public static final String CFG_ASYNC_CLASSES = "asyncClasses";
    static final ObjectMapper mapper = new ObjectMapper();
    private static final List<Class<?>> assignableContainers = List.of(List.class, Set.class, ArrayList.class, Collection.class, Map.class, HashMap.class, Optional.class);

    private static final Logger logger = TypeScriptGenerator.getLogger();
    //Hold types that were mapped by the bean model
//    private final MappedTypeExtractor typeExtractor = new MappedTypeExtractor();
    private List<String> asnycClasses = new ArrayList<>();

    public MethodExtension() {
    }

    public MethodExtension(List<String> asyncClasses) {
        this.asnycClasses = asyncClasses;
    }


    private static boolean filterMethods(Method method) {
        return !((method.getDeclaringClass() == Object.class) || (method.getName().matches("hashCode|toString|equals")));
    }

    private static Optional<Class<?>> getAssignableClass(Class<?> clz) {
        return assignableContainers.stream().filter(container -> container.isAssignableFrom(clz)).findFirst();
    }

    private static List<TsParameter> getMethodParameters(Executable method, TsModel model, ProcessingContext processingContext) {
        return Arrays.stream(method.getParameters()).map(parameter -> new TsParameter(parameter.getName(), ResolveGenericType(parameter.getParameterizedType(), model, processingContext))).collect(Collectors.toList());
    }


    private static TsType ResolveGenericType(Type type, TsModel model, ProcessingContext processingContext) {

        TypeProcessor.Context context = new TypeProcessor.Context(processingContext.getSymbolTable(), processingContext.getLocalProcessor(), null);
        return context.processType(type).getTsType();

    }

    private static TsType getReturnType(Method method, TsModel model, ProcessingContext processingContext) {
        return ResolveGenericType(method.getGenericReturnType(), model, processingContext);

    }

    private static TsPropertyModel makeFunction(Method method, TsModel model, ProcessingContext processingContext) {
        List<TsParameter> params = getMethodParameters(method, model, processingContext);
        TsType returnType = getReturnType(method, model, processingContext);
        return new TsPropertyModel(method.getName(), new TsType.FunctionType(params, returnType), TsModifierFlags.None, false, null);
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

    private static TsPropertyModel makeCallBackFunction(Method method, TsModel model, ProcessingContext processingContext) {
        List<TsParameter> params = getMethodParameters(method, model, processingContext);
        TsType returnType = getReturnType(method, model, processingContext);
        TsParameter callbackParam = new TsParameter("callback", returnType);
        TsType.FunctionType arrowFunction = new TsType.FunctionType(List.of(callbackParam), TsType.Null);
        TsParameter callback = new TsParameter("callback", arrowFunction);
        List<TsParameter> paramsWithCallback = Stream.concat(params.stream(), Stream.of(callback)).collect(Collectors.toList());
        return new TsPropertyModel(method.getName(), new TsType.FunctionType(paramsWithCallback, TsType.Null), TsModifierFlags.None, false, null);
    }

    private static TsPropertyModel makePromiseReturningFunction(Method method, TsModel model, ProcessingContext processingContext) {
        List<TsParameter> params = getMethodParameters(method, model, processingContext);
        List<TsType> returnType = List.of(getReturnType(method, model, processingContext));
        TsType promiseType = new TsType.GenericReferenceType(new Symbol("Promise"), returnType);
        return new TsPropertyModel(method.getName(), new TsType.FunctionType(params, promiseType), TsModifierFlags.None, false, null);
    }

    private static TsMethodModel makeConstructor(Constructor<?> constructor, TsBeanModel beanModel, TsModel model, ProcessingContext processingContext) {
        List<TsParameter> params = getMethodParameters(constructor, model, processingContext);
        //Exclude parameters that correspond to the bean itself
        List<TsParameter> paramsWithoutDeclaringClass = params.stream().collect(Collectors.toList());
        TsType returnType = ResolveGenericType(constructor.getDeclaringClass(), model, processingContext);
        System.out.printf("Constructor %s, params %s, return type %s\n", constructor, params, returnType);
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
                TypeScriptGenerator.getLogger().info(String.format("MethodExtension: setConfiguration, %s, %s", configuration, classString));
                asnycClasses = mapper.readValue(classString, new TypeReference<ArrayList<String>>() {
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println(classString);

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
            //Extract all types that were mapped by the bean model to reuse them
            MappedTypeExtractor typeExtractor = new MappedTypeExtractor();
            typeExtractor.extractMappedTypes(model);
            String classNames = model.getBeans().stream().map(it -> it.getName().getFullName()).collect(Collectors.joining("\n"));
            System.out.printf("model %s\n", classNames);
            System.out.printf("Mapped types %s\n", typeExtractor.getMappedTypes());
            TypeProcessor localProcessor = new DefaultTypeProcessor();
            ProcessingContext processingContext = new ProcessingContext(context.getSymbolTable(), typeExtractor, localProcessor);
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
