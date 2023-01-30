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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractSetEntitySampleRelationExecutor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class SetDataSetSampleExecutor extends AbstractSetEntitySampleRelationExecutor<DataSetCreation, DataPE> implements
        ISetDataSetSampleExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Override
    protected String getRelationName()
    {
        return "dataset-sample";
    }

    @Override
    protected ISampleId getRelatedId(DataSetCreation creation)
    {
        return creation.getSampleId();
    }

    @Override
    protected void set(IOperationContext context, DataPE entity, SamplePE related)
    {
        if (related != null)
        {
            Session session = context.getSession();
            Date timeStamp = daoFactory.getTransactionTimestamp();
            RelationshipUtils.setSampleForDataSet(entity, related, session, timeStamp);
            RelationshipUtils.setExperimentForDataSet(entity, related.getExperiment(), session, timeStamp);
        }
    }

}
