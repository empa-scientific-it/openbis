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
    TsPropertyModel makeFunction(Method method, SymbolTable symbolTable);
}

public class MethodExtension extends Extension {

    public static final String CFG_ASYNC_CLASSES = "asyncClasses";
    static final ObjectMapper mapper = new ObjectMapper();
    private List<String> asnycClasses = new ArrayList<>();

    public MethodExtension() {
    }

    public MethodExtension(List<String> asyncClasses) {
        this.asnycClasses = asyncClasses;
    }

    private static boolean filterMethods(Method method) {
        return !((method.getDeclaringClass() == Object.class) || (method.getName().matches("hashCode|toString|equals")));
    }

    private static List<TsParameter> getMethodParameters(Method method, SymbolTable symbolTable) {
        return Arrays.stream(method.getParameters()).map(parameter -> new TsParameter(method.getName(), ResolveGenericType(parameter.getParameterizedType(), symbolTable))).collect(Collectors.toList());
    }

    private static TsType ResolveGenericType(Type type, SymbolTable symbolTable) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Class<?> rawType = (Class<?>) parameterizedType.getRawType();

            if (List.class.isAssignableFrom(rawType)) {
                // Manually map List<T> to Array<T>
                TsType elementType = ResolveGenericType(parameterizedType.getActualTypeArguments()[0], symbolTable);
                return new TsType.GenericReferenceType(symbolTable.getSymbol(Array.class), Collections.singletonList(elementType));
            } else if (Set.class.isAssignableFrom(rawType)) {
                // Manually map List<T> to Array<T>
                TsType elementType = ResolveGenericType(parameterizedType.getActualTypeArguments()[0], symbolTable);
                return new TsType.GenericReferenceType(symbolTable.getSymbol(Set.class), Collections.singletonList(elementType));
            } else if (ArrayList.class.isAssignableFrom(rawType)) {
                TsType elementType = ResolveGenericType(parameterizedType.getActualTypeArguments()[0], symbolTable);
                return new TsType.GenericReferenceType(symbolTable.getSymbol(ArrayList.class), Collections.singletonList(elementType));
            } else {
                Class<?> rawClass = (Class<?>) parameterizedType.getRawType();
                System.out.printf("Other type %s\n", rawClass.getName());
                TsType[] typeArguments = Arrays.stream(parameterizedType.getActualTypeArguments()).map(typeArgument -> ResolveGenericType(typeArgument, symbolTable)).toArray(TsType[]::new);
                return new TsType.GenericReferenceType(symbolTable.getSymbol(rawClass), Arrays.asList(typeArguments));
            }
        } else if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            return new TsType.BasicArrayType(ResolveGenericType(genericArrayType.getGenericComponentType(), symbolTable));
        } else if (type instanceof Class) {
            Class<?> clz = (Class<?>) type;
            return new TsType.ReferenceType(symbolTable.getSymbol(clz));
        } else if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            final Type[] upperBounds = wildcardType.getUpperBounds();
            return upperBounds.length > 0 ? ResolveGenericType(upperBounds[0], symbolTable) : TsType.Any;
        } else {
            // Handle TypeVariable case
            return TsType.Any;
        }
    }

    private static TsType getReturnType(Method method, SymbolTable symbolTable) {
        return ResolveGenericType(method.getGenericReturnType(), symbolTable);

    }

    private static TsPropertyModel makeFunction(Method method, SymbolTable symbolTable) {
        List<TsParameter> params = getMethodParameters(method, symbolTable);
        TsType returnType = getReturnType(method, symbolTable);
        return new TsPropertyModel(method.getName(), new TsType.FunctionType(params, returnType), TsModifierFlags.None, false, null);
    }

    private static TsPropertyModel makeCallBackFunction(Method method, SymbolTable symbolTable) {
        List<TsParameter> params = getMethodParameters(method, symbolTable);
        TsType returnType = getReturnType(method, symbolTable);
        TsParameter callbackParam = new TsParameter("callback", returnType);
        TsType.FunctionType arrowFunction = new TsType.FunctionType(List.of(callbackParam), TsType.Null);
        TsParameter callback = new TsParameter("callback", arrowFunction);
        List<TsParameter> paramsWithCallback = Stream.concat(params.stream(), Stream.of(callback)).collect(Collectors.toList());
        return new TsPropertyModel(method.getName(), new TsType.FunctionType(paramsWithCallback, TsType.Null), TsModifierFlags.None, false, null);
    }

    private static TsPropertyModel makePromiseReturningFunction(Method method, SymbolTable symbolTable) {
        List<TsParameter> params = getMethodParameters(method, symbolTable);
        List<TsType> returnType = List.of(getReturnType(method, symbolTable));
        TsType promiseType = new TsType.GenericReferenceType(new Symbol("Promise"), returnType);
        return new TsPropertyModel(method.getName(), new TsType.FunctionType(params, promiseType), TsModifierFlags.None, false, null);
    }

    private static TsBeanModel addFunctions(TsBeanModel bean, TsModelTransformer.Context context, SymbolTable symbolTable, MethodProcessor processor) {
        Class<?> origin = bean.getOrigin();
        Stream<TsPropertyModel> params = Arrays.stream(origin.getMethods()).filter(MethodExtension::filterMethods).map(method -> processor.makeFunction(method, symbolTable));
        List<TsPropertyModel> allProps = Stream.concat(params, bean.getProperties().stream()).collect(Collectors.toList());
        return bean.withProperties(allProps);
    }

    @Override
    public void setConfiguration(Map<String, String> configuration) throws RuntimeException {

        if (configuration.containsKey(CFG_ASYNC_CLASSES)) {

            String classString = configuration.get(CFG_ASYNC_CLASSES);
            try {
                TypeScriptGenerator.getLogger().info(String.format("MethodExtension: setConfiguration, %s, %s", configuration, classString));
                ArrayList<String> classes = mapper.readValue(classString, new TypeReference<ArrayList<String>>() {
                });
                asnycClasses = classes;
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
        return List.of(new TransformerDefinition(ModelCompiler.TransformationPhase.BeforeSymbolResolution, (TsModelTransformer) (context, model) -> {
            Stream<TsBeanModel> processedBeans = model.getBeans().stream().map(bean -> {
                if (asnycClasses.contains(bean.getOrigin().getName())) {
                    return addFunctions(bean, context, context.getSymbolTable(), MethodExtension::makePromiseReturningFunction);
                } else {
                    return addFunctions(bean, context, context.getSymbolTable(), MethodExtension::makeFunction);
                }
            });

            return model.withBeans(processedBeans.collect(Collectors.toList()));
        }));
    }


}
