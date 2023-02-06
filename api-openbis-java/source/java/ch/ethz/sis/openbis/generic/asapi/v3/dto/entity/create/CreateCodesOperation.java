/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.entity.create;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.entity.create.CreateCodesOperation")
public class CreateCodesOperation implements IOperation
{

    private static final long serialVersionUID = 1L;

    private String prefix;

    private EntityKind entityKind;

    private int count;

    @SuppressWarnings("unused")
    private CreateCodesOperation()
    {
    }

    public CreateCodesOperation(String prefix, EntityKind entityKind, int count)
    {
        this.prefix = prefix;
        this.entityKind = entityKind;
        this.count = count;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public EntityKind getEntityKind()
    {
        return entityKind;
    }

    public int getCount()
    {
        return count;
    }

    @Override
    public String getMessage()
    {
        return toString();
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName();
    }

}
