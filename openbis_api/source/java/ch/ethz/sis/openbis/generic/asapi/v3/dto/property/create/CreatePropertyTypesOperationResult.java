/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.CreateObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 *
 */
@JsonObject("as.dto.property.create.CreatePropertyTypesOperationResult")
public class CreatePropertyTypesOperationResult extends CreateObjectsOperationResult<PropertyTypePermId>
{

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private CreatePropertyTypesOperationResult()
    {
    }

    public CreatePropertyTypesOperationResult(List<PropertyTypePermId> objectIds)
    {
        super(objectIds);
    }
}
