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

package ch.systemsx.cisd.etlserver.registrator.api.v2;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISampleImmutable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;

/**
 * An example dropbox implemented in Java.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ExampleJavaDataSetRegistrationDropboxV2 extends
        AbstractJavaDataSetRegistrationDropboxV2
{

    @Override
    public void process(IDataSetRegistrationTransactionV2 transaction)
    {
        @SuppressWarnings("deprecation")
        ISampleImmutable container = transaction.getSample("/CISD/DEFAULT/PLATE_WELLSEARCH");
        ISample sample = transaction.createNewSample("/CISD/DP1-A", "NORMAL");
        sample.setContainer(container);
        IDataSet dataSet = transaction.createNewDataSet("UNKNOWN");
        dataSet.setDataSetKind(DataSetKind.PHYSICAL);
        dataSet.setSample(sample);
        transaction.moveFile(transaction.getIncoming().getAbsolutePath(), dataSet);
    }
}
