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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.revert;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.deletion.revert.RevertDeletionsOperation")
public class RevertDeletionsOperation implements IOperation
{

    private static final long serialVersionUID = 1L;

    private List<? extends IDeletionId> deletionIds;

    @SuppressWarnings("unused")
    private RevertDeletionsOperation()
    {
    }

    public RevertDeletionsOperation(List<? extends IDeletionId> deletionIds)
    {
        this.deletionIds = deletionIds;
    }

    public List<? extends IDeletionId> getDeletionIds()
    {
        return deletionIds;
    }

    @Override
    public String getMessage()
    {
        return toString();
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + (deletionIds != null ? " " + deletionIds.size() + " deletion(s)" : "");
    }

}
