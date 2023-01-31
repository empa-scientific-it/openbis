/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.io.File;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.Constants;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetTest extends AssertJUnit
{
    @Test
    public void testSpeedHint()
    {
        DataSetRegistrationDetails<DataSetInformation> registrationDetails =
                new DataSetRegistrationDetails<DataSetInformation>();
        registrationDetails.setDataSetInformation(new DataSetInformation());
        IDataSet dataSet = new DataSet<DataSetInformation>(registrationDetails, new File("."), null);

        assertEquals(Constants.DEFAULT_SPEED_HINT, dataSet.getSpeedHint());

        dataSet.setSpeedHint(Constants.MAX_SPEED * 2);
        assertEquals(Constants.MAX_SPEED, dataSet.getSpeedHint());

        dataSet.setSpeedHint(-Constants.MAX_SPEED * 2);
        assertEquals(-Constants.MAX_SPEED, dataSet.getSpeedHint());

        dataSet.setSpeedHint(Constants.MAX_SPEED / 3);
        assertEquals(Constants.MAX_SPEED / 3, dataSet.getSpeedHint());

        dataSet.setSpeedHint(-Constants.MAX_SPEED / 3);
        assertEquals(-Constants.MAX_SPEED / 3, dataSet.getSpeedHint());
    }
}
