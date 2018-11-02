/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.ethz.sis.filetransfer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.filetransfer.DefaultChunkDeserializer;
import ch.ethz.sis.filetransfer.DefaultChunkSerializer;
import ch.ethz.sis.filetransfer.DefaultRetryProvider;
import ch.ethz.sis.filetransfer.DownloadClient;
import ch.ethz.sis.filetransfer.DownloadClientConfig;
import ch.ethz.sis.filetransfer.DownloadClientDownload;
import ch.ethz.sis.filetransfer.DownloadException;
import ch.ethz.sis.filetransfer.DownloadListenerAdapter;
import ch.ethz.sis.filetransfer.DownloadPreferences;
import ch.ethz.sis.filetransfer.DownloadServer;
import ch.ethz.sis.filetransfer.DownloadServerConfig;
import ch.ethz.sis.filetransfer.IChunkDeserializer;
import ch.ethz.sis.filetransfer.IChunkSerializer;
import ch.ethz.sis.filetransfer.IDeserializerProvider;
import ch.ethz.sis.filetransfer.IDownloadItemId;
import ch.ethz.sis.filetransfer.ISerializerProvider;
import ch.ethz.sis.filetransfer.IUserSessionId;
import ch.ethz.sis.filetransfer.LogLevel;
import ch.ethz.sis.filetransfer.TestAssertions.IAssertion;

/**
 * @author pkupczyk
 */
public class DownloadClientTest
{

    private static final String PROVIDER_OPERATION_NAMES = "provider-retriable-operation-names";

    private static final Path TEST_STORE_PATH = Paths.get("targets/test-store");

    private static final TestDownloadItemId TEST_ITEM_1_ID = new TestDownloadItemId("sourceTest/test-files/testFile1.txt");

    private static final TestDownloadItemId TEST_ITEM_2_ID = new TestDownloadItemId("sourceTest/test-files/testFile2.txt");

    private TestLogger logger;

    @BeforeClass
    private void initClass() throws Exception
    {
        if (TEST_STORE_PATH.toFile().exists())
        {
            FileUtils.deleteDirectory(TEST_STORE_PATH.toFile());
        }
        TEST_STORE_PATH.toFile().mkdirs();
    }

    @BeforeMethod
    private void initMethod(Method method, Object[] params)
    {
        logger = new TestLogger();
        logger.log(getClass(), LogLevel.INFO, "");
        logger.log(getClass(), LogLevel.INFO, ">>>>>>>> " + method.getName() + " (" + Arrays.toString(params) + ") <<<<<<<<");
        logger.log(getClass(), LogLevel.INFO, "");
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Config cannot be null")
    public void testConstructWithoutConfig()
    {
        new DownloadClient(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Server cannot be null")
    public void testConstructWithoutServer()
    {
        DownloadClientConfig clientConfig = new TestDownloadClientConfig();
        clientConfig.setServer(null);

        new DownloadClient(clientConfig);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Store cannot be null")
    public void testConstructWithoutStore()
    {
        DownloadClientConfig clientConfig = new TestDownloadClientConfig();
        clientConfig.setStore(null);

        new DownloadClient(clientConfig);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "User session id cannot be null")
    public void testCreateDownloadWithoutUserSessionId()
    {
        DownloadClient client = new DownloadClient(new TestDownloadClientConfig());

        client.createDownload(null);
    }

    @Test
    public void testCreateDownloadWithUserSessionId()
    {
        DownloadClient client = new DownloadClient(new TestDownloadClientConfig());

        IUserSessionId userSession = new TestUserSession();
        IDownloadItemId item1 = new TestDownloadItemId("testPath1");
        IDownloadItemId item2 = new TestDownloadItemId("testPath2");
        IDownloadItemId item3 = new TestDownloadItemId("testPath3");

        DownloadClientDownload download = client.createDownload(userSession);
        download.addItem(item1);
        download.addItems(Arrays.asList(item2, item3));

        assertEquals(download.getUserSession(), userSession);
        assertEquals(download.getItems(), Arrays.asList(item1, item2, item3));
    }

    @Test(invocationCount = 20)
    public void testDownloadMultipleItems() throws DownloadException
    {
        TestAssertions assertion = new TestAssertions();
        DownloadClient client = new DownloadClient(new TestDownloadClientConfig());

        DownloadClientDownload download = client.createDownload(new TestUserSession());
        download.addItem(TEST_ITEM_1_ID);
        download.addItem(TEST_ITEM_2_ID);
        download.addListener(new DownloadListenerAdapter()
            {
                @Override
                public void onDownloadStarted()
                {
                    logger.log(getClass(), LogLevel.DEBUG, "onDownloadStarted");
                }

                @Override
                public void onItemFinished(IDownloadItemId itemId, Path itemPath)
                {
                    assertion.executeAssertion(new IAssertion()
                        {
                            @Override
                            public void execute() throws Exception
                            {
                                assertDownloadedFile(itemId, itemPath);
                            }
                        });
                    logger.log(getClass(), LogLevel.DEBUG, "onItemFinished");
                }

                @Override
                public void onDownloadFinished(Map<IDownloadItemId, Path> itemPaths)
                {
                    assertion.executeAssertion(new IAssertion()
                        {
                            @Override
                            public void execute() throws Exception
                            {
                                assertEquals(itemPaths.size(), 2);
                                assertDownloadedFile(TEST_ITEM_1_ID, itemPaths.get(TEST_ITEM_1_ID));
                                assertDownloadedFile(TEST_ITEM_2_ID, itemPaths.get(TEST_ITEM_2_ID));
                            }
                        });
                    logger.log(getClass(), LogLevel.DEBUG, "onDownloadFinished");
                }
            });
        download.start();
        logger.awaitLogs("onDownloadStarted", "onItemFinished", "onItemFinished", "onDownloadFinished");
        assertion.assertOK();
    }

    @Test(invocationCount = 20)
    public void testDownloadListeners() throws DownloadException
    {
        TestAssertions assertion = new TestAssertions();
        DownloadClient client = new DownloadClient(new TestDownloadClientConfig());

        DownloadClientDownload download = client.createDownload(new TestUserSession());
        download.addItem(TEST_ITEM_1_ID);
        download.setPreferences(new DownloadPreferences(3));
        download.addListener(new DownloadListenerAdapter()
            {
                @Override
                public void onDownloadStarted()
                {
                    logger.log(getClass(), LogLevel.DEBUG, "testOnDownloadStarted");
                }

                @Override
                public void onItemStarted(IDownloadItemId itemId)
                {
                    logger.log(getClass(), LogLevel.DEBUG, "testOnItemStarted");
                }

                @Override
                public void onItemFinished(IDownloadItemId itemId, Path itemPath)
                {
                    assertion.executeAssertion(new IAssertion()
                        {
                            @Override
                            public void execute() throws Exception
                            {
                                assertDownloadedFile(TEST_ITEM_1_ID, itemPath);
                            }
                        });
                    logger.log(getClass(), LogLevel.DEBUG, "testOnItemFinished");
                }

                @Override
                public void onDownloadFinished(Map<IDownloadItemId, Path> itemPaths)
                {
                    assertion.executeAssertion(new IAssertion()
                        {
                            @Override
                            public void execute() throws Exception
                            {
                                assertEquals(itemPaths.size(), 1);
                                assertDownloadedFile(TEST_ITEM_1_ID, itemPaths.get(TEST_ITEM_1_ID));
                            }
                        });
                    logger.log(getClass(), LogLevel.DEBUG, "testOnDownloadFinished");
                }
            });
        download.start();
        logger.awaitLogs("testOnDownloadStarted", "testOnItemStarted", "testOnItemFinished", "testOnDownloadFinished");
        assertion.assertOK();
    }

    @Test(dataProvider = PROVIDER_OPERATION_NAMES)
    public void testDownloadWithRetriableFailure(String operationName) throws DownloadException
    {
        TestAssertions assertion = new TestAssertions();
        FailureGenerator generator = new FailureGenerator(operationName, 3, true);

        TestDownloadClientConfig config = new TestDownloadClientConfig();
        config.setServer(new FailingDownloadServer(config.getServer(), generator));
        config.setStore(new FailingDownloadStore(config.getStore(), generator));
        config.setRetryProvider(new DefaultRetryProvider(logger, 3, 1, 1));

        DownloadClient client = new DownloadClient(config);
        DownloadClientDownload download = client.createDownload(new TestUserSession());
        download.addItem(TEST_ITEM_1_ID);
        download.setPreferences(new DownloadPreferences(3));
        download.addListener(new DownloadListenerAdapter()
            {
                @Override
                public void onDownloadFinished(Map<IDownloadItemId, Path> itemPaths)
                {
                    assertion.executeAssertion(new IAssertion()
                        {
                            @Override
                            public void execute() throws Exception
                            {
                                assertEquals(itemPaths.size(), 1);
                                assertDownloadedFile(TEST_ITEM_1_ID, itemPaths.get(TEST_ITEM_1_ID));
                            }
                        });
                    logger.log(getClass(), LogLevel.DEBUG, "testOnDownloadFinished");
                }
            });

        download.start();
        logger.awaitLogs("Call failed - will retry", "Call failed - will retry", "Call failed - will retry", "testOnDownloadFinished");
        assertion.assertOK();
    }

    @Test(dataProvider = PROVIDER_OPERATION_NAMES, enabled=false)
    public void testDownloadWithNonRetriableFailure(String operationName) throws DownloadException
    {
        TestAssertions assertion = new TestAssertions();
        FailureGenerator generator = new FailureGenerator(operationName, 1, false);

        TestDownloadClientConfig config = new TestDownloadClientConfig();
        config.setServer(new FailingDownloadServer(config.getServer(), generator));
        config.setStore(new FailingDownloadStore(config.getStore(), generator));
        config.setRetryProvider(new DefaultRetryProvider(logger, 3, 1, 1));

        DownloadClient client = new DownloadClient(config);
        DownloadClientDownload download = client.createDownload(new TestUserSession());
        download.addItem(TEST_ITEM_1_ID);
        download.setPreferences(new DownloadPreferences(3));
        download.addListener(new DownloadListenerAdapter()
            {
                @Override
                public void onDownloadFailed(Collection<Exception> e)
                {
                    logger.log(getClass(), LogLevel.DEBUG, "testOnDownloadFailed");
                }
            });

        download.start();
        logger.awaitLogs("Call failed - will NOT retry", "testOnDownloadFailed");
        assertion.assertOK();
    }

    @Test
    public void testDownloadWithInconsistentCRCInHeader() throws DownloadException
    {
        testDownloadWithInconsistentCRC(1, "Error in header data detected");
    }

    @Test
    public void testDownloadWithInconsistentCRCInPayload() throws DownloadException
    {
        testDownloadWithInconsistentCRC(100, "Error in payload data detected");
    }

    private void testDownloadWithInconsistentCRC(int byteIndexToFail, String expectedErrorMessage) throws DownloadException
    {
        TestAssertions assertion = new TestAssertions();

        TestDownloadClientConfig config = new TestDownloadClientConfig();
        config.setServer(new InconsistentCRCDownloadServer(config.getServer(), 1, byteIndexToFail));
        config.setRetryProvider(new DefaultRetryProvider(logger, 1, 1, 1));

        DownloadClient client = new DownloadClient(config);
        DownloadClientDownload download = client.createDownload(new TestUserSession());
        download.addItem(TEST_ITEM_1_ID);
        download.setPreferences(new DownloadPreferences(3));
        download.addListener(new DownloadListenerAdapter()
            {
                @Override
                public void onDownloadFinished(Map<IDownloadItemId, Path> itemPaths)
                {
                    assertion.executeAssertion(new IAssertion()
                        {
                            @Override
                            public void execute() throws Exception
                            {
                                assertEquals(itemPaths.size(), 1);
                                assertDownloadedFile(TEST_ITEM_1_ID, itemPaths.get(TEST_ITEM_1_ID));
                            }
                        });
                    logger.log(getClass(), LogLevel.DEBUG, "testOnDownloadFinished");
                }
            });

        download.start();
        logger.awaitLogs(expectedErrorMessage, "Retrying (1/1)", "testOnDownloadFinished");
        assertion.assertOK();
    }

    private class TestDownloadServerConfig extends DownloadServerConfig
    {

        public TestDownloadServerConfig()
        {
            setLogger(logger);
            setSessionManager(new TestUserSessionManager());
            setChunkProvider(new TestChunkProvider(logger));
            setConcurrencyProvider(new TestConcurrencyProvider());
            setSerializerProvider(new ISerializerProvider()
                {
                    @Override
                    public IChunkSerializer createChunkSerializer()
                    {
                        return new DefaultChunkSerializer(logger, new TestDownloadItemSerializer());
                    }
                });
        }

    }

    private class TestDownloadClientConfig extends DownloadClientConfig
    {

        public TestDownloadClientConfig()
        {
            setLogger(logger);
            setServer(new DownloadServer(new TestDownloadServerConfig()));
            setStore(new TestDownloadStore(logger, TEST_STORE_PATH));
            setDeserializerProvider(new IDeserializerProvider()
                {
                    @Override
                    public IChunkDeserializer createChunkDeserializer()
                    {
                        return new DefaultChunkDeserializer(logger, new TestDownloadItemIdDeserializer());
                    }
                });
        }

    }

    private void assertDownloadedFile(IDownloadItemId itemId, Path downloadedFilePath) throws Exception
    {
        assertTrue(downloadedFilePath.startsWith(TEST_STORE_PATH));

        TestDownloadItemId testItemId = (TestDownloadItemId) itemId;

        String originalContent = IOUtils.toString(new FileInputStream(testItemId.getFilePath()), Charset.defaultCharset());
        String downloadedContent = IOUtils.toString(new FileInputStream(downloadedFilePath.toFile()), Charset.defaultCharset());

        assertEquals(downloadedContent, originalContent);
    }

    @DataProvider(name = PROVIDER_OPERATION_NAMES)
    private Object[][] provideRetriableOperationNames()
    {
        return new Object[][] { { "IDownloadServer.startDownloadSession" }, { "IDownloadServer.download" }, { "IDownloadServer.download.read" },
                { "IDownloadServer.finishDownloadSession" }, { "IDownloadStore.getItemPath" }, { "IDownloadStore.storeChunk" }
        };
    }

}
