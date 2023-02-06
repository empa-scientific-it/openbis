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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create;

import java.util.Arrays;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.common.create.CreateObjectsOperation")
public abstract class CreateObjectsOperation<C extends IObjectCreation> implements IOperation
{

    private static final long serialVersionUID = 1L;

    private List<C> creations;

    @SuppressWarnings("unchecked")
    public CreateObjectsOperation(C... creations)
    {
        this.creations = Arrays.asList(creations);
    }

    public CreateObjectsOperation(List<C> creations)
    {
        this.creations = creations;
    }

    public List<C> getCreations()
    {
        return creations;
    }

    @Override
    public String getMessage()
    {
        return toString();
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + (getCreations() != null ? " " + getCreations().size() + " creation(s)" : "");
    }

}
