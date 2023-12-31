/*
 * Copyright ETH 2016 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update;

import java.util.Arrays;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.common.update.UpdateObjectsOperation")
public abstract class UpdateObjectsOperation<U extends IObjectUpdate<?>> implements IOperation
{

    private static final long serialVersionUID = 1L;

    private List<U> updates;

    @SuppressWarnings("unchecked")
    public UpdateObjectsOperation(U... updates)
    {
        this.updates = Arrays.asList(updates);
    }

    public UpdateObjectsOperation(List<U> updates)
    {
        this.updates = updates;
    }

    public List<U> getUpdates()
    {
        return updates;
    }

    @Override
    public String getMessage()
    {
        return toString();
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + (getUpdates() != null ? " " + getUpdates().size() + " updates(s)" : "");
    }

}
