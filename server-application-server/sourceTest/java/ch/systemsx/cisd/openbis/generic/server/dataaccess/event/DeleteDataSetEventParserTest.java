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
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;

/**
 * @author pkupczyk
 */
public class DeleteDataSetEventParserTest extends AssertJUnit
{

    @Test
    public void testParseOneDataSet()
    {
        EventPE event = new EventPE();
        event.setId(100L);
        event.setIdentifiers(Arrays.asList("TEST-DATA-SET-1"));
        event.setDescription("/TEST-DSS/TEST-SHARE/TEST-LOCATION");

        List<DeletedDataSet> dataSets = new DeleteDataSetEventParser(event).getDeletedDatasets();

        assertEquals(1, dataSets.size());
        assertEquals(100L, dataSets.get(0).getEventId());
        assertEquals("TEST-DATA-SET-1", dataSets.get(0).getCode());
        assertEquals("TEST-DSS", dataSets.get(0).getDatastoreCodeOrNull());
        assertEquals("TEST-SHARE", dataSets.get(0).getShareIdOrNull());
        assertEquals("TEST-LOCATION", dataSets.get(0).getLocationOrNull());
    }

    @Test
    public void testParseManyDataSets()
    {
        EventPE event = new EventPE();
        event.setId(100L);
        event.setIdentifiers(Arrays.asList("TEST-DATA-SET-1", "TEST-DATA-SET-2", "TEST-DATA-SET-3"));
        event.setDescription("/TEST-DSS/TEST-SHARE/TEST-LOCATION, /TEST-DSS-2//, ");

        List<DeletedDataSet> dataSets = new DeleteDataSetEventParser(event).getDeletedDatasets();

        assertEquals(3, dataSets.size());

        assertEquals(100L, dataSets.get(0).getEventId());
        assertEquals("TEST-DATA-SET-1", dataSets.get(0).getCode());
        assertEquals("TEST-DSS", dataSets.get(0).getDatastoreCodeOrNull());
        assertEquals("TEST-SHARE", dataSets.get(0).getShareIdOrNull());
        assertEquals("TEST-LOCATION", dataSets.get(0).getLocationOrNull());

        assertEquals(100L, dataSets.get(1).getEventId());
        assertEquals("TEST-DATA-SET-2", dataSets.get(1).getCode());
        assertEquals("TEST-DSS-2", dataSets.get(1).getDatastoreCodeOrNull());
        assertEquals(null, dataSets.get(1).getShareIdOrNull());
        assertEquals(null, dataSets.get(1).getLocationOrNull());

        assertEquals(100L, dataSets.get(2).getEventId());
        assertEquals("TEST-DATA-SET-3", dataSets.get(2).getCode());
        assertEquals(null, dataSets.get(2).getDatastoreCodeOrNull());
        assertEquals(null, dataSets.get(2).getShareIdOrNull());
        assertEquals(null, dataSets.get(2).getLocationOrNull());
    }

}
