/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.property;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListObjectById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

/**
 * @author pkupczyk
 */
public class ListPropertyTypeByPermId extends AbstractListObjectById<PropertyTypePermId, PropertyTypePE>
{

    private IPropertyTypeDAO propertyTypeDAO;

    public ListPropertyTypeByPermId(IPropertyTypeDAO propertyTypeDAO)
    {
        this.propertyTypeDAO = propertyTypeDAO;
    }

    @Override
    public Class<PropertyTypePermId> getIdClass()
    {
        return PropertyTypePermId.class;
    }

    @Override
    public PropertyTypePermId createId(PropertyTypePE propertyType)
    {
        return new PropertyTypePermId(propertyType.getCode());
    }

    @Override
    public List<PropertyTypePE> listByIds(IOperationContext context, List<PropertyTypePermId> ids)
    {
        List<PropertyTypePE> types = new ArrayList<PropertyTypePE>();

        for (PropertyTypePermId id : ids)
        {
            PropertyTypePE type = propertyTypeDAO.tryFindPropertyTypeByCode(id.getPermId());
            if (type != null)
            {
                types.add(type);
            }
        }

        return types;
    }

}
