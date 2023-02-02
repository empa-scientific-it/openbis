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

package ch.systemsx.cisd.common.mail;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.activation.DataHandler;

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * Test cases for corresponding {@link MailClient} class.
 * 
 * @author Franz-Josef Elmer
 */
public final class MailClientTest extends AbstractFileSystemTestCase
{
    @Test
    public final void test()
    {
        String path = workingDirectory.getPath() + "/emails";
        File emailFolder = new File(path);
        assert emailFolder.exists() == false;

        MailClient mailClient = new MailClient("sender", "file://" + path);
        mailClient.sendMessage("some message", "Hello world\nHow are you today?", "user@reply.com",
                null, "a@b.c", "d@e.f");

        assert emailFolder.exists();
        assert emailFolder.isDirectory();
        File[] files = emailFolder.listFiles();
        assertEquals(1, files.length);
        assertTrue(files[0].getName().matches("email.*"));
        List<String> lines = FileUtilities.loadToStringList(files[0]);
        assertEquals(8, lines.size());
        assertTrue(lines.get(0).startsWith("Date: "));
        assertEquals("From: sender", lines.get(1));
        assertEquals("Reply-To: user@reply.com", lines.get(2));
        assertEquals("To: a@b.c, d@e.f", lines.get(3));
        assertEquals("Subject: some message", lines.get(4));
        assertEquals("Content:", lines.get(5));
        assertEquals("Hello world", lines.get(6));
        assertEquals("How are you today?", lines.get(7));
        // second mail
        mailClient.sendMessage("Greetings", "Hello world!", null, null);
        files = emailFolder.listFiles();
        Arrays.sort(files);
        assertEquals(2, files.length);
        assertTrue(files[0].getName().matches("email.*"));
        assertTrue(files[1].getName().matches("email.*"));
        lines = FileUtilities.loadToStringList(files[1]);
        assertEquals(5, lines.size());
        assertTrue(lines.get(0).startsWith("Date: "));
        assertEquals("From: sender", lines.get(1));
        assertEquals("Subject: Greetings", lines.get(2));
        assertEquals("Content:", lines.get(3));
        assertEquals("Hello world!", lines.get(4));

        // third mail - 'from' overwritten
        mailClient.sendMessage("Greetings", "Hello world!", null, new From("user@from.com"));
        files = emailFolder.listFiles();
        Arrays.sort(files);
        assertEquals(3, files.length);
        assertTrue(files[0].getName().matches("email.*"));
        assertTrue(files[1].getName().matches("email.*"));
        assertTrue(files[2].getName().matches("email.*"));
        lines = FileUtilities.loadToStringList(files[2]);
        assertEquals(5, lines.size());
        assertTrue(lines.get(0).startsWith("Date: "));
        assertEquals("From: user@from.com", lines.get(1));
        assertEquals("Subject: Greetings", lines.get(2));
        assertEquals("Content:", lines.get(3));
        assertEquals("Hello world!", lines.get(4));
    }

    @Test
    public final void testEscapeReplyTo()
    {
        String path = workingDirectory.getPath() + "/emails";
        File emailFolder = new File(path);
        assert emailFolder.exists() == false;

        MailClient mailClient = new MailClient("sender", "file://" + path);
        // first mail
        mailClient.sendEmailMessage("some message", "Hello world\nHow are you today?",
                new EMailAddress("user@reply.com", "User, Special"), null,
                new EMailAddress("a@b.c"), new EMailAddress("d@e.f"));
        File[] files = emailFolder.listFiles();
        assertEquals(1, files.length);
        assertTrue(files[0].getName().matches("email.*"));
        List<String> lines = FileUtilities.loadToStringList(files[0]);
        assertEquals("Reply-To: \"User, Special\" <user@reply.com>", lines.get(2));

        // second mail
        mailClient.sendEmailMessage("some message", "Hello world\nHow are you today?",
                new EMailAddress("user@reply.com", "User;\" Special"), null, new EMailAddress(
                        "a@b.c"),
                new EMailAddress("d@e.f"));
        files = emailFolder.listFiles();
        Arrays.sort(files);
        lines = FileUtilities.loadToStringList(files[1]);
        assertEquals("Reply-To: \"User;\\\" Special\" <user@reply.com>", lines.get(2));
    }

    @Test
    public final void testAttachments()
    {
        String path = workingDirectory.getPath() + "/emails";
        File emailFolder = new File(path);
        assert emailFolder.exists() == false;

        MailClient mailClient = new MailClient("sender", "file://" + path);

        DataHandler attachment =
                new DataHandler("name.first = First Name\nname.last = Last Name",
                        "application/octet-stream");
        mailClient.sendMessageWithAttachment("some message", "Hello world\nHow are you today?",
                "file.properties", attachment, "user@reply.com", null, "a@b.c", "d@e.f");

        assert emailFolder.exists();
        assert emailFolder.isDirectory();
        File[] files = emailFolder.listFiles();
        assertEquals(1, files.length);
        assertTrue(files[0].getName().matches("email.*"));
        String fileContent = FileUtilities.loadToString(files[0]);

        // Split the file into lines and check one line at a time
        String[] lines = fileContent.split("\n+");
        assertEquals(lines.length, 14);
        assertTrue(lines[0].startsWith("Date:"));
        assertEquals(lines[1], "From: sender");
        assertEquals(lines[2], "Reply-To: user@reply.com");
        assertEquals(lines[3], "To: a@b.c, d@e.f");
        assertEquals(lines[4], "Subject: some message");
        assertEquals(lines[5], "Content:");
        assertTrue(lines[6].startsWith("------=_Part_0"));
        assertEquals(lines[7], "Hello world");
        assertEquals(lines[8], "How are you today?");

        assertTrue(lines[9].startsWith("------=_Part_0"));
        assertEquals(lines[10], "Content-Disposition: attachment; filename=file.properties");
        assertEquals(lines[11], "name.first = First Name");
        assertEquals(lines[12], "name.last = Last Name");
        assertTrue(lines[13].startsWith("------=_Part_0"));
    }
}
