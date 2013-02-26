/*
 * Copyright 2013 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.INonAbstractEntityAdapter;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.JythonEntityValidationCalculator.IValidationRequestDelegate;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation.api.IEntityValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.AbstractCommonPropertyBasedHotDeployPluginFactory;

/**
 * @author Pawel Glyzewski
 */
public class EntityValidatorFactory extends
        AbstractCommonPropertyBasedHotDeployPluginFactory<IEntityValidatorHotDeployPlugin>
        implements IEntityValidatorFactory
{
    public EntityValidatorFactory(String pluginDirectoryPath)
    {
        super(pluginDirectoryPath);
    }

    @Override
    public IEntityValidator createEntityValidator(EntityTypePE entityTypePE,
            IValidationRequestDelegate<INonAbstractEntityAdapter> validationRequestedDelegate)
    {
        ScriptPE scriptPE = entityTypePE.getValidationScript();

        if (scriptPE != null)
        {
            IEntityValidator validator =
                    createEntityValidator(scriptPE.getPluginType(), scriptPE.getName(),
                            scriptPE.getScript());
            validator.init(validationRequestedDelegate);
            return validator;
        }

        return null;
    }

    @Override
    public IEntityValidator createEntityValidator(PluginType pluginType, String scriptName,
            String script)
    {
        switch (pluginType)
        {
            case JYTHON:
                return new JythonEntityValidator(script);
            case PREDEPLOYED:
                IEntityValidator entityValidator = tryGetPredeployedPluginByName(scriptName);
                if (entityValidator == null)
                {
                    throw new UserFailureException("Couldn't find plugin named '" + scriptName
                            + "'.");
                }

                return entityValidator;
        }

        return null;
    }

    @Override
    protected String getPluginDescription()
    {
        return "entity validation";
    }

    @Override
    protected Class<IEntityValidatorHotDeployPlugin> getPluginClass()
    {
        return IEntityValidatorHotDeployPlugin.class;
    }

    @Override
    protected ScriptType getScriptType()
    {
        return ScriptType.ENTITY_VALIDATION;
    }

    @Override
    protected String getDefaultPluginSubDirName()
    {
        return "entity-validation";
    }
}
