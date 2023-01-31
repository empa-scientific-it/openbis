/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator;

import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IJavaDataSetRegistrationDropboxV2;

/**
 * @author Pawel Glyzewski
 */
public class JavaV2TestcaseRollback extends JavaAllHooks implements
        IJavaDataSetRegistrationDropboxV2
{

    @Override
    public void process(IDataSetRegistrationTransactionV2 transaction)
    {
        IDataSet dataSet = transaction.createNewDataSet();
        transaction.moveFile(transaction.getIncoming().getPath() + "/sub_data_set_1", dataSet);
        dataSet.setDataSetType("O1");
        dataSet.setExperiment(transaction.getSearchService().getExperiment("/SPACE/PROJECT/EXP"));
        throw new NullPointerException();
    }

    @Override
    public boolean isRetryFunctionDefined()
    {
        return false;
    }

    @Override
    public boolean shouldRetryProcessing(DataSetRegistrationContext context, Exception problem)
            throws NotImplementedException
    {
        throw new NotImplementedException();
    }

}
