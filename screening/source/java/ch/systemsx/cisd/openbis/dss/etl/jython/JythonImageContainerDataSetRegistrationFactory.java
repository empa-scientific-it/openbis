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

package ch.systemsx.cisd.openbis.dss.etl.jython;

import java.io.File;

import ch.systemsx.cisd.etlserver.registrator.AbstractDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageContainerDataSet;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * @author jakubs
 */
public class JythonImageContainerDataSetRegistrationFactory extends
        AbstractDataSetRegistrationDetailsFactory<DataSetInformation>
{

    public JythonImageContainerDataSetRegistrationFactory(
            ch.systemsx.cisd.etlserver.registrator.AbstractOmniscientTopLevelDataSetRegistrator.OmniscientTopLevelDataSetRegistratorState registratorState,
            DataSetInformation userProvidedDataSetInformationOrNull)
    {
        super(registratorState, userProvidedDataSetInformationOrNull);
    }

    @Override
    public ImageContainerDataSet createDataSet(
            DataSetRegistrationDetails<DataSetInformation> registrationDetails, File stagingFile)
    {
        IEncapsulatedOpenBISService service = registratorState.getGlobalState().getOpenBisService();
        return new ImageContainerDataSet(registrationDetails, stagingFile, service);
    }

    @Override
    protected DataSetInformation createDataSetInformation()
    {
        return new DataSetInformation();
    }
}