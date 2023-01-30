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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;

/**
 * Set of specific dynamic property related functions for functions used in jython expressions.
 * <p>
 * All public methods of this class are part of the Dynamic Property Evaluation API.
 * 
 * @author Piotr Buczek
 */
public final class DynamicPropertyFunctions
{
    /**
     * @return String representation of material identifier for given material code and material type code.
     */
    public static String material(String code, String typeCode)
    {
        return MaterialIdentifier.print(code, typeCode);
    }

    /**
     * @return code of material with given identifier or null if the identifier is not a valid material identifier
     */
    public static String materialCode(String materialIdentifier)
    {
        final MaterialIdentifier identifierOrNull =
                MaterialIdentifier.tryParseIdentifier(materialIdentifier);
        return identifierOrNull == null ? null : identifierOrNull.getCode();
    }

    /**
     * @return type of material with given identifier or null if the identifier is not a valid material identifier
     */
    public static String materialTypeCode(String materialIdentifier)
    {
        final MaterialIdentifier identifierOrNull =
                MaterialIdentifier.tryParseIdentifier(materialIdentifier);
        return identifierOrNull == null ? null : identifierOrNull.getTypeCode();
    }
}