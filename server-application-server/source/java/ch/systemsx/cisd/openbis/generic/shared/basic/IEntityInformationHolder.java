/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * A unique pointer to an entity (see {@link EntityKind} for different kinds of entities).<br>
 * Specifies entity technical id, code, type code and {@link EntityKind} .
 * 
 * @author Piotr Buczek
 */
public interface IEntityInformationHolder extends Serializable, IIdAndCodeHolder
{
    /**
     * Returns the entity type of this matching entity.
     */
    public BasicEntityType getEntityType();

    /**
     * Return the entity kind of this matching entity.
     */
    public EntityKind getEntityKind();

}
