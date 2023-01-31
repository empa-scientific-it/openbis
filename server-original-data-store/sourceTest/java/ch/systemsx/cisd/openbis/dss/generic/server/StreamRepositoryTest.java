/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.ByteArrayInputStream;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.openbis.dss.generic.server.StreamRepository.IUniqueIdGenerator;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = StreamRepository.class)
public class StreamRepositoryTest extends AssertJUnit
{
    private IUniqueIdGenerator idGenerator;

    private ITimeProvider timeProvider;

    @BeforeMethod
    public void setUp()
    {
        idGenerator = new IUniqueIdGenerator()
            {
                private int id;

                @Override
                public String createUniqueID()
                {
                    return Integer.toString(id++);
                }
            };
        timeProvider = new ITimeProvider()
            {
                private long time;

                @Override
                public long getTimeInMilliseconds()
                {
                    long result = time;
                    time += 500;
                    return result;
                }
            };
    }

    @Test
    public void testAddingAndRetrievingTwoStreams()
    {
        StreamRepository repository = new StreamRepository(2, 10, idGenerator, timeProvider);
        ByteArrayInputStream stream1 = new ByteArrayInputStream("s1".getBytes());
        String id1 = repository.addStream(stream1, "f1.txt", 0);
        ByteArrayInputStream stream2 = new ByteArrayInputStream("s2".getBytes());
        String id2 = repository.addStream(stream2, "f2.txt", 0);

        assertEquals("0", id1);
        assertEquals("1", id2);

        InputStreamWithPath actualStream1 = repository.getStream("0");
        assertSame(stream1, actualStream1.getInputStream());
        assertSame("f1.txt", actualStream1.getPath());
        InputStreamWithPath actualStream2 = repository.getStream("1");
        assertSame(stream2, actualStream2.getInputStream());
        assertSame("f2.txt", actualStream2.getPath());
    }

    @Test
    public void testThatAStreamCanBeRetrievedOnlyOnce()
    {
        StreamRepository repository = new StreamRepository(2, 10, idGenerator, timeProvider);
        ByteArrayInputStream stream1 = new ByteArrayInputStream("s1".getBytes());
        String id1 = repository.addStream(stream1, "f1.txt", 0);

        assertEquals("0", id1);

        assertSame(stream1, repository.getStream("0").getInputStream());
        try
        {
            repository.getStream("0");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Stream 0 is no longer available.", ex.getMessage());
        }
    }

    @Test
    public void testAddingAndRetrievingTwoStreamsButSecondStreamNoLongerExists()
    {
        StreamRepository repository = new StreamRepository(2, 10, idGenerator, timeProvider);
        ByteArrayInputStream stream1 = new ByteArrayInputStream("s1".getBytes());
        String id1 = repository.addStream(stream1, "f1.txt", 0);
        ByteArrayInputStream stream2 = new ByteArrayInputStream("s2".getBytes());
        String id2 = repository.addStream(stream2, "f2.txt", 0);

        assertEquals("0", id1);
        assertEquals("1", id2);

        assertSame(stream1, repository.getStream("0").getInputStream());
        timeProvider.getTimeInMilliseconds(); // wait until stream2 will be removed
        timeProvider.getTimeInMilliseconds();
        timeProvider.getTimeInMilliseconds();
        try
        {
            repository.getStream("1");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Stream 1 is no longer available.", ex.getMessage());
        }
    }

    @Test
    public void testValidityDuration()
    {
        StreamRepository repository = new StreamRepository(2, 10, idGenerator, timeProvider);
        ByteArrayInputStream stream0 = new ByteArrayInputStream("s1".getBytes());
        repository.addStream(stream0, "f1.txt", 5);
        ByteArrayInputStream stream1 = new ByteArrayInputStream("s2".getBytes());
        repository.addStream(stream1, "f2.txt", 3);

        // Advance to a time when stream 1 is still available but not stream2
        timeProvider.getTimeInMilliseconds();
        timeProvider.getTimeInMilliseconds();
        timeProvider.getTimeInMilliseconds();
        timeProvider.getTimeInMilliseconds();
        timeProvider.getTimeInMilliseconds();
        assertSame(stream0, repository.getStream("0").getInputStream());
        try
        {
            repository.getStream("1");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Stream 1 is no longer available.", ex.getMessage());
        }
    }

    @Test
    public void testMaxValidityDuration()
    {
        StreamRepository repository = new StreamRepository(2, 3, idGenerator, timeProvider);
        ByteArrayInputStream stream0 = new ByteArrayInputStream("s1".getBytes());
        repository.addStream(stream0, "f1.txt", 10);

        // Advance to the requested time, but beyond the stream repository's max allowed
        timeProvider.getTimeInMilliseconds();
        timeProvider.getTimeInMilliseconds();
        timeProvider.getTimeInMilliseconds();
        timeProvider.getTimeInMilliseconds();
        timeProvider.getTimeInMilliseconds();
        timeProvider.getTimeInMilliseconds();
        try
        {
            repository.getStream("0");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Stream 0 is no longer available.", ex.getMessage());
        }
    }
}
