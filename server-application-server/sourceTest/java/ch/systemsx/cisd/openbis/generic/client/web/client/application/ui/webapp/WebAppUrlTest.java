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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.webapp;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * @author pkupczyk
 */
public class WebAppUrlTest
{

    @Test
    public void testApplicationUrlWithoutParameters()
    {
        WebAppUrl url =
                new WebAppUrl("http:", "localhost:8888", "/openbis/", "webapp1", "mysessionid");
        assertEquals(
                "http://localhost:8888/openbis/webapp/webapp1/?webapp-code=webapp1&session-id=mysessionid",
                url.toString());
    }

    @Test
    public void testFileUrlWithoutParameters()
    {
        WebAppUrl url =
                new WebAppUrl("http:", "localhost:8888", "/openbis/index.html", "webapp1",
                        "mysessionid");
        assertEquals(
                "http://localhost:8888/openbis/webapp/webapp1/?webapp-code=webapp1&session-id=mysessionid",
                url.toString());
    }

    @Test
    public void testUrlWithNullParameters()
    {
        WebAppUrl url =
                new WebAppUrl("http:", "localhost:8888", "/openbis/", "webapp1", "mysessionid");
        url.addEntityKind(null);
        url.addEntityType(null);
        url.addEntityIdentifier(null);
        url.addEntityPermId(null);
        assertEquals(
                "http://localhost:8888/openbis/webapp/webapp1/?webapp-code=webapp1&session-id=mysessionid",
                url.toString());
    }

    @Test
    public void testUrlWithNotNullParameters()
    {
        WebAppUrl url =
                new WebAppUrl("http:", "localhost:8888", "/openbis/", "webapp1", "mysessionid");
        url.addEntityKind(EntityKind.EXPERIMENT);
        url.addEntityType(new BasicEntityType("TEST_EXPERIMENT_TYPE"));
        url.addEntityIdentifier("TEST_EXPERIMENT_IDENTIFIER");
        url.addEntityPermId("TEST_EXPERIMENT_PERM_ID");
        assertEquals(
                "http://localhost:8888/openbis/webapp/webapp1/?webapp-code=webapp1&session-id=mysessionid&entity-kind=EXPERIMENT"
                        + "&entity-type=TEST_EXPERIMENT_TYPE&entity-identifier=TEST_EXPERIMENT_IDENTIFIER"
                        + "&entity-perm-id=TEST_EXPERIMENT_PERM_ID", url.toString());
    }

    @Test
    public void testUrlWithParametersThatContainReservedCharacters()
    {
        WebAppUrl url =
                new WebAppUrl("http:", "localhost:8888", "/openbis/", "(webapp1)", "[mysessionid]");
        url.addEntityKind(EntityKind.EXPERIMENT);
        url.addEntityType(new BasicEntityType("TEST EXPERIMENT TYPE"));
        url.addEntityIdentifier("TEST/EXPERIMENT/IDENTIFIER");
        url.addEntityPermId("TEST&EXPERIMENT&PERM&ID");
        assertEquals(
                "http://localhost:8888/openbis/webapp/%28webapp1%29/?webapp-code=%28webapp1%29&session-id=%5Bmysessionid%5D&entity-kind=EXPERIMENT"
                        + "&entity-type=TEST+EXPERIMENT+TYPE&entity-identifier=TEST%2FEXPERIMENT%2FIDENTIFIER"
                        + "&entity-perm-id=TEST%26EXPERIMENT%26PERM%26ID", url.toString());
    }
}
