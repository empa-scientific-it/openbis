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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.Collections;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class LinkModelTest extends AssertJUnit
{

    private static final String SESSION_ID = "SESSION_ID";

    @Test
    public void testSessionId()
    {
        LinkModel linkModel = new LinkModel();
        assertNull(linkModel.trySessionId());

        linkModel.setSessionId(SESSION_ID);
        assertEquals(SESSION_ID, linkModel.trySessionId());

        linkModel.setSessionId("NewSessionId");
        assertEquals("NewSessionId", linkModel.trySessionId());
    }

    @Test
    public void testSessionIdInParameters()
    {
        LinkModel linkModel = new LinkModel();
        assertNull(linkModel.trySessionId());

        LinkModel.LinkParameter parameter =
                new LinkModel.LinkParameter(LinkModel.SESSION_ID_PARAMETER_NAME, SESSION_ID);
        List<LinkModel.LinkParameter> params = Collections.singletonList(parameter);
        linkModel.setParameters(params);

        assertEquals(SESSION_ID, linkModel.trySessionId());

        linkModel.setSessionId("NewSessionId");
        assertEquals("NewSessionId", linkModel.trySessionId());
    }
}
