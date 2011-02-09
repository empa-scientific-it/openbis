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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.ArrayList;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class AtomicEntityOperationDetailsTest extends AssertJUnit
{

    @Test
    public void testToString()
    {
        ArrayList<NewExperiment> experimentRegistrations = new ArrayList<NewExperiment>();
        experimentRegistrations.add(new NewExperiment("/SPACE/PROJECT/EXP-ID1", "EXP-TYPE"));
        experimentRegistrations.add(new NewExperiment("/SPACE/PROJECT/EXP-ID2", "EXP-TYPE"));

        ArrayList<SampleUpdatesDTO> sampleUpdates = new ArrayList<SampleUpdatesDTO>();

        ArrayList<NewSample> sampleRegistrations = new ArrayList<NewSample>();
        sampleRegistrations.add(new NewSample("/SPACE/SAMPLE-ID1", new SampleType(), null, null,
                "/SPACE/PROJECT/EXP-ID1", new IEntityProperty[0], new ArrayList<NewAttachment>()));
        sampleRegistrations.add(new NewSample("/SPACE/SAMPLE-ID2", new SampleType(), null, null,
                "/SPACE/PROJECT/EXP-ID1", new IEntityProperty[0], new ArrayList<NewAttachment>()));

        ArrayList<NewExternalData> dataSetRegistrations = new ArrayList<NewExternalData>();
        NewExternalData newExternalData = new NewExternalData();
        newExternalData.setCode("DATA-SET-CODE");
        newExternalData.setSampleIdentifierOrNull(new SampleIdentifierFactory("/SPACE/SAMPLE-ID1")
                .createIdentifier());
        dataSetRegistrations.add(newExternalData);

        AtomicEntityOperationDetails details =
                new AtomicEntityOperationDetails(experimentRegistrations, sampleUpdates,
                        sampleRegistrations, dataSetRegistrations);

        assertEquals(
                "AtomicEntityOperationDetails[experimentUpdates=[]"
                        + ",experimentRegistrations=[/SPACE/PROJECT/EXP-ID1, /SPACE/PROJECT/EXP-ID2]"
                        + ",sampleUpdates=[]"
                        + ",sampleRegistrations=[/SPACE/SAMPLE-ID1, /SPACE/SAMPLE-ID2]"
                        + ",dataSetRegistrations=[NewExternalData[code=DATA-SET-CODE,type=<null>,fileFormat=<null>,properties=[]]]]",
                details.toString());

    }
}
