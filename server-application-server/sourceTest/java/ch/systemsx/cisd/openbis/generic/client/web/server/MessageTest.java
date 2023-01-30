/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.client.web.server.Message;

/**
 * Test cases for the {@link Message}.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = Message.class)
public final class MessageTest
{

    @Test
    public final void testConstructor()
    {
        boolean fail = true;
        try
        {
            new Message(null, null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    @Test
    public final void testToXml()
    {
        Message message = Message.createErrorMessage("my message");
        assertEquals("<message type=\"error\">my message</message>", message.toXml());
        message = Message.createWarnMessage("<my cdata message>");
        assertEquals("<message type=\"warning\"><![CDATA[<my cdata message>]]></message>", message
                .toXml());
    }
}
