/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.genedata;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.DefaultDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * {@link DefaultDataSetInfoExtractor} which sets the dataset code to 'spaceCode-plateCode'. Useful for screening image datasets.
 * 
 * @author Tomasz Pylak
 */
public class DataSetInfoExtractorForDataAcquisition extends DefaultDataSetInfoExtractor
{

    public DataSetInfoExtractorForDataAcquisition(Properties properties)
    {
        super(properties);
    }

    @Override
    public DataSetInformation getDataSetInformation(final File incomingDataSetPath,
            IEncapsulatedOpenBISService openbisService) throws EnvironmentFailureException,
            UserFailureException
    {
        DataSetInformation dataSetInformation =
                super.getDataSetInformation(incomingDataSetPath, openbisService);
        String dataSetCode =
                dataSetInformation.getSpaceCode() + "-" + dataSetInformation.getSampleCode();
        dataSetCode = dataSetCode.toUpperCase();
        dataSetInformation.setDataSetCode(dataSetCode);
        return dataSetInformation;
    }
}
