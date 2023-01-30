/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.DataSetPEByExperimentOrSampleIdentifierValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataSetTable;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;

/**
 * @author pkupczyk
 */
abstract class AbstractArchiveUnarchiveDataSetExecutor
{
    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    protected IMapDataSetByIdExecutor mapDataSetByIdExecutor;

    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    protected ICommonBusinessObjectFactory businessObjectFactory;

    protected void doArchiveUnarchive(IOperationContext context, List<? extends IDataSetId> dataSetIds, Object options,
            IArchiveUnarchiveAction action)
    {
        if (context == null)
        {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (dataSetIds == null)
        {
            throw new IllegalArgumentException("Data set ids cannot be null");
        }
        if (options == null)
        {
            throw new IllegalArgumentException("Options cannot be null");
        }

        Map<IDataSetId, DataPE> dataSetMap = mapDataSetByIdExecutor.map(context, dataSetIds);
        Set<String> dataSetCodes = new HashSet<String>();

        for (IDataSetId dataSetId : dataSetIds)
        {
            DataPE dataSet = dataSetMap.get(dataSetId);
            if (dataSet == null)
            {
                throw new ObjectNotFoundException(dataSetId);
            }
            assertAuthorization(context, dataSetId, dataSet);
            if (false == dataSetCodes.contains(dataSet.getCode()))
            {
                DataSetPEByExperimentOrSampleIdentifierValidator validator = new DataSetPEByExperimentOrSampleIdentifierValidator();
                validator.init(new AuthorizationDataProvider(daoFactory));

                if (false == validator.doValidation(context.getSession().tryGetPerson(), dataSet))
                {
                    throw new UnauthorizedObjectAccessException(dataSetId);
                }
                dataSetCodes.add(dataSet.getCode());
            }
        }

        IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(context.getSession());
        dataSetTable.loadByDataSetCodes(new ArrayList<String>(dataSetCodes), false, true);
        action.execute(dataSetTable);
    }

    protected abstract void assertAuthorization(IOperationContext context, IDataSetId dataSetId, DataPE dataSet);

    public static interface IArchiveUnarchiveAction
    {

        public void execute(IDataSetTable dataSetTable);

    }

}
