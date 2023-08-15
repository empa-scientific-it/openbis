package ch.empa.tsprocessor;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.habarta.typescript.generator.*;
import cz.habarta.typescript.generator.compiler.ModelCompiler;
import cz.habarta.typescript.generator.compiler.SymbolTable;
import cz.habarta.typescript.generator.compiler.TsModelTransformer;
import cz.habarta.typescript.generator.emitter.*;
import cz.habarta.typescript.generator.parser.BeanModel;
import cz.habarta.typescript.generator.util.Utils;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;// in play 2.3

interface MethodProcessor {
    TsPropertyModel makeFunction(Method method, TsModelTransformer.Context context, SymbolTable symbolTable);
}

public class MethodExtension extends Extension {

    static final ObjectMapper mapper = new ObjectMapper();

    public static final String CFG_ASYNC_CLASSES = "asyncClasses";

    private List<String> asnycClasses = new ArrayList<>();

    public MethodExtension() {
    }
    public MethodExtension(List<String> asyncClasses) {
        this.asnycClasses = asyncClasses;
    }



    @Override
    public void setConfiguration(Map<String, String> configuration) throws RuntimeException {
        if (configuration.containsKey(CFG_ASYNC_CLASSES)) {
            String classString = configuration.get(CFG_ASYNC_CLASSES);
            try {
                String[] classes = mapper.readValue(classString, String[].class);
                asnycClasses = Arrays.asList(classes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println(classString);

        }
    }

    private static String formatAllMethods(TsBeanModel bean) {
        System.out.println("MethodExtension.formatAllMethos");
        Class<?> origin = bean.getOrigin();
        return Arrays.stream(origin.getMethods()).map(method -> method.getName()).collect(Collectors.joining("\n"));

    }

    private static boolean filterMethods(Method method) {
        System.out.println("MethodExtension.filterMethods");
        return !((method.getDeclaringClass() == Object.class) || (method.getName().matches("^(set|get|is).*|hashCode|toString|equals")));
    }

    private static List<TsParameter> getMethodParameters(Method method, SymbolTable symbolTable) {
        System.out.println("MethodExtension.getMethodParameters");
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
            } else {
                Class<?> rawClass = (Class<?>) parameterizedType.getRawType();
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
            return upperBounds.length > 0 ? new TsType.ReferenceType(symbolTable.getSymbol(upperBounds[0].getClass())) : TsType.Any;
        } else {
            // Handle TypeVariable case
            return TsType.Any;
        }
    }

    private static TsType getReturnType(Method method, TsModelTransformer.Context context, SymbolTable symbolTable) {
        System.out.println("MethodExtension.getReturnType");
        TsType typeParams = ResolveGenericType(method.getGenericReturnType(), symbolTable);
        return typeParams;

    }


    private static TsPropertyModel makeFunction(Method method, TsModelTransformer.Context context, SymbolTable symbolTable) {
        //System.out.println("MethodExtension.makeFunction");
        List<TsParameter> params = getMethodParameters(method, symbolTable);
        TsType returnType = getReturnType(method, context, symbolTable);
        return new TsPropertyModel(method.getName(), new TsType.FunctionType(params, returnType), TsModifierFlags.None, false, null);
    }

    private static TsPropertyModel makeCallBackFunction(Method method, TsModelTransformer.Context context, SymbolTable symbolTable) {
        //System.out.println("MethodExtension.makeCallBackFunction");
        List<TsParameter> params = getMethodParameters(method, symbolTable);
        TsType returnType = getReturnType(method, context, symbolTable);
        TsParameter callbackParam = new TsParameter("callback", returnType);
        TsType.FunctionType arrowFunction = new TsType.FunctionType(List.of(callbackParam), TsType.Null);
        TsParameter callback = new TsParameter("callback", arrowFunction);
        List<TsParameter> paramsWithCallback = Stream.concat(params.stream(), Stream.of(callback)).collect(Collectors.toList());
        return new TsPropertyModel(method.getName(), new TsType.FunctionType(paramsWithCallback, TsType.Null), TsModifierFlags.None, false, null);
    }


    private static TsBeanModel addFunctions(TsBeanModel bean, TsModelTransformer.Context context, SymbolTable symbolTable, MethodProcessor processor) {
        //System.out.println("MethodExtension.addFunction");
        Class<?> origin = bean.getOrigin();
        Stream<TsPropertyModel> params = Arrays.stream(origin.getMethods()).filter(mt -> filterMethods(mt)).map(method -> processor.makeFunction(method, context, symbolTable));
        List<TsPropertyModel> allProps = Stream.concat(params, bean.getProperties().stream()).collect(Collectors.toList());
        return bean.withProperties(allProps);
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
            //System.out.println("MethodExtension.getTransformers");
            Stream<TsBeanModel>  processedBeans =  model.getBeans().stream().map(bean -> {
                System.out.printf("bean: %s\n", (bean.getOrigin().getName()));
                if (asnycClasses.contains(bean.getOrigin().getName())){
                    return addFunctions(bean, context, context.getSymbolTable(), MethodExtension::makeCallBackFunction);
                } else {
                    return  addFunctions(bean, context, context.getSymbolTable(), MethodExtension::makeFunction);
                }
            } );

            return model.withBeans(processedBeans.collect(Collectors.toList()));
        }));
    }

    public void emitElements(Writer writer, Settings settings, boolean exportKeyword, TsModel model) {
        System.out.println("MethodExtension.emitElements");
        for (TsBeanModel bean : model.getBeans()) {
            //System.out.printf("bean: %s\n", formatAllMethods(bean));
            if (bean.isJaxrsApplicationClientBean()) {
                final String clientName = bean.getName().getSimpleName();
                final String clientFullName = settings.mapPackagesToNamespaces ? bean.getName().getFullName() : bean.getName().getSimpleName();
            }
        }
    }

}
