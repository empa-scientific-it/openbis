/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

/**
 * A experiment to register.
 * 
 * @author Izabela Adamczyk
 */
public class NewBasicExperiment extends Identifier<NewBasicExperiment> implements IPropertiesBean
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static final String EXPERIMENT_REGISTRATION_TEMPLATE_COMMENT =
            "# Besides the full identifier of format '/SPACE_CODE/PROJECT_CODE/EXPERIMENT_CODE', two short formats 'EXPERIMENT_CODE' and 'PROJECT_CODE/EXPERIMENT_CODE' are accepted given that the default project (former short format) or default space (latter short format) are configured. If the proper default value is not configured when using a short format, experiment import will fail.\n";

    private IEntityProperty[] properties = IEntityProperty.EMPTY_ARRAY;

    public NewBasicExperiment()
    {
    }

    public NewBasicExperiment(final String identifier)
    {
        setIdentifier(identifier);
    }

    @Override
    public final IEntityProperty[] getProperties()
    {
        return properties;
    }

    @Override
    public final void setProperties(final IEntityProperty[] properties)
    {
        this.properties = properties;
    }

    @Override
    public final String toString()
    {
        return getIdentifier();
    }

}
