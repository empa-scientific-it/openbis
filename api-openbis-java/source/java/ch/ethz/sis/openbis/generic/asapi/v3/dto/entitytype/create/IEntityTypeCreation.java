/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.create;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.ICreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.IObjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.entitytype.create.IEntityTypeCreation")
public interface IEntityTypeCreation extends ICreation, IObjectCreation
{

    public String getCode();

    public void setCode(String code);

    public String getDescription();

    public void setDescription(String description);

    public IPluginId getValidationPluginId();

    public void setValidationPluginId(IPluginId pluginId);

    public List<PropertyAssignmentCreation> getPropertyAssignments();

    public void setPropertyAssignments(List<PropertyAssignmentCreation> propertyAssignments);

}
