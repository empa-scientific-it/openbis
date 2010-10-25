/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator;

import java.util.Collection;

/**
 * Interface implemented by all entity adaptors
 * <p>
 * All methods of this interface are part of the Dynamic Properties API.
 * 
 * @author Piotr Buczek
 */
public interface IEntityAdaptor
{
    /** Returns the code. */
    public String code();

    /** Returns the property by code of the property type. */
    public IEntityPropertyAdaptor propertyByCode(String propertyTypeCode);

    /** Returns the property value by code of the property type. */
    public String propertyValueByCode(String propertyTypeCode);

    /** Returns collection of properties of the entity */
    public Collection<IEntityPropertyAdaptor> properties();

}
