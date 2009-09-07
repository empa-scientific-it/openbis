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
        assertEquals("email", files[0].getName());
        assertEquals(
                "Subj: some message\n" + "From: sender\n" + "To:   a@b.c, d@e.f\n"
                        + "Reply-To: user@reply.com\n" + "Content:\n"
                        + "Hello world\nHow are you today?\n", FileUtilities.loadToString(files[0]));

        // second mail
        mailClient.sendMessage("Greetings", "Hello world!", null, null);
        files = emailFolder.listFiles();
        Arrays.sort(files);
        assertEquals(2, files.length);
        assertEquals("email", files[0].getName());
        assertEquals("email1", files[1].getName());
        assertEquals("Subj: Greetings\n" + "From: sender\n" + "To:   \n" + "Reply-To: sender\n"
                + "Content:\n" + "Hello world!\n", FileUtilities.loadToString(files[1]));

        // third mail - 'from' overwritten
        mailClient.sendMessage("Greetings", "Hello world!", null, new From("user@from.com"));
        files = emailFolder.listFiles();
        Arrays.sort(files);
        assertEquals(3, files.length);
        assertEquals("email", files[0].getName());
        assertEquals("email1", files[1].getName());
        assertEquals("email2", files[2].getName());
        assertEquals("Subj: Greetings\n" + "From: user@from.com\n" + "To:   \n"
                + "Reply-To: user@from.com\n" + "Content:\n" + "Hello world!\n", FileUtilities
                .loadToString(files[2]));

    }

}
