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

package ch.systemsx.cisd.etlserver.registrator;

import ch.systemsx.cisd.etlserver.registrator.api.v2.AbstractJavaDataSetRegistrationDropboxV2;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISampleImmutable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;

/**
 * Default drop box which can register everything.
 *
 * @author Franz-Josef Elmer
 */
public class DefaultDropbox extends AbstractJavaDataSetRegistrationDropboxV2
{

    private static final String SAMPLE_IDENTIFIER = "/DEFAULT/DEFAULT";

    @Override
    public void process(IDataSetRegistrationTransactionV2 transaction)
    {
        IDataSet dataSet = transaction.createNewDataSet();
        if (dataSet.getExperiment() == null && dataSet.getSample() == null)
        {
            ISampleImmutable sample = transaction.getSearchService().getSample(SAMPLE_IDENTIFIER);
            if (sample == null)
            {
                sample = transaction.createNewSample(SAMPLE_IDENTIFIER, "UNKNOWN");
            }
            dataSet.setSample(sample);
        }
        if (dataSet.getDataSetType() == null)
        {
            dataSet.setDataSetType("UNKNOWN");
        }
        if (dataSet.getDataSetKind() == null)
        {
        	dataSet.setDataSetKind(DataSetKind.PHYSICAL);
        }
        transaction.moveFile(transaction.getIncoming().getAbsolutePath(), dataSet);
    }
}
