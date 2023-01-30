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

import java.util.HashSet;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.ExpressionUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IExpressionUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewColumnOrFilter;
import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomColumnPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * Operations on grid custom columns.
 * 
 * @author Tomasz Pylak
 */
public class GridCustomColumnBO extends AbstractBusinessObject implements
        IGridCustomFilterOrColumnBO
{
    private static final String CUSTOM_COLUMN_ID_PREFIX = "$";

    private GridCustomColumnPE column;

    public GridCustomColumnBO(IDAOFactory daoFactory, Session session,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker,
            IRelationshipService relationshipService)
    {
        super(daoFactory, session, managedPropertyEvaluatorFactory, dataSetTypeChecker, relationshipService);
    }

    @Override
    public void define(NewColumnOrFilter newColumn) throws UserFailureException
    {
        column = new GridCustomColumnPE();
        column.setCode(createUniqueCode(newColumn));
        column.setLabel(newColumn.getName());

        column.setDescription(newColumn.getDescription());
        column.setExpression(checkExpressionFreeOfParameters(newColumn.getExpression()));
        column.setGridId(newColumn.getGridId());
        column.setPublic(newColumn.isPublic());
        column.setRegistrator(findPerson());
    }

    private static String createUniqueCode(NewColumnOrFilter newColumn)
    {
        String name = newColumn.getName();
        String code = name.replace(" ", "_");
        code = code.toLowerCase();
        return CUSTOM_COLUMN_ID_PREFIX + code;
    }

    @Override
    public void loadDataByTechId(TechId id)
    {
        try
        {
            column = getGridCustomColumnDAO().getByTechId(id);
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
            getGridCustomColumnDAO().delete(column);
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("Column '%s'", column));
        }
    }

    @Override
    public void save() throws UserFailureException
    {
        assert column != null : "Column not defined";
        try
        {
            getGridCustomColumnDAO().createColumn(column);
        } catch (final DataAccessException e)
        {
            throwException(e, "Column '" + column + "'");
        }
    }

    @Override
    public void update(IExpressionUpdates updates)
    {
        loadDataByTechId(TechId.create(updates));
        if (column.getModificationDate().equals(updates.getModificationDate()) == false)
        {
            throwModifiedEntityException("Custom column");
        }

        column.setLabel(updates.getName());
        column.setDescription(updates.getDescription());
        column.setExpression(checkExpressionFreeOfParameters(updates.getExpression()));
        column.setPublic(updates.isPublic());

        validateAndSave();
    }

    private String checkExpressionFreeOfParameters(String expression)
    {
        Set<String> parameters = new HashSet<String>(ExpressionUtil.extractParameters(expression));
        if (parameters.isEmpty())
        {
            return expression;
        }
        throw new UserFailureException("Unexpected parameters " + parameters + " in expression: "
                + expression);
    }

    private void validateAndSave()
    {
        getGridCustomColumnDAO().validateAndSaveUpdatedEntity(column);
    }

}
