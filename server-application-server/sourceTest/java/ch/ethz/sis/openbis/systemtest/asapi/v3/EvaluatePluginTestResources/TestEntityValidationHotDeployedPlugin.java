package ch.ethz.sis.openbis.systemtest.asapi.v3.EvaluatePluginTestResources;

import java.util.EnumSet;

import ch.ethz.cisd.hotdeploy.PluginInfo;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.INonAbstractEntityAdapter;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.JythonEntityValidationCalculator.IValidationRequestDelegate;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation.IEntityValidatorHotDeployPlugin;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.IEntityAdaptor;

@PluginInfo(name = TestEntityValidationHotDeployedPlugin.PLUGIN_NAME, pluginType = TestEntityValidationHotDeployedPlugin.class)
public class TestEntityValidationHotDeployedPlugin implements IEntityValidatorHotDeployPlugin
{

    public static final String PLUGIN_NAME = "test entity validation hot deployed name";

    private IValidationRequestDelegate<INonAbstractEntityAdapter> validationRequestedDelegate;

    @Override
    public void init(IValidationRequestDelegate<INonAbstractEntityAdapter> validationRequestedDelegate)
    {
        this.validationRequestedDelegate = validationRequestedDelegate;
    }

    @Override
    public String validate(IEntityAdaptor entity, boolean isNew)
    {
        validationRequestedDelegate.requestValidation((INonAbstractEntityAdapter) entity);
        return entity.properties().isEmpty() ? null : entity.properties().iterator().next().valueAsString();
    }

    @Override
    public String getDescription()
    {
        return "test entity validation hot deployed description";
    }

    @Override
    public EnumSet<EntityKind> getSupportedEntityKinds()
    {
        return EnumSet.of(EntityKind.SAMPLE);
    }

}
