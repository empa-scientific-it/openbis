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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * Test cases for the {@link PersistentExtendedBlockingQueueDecorator}.
 * 
 * @author Bernd Rinn
 */
public class PersistentExtendedBlockingQueueDecoratorTest
{

    private static final File workingDirectory = new File("targets" + File.separator
            + "unit-test-wd/" + PersistentExtendedBlockingQueueDecoratorTest.class.getName());

    private static final File queueFile = new File(workingDirectory, "persistentQueue.dat");

    private PersistentExtendedBlockingQueueDecorator<String> createQueue()
    {
        return PersistentExtendedBlockingQueueFactory.createSmartQueue(queueFile, false);
    }

    private List<String> asList(Queue<String> queue)
    {
        return Arrays.asList(queue.toArray(new String[queue.size()]));
    }

    private void createQueueFile(List<String> entries)
    {
        queueFile.delete();
        assertFalse(queueFile.exists());
        final PersistentExtendedBlockingQueueDecorator<String> q = createQueue();
        q.addAll(entries);
        q.close();
        assertTrue(queueFile.exists());
    }

    @BeforeTest
    public void setUp()
    {
        workingDirectory.mkdirs();
        queueFile.deleteOnExit();
    }

    @BeforeMethod
    public void deleteQueueFile()
    {
        FileUtilities.deleteRecursively(workingDirectory);
        workingDirectory.mkdirs();
    }

    @Test
    public void testCreateEmpty()
    {
        final PersistentExtendedBlockingQueueDecorator<String> persistentQueue = createQueue();
        assertTrue(persistentQueue.isEmpty());
    }

    @Test
    public void testCreateWithEntries()
    {
        final List<String> itemsWritten = Arrays.asList("a", "b", "c'");
        createQueueFile(itemsWritten);
        final PersistentExtendedBlockingQueueDecorator<String> persistentQueue = createQueue();
        assertEquals(itemsWritten.size(), persistentQueue.size());
        assertEquals(itemsWritten, asList(persistentQueue));
    }

    @Test
    public void testAdd()
    {
        final List<String> items = Arrays.asList("one", "two");
        PersistentExtendedBlockingQueueDecorator<String> persistentQueue = createQueue();
        for (String item : items)
        {
            persistentQueue.add(item);
        }
        assertEquals(items, asList(createQueue()));
    }

    @Test
    public void testAddClose()
    {
        final List<String> items = Arrays.asList("one", "two");
        PersistentExtendedBlockingQueueDecorator<String> persistentQueue = createQueue();
        for (String item : items)
        {
            persistentQueue.add(item);
        }
        persistentQueue.close();
        persistentQueue.add("three");

        assertEquals(items.size() + 1, asList(createQueue()).size());
    }

    @Test
    public void testAddLongItem()
    {
        final List<String> items = Arrays.asList("one", "two", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        PersistentExtendedBlockingQueueDecorator<String> persistentQueue = createQueue();
        for (String item : items)
        {
            persistentQueue.add(item);
        }
        assertEquals(items, asList(createQueue()));
    }

    @Test
    public void testAddAllLongItems()
    {
        final List<String> items = Arrays.asList("one", "two");
        final List<String> longItems =
                Arrays.asList("aaaaaaaaaaaaaaaaaaaa", "bbbbbbbbbbbbbbbbbbbbbbbb");
        PersistentExtendedBlockingQueueDecorator<String> persistentQueue = createQueue();
        final List<String> allItems = new ArrayList<String>();
        allItems.addAll(items);
        allItems.addAll(longItems);
        persistentQueue.addAll(items);
        persistentQueue.addAll(longItems);

        assertEquals(allItems, asList(createQueue()));
    }

    @Test
    public void testPut() throws InterruptedException
    {
        final PersistentExtendedBlockingQueueDecorator<String> persistentQueue = createQueue();
        persistentQueue.put("one");
        persistentQueue.put("two");
        assertEquals(asList(persistentQueue), asList(createQueue()));
    }

    @Test
    public void testRemove()
    {
        final List<String> itemsWritten = Arrays.asList("1", "2", "3", "4");
        createQueueFile(itemsWritten);
        final PersistentExtendedBlockingQueueDecorator<String> persistentQueue = createQueue();
        persistentQueue.remove("3");
        assertEquals(asList(persistentQueue), asList(createQueue()));
    }

    @Test
    public void testRemoveAll()
    {
        createQueueFile(Arrays.asList("1", "2", "3", "4"));
        final PersistentExtendedBlockingQueueDecorator<String> persistentQueue = createQueue();
        persistentQueue.removeAll(Arrays.asList("4", "1", "17"));
        assertEquals(asList(persistentQueue), asList(createQueue()));
    }

    @Test
    public void testRetainAll()
    {
        createQueueFile(Arrays.asList("1", "2", "3", "4"));
        final PersistentExtendedBlockingQueueDecorator<String> persistentQueue = createQueue();
        persistentQueue.retainAll(Arrays.asList("4", "1", "17"));
        assertEquals(asList(persistentQueue), asList(createQueue()));
    }

    @Test
    public void testRemoveUntilEmpty()
    {
        createQueueFile(Arrays.asList("1", "2", "3", "4"));
        PersistentExtendedBlockingQueueDecorator<String> persistentQueue = createQueue();
        while (persistentQueue.isEmpty() == false)
        {
            persistentQueue.remove();
            final List<String> snapshot = asList(persistentQueue);
            persistentQueue = createQueue();
            assertEquals(snapshot, asList(persistentQueue));
        }
    }

    @Test
    public void testTakeUntilEmpty() throws InterruptedException
    {
        createQueueFile(Arrays.asList("1", "2", "3", "4"));
        while (true)
        {
            PersistentExtendedBlockingQueueDecorator<String> persistentQueue = createQueue();
            if (persistentQueue.isEmpty())
            {
                break;
            }
            persistentQueue.take();
            final List<String> snapshot = asList(persistentQueue);
            persistentQueue = createQueue();
            assertEquals(snapshot, asList(persistentQueue));
        }
    }

    @Test
    public void testClear()
    {
        final List<String> itemsWritten = Arrays.asList("1", "2", "3", "4");
        createQueueFile(itemsWritten);
        final PersistentExtendedBlockingQueueDecorator<String> persistentQueue = createQueue();
        persistentQueue.clear();
        assertEquals(asList(persistentQueue), asList(createQueue()));
    }

    @Test
    public void testIterator()
    {
        final List<String> itemsWritten = Arrays.asList("1", "2", "3", "4");
        createQueueFile(itemsWritten);
        PersistentExtendedBlockingQueueDecorator<String> persistentQueue = createQueue();
        final Iterator<String> iterItemsWritten = itemsWritten.iterator();
        final Iterator<String> iter = persistentQueue.iterator();
        while (iter.hasNext())
        {
            assertEquals(iterItemsWritten.next(), iter.next());
            iter.remove();
        }
        assertTrue(persistentQueue.isEmpty());
        assertTrue(createQueue().isEmpty());
    }

    @Test()
    public void testAddLotsOfItems()
    {
        PersistentExtendedBlockingQueueDecorator<String> persistentQueue = createQueue();

        final List<String> items = new ArrayList<String>();
        for (int i = 0; i < 128; ++i)
        {
            items.add(createItemOfLength(i));
            persistentQueue.add(items.get(i));
        }

        assertEquals(items, asList(createQueue()));
    }

    @Test
    public void testThreadSafetiness() throws Exception
    {
        final PersistentExtendedBlockingQueueDecorator<Integer> queue =
                PersistentExtendedBlockingQueueFactory.<Integer> createSmartPersist(queueFile);
        final MessageChannel messageChannel = new MessageChannel(30000);
        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        while (true)
                        {
                            Integer number = queue.peekWait();
                            if (number < 0)
                            {
                                System.out.println("BREAK");
                                break;
                            }
                            beBusy(number);
                            queue.take();
                        }
                        messageChannel.send("finished");
                    } catch (InterruptedException ex)
                    {
                        ex.printStackTrace();
                        System.out.println("Queue size: " + queue.size());
                    }
                }
            }).start();
        Random random = new Random(37);
        for (int i = 0; i < 1000; i++)
        {
            try
            {
                queue.add(random.nextInt(100));
            } catch (Exception ex)
            {
                throw new RuntimeException("Crash for queue size " + queue.size(), ex);
            }

            beBusy(0);
        }
        queue.add(-1);
        messageChannel.assertNextMessage("finished");

        queueFile.delete();
    }

    private String createItemOfLength(int itemLength)
    {
        String item = "";
        for (int j = 0; j < itemLength; ++j)
        {
            item += "1";
        }
        return item;
    }

    private double beBusy(Integer number)
    {
        double sum = 0;
        for (int i = 0; i < (10 + number) * 100; i++)
        {
            sum += Math.sin(i);
        }
        return sum;
    }

}
