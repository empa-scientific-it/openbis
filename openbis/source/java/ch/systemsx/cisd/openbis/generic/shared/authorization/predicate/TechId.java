/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.authorization.predicate;

import ch.systemsx.cisd.openbis.generic.shared.dto.IIdHolder;

/**
 * Technical identifier of an entity.
 * 
 * @author Piotr Buczek
 */
public class TechId implements IIdHolder
{
    private final Long id;

    public TechId(Long id)
    {
        super();
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    @Override
    public String toString()
    {
        return String.valueOf(id);
    }

}
