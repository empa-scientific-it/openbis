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

package ch.systemsx.cisd.common.io;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.ArrayBlockingQueue;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.io.QueuePersister.LegacyQueuePersister;

/**
 * @author Franz-Josef Elmer
 */
public class LegacyQueuePersisterTest extends AssertJUnit
{
    private static final File TMP = new File("targets/unit-test-wd/" + LegacyQueuePersisterTest.class.getSimpleName());

    private static final File QUEUE_FILE = new File(TMP, "LegacyQueuePersisterTestQueue");

    private ArrayBlockingQueue<String> queue;

    private LegacyQueuePersister<String> persister;

    @BeforeClass
    public void setUp()
    {
        FileUtilities.deleteRecursively(TMP);
        TMP.mkdirs();
        assertEquals(false, QUEUE_FILE.exists());
        queue = new ArrayBlockingQueue<String>(10);
        persister = new LegacyQueuePersister<String>(queue, QUEUE_FILE);
    }

    @AfterClass
    public void tearDown()
    {
        persister.close();
    }

    @Test
    public void testPersist()
    {
        String element =
                "a string with more characters than "
                        + "LegacyQueuePersister.DEFAULT_INITIAL_RECORD_SIZE";
        assertEquals(true, element.length() > LegacyQueuePersister.DEFAULT_INITIAL_RECORD_SIZE);
        queue.add(element);

        persister.persist();

        persister.close();
        queue.clear();
        persister = new LegacyQueuePersister<String>(queue, QUEUE_FILE);

        assertEquals(1, queue.size());
        assertEquals(element, queue.peek());
    }

    @Test
    public void testAddToTail()
    {
        persister.addToTail("hello world");
    }

    @Test
    public void testZeroRecordSize()
    {
        // First delete the queue file created by setUp so we can create one where the initial
        // record size is 0.
        assertEquals("Couldn't delete " + TMP, true, FileUtilities.deleteRecursively(TMP));
        TMP.mkdirs();
        queue = new ArrayBlockingQueue<String>(10);
        persister = new LegacyQueuePersister<String>(queue, QUEUE_FILE, 0, false);

        queue.add("");
        queue.add("foo");
        persister.persist();
        persister.close();
        queue.clear();
        persister = new LegacyQueuePersister<String>(queue, QUEUE_FILE);

        assertEquals(2, queue.size());
        assertEquals("", queue.poll());
        assertEquals("foo", queue.poll());
    }

    @Test
    public void testQueueFileSizeLessThanHeaderSize() throws Exception
    {
        assertEquals("Couldn't delete " + TMP, true, FileUtilities.deleteRecursively(TMP));
        TMP.mkdirs();
        queue = new ArrayBlockingQueue<String>(10);

        // write a data shorter than header size
        RandomAccessFile tooShortRaf = new RandomAccessFile(QUEUE_FILE, "rw");
        tooShortRaf.writeInt(1);
        tooShortRaf.close();

        persister = new LegacyQueuePersister<String>(queue, QUEUE_FILE);

        queue.add("test");
        persister.persist();
        persister.close();
        queue.clear();
        persister = new LegacyQueuePersister<String>(queue, QUEUE_FILE);

        assertEquals(1, queue.size());
        assertEquals("test", queue.poll());
    }

}
