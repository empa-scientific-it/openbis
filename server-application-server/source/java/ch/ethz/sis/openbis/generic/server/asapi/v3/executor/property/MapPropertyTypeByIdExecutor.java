/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.AbstractMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.IListObjectById;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.property.ListPropertyTypeByPermId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

/**
 * @author pkupczyk
 */
@Component
public class MapPropertyTypeByIdExecutor extends AbstractMapObjectByIdExecutor<IPropertyTypeId, PropertyTypePE>
        implements IMapPropertyTypeByIdExecutor
{
    private IPropertyTypeDAO propertyTypeDAO;

    @Autowired
    private IPropertyTypeAuthorizationExecutor authorizationExecutor;

    @Override
    protected void checkAccess(IOperationContext context)
    {
        authorizationExecutor.canGet(context);
    }

    @Override
    protected void addListers(IOperationContext context, List<IListObjectById<? extends IPropertyTypeId, PropertyTypePE>> listers)
    {
        listers.add(new ListPropertyTypeByPermId(propertyTypeDAO));
    }

    @Autowired
    private void setDAOFactory(IDAOFactory daoFactory)
    {
        propertyTypeDAO = daoFactory.getPropertyTypeDAO();
    }

}
