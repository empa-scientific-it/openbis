/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.multiplexer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.concurrent.MessageChannelBuilder;

/**
 * @author pkupczyk
 */
public class ThreadPoolMultiplexerTest extends AssertJUnit
{

    private static final Integer BATCH_1_ID = 1;

    private static final Integer BATCH_2_ID = 2;

    private static final String BATCH_1_OBJECT_PREFIX = "BATCH_1";

    private static final String BATCH_2_OBJECT_PREFIX = "BATCH_2";

    private static final String BATCH_1_OBJECT_1 = BATCH_1_OBJECT_PREFIX + "_OBJECT_1";

    private static final String BATCH_1_OBJECT_2 = BATCH_1_OBJECT_PREFIX + "_OBJECT_2";

    private static final String BATCH_2_OBJECT_1 = BATCH_2_OBJECT_PREFIX + "_OBJECT_1";

    private static final Long BATCH_1_RESULT_1 = 11L;

    private static final Long BATCH_1_RESULT_2 = 12L;

    private static final Long BATCH_2_RESULT_1 = 21L;

    private Mockery context;

    private IBatchIdProvider<String, Integer> batchIdProvider;

    private IBatchHandler<String, Integer, String> batchHandler;

    private MessageChannel channel1;

    private MessageChannel channel2;

    private IMultiplexer multiplexer;

    @SuppressWarnings("unchecked")
    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();

        batchIdProvider = new IBatchIdProvider<String, Integer>()
            {
                @Override
                public Integer getBatchId(String object)
                {
                    if (object.startsWith(BATCH_1_OBJECT_PREFIX))
                    {
                        return BATCH_1_ID;
                    } else if (object.startsWith(BATCH_2_OBJECT_PREFIX))
                    {
                        return BATCH_2_ID;
                    } else
                    {
                        throw new IllegalArgumentException("Unknown object: " + object);
                    }
                }
            };
        batchHandler = context.mock(IBatchHandler.class);

        channel1 = new MessageChannelBuilder(1000).getChannel();
        channel2 = new MessageChannelBuilder(1000).getChannel();

        multiplexer = new ThreadPoolMultiplexer("multiplexer-test");
    }

    @AfterMethod
    public void afterMethod()
    {
        context.assertIsSatisfied();
    }

    private List<String> getAllObjects()
    {
        return Arrays.asList(BATCH_1_OBJECT_1, BATCH_1_OBJECT_2, BATCH_2_OBJECT_1);
    }

    private List<String> getBatch1Objects()
    {
        return Arrays.asList(BATCH_1_OBJECT_1, BATCH_1_OBJECT_2);
    }

    private List<String> getBatch2Objects()
    {
        return Arrays.asList(BATCH_2_OBJECT_1);
    }

    private Action getBatch1ResultsAction()
    {
        return new CustomAction("action")
            {

                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    return Arrays.asList(BATCH_1_RESULT_1, BATCH_1_RESULT_2);
                }

            };
    }

    private Action getBatch2ResultsAction()
    {
        return new CustomAction("action")
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    return Arrays.asList(BATCH_2_RESULT_1);
                }
            };
    }

    private Action getEmptyResultsAction()
    {
        return new CustomAction("action")
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    return new ArrayList<String>();
                }
            };
    }

    private Action getNullResultsAction()
    {
        return new CustomAction("action")
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    return null;
                }
            };
    }

    private Action getExceptionAction()
    {
        return new CustomAction("action")
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    throw new RuntimeException("MULTIPLEXER_EXCEPTION");
                }
            };
    }

    private Action getSleepAction(final int sleep)
    {
        return new CustomAction("action")
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    Thread.sleep(sleep);
                    return null;
                }
            };
    }

    private void assertBatch1Results(List<String> results)
    {
        assertEquals(Arrays.asList(BATCH_1_RESULT_1, BATCH_1_RESULT_2), results);
    }

    private void assertAllResults(List<String> results)
    {
        assertEquals(Arrays.asList(BATCH_1_RESULT_1, BATCH_1_RESULT_2,
                BATCH_2_RESULT_1), results);
    }

    @Test
    public void testWithNullReferenceLists()
    {
        List<String> results = multiplexer.process(null, batchIdProvider, batchHandler).getMergedBatchResultsWithDuplicates();
        assertTrue(results.isEmpty());
    }

    @Test
    public void testWithEmptyReferenceLists()
    {
        List<String> results =
                multiplexer.process(new ArrayList<String>(), batchIdProvider, batchHandler)
                        .getMergedBatchResultsWithDuplicates();
        assertTrue(results.isEmpty());
    }

    @Test
    public void testWithNullReferences()
    {
        List<String> results =
                test(Arrays.asList(null, BATCH_1_OBJECT_1, null, BATCH_1_OBJECT_2,
                        BATCH_2_OBJECT_1), getBatch1Objects(), getBatch2Objects(),
                        getBatch1ResultsAction(), getBatch2ResultsAction());
        assertAllResults(results);
    }

    @Test
    public void testWithNotEmptyResults()
    {
        List<String> results =
                test(getAllObjects(), getBatch1Objects(), getBatch2Objects(),
                        getBatch1ResultsAction(), getBatch2ResultsAction());
        assertAllResults(results);
    }

    @Test
    public void testWithEmptyResult()
    {
        List<String> results =
                test(getAllObjects(), getBatch1Objects(), getBatch2Objects(),
                        getBatch1ResultsAction(), getEmptyResultsAction());
        assertBatch1Results(results);
    }

    @Test
    public void testWithNullResult()
    {
        List<String> results =
                test(getAllObjects(), getBatch1Objects(), getBatch2Objects(),
                        getBatch1ResultsAction(), getNullResultsAction());
        assertBatch1Results(results);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "MULTIPLEXER_EXCEPTION")
    public void testWithException()
    {
        long startTime = System.currentTimeMillis();
        try
        {
            test(getAllObjects(), getBatch1Objects(), getBatch2Objects(),
                    getExceptionAction(), getSleepAction(500));
        } finally
        {
            long endTime = System.currentTimeMillis();
            assertTrue(endTime - startTime < 500);
        }
    }

    private List<String> test(final List<String> allObjects,
            final List<String> batch1Objects,
            final List<String> batch2Objects, final Action batch1Action,
            final Action batch2Action)
    {
        context.checking(new Expectations()
            {
                {
                    IBatch<String, Integer> batch1 = new Batch<String, Integer>(batch1Objects, BATCH_1_ID);
                    IBatch<String, Integer> batch2 = new Batch<String, Integer>(batch2Objects, BATCH_2_ID);

                    one(batchHandler).validateBatch(with(batch1));
                    one(batchHandler).validateBatch(with(batch2));

                    one(batchHandler).processBatch(with(batch1));
                    will(new Action()
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                channel1.send("DATA_STORE_1_BATCH_START");
                                channel2.assertNextMessage("DATA_STORE_2_BATCH_START");
                                return batch1Action.invoke(invocation);
                            }

                            @Override
                            public void describeTo(Description description)
                            {
                            }
                        });

                    one(batchHandler).processBatch(with(batch2));
                    will(new Action()
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                channel2.send("DATA_STORE_2_BATCH_START");
                                channel1.assertNextMessage("DATA_STORE_1_BATCH_START");
                                return batch2Action.invoke(invocation);
                            }

                            @Override
                            public void describeTo(Description description)
                            {
                            }
                        });
                }
            });

        return multiplexer.process(allObjects, batchIdProvider, batchHandler).getMergedBatchResultsWithDuplicates();
    }

}
