package ch.empa.tsprocessor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                        properties.add(new TsPropertyModel(bean.getName().getSimpleName(),
                                new TsType.ReferenceType(new Symbol(bean.getName().getSimpleName() + "Constructor")), null, true, null));
                    } else
                    {
                        properties.add(
                                new TsPropertyModel(bean.getName().getSimpleName(), new TsType.ReferenceType(bean.getName()), null, true, null));
                    }
                }
            }

            beans.add(new TsBeanModel(null, TsBeanCategory.Data, false, new Symbol("openbis"), null, null, null, null, properties, null, null, null));

            return model.withBeans(beans);
        }));
    }

    @Override public void emitElements(final Writer writer, final Settings settings, final boolean exportKeyword, final TsModel model)
    {
        writer.writeIndentedLine("export default openbis");
    }
}
