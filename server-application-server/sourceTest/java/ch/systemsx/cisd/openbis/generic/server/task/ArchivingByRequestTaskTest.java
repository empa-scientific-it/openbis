/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.task;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.task.ArchivingByRequestTask.SizeHolder;

/**
 * @author Franz-Josef Elmer
 */
public class ArchivingByRequestTaskTest
{
    private static final class Item implements SizeHolder
    {
        private final long size;

        Item(long size)
        {
            this.size = size;
        }

        @Override
        public long getSize()
        {
            return size;
        }

        @Override
        public int hashCode()
        {
            return (int) size;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (obj instanceof Item == false)
            {
                return false;
            }
            Item that = (Item) obj;
            return that.size == this.size;
        }

        @Override
        public String toString()
        {
            return Long.toString(size);
        }
    }

    @Test(dataProvider = "test-cases")
    public void testGetChunks(GetChunksTestCase testCase)
    {
        // Given
        List<Item> items = new ArrayList<>();
        for (int item : testCase.items)
        {
            items.add(new Item(item));
        }

        // When
        List<List<Item>> chunks = ArchivingByRequestTask.getChunks(items, testCase.min, testCase.max);

        // Then
        assertEquals(chunks.toString(), testCase.expectedChunks);
    }

    @Test
    public void testGetChunksWithRandomExamples()
    {
        Random random = new Random(31);
        for (int i = 0; i < 1000; i++)
        {
            // Given
            long min = random.nextInt(100);
            long max = min + random.nextInt(100);
            List<Item> items = new ArrayList<>();
            for (int j = 0, n = random.nextInt(20); j < n; j++)
            {
                items.add(new Item(random.nextInt(120)));
            }
            String inputDescription = " {" + items + ", min=" + min + ", max=" + max + "}";

            // When
            List<List<Item>> chunks = ArchivingByRequestTask.getChunks(items, min, max);

            // Then
            System.out.println(inputDescription + ": " + chunks);
            for (List<Item> chunk : chunks)
            {
                long sum = 0;
                for (Item item : chunk)
                {
                    sum += item.getSize();
                    if (items.remove(item) == false)
                    {
                        fail(item + " not in chunk " + chunk + inputDescription);
                    }
                }
                if (sum < min)
                {
                    fail("chunk size " + sum + " too small" + inputDescription);
                }
                if (sum > max && chunk.size() > 1)
                {
                    fail("chunk size " + sum + " too large" + inputDescription);
                }
            }
        }
    }

    @DataProvider(name = "test-cases")
    public static Object[][] chunksExamples()
    {
        return new Object[][] {
                { testCase(20, 100).items(12, 100, 150, 10).chunks("[[150], [100], [12, 10]]") },
                { testCase(20, 100).items(12, 80, 50, 10).chunks("[[80], [50, 12, 10]]") },
                { testCase(90, 100).items(12, 80, 50, 10).chunks("[[80, 12]]") },
                { testCase(20, 100).items(8, 40, 50, 11).chunks("[[50, 40]]") }
        };
    }

    private static GetChunksTestCase testCase(long min, long max)
    {
        return new GetChunksTestCase().min(min).max(max);
    }

    private static final class GetChunksTestCase
    {
        private long min;

        private long max;

        private Integer[] items;

        private String expectedChunks;

        GetChunksTestCase min(long min)
        {
            this.min = min;
            return this;
        }

        GetChunksTestCase max(long max)
        {
            this.max = max;
            return this;
        }

        GetChunksTestCase items(Integer... items)
        {
            this.items = items;
            return this;
        }

        GetChunksTestCase chunks(String expectedChunks)
        {
            this.expectedChunks = expectedChunks;
            return this;
        }

        @Override
        public String toString()
        {
            return Arrays.asList(items) + ", min=" + min + ", max=" + max;
        }

    }

}
