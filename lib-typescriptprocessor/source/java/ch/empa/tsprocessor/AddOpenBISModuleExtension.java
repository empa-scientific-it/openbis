package ch.empa.tsprocessor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.base.annotation.JsonObject;
import cz.habarta.typescript.generator.Extension;
import cz.habarta.typescript.generator.Settings;
import cz.habarta.typescript.generator.TsType;
import cz.habarta.typescript.generator.compiler.ModelCompiler;
import cz.habarta.typescript.generator.compiler.Symbol;
import cz.habarta.typescript.generator.compiler.TsModelTransformer;
import cz.habarta.typescript.generator.emitter.EmitterExtensionFeatures;
import cz.habarta.typescript.generator.emitter.TsBeanCategory;
import cz.habarta.typescript.generator.emitter.TsBeanModel;
import cz.habarta.typescript.generator.emitter.TsModel;
import cz.habarta.typescript.generator.emitter.TsPropertyModel;

public class AddOpenBISModuleExtension extends Extension
{
    @Override public EmitterExtensionFeatures getFeatures()
    {
        final EmitterExtensionFeatures features = new EmitterExtensionFeatures();
        features.generatesRuntimeCode = false;
        features.generatesModuleCode = true;
        features.worksWithPackagesMappedToNamespaces = true;
        features.generatesJaxrsApplicationClient = false;
        return features;
    }

    @Override public List<TransformerDefinition> getTransformers()
    {
        return List.of(new TransformerDefinition(ModelCompiler.TransformationPhase.AfterDeclarationSorting, (TsModelTransformer) (context, model) ->
        {
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
        }));
    }

}
