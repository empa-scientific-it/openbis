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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * The only productive implementation of {@link IPropertyTypeTable}.
 * 
 * @author Izabela Adamczyk
 */
public final class PropertyTypeTable extends AbstractBusinessObject implements IPropertyTypeTable
{
    private List<PropertyTypePE> propertyTypes;

    public PropertyTypeTable(final IDAOFactory daoFactory, final Session session,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker,
            IRelationshipService relationshipService)
    {
        super(daoFactory, session, managedPropertyEvaluatorFactory, dataSetTypeChecker, relationshipService);
    }

    @Override
    public List<PropertyTypePE> getPropertyTypes()
    {
        if (propertyTypes == null)
        {
            throw new IllegalStateException("Unloaded property types.");
        }
        return propertyTypes;
    }

    @Override
    public void load()
    {
        propertyTypes = getPropertyTypeDAO().listAllPropertyTypes();
    }

    @Override
    public void loadWithRelations()
    {
        propertyTypes = getPropertyTypeDAO().listAllPropertyTypesWithRelations();
    }

    @Override
    public final void enrichWithRelations()
    {
        if (propertyTypes == null)
        {
            throw new IllegalStateException("Unloaded property types.");
        }
        for (final PropertyTypePE pt : propertyTypes)
        {
            // optimize?
            HibernateUtils.initialize(pt.getMaterialTypePropertyTypes());
            HibernateUtils.initialize(pt.getSampleTypePropertyTypes());
            HibernateUtils.initialize(pt.getExperimentTypePropertyTypes());
            HibernateUtils.initialize(pt.getDataSetTypePropertyTypes());
            HibernateUtils.initialize(pt.getVocabulary());
        }
    }
}
