/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.common.get.GetObjectsOperation")
public abstract class GetObjectsOperation<ID extends IObjectId, FETCH_OPTIONS extends FetchOptions<?>> implements IOperation
{

    private static final long serialVersionUID = 1L;

    private List<? extends ID> objectIds;

    private FETCH_OPTIONS fetchOptions;

    protected GetObjectsOperation()
    {
    }

    public GetObjectsOperation(List<? extends ID> objectIds, FETCH_OPTIONS fetchOptions)
    {
        this.objectIds = objectIds;
        this.fetchOptions = fetchOptions;
    }

    public List<? extends ID> getObjectIds()
    {
        return objectIds;
    }

    public FETCH_OPTIONS getFetchOptions()
    {
        return fetchOptions;
    }

    @Override
    public String getMessage()
    {
        return toString();
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + (getObjectIds() != null ? " " + getObjectIds().size() + " object(s)" : "");
    }

}
