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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author pkupczyk
 */
public class DeletedDataSetLocationTest extends AssertJUnit
{

    @Test
    public void testFormatNull()
    {
        assertEquals("", DeletedDataSetLocation.format(null));
    }

    @Test
    public void testFormatEmptyList()
    {
        assertEquals("", DeletedDataSetLocation.format(new ArrayList<DeletedDataSetLocation>()));
    }

    @Test
    public void testFormatEmptyLocation()
    {
        DeletedDataSetLocation location = new DeletedDataSetLocation();
        assertEquals("", DeletedDataSetLocation.format(Collections.singletonList(location)));
    }

    @Test
    public void testFormatLocationWithDssCodeOnly()
    {
        DeletedDataSetLocation location = new DeletedDataSetLocation();
        location.setDatastoreCode("TEST-DSS");
        assertEquals("/TEST-DSS//",
                DeletedDataSetLocation.format(Collections.singletonList(location)));
    }

    @Test
    public void testFormatLocationWithDssCodeAndShareId()
    {
        DeletedDataSetLocation location = new DeletedDataSetLocation();
        location.setDatastoreCode("TEST-DSS");
        location.setShareId("TEST-SHARE");
        assertEquals("/TEST-DSS/TEST-SHARE/",
                DeletedDataSetLocation.format(Collections.singletonList(location)));
    }

    @Test
    public void testFormatLocationWithDssCodeAndLocation()
    {
        DeletedDataSetLocation location = new DeletedDataSetLocation();
        location.setDatastoreCode("TEST-DSS");
        location.setLocation("TEST-LOCATION-DIR-1/TEST-LOCATION-DIR-2/");
        assertEquals("/TEST-DSS//TEST-LOCATION-DIR-1/TEST-LOCATION-DIR-2/",
                DeletedDataSetLocation.format(Collections.singletonList(location)));
    }

    @Test
    public void testFormatLocationWithDssCodeShareIdAndLocation()
    {
        DeletedDataSetLocation location = new DeletedDataSetLocation();
        location.setDatastoreCode("TEST-DSS");
        location.setShareId("TEST-SHARE");
        location.setLocation("TEST-LOCATION-DIR-1/TEST-LOCATION-DIR-2");

        assertEquals("/TEST-DSS/TEST-SHARE/TEST-LOCATION-DIR-1/TEST-LOCATION-DIR-2",
                DeletedDataSetLocation.format(Collections.singletonList(location)));
    }

    @Test
    public void testFormatManyLocations()
    {
        DeletedDataSetLocation location1 = new DeletedDataSetLocation();

        DeletedDataSetLocation location2 = new DeletedDataSetLocation();
        location2.setDatastoreCode("TEST-DSS-2");

        DeletedDataSetLocation location3 = new DeletedDataSetLocation();
        location3.setDatastoreCode("TEST-DSS-3");
        location3.setShareId("TEST-SHARE-3");
        location3.setLocation("TEST-LOCATION-DIR-3/TEST-LOCATION-DIR-33");

        List<DeletedDataSetLocation> locations = Arrays.asList(location1, location2, location3);

        assertEquals(
                ", /TEST-DSS-2//, /TEST-DSS-3/TEST-SHARE-3/TEST-LOCATION-DIR-3/TEST-LOCATION-DIR-33",
                DeletedDataSetLocation.format(locations));
    }

    @Test
    public void testParseNull()
    {
        assertEquals(Collections.emptyList(), DeletedDataSetLocation.parse(null));
    }

    @Test
    public void testParseEmptyString()
    {
        assertEquals(Collections.singletonList(new DeletedDataSetLocation()),
                DeletedDataSetLocation.parse(""));
    }

    @Test
    public void testParseLocationWithDssCodeOnly()
    {
        DeletedDataSetLocation location = new DeletedDataSetLocation();
        location.setDatastoreCode("TEST-DSS");

        assertEquals(Collections.singletonList(location),
                DeletedDataSetLocation.parse("/TEST-DSS//"));
        assertEquals(Collections.singletonList(location),
                DeletedDataSetLocation.parse("/TEST-DSS/"));
        assertEquals(Collections.singletonList(location), DeletedDataSetLocation.parse("/TEST-DSS"));
    }

    @Test
    public void testParseLocationWithDssCodeAndShareId()
    {
        DeletedDataSetLocation location = new DeletedDataSetLocation();
        location.setDatastoreCode("TEST-DSS");
        location.setShareId("TEST-SHARE");

        assertEquals(Collections.singletonList(location),
                DeletedDataSetLocation.parse("/TEST-DSS/TEST-SHARE/"));
        assertEquals(Collections.singletonList(location),
                DeletedDataSetLocation.parse("/TEST-DSS/TEST-SHARE"));
    }

    @Test
    public void testParseLocationWithDssCodeAndLocation()
    {
        DeletedDataSetLocation location = new DeletedDataSetLocation();
        location.setDatastoreCode("TEST-DSS");
        location.setLocation("TEST-LOCATION-DIR-1/TEST-LOCATION-DIR-2");

        assertEquals(Collections.singletonList(location),
                DeletedDataSetLocation.parse("/TEST-DSS//TEST-LOCATION-DIR-1/TEST-LOCATION-DIR-2"));
    }

    @Test
    public void testParseLocationWithDssCodeShareIdAndLocation()
    {
        DeletedDataSetLocation location = new DeletedDataSetLocation();
        location.setDatastoreCode("TEST-DSS");
        location.setShareId("TEST-SHARE");
        location.setLocation("TEST-LOCATION-DIR-1/TEST-LOCATION-DIR-2");

        assertEquals(Collections.singletonList(location),
                DeletedDataSetLocation
                        .parse("/TEST-DSS/TEST-SHARE/TEST-LOCATION-DIR-1/TEST-LOCATION-DIR-2"));
    }

    @Test
    public void testParseLegacyLocation()
    {
        DeletedDataSetLocation location = new DeletedDataSetLocation();
        location.setLocation("TEST-LOCATION-DIR-1/TEST-LOCATION-DIR-2");

        assertEquals(Collections.singletonList(location),
                DeletedDataSetLocation.parse("TEST-LOCATION-DIR-1/TEST-LOCATION-DIR-2"));
    }

    @Test
    public void testParseManyLocations()
    {
        DeletedDataSetLocation location1 = new DeletedDataSetLocation();
        location1.setDatastoreCode("TEST-DSS-1");
        location1.setShareId("TEST-SHARE-1");
        location1.setLocation("TEST-LOCATION-DIR-1/TEST-LOCATION-DIR-11");

        DeletedDataSetLocation location2 = new DeletedDataSetLocation();

        DeletedDataSetLocation location3 = new DeletedDataSetLocation();
        location3.setDatastoreCode("TEST-DSS-3");

        DeletedDataSetLocation location4 = new DeletedDataSetLocation();

        List<DeletedDataSetLocation> locations = new ArrayList<DeletedDataSetLocation>();
        locations.add(location1);
        locations.add(location2);
        locations.add(location3);
        locations.add(location4);

        assertEquals(
                locations,
                DeletedDataSetLocation
                        .parse("/TEST-DSS-1/TEST-SHARE-1/TEST-LOCATION-DIR-1/TEST-LOCATION-DIR-11, , /TEST-DSS-3, "));
    }

}
