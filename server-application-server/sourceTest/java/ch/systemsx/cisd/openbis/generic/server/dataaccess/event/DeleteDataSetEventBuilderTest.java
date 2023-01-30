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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.event;

import java.util.Arrays;
import java.util.Collections;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
public class DeleteDataSetEventBuilderTest extends AssertJUnit
{

    @Test
    public void testBuildForOneDataSet()
    {
        PersonPE registrator = new PersonPE();

        DataStorePE store = new DataStorePE();
        store.setCode("TEST-DSS");

        ExternalDataPE dataSet = new ExternalDataPE();
        dataSet.setCode("TEST-DATA-SET");
        dataSet.setDataStore(store);
        dataSet.setShareId("TEST-SHARE-ID");
        dataSet.setLocation("TEST-LOCATION");

        DeleteDataSetEventBuilder builder = new DeleteDataSetEventBuilder(dataSet, registrator);

        EventPE event = builder.getEvent();
        assertEquals(EntityType.DATASET, event.getEntityType());
        assertEquals(EventType.DELETION, event.getEventType());
        assertEquals(Collections.singletonList("TEST-DATA-SET"), event.getIdentifiers());
        assertEquals("/TEST-DSS/TEST-SHARE-ID/TEST-LOCATION", event.getDescription());
        assertEquals(registrator, event.getRegistrator());
    }

    @Test
    public void testBuildForManyDataSets()
    {
        PersonPE registrator = new PersonPE();

        DataStorePE store = new DataStorePE();
        store.setCode("TEST-DSS");

        ExternalDataPE dataSet1 = new ExternalDataPE();
        dataSet1.setCode("TEST-DATA-SET-1");
        dataSet1.setDataStore(store);
        dataSet1.setShareId("TEST-SHARE-ID-1");
        dataSet1.setLocation("TEST-LOCATION-1");

        DataPE dataSet2 = new DataPE();
        dataSet2.setCode("TEST-DATA-SET-2");
        dataSet2.setDataStore(store);

        DeleteDataSetEventBuilder builder =
                new DeleteDataSetEventBuilder(Arrays.asList(dataSet1, dataSet2), registrator);

        EventPE event = builder.getEvent();
        assertEquals(EntityType.DATASET, event.getEntityType());
        assertEquals(EventType.DELETION, event.getEventType());
        assertEquals(Arrays.asList("TEST-DATA-SET-1", "TEST-DATA-SET-2"), event.getIdentifiers());
        assertEquals("/TEST-DSS/TEST-SHARE-ID-1/TEST-LOCATION-1, /TEST-DSS//",
                event.getDescription());
        assertEquals(registrator, event.getRegistrator());
    }
}
