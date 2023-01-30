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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IExpressionUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewColumnOrFilter;
import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomFilterPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * Operations on grid custom filters.
 * 
 * @author Izabela Adamczyk
 */
public class GridCustomFilterBO extends AbstractBusinessObject implements
        IGridCustomFilterOrColumnBO
{
    private GridCustomFilterPE filter;

    public GridCustomFilterBO(IDAOFactory daoFactory, Session session,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker,
            IRelationshipService relationshipService)
    {
        super(daoFactory, session, managedPropertyEvaluatorFactory, dataSetTypeChecker, relationshipService);
    }

    @Override
    public void define(NewColumnOrFilter newFilter) throws UserFailureException
    {
        filter = new GridCustomFilterPE();
        filter.setDescription(newFilter.getDescription());
        filter.setExpression(newFilter.getExpression());
        filter.setGridId(newFilter.getGridId());
        filter.setName(newFilter.getName());
        filter.setPublic(newFilter.isPublic());
        filter.setRegistrator(findPerson());
    }

    @Override
    public void loadDataByTechId(TechId id)
    {
        try
        {
            filter = getGridCustomFilterDAO().getByTechId(id);
        } catch (DataRetrievalFailureException exception)
        {
            throw new UserFailureException(exception.getMessage());
        }

    }

    @Override
    public void deleteByTechId(TechId groupId) throws UserFailureException
    {
        loadDataByTechId(groupId);
        try
        {
            getGridCustomFilterDAO().delete(filter);
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("Filter '%s'", filter.getName()));
        }
    }

    @Override
    public void save() throws UserFailureException
    {
        assert filter != null : "Filter not defined";
        try
        {
            getGridCustomFilterDAO().createFilter(filter);
        } catch (final DataAccessException e)
        {
            throwException(e, "Filter '" + filter + "'");
        }
    }

    @Override
    public void update(IExpressionUpdates updates)
    {
        loadDataByTechId(TechId.create(updates));
        if (filter.getModificationDate().equals(updates.getModificationDate()) == false)
        {
            throwModifiedEntityException("Custom filter");
        }

        filter.setName(updates.getName());
        filter.setDescription(updates.getDescription());
        filter.setExpression(updates.getExpression());
        filter.setPublic(updates.isPublic());

        validateAndSave();
    }

    private void validateAndSave()
    {
        assert filter != null : "Filter not defined";
        try
        {
            getGridCustomFilterDAO().validateAndSaveUpdatedEntity(filter);
        } catch (final DataAccessException e)
        {
            throwException(e, "Filter '" + filter + "'");
        }
    }

}
