/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.UpdateObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.dataset.update.UpdateDataSetTypesOperationResult")
public class UpdateDataSetTypesOperationResult extends UpdateObjectsOperationResult<EntityTypePermId>
{

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private UpdateDataSetTypesOperationResult()
    {
    }

    public UpdateDataSetTypesOperationResult(List<EntityTypePermId> ids)
    {
        super(ids);
    }

}