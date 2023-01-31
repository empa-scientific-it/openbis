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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * {@link AbstractDelegatingDataSetInfoExtractor} performing additional file name property extraction. Registration will fail if this property type
 * doesn't exist in openBIS DB or is not attached to the data set type extracted by {@link ITypeExtractor}.
 * 
 * @author Piotr Buczek
 */
public class DataSetInfoExtractorWithFileNameProperty extends
        AbstractDelegatingDataSetInfoExtractor
{
    private final DataSetInfoFileNameDecorator fileNameDecorator;

    public DataSetInfoExtractorWithFileNameProperty(Properties properties)
    {
        super(properties);
        this.fileNameDecorator = new DataSetInfoFileNameDecorator(properties);
    }

    @Override
    public DataSetInformation getDataSetInformation(File incomingDataSetPath,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException
    {
        DataSetInformation dataSetInformation =
                super.getDataSetInformation(incomingDataSetPath, openbisService);
        fileNameDecorator.enrich(dataSetInformation, incomingDataSetPath);
        return dataSetInformation;
    }

}
