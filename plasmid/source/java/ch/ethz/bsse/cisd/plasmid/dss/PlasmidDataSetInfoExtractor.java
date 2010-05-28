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

package ch.ethz.bsse.cisd.plasmid.dss;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.DefaultDataSetInfoExtractor;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * CSB uses DSS to register data sets attached to an existing sample plasmid. This extractor will
 * receive a single file that is inside a directory from which {@link DefaultDataSetInfoExtractor}
 * will extract information about the sample. All properties will be passed to
 * {@link DefaultDataSetInfoExtractor}.
 * 
 * @author Piotr Buczek
 */
public class PlasmidDataSetInfoExtractor implements IDataSetInfoExtractor
{
    private final IDataSetInfoExtractor delegator;

    public PlasmidDataSetInfoExtractor(final Properties globalProperties)
    {
        this.delegator = new DefaultDataSetInfoExtractor(globalProperties);
    }

    public DataSetInformation getDataSetInformation(File incomingDataSetFile,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException
    {
        return delegator.getDataSetInformation(incomingDataSetFile.getParentFile(), openbisService);
    }

}
