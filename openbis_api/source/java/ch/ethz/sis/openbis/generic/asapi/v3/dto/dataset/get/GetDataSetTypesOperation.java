/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.get;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.dataset.get.GetDataSetTypesOperation")
public class GetDataSetTypesOperation extends GetObjectsOperation<IEntityTypeId, DataSetTypeFetchOptions>
{
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private GetDataSetTypesOperation()
    {
    }

    public GetDataSetTypesOperation(List<? extends IEntityTypeId> ids, DataSetTypeFetchOptions fetchOptions)
    {
        super(ids, fetchOptions);
    }

}
