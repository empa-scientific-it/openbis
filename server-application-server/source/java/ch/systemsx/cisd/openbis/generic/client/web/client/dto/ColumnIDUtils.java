/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.TableCellUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * Helper class.
 * 
 * @author Kaloyan Enimanev
 */
public class ColumnIDUtils
{

    /**
     * Computes the column id for a property given the id of a column group and the property type.
     * 
     * @param propertyGroupId the id of the column group containing the entity properties.
     * @param propertyTypeName the type of the property whose column id we want to compute
     */
    public static String getColumnIdForProperty(String propertyGroupId, String propertyTypeName)
    {
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(propertyTypeName);
        propertyType.setSimpleCode(propertyTypeName);
        String detailsPropertyColumnId =
                propertyGroupId + TableCellUtil.getPropertyTypeCode(propertyType);
        return detailsPropertyColumnId;
    }

}
