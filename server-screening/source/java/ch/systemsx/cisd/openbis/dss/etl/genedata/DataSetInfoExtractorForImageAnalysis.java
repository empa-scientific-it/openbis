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
import java.util.Arrays;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.DefaultDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * {@link DefaultDataSetInfoExtractor} which sets the parent dataset code to 'spaceCode-plateCode'. Useful for screening image analysis datasets.
 * 
 * @author Tomasz Pylak
 */
public class DataSetInfoExtractorForImageAnalysis extends DefaultDataSetInfoExtractor
{

    public DataSetInfoExtractorForImageAnalysis(Properties properties)
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
        String parentDataSetCode =
                dataSetInformation.getSpaceCode() + "-" + dataSetInformation.getSampleCode();
        parentDataSetCode = parentDataSetCode.toUpperCase();
        dataSetInformation.setParentDataSetCodes(Arrays.asList(parentDataSetCode));

        // by default the dataset is conected to the sample. We want to reconnect it directly to the
        // experiment, since datasets which have parents cannot be connected directly to samples.
        Sample sample =
                openbisService.tryGetSampleWithExperiment(dataSetInformation.getSampleIdentifier());
        if (sample == null)
        {
            throw UserFailureException.fromTemplate(
                    "Cannot register dataset %s because the sample %s does not exist.",
                    dataSetInformation.getDataSetCode(), dataSetInformation.getSampleIdentifier());
        }
        Experiment experiment = sample.getExperiment();
        dataSetInformation.setExperiment(experiment);
        dataSetInformation.setExperimentIdentifier(new ExperimentIdentifier(experiment));
        dataSetInformation.setSample(null);
        dataSetInformation.setSampleCode(null);

        return dataSetInformation;
    }
}
