/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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

import java.util.List;
import java.util.Map;

/**
 * The <i>GWT</i> equivalent to ExperimentTypePE.
 * 
 * @author Tomasz Pylak
 */
public class ExperimentType extends EntityType
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private List<ExperimentTypePropertyType> experimentTypePropertyTypes;

    private Map<String, String> metaData;

    @Override
    public List<ExperimentTypePropertyType> getAssignedPropertyTypes()
    {
        return experimentTypePropertyTypes;
    }

    public void setExperimentTypePropertyTypes(
            List<ExperimentTypePropertyType> experimentTypePropertyTypes)
    {
        this.experimentTypePropertyTypes = experimentTypePropertyTypes;
    }

    @Override
    public boolean isEntityKind(EntityKind kind)
    {
        return kind == null || kind == EntityKind.EXPERIMENT;
    }

    @Override
    public EntityKind getEntityKind()
    {
        return EntityKind.EXPERIMENT;
    }

    public Map<String, String> getMetaData()
    {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData)
    {
        this.metaData = metaData;
    }
}
