package ch.ethz.sis.openbis.systemtest.asapi.v3.EvaluatePluginTestResources;

import java.util.EnumSet;

import ch.ethz.cisd.hotdeploy.PluginInfo;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IDynamicPropertyCalculatorHotDeployPlugin;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.IEntityAdaptor;

@PluginInfo(name = TestDynamicPropertyHotDeployedPlugin.PLUGIN_NAME, pluginType = TestDynamicPropertyHotDeployedPlugin.class)
public class TestDynamicPropertyHotDeployedPlugin implements IDynamicPropertyCalculatorHotDeployPlugin
{

    public static final String PLUGIN_NAME = "test dynamic property hot deployed name";

    @Override
    public String eval(IEntityAdaptor entity)
    {
        return entity.properties().isEmpty() ? null : entity.properties().iterator().next().valueAsString();
    }

    @Override
    public String getDescription()
    {
        return "test dynamic property hot deployed description";
    }

    @Override
    public EnumSet<EntityKind> getSupportedEntityKinds()
    {
        return EnumSet.of(EntityKind.SAMPLE);
    }

}
