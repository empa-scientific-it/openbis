

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
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.reflect.TypeToken;

import ch.ethz.sis.openbis.generic.OpenBIS;
import ch.systemsx.cisd.base.annotation.JsonObject;
import cz.habarta.typescript.generator.DefaultTypeProcessor;
import cz.habarta.typescript.generator.Extension;
import cz.habarta.typescript.generator.Logger;
import cz.habarta.typescript.generator.Settings;
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
 * This extension for the typescript-generator ({@link cz.habarta.typescript.generator}) Gradle <a href="URL#https://github.com/vojtechhabarta/typescript-generator">plugin</a> adds method and constructor signatures to the generated typescript interfaces.
 * The methods are extracted from the java classes using reflection. Currently, it can only create interface signatures to be exported in a d.ts. file, not the implementation.
 *
 * @author Simone Baffelli
 * @author pkupczyk
 */
public class OpenBISExtension extends Extension
{

    private static final Logger logger = TypeScriptGenerator.getLogger();

    private static TsType resolveType(ProcessingContext processingContext, TsBeanModel bean, Type type)
    {
        TypeToken<?> typeToken = TypeToken.of(bean.getOrigin()).resolveType(type);
        TypeProcessor.Context context = new TypeProcessor.Context(processingContext.getSymbolTable(), processingContext.getLocalProcessor(), null);
        TsType tsType = context.processType(typeToken.getType()).getTsType();

        if (tsType instanceof TsType.ReferenceType)
        {
            if (!((TsType.ReferenceType) tsType).symbol.isResolved())
            {
                throw new UnresolvedTypeException(bean.getOrigin(), type);
            }
        }

        return tsType;
    }

    private static List<TsType.GenericVariableType> resolveTypeParameters(ProcessingContext processingContext, TsBeanModel bean,
            TypeVariable<?>[] typeParameters, boolean withBounds)
    {
        List<TsType.GenericVariableType> tsTypeParameters = new ArrayList<>();

        for (TypeVariable<?> typeParameter : typeParameters)
        {
            Type[] boundsTypes = typeParameter.getBounds();

            if (withBounds && boundsTypes.length > 0)
            {
                try
                {
                    List<String> boundsStrings = new ArrayList<>();
                    for (Type boundType : boundsTypes)
                    {
                        TsType tsBoundType = resolveType(processingContext, bean, boundType);
                        boundsStrings.add(tsBoundType.toString());
                    }

                    tsTypeParameters.add(new TsType.GenericVariableType(typeParameter.getName() + " extends " + boundsStrings.get(0)));
                } catch (UnresolvedTypeException e)
                {
                    tsTypeParameters.add(new TsType.GenericVariableType(typeParameter.getName()));
                }
            } else
            {
                tsTypeParameters.add(new TsType.GenericVariableType(typeParameter.getName()));
            }
        }

        return tsTypeParameters;
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
            logger.info("Started processing beans");

            ProcessingContext processingContext = new ProcessingContext(context.getSymbolTable());

            List<TsBeanModel> tsBeans = new ArrayList<>();
            List<TsPropertyModel> tsBundleProperties = new ArrayList<>();

            for (TsBeanModel bean : model.getBeans())
            {
                List<TsType.GenericVariableType> tsBeanTypeParametersWithBounds =
                        resolveTypeParameters(processingContext, bean, bean.getOrigin().getTypeParameters(), true);
                List<TsType.GenericVariableType> tsBeanTypeParametersWithoutBounds =
                        resolveTypeParameters(processingContext, bean, bean.getOrigin().getTypeParameters(), false);

                List<TsMethodModel> tsBeanMethods = new ArrayList<>();

                for (Method method : bean.getOrigin().getMethods())
                {
                    if (method.isBridge() || (method.getDeclaringClass() == Object.class) || (method.getName()
                            .matches("hashCode|toString|equals")))
                    {
                        continue;
                    }

                    try
                    {
                        List<TsParameterModel> tsMethodParameters = new ArrayList<>();

                        for (Parameter methodParameter : method.getParameters())
                        {
                            TsType tsMethodParameterType = resolveType(processingContext, bean, methodParameter.getParameterizedType());
                            tsMethodParameters.add(new TsParameterModel(methodParameter.getName(), tsMethodParameterType));
                        }

                        TsType tsMethodReturnType = resolveType(processingContext, bean, method.getGenericReturnType());

                        if (OpenBIS.class.equals(bean.getOrigin()))
                        {
                            tsMethodReturnType = new TsType.GenericBasicType("Promise", List.of(tsMethodReturnType));
                        }

                        List<TsType.GenericVariableType> tsMethodTypeParameters =
                                resolveTypeParameters(processingContext, bean, method.getTypeParameters(), false);

                        tsBeanMethods.add(new TsMethodModel(method.getName(), TsModifierFlags.None, tsMethodTypeParameters, tsMethodParameters,
                                tsMethodReturnType,
                                null, null));

                    } catch (UnresolvedTypeException e)
                    {
                        logger.warning("Skipping method " + method.getDeclaringClass() + "." + method.getName()
                                + " as it contains unresolved type: " + e.getType());
                    }
                }

                List<TsMethodModel> tsConstructors = new ArrayList<>();

                for (Constructor<?> constructor : bean.getOrigin().getDeclaredConstructors())
                {
                    try
                    {
                        List<TsParameterModel> tsConstructorParameter = new ArrayList<>();

                        for (Parameter constructorParameter : constructor.getParameters())
                        {
                            TsType tsConstructorParameterType = resolveType(processingContext, bean, constructorParameter.getParameterizedType());
                            tsConstructorParameter.add(new TsParameterModel(constructorParameter.getName(), tsConstructorParameterType));
                        }

                        TsType tsConstructorReturnType;

                        if (tsBeanTypeParametersWithoutBounds.isEmpty())
                        {
                            tsConstructorReturnType = new TsType.ReferenceType(bean.getName());
                        } else
                        {
                            tsConstructorReturnType = new TsType.GenericReferenceType(bean.getName(), tsBeanTypeParametersWithoutBounds);
                        }

                        tsConstructors.add(new TsMethodModel("new ", TsModifierFlags.None, tsBeanTypeParametersWithBounds, tsConstructorParameter,
                                tsConstructorReturnType,null, null));

                    } catch (UnresolvedTypeException e)
                    {
                        logger.warning(
                                "Skipping method " + constructor.getDeclaringClass() + "." + constructor.getName()
                                        + " as it contains unresolved type: " + e.getType());
                    }
                }

                if (!tsConstructors.isEmpty())
                {
                    String tsConstructorBeanName = bean.getName().getSimpleName() + "Constructor";

                    tsBeans.add(new TsBeanModel(bean.getOrigin(), bean.getCategory(), bean.isClass(),
                            new Symbol(tsConstructorBeanName),
                            Collections.emptyList(), null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null,
                            tsConstructors, Collections.emptyList()));

                    tsBundleProperties.add(new TsPropertyModel(bean.getName().getSimpleName(),
                            new TsType.ReferenceType(new Symbol(tsConstructorBeanName)), null, true, null));

                    JsonObject tsBeanJsonObject = bean.getOrigin().getAnnotation(JsonObject.class);

                    if (tsBeanJsonObject != null)
                    {
                        String tsBeanJsonName = tsBeanJsonObject.value().replaceAll("\\.", "_");

                        if (!tsBeanJsonName.equals(bean.getName().getSimpleName()))
                        {
                            tsBundleProperties.add(new TsPropertyModel(tsBeanJsonName,
                                    new TsType.ReferenceType(new Symbol(tsConstructorBeanName)), null, true, null));
                        }
                    }
                }

                tsBeans.add(new TsBeanModel(bean.getOrigin(), bean.getCategory(), bean.isClass(), bean.getName(), tsBeanTypeParametersWithBounds,
                        bean.getParent(), bean.getExtendsList(), bean.getImplementsList(), Collections.emptyList(), bean.getConstructor(),
                        tsBeanMethods, bean.getComments()));
            }

            tsBeans.add(
                    new TsBeanModel(null, TsBeanCategory.Data, false, new Symbol("bundle"), null, null, null, null, tsBundleProperties, null, null,
                            null));

            return model.withBeans(tsBeans);
        }));
    }

    @Override public void emitElements(final Writer writer, final Settings settings, final boolean exportKeyword, final TsModel model)
    {
        Set<String> constructors = new HashSet<>();

        // create "export const bean:beanConstructor"
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

                    if (!jsonName.equals(originalBeanName))
                    {
                        writer.writeIndentedLine(
                                "export const " + jsonName + ":" + bean.getName().getSimpleName());
                    }
                }

                constructors.add(bean.getName().getSimpleName());
            }
        });

        // create "type beanJsonName:bean"
        model.getBeans().forEach(bean ->
        {
            if (!bean.getName().getSimpleName().endsWith("Constructor") && constructors.contains(bean.getName().getSimpleName() + "Constructor"))
            {
                JsonObject jsonObjectAnnotation = bean.getOrigin().getAnnotation(JsonObject.class);

                if (jsonObjectAnnotation != null)
                {
                    String jsonName = jsonObjectAnnotation.value().replaceAll("\\.", "_");

                    if (!jsonName.equals(bean.getName().getSimpleName()))
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

    private static class ProcessingContext
    {
        private final SymbolTable symbolTable;

        private final TypeProcessor localProcessor;

        ProcessingContext(SymbolTable symbolTable)
        {
            this.symbolTable = symbolTable;
            this.localProcessor = new DefaultTypeProcessor();
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

    private static class UnresolvedTypeException extends RuntimeException
    {
        private final Class<?> clazz;

        private final Type type;

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
