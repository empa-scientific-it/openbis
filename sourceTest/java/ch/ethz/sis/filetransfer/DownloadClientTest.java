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

import static ch.ethz.sis.filetransfer.FailingDownloadServer.OPERATION_DOWNLOAD;
import static ch.ethz.sis.filetransfer.FailingDownloadServer.OPERATION_DOWNLOAD_READ;
import static ch.ethz.sis.filetransfer.FailingDownloadServer.OPERATION_FINISH_DOWNLOAD_SESSION;
import static ch.ethz.sis.filetransfer.FailingDownloadServer.OPERATION_QUEUE;
import static ch.ethz.sis.filetransfer.FailingDownloadServer.OPERATION_START_DOWNLOAD_SESSION;
import static ch.ethz.sis.filetransfer.FailingDownloadStore.OPERATION_GET_ITEM_PATH;
import static ch.ethz.sis.filetransfer.FailingDownloadStore.OPERATION_STORE_CHUNK;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.filetransfer.TestAssertions.IAssertion;

/**
 * @author pkupczyk
 */
public class DownloadClientTest
{

    private static final int INVOCATION_COUNT = 10;

    private static final int TIMEOUT = 2000;

    private static final int TIMEOUT_LONG = 30000;

    private static final Collection<String> OPERATIONS =
            Arrays.asList(OPERATION_START_DOWNLOAD_SESSION, OPERATION_QUEUE, OPERATION_DOWNLOAD, OPERATION_DOWNLOAD_READ,
                    OPERATION_FINISH_DOWNLOAD_SESSION, OPERATION_GET_ITEM_PATH, OPERATION_STORE_CHUNK);

    private static final String OPERATIONS_PROVIDER = "provider-operations";

    private static final Path TEST_STORE_PATH = Paths.get("targets/test-store");

    private static final Path TEST_FILES_PATH = Paths.get("sourceTest/test-files");

    private static final Path TEMP_FILES_PATH = Paths.get("targets/temp-files");

    private static final TestDownloadItemId TEST_ITEM_1_ID = new TestDownloadItemId(TEST_FILES_PATH.resolve("testFile1.txt"));

    private static final TestDownloadItemId TEST_ITEM_2_ID = new TestDownloadItemId(TEST_FILES_PATH.resolve("testFile2.txt"));

    private static final TestDownloadItemId TEST_FOLDER_ITEM_ID = new TestDownloadItemId(TEST_FILES_PATH.resolve("testFolder"));

    private TestLogger logger;

    @AfterClass(alwaysRun = true)
    private void afterClass() throws Exception
    {
        HttpDownloadServer.stop();
    }

    @BeforeMethod
    private void beforeMethod(Method method, Object[] params) throws Exception
    {
        deleteDirectory(TEST_STORE_PATH);
        TEST_STORE_PATH.toFile().mkdirs();

        deleteDirectory(TEMP_FILES_PATH);
        TEMP_FILES_PATH.toFile().mkdirs();

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
        IDownloadItemId item1 = new TestDownloadItemId(Paths.get("testPath1"));
        IDownloadItemId item2 = new TestDownloadItemId(Paths.get("testPath2"));
        IDownloadItemId item3 = new TestDownloadItemId(Paths.get("testPath3"));

        DownloadClientDownload download = client.createDownload(userSession);
        download.addItem(item1);
        download.addItems(Arrays.asList(item2, item3));

        assertEquals(download.getUserSession(), userSession);
        assertEquals(download.getItems(), Arrays.asList(item1, item2, item3));
    }

    @Test(invocationCount = INVOCATION_COUNT)
    public void testMultipleDownloadsWithSameUserSessionId() throws Exception
    {
        IUserSessionId userSession = new TestUserSession();
        testMultipleDownloads(userSession, userSession);
    }

    @Test(invocationCount = INVOCATION_COUNT)
    public void testMultipleDownloadsWithDifferentUserSessionId() throws Exception
    {
        testMultipleDownloads(new TestUserSession(), new TestUserSession());
    }

    private void testMultipleDownloads(IUserSessionId userSession1, IUserSessionId userSession2) throws Exception
    {
        TestAssertions assertion = new TestAssertions();

        Path path1 = TEMP_FILES_PATH.resolve("testFolder/testFile");
        Path path2 = TEMP_FILES_PATH.resolve("testFolder/testFile2");
        Path path3 = TEMP_FILES_PATH.resolve("testFile");
        Path path4 = TEMP_FILES_PATH.resolve("testFile2");

        createRandomFile(path1, FileUtils.ONE_MB);
        createRandomFile(path2, FileUtils.ONE_MB / 2);
        createRandomFile(path3, FileUtils.ONE_MB * 2);
        createRandomFile(path4, FileUtils.ONE_MB);

        TestDownloadItemId item1 = new TestDownloadItemId(path1);
        TestDownloadItemId item2 = new TestDownloadItemId(path2);
        TestDownloadItemId item3 = new TestDownloadItemId(path3);
        TestDownloadItemId item4 = new TestDownloadItemId(path1);
        TestDownloadItemId item5 = new TestDownloadItemId(path3);
        TestDownloadItemId item6 = new TestDownloadItemId(path4);

        TestDownloadServerConfig serverConfig = new TestDownloadServerConfig();
        serverConfig.setChunkProvider(new TestChunkProvider(logger, FileUtils.ONE_MB / 50));

        TestDownloadClientConfig clientConfig = new TestDownloadClientConfig();
        clientConfig.setServer(new HttpDownloadServer(logger, new DownloadServer(serverConfig)));

        DownloadClient client = new DownloadClient(clientConfig);

        DownloadClientDownload download1 = client.createDownload(userSession1);
        download1.addItem(item1);
        download1.addItem(item2);
        download1.addItem(item3);
        download1.setPreferences(new DownloadPreferences(1));
        download1.addListener(new DownloadListenerAdapter()
            {
                @Override
                public void onDownloadFinished(Map<IDownloadItemId, Path> itemPaths)
                {
                    assertion.executeAssertion(new IAssertion()
                        {
                            @Override
                            public void execute() throws Exception
                            {
                                assertEquals(itemPaths.size(), 3);
                                assertFilesEqual(item1.getFilePath(), itemPaths.get(item1));
                                assertFilesEqual(item2.getFilePath(), itemPaths.get(item2));
                                assertFilesEqual(item3.getFilePath(), itemPaths.get(item3));
                            }
                        });
                    logger.log(getClass(), LogLevel.INFO, "testOnDownloadFinished");
                }
            });

        DownloadClientDownload download2 = client.createDownload(userSession2);
        download2.addItem(item4);
        download2.addItem(item5);
        download2.addItem(item6);
        download2.setPreferences(new DownloadPreferences(2));
        download2.addListener(new DownloadListenerAdapter()
            {
                @Override
                public void onDownloadFinished(Map<IDownloadItemId, Path> itemPaths)
                {
                    assertion.executeAssertion(new IAssertion()
                        {
                            @Override
                            public void execute() throws Exception
                            {
                                assertEquals(itemPaths.size(), 3);
                                assertFilesEqual(item4.getFilePath(), itemPaths.get(item4));
                                assertFilesEqual(item5.getFilePath(), itemPaths.get(item5));
                                assertFilesEqual(item6.getFilePath(), itemPaths.get(item6));
                            }
                        });
                    logger.log(getClass(), LogLevel.INFO, "testOnDownloadFinished");
                }
            });

        download1.start();
        download2.start();

        logger.awaitLogs(TIMEOUT_LONG, "testOnDownloadFinished", "testOnDownloadFinished");
        logger.assertNoLogsWithLevels(LogLevel.ERROR, LogLevel.WARN);
        assertion.assertOK();
    }

    @Test(invocationCount = INVOCATION_COUNT)
    public void testIllegalStateWithStartAlreadyStarted() throws Exception
    {
        testActionWithAlreadyStarted(new Function<DownloadClientDownload, Void>()
            {

                @Override
                public Void apply(DownloadClientDownload download)
                {
                    try
                    {
                        download.start();
                    } catch (Exception e)
                    {
                        assertEquals(download.getStatus(), DownloadStatus.STARTED);
                        assertEquals(e.getClass(), IllegalStateException.class);
                        assertEquals(e.getMessage(), "Download has been already started.");
                    }
                    return null;
                }

            });
    }

    @Test
    public void testIllegalStateWithSetPreferencesOnAlreadyStarted() throws Exception
    {
        testActionWithAlreadyStarted(new Function<DownloadClientDownload, Void>()
            {

                @Override
                public Void apply(DownloadClientDownload download)
                {
                    try
                    {
                        download.setPreferences(new DownloadPreferences());
                    } catch (Exception e)
                    {
                        assertEquals(download.getStatus(), DownloadStatus.STARTED);
                        assertEquals(e.getClass(), IllegalStateException.class);
                        assertEquals(e.getMessage(), "Preferences cannot be set once a download is started.");
                    }
                    return null;
                }

            });
    }

    @Test
    public void testIllegalStateWithAddItemOnAlreadyStarted() throws Exception
    {
        testActionWithAlreadyStarted(new Function<DownloadClientDownload, Void>()
            {

                @Override
                public Void apply(DownloadClientDownload download)
                {
                    try
                    {
                        download.addItem(TEST_ITEM_2_ID);
                    } catch (Exception e)
                    {
                        assertEquals(download.getStatus(), DownloadStatus.STARTED);
                        assertEquals(e.getClass(), IllegalStateException.class);
                        assertEquals(e.getMessage(), "Item ids cannot be added once a download is started.");
                    }
                    return null;
                }

            });
    }

    @Test
    public void testIllegalStateWithGetDownloadSessionIdOnNotStarted()
    {
        DownloadClient client = new DownloadClient(new TestDownloadClientConfig());

        DownloadClientDownload download = client.createDownload(new TestUserSession());

        try
        {
            download.getDownloadSessionId();
        } catch (IllegalStateException e)
        {
            assertEquals(e.getMessage(), "Download session id cannot be read before a download is started.");
        }
    }

    private void testActionWithAlreadyStarted(Function<DownloadClientDownload, Void> afterStartAction) throws Exception
    {
        TestAssertions assertion = new TestAssertions();

        DownloadClient client = new DownloadClient(new TestDownloadClientConfig());

        DownloadClientDownload download = client.createDownload(new TestUserSession());
        download.addItem(TEST_ITEM_1_ID);
        download.addListener(new DownloadListenerAdapter()
            {
                @Override
                public void onDownloadStarted()
                {
                    logger.log(getClass(), LogLevel.INFO, "testDownloadStarted");
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
                                assertFilesEqual(TEST_ITEM_1_ID.getFilePath(), itemPaths.get(TEST_ITEM_1_ID));
                            }
                        });
                    logger.log(getClass(), LogLevel.INFO, "testDownloadFinished");
                }
            });

        download.start();
        assertEquals(download.getStatus(), DownloadStatus.STARTED);
        logger.awaitLogs(0, "testDownloadStarted");

        afterStartAction.apply(download);

        logger.awaitLogs(TIMEOUT, "testDownloadFinished");
        assertEquals(download.getStatus(), DownloadStatus.FINISHED);
        assertion.assertOK();
    }

    @Test
    public void testDownloadWithComplexSetup() throws Exception
    {
        TestAssertions assertion = new TestAssertions();

        TestUserSession userSession1 = new TestUserSession();
        TestUserSession userSession2 = new TestUserSession();

        Path path1 = TEMP_FILES_PATH.resolve("testFolder/testFile");
        Path path2 = TEMP_FILES_PATH.resolve("testFolder/testFile2");
        Path path3 = TEMP_FILES_PATH.resolve("testFile");
        Path path4 = TEMP_FILES_PATH.resolve("testFile2");

        createRandomFile(path1, FileUtils.ONE_MB);
        createRandomFile(path2, FileUtils.ONE_MB / 2);
        createRandomFile(path3, FileUtils.ONE_MB * 2);
        createRandomFile(path4, FileUtils.ONE_MB);

        TestDownloadItemId item1 = new TestDownloadItemId(path1);
        TestDownloadItemId item2 = new TestDownloadItemId(path2);
        TestDownloadItemId item3 = new TestDownloadItemId(path3);
        TestDownloadItemId item4 = new TestDownloadItemId(path1);
        TestDownloadItemId item5 = new TestDownloadItemId(path3);
        TestDownloadItemId item6 = new TestDownloadItemId(path4);

        FailureGenerator generator = new FailureGenerator(OPERATIONS, 3, Integer.MAX_VALUE, true);

        TestDownloadServerConfig serverConfig = new TestDownloadServerConfig();
        serverConfig.setChunkProvider(new TestChunkProvider(logger, FileUtils.ONE_MB / 50));

        IDownloadServer server = new FailingDownloadServer(new HttpDownloadServer(logger, new DownloadServer(serverConfig)), generator);
        IDownloadStore store = new FailingDownloadStore(new TestDownloadStore(logger, TEST_STORE_PATH), generator);

        TestDownloadClientConfig clientConfig = new TestDownloadClientConfig();
        clientConfig.setServer(server);
        clientConfig.setStore(store);
        clientConfig.setRetryProvider(new DefaultRetryProvider(logger, 3, 1, 1));

        DownloadClient client = new DownloadClient(clientConfig);

        DownloadClientDownload download1 = client.createDownload(userSession1);
        download1.addItem(item1);
        download1.addItem(item2);
        download1.setPreferences(new DownloadPreferences(2));
        download1.addListener(new DownloadListenerAdapter()
            {
                @Override
                public void onDownloadFinished(Map<IDownloadItemId, Path> itemPaths)
                {
                    assertion.executeAssertion(new IAssertion()
                        {
                            @Override
                            public void execute() throws Exception
                            {
                                assertEquals(itemPaths.size(), 2);
                                assertFilesEqual(item1.getFilePath(), itemPaths.get(item1));
                                assertFilesEqual(item2.getFilePath(), itemPaths.get(item2));
                            }
                        });
                    logger.log(getClass(), LogLevel.INFO, "testOnDownloadFinished");
                }
            });

        DownloadClientDownload download2 = client.createDownload(userSession1);
        download2.addItem(item3);
        download2.setPreferences(new DownloadPreferences(1));
        download2.addListener(new DownloadListenerAdapter()
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
                                assertFilesEqual(item3.getFilePath(), itemPaths.get(item3));
                            }
                        });
                    logger.log(getClass(), LogLevel.INFO, "testOnDownloadFinished");
                }
            });

        DownloadClientDownload download3 = client.createDownload(userSession2);
        download3.addItem(item4);
        download3.addItem(item5);
        download3.addItem(item6);
        download3.setPreferences(new DownloadPreferences(3));
        download3.addListener(new DownloadListenerAdapter()
            {
                @Override
                public void onDownloadFinished(Map<IDownloadItemId, Path> itemPaths)
                {
                    assertion.executeAssertion(new IAssertion()
                        {
                            @Override
                            public void execute() throws Exception
                            {
                                assertEquals(itemPaths.size(), 3);
                                assertFilesEqual(item4.getFilePath(), itemPaths.get(item4));
                                assertFilesEqual(item5.getFilePath(), itemPaths.get(item5));
                                assertFilesEqual(item6.getFilePath(), itemPaths.get(item6));
                            }
                        });
                    logger.log(getClass(), LogLevel.INFO, "testOnDownloadFinished");
                }
            });

        download1.start();
        download2.start();
        download3.start();

        logger.awaitLogs(TIMEOUT_LONG, "testOnDownloadFinished", "testOnDownloadFinished", "testOnDownloadFinished");
        logger.assertNoLogsWithLevels(LogLevel.ERROR);
        assertion.assertOK();
    }

    @Test(invocationCount = INVOCATION_COUNT)
    public void testDownloadWithMultipleItems() throws DownloadException
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
                    logger.log(getClass(), LogLevel.INFO, "testOnDownloadStarted");
                }

                @Override
                public void onItemFinished(IDownloadItemId itemId, Path itemPath)
                {
                    assertion.executeAssertion(new IAssertion()
                        {
                            @Override
                            public void execute() throws Exception
                            {
                                TestDownloadItemId testItemId = (TestDownloadItemId) itemId;
                                assertFilesEqual(testItemId.getFilePath(), itemPath);
                            }
                        });
                    logger.log(getClass(), LogLevel.INFO, "testOnItemFinished");
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
                                assertFilesEqual(TEST_ITEM_1_ID.getFilePath(), itemPaths.get(TEST_ITEM_1_ID));
                                assertFilesEqual(TEST_ITEM_2_ID.getFilePath(), itemPaths.get(TEST_ITEM_2_ID));
                            }
                        });
                    logger.log(getClass(), LogLevel.INFO, "testOnDownloadFinished");
                }
            });
        download.start();
        logger.awaitLogs(TIMEOUT, "testOnDownloadStarted", "testOnItemFinished", "testOnItemFinished", "testOnDownloadFinished");
        logger.assertNoLogsWithLevels(LogLevel.ERROR, LogLevel.WARN);
        assertion.assertOK();
    }

    @Test
    public void testDownloadWithBigItems() throws Exception
    {
        TestAssertions assertion = new TestAssertions();

        TestDownloadItemId item1 = new TestDownloadItemId(TEMP_FILES_PATH.resolve("testFile1"));
        TestDownloadItemId item2 = new TestDownloadItemId(TEMP_FILES_PATH.resolve("testFile2"));

        createRandomFile(item1.getFilePath(), FileUtils.ONE_MB * 100);
        createRandomFile(item2.getFilePath(), FileUtils.ONE_MB * 50);

        TestDownloadServerConfig serverConfig = new TestDownloadServerConfig();
        serverConfig.setChunkProvider(new TestChunkProvider(logger, FileUtils.ONE_MB));

        TestDownloadClientConfig clientConfig = new TestDownloadClientConfig();
        clientConfig.setServer(new HttpDownloadServer(logger, new DownloadServer(serverConfig)));

        DownloadClient client = new DownloadClient(clientConfig);
        DownloadClientDownload download = client.createDownload(new TestUserSession());
        download.addItem(item1);
        download.addItem(item2);
        download.setPreferences(new DownloadPreferences(5));
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
                                assertEquals(itemPaths.size(), 2);
                                assertFilesEqual(item1.getFilePath(), itemPaths.get(item1));
                                assertFilesEqual(item2.getFilePath(), itemPaths.get(item2));
                            }
                        });
                    logger.log(getClass(), LogLevel.INFO, "testOnDownloadFinished");
                }
            });

        download.start();
        logger.awaitLogs(TIMEOUT_LONG, "testOnDownloadFinished");
        logger.assertNoLogsWithLevels(LogLevel.ERROR, LogLevel.WARN);
        assertion.assertOK();
    }

    @Test
    public void testDownloadWithFolderItem() throws Exception
    {
        TestAssertions assertion = new TestAssertions();

        DownloadClient client = new DownloadClient(new TestDownloadClientConfig());
        DownloadClientDownload download = client.createDownload(new TestUserSession());
        download.addItem(TEST_FOLDER_ITEM_ID);
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

                                Path downloadedFolder = itemPaths.values().iterator().next();

                                assertFilesEqual(TEST_FOLDER_ITEM_ID.getFilePath().resolve("testFile3.txt"),
                                        downloadedFolder.resolve("testFile3.txt"));
                                assertFilesEqual(TEST_FOLDER_ITEM_ID.getFilePath().resolve("testFile4.txt"),
                                        downloadedFolder.resolve("testFile4.txt"));
                                assertFilesEqual(TEST_FOLDER_ITEM_ID.getFilePath().resolve("testFolder2/testFile5.txt"),
                                        downloadedFolder.resolve("testFolder2/testFile5.txt"));
                            }
                        });
                    logger.log(getClass(), LogLevel.INFO, "testOnDownloadFinished");
                }
            });

        download.start();
        logger.awaitLogs(TIMEOUT, "testOnDownloadFinished");
        logger.assertNoLogsWithLevels(LogLevel.ERROR, LogLevel.WARN);
        assertion.assertOK();
    }

    @Test(invocationCount = INVOCATION_COUNT)
    public void testDownloadWithListeners() throws DownloadException
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
                    logger.log(getClass(), LogLevel.INFO, "testOnDownloadStarted");
                }

                @Override
                public void onItemStarted(IDownloadItemId itemId)
                {
                    logger.log(getClass(), LogLevel.INFO, "testOnItemStarted");
                }

                @Override
                public void onChunkDownloaded(int chunkSequenceNumber)
                {
                    logger.log(getClass(), LogLevel.INFO, "testOnChunkDownloaded");
                }

                @Override
                public void onItemFinished(IDownloadItemId itemId, Path itemPath)
                {
                    assertion.executeAssertion(new IAssertion()
                        {
                            @Override
                            public void execute() throws Exception
                            {
                                assertFilesEqual(TEST_ITEM_1_ID.getFilePath(), itemPath);
                            }
                        });
                    logger.log(getClass(), LogLevel.INFO, "testOnItemFinished");
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
                                assertFilesEqual(TEST_ITEM_1_ID.getFilePath(), itemPaths.get(TEST_ITEM_1_ID));
                            }
                        });
                    logger.log(getClass(), LogLevel.INFO, "testOnDownloadFinished");
                }
            });
        download.start();
        logger.awaitLogs(TIMEOUT, "testOnDownloadStarted", "testOnItemStarted", "testOnChunkDownloaded", "testOnItemFinished", "testOnDownloadFinished");
        logger.assertNoLogsWithLevels(LogLevel.ERROR, LogLevel.WARN);
        assertion.assertOK();
    }

    @Test(dataProvider = OPERATIONS_PROVIDER, invocationCount = INVOCATION_COUNT)
    public void testDownloadWithRetriableFailure(String operationName) throws DownloadException
    {
        TestAssertions assertion = new TestAssertions();
        FailureGenerator generator = new FailureGenerator(Collections.singleton(operationName), 3, Integer.MAX_VALUE, true);

        TestDownloadClientConfig config = new TestDownloadClientConfig();
        config.setServer(new FailingDownloadServer(new HttpDownloadServer(logger, config.getServer()), generator));
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
                                assertFilesEqual(TEST_ITEM_1_ID.getFilePath(), itemPaths.get(TEST_ITEM_1_ID));
                            }
                        });
                    logger.log(getClass(), LogLevel.INFO, "testOnDownloadFinished");
                }
            });

        download.start();
        logger.awaitLogs(TIMEOUT, "Call failed - will retry", "Call failed - will retry", "Call failed - will retry", "testOnDownloadFinished");
        logger.assertNoLogsWithLevels(LogLevel.ERROR);
        assertion.assertOK();
    }

    @Test(dataProvider = OPERATIONS_PROVIDER, invocationCount = INVOCATION_COUNT)
    public void testDownloadWithNonRetriableFailure(String operationName) throws DownloadException
    {
        TestAssertions assertion = new TestAssertions();
        FailureGenerator generator = new FailureGenerator(Collections.singleton(operationName), 1, Integer.MAX_VALUE, false);

        TestDownloadClientConfig config = new TestDownloadClientConfig();
        config.setServer(new FailingDownloadServer(new HttpDownloadServer(logger, config.getServer()), generator));
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
                    logger.log(getClass(), LogLevel.INFO, "testOnDownloadFailed");
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
                                assertFilesEqual(TEST_ITEM_1_ID.getFilePath(), itemPaths.get(TEST_ITEM_1_ID));
                            }
                        });
                    logger.log(getClass(), LogLevel.INFO, "testOnDownloadFinished");
                }
            });

        Exception exception = null;

        try
        {
            download.start();
        } catch (Exception e)
        {
            exception = e;
        }

        if (operationName.equals(OPERATION_START_DOWNLOAD_SESSION) || operationName.equals(OPERATION_QUEUE))
        {
            // DownloadException is thrown from the start method and listeners get executed
            logger.awaitLogs(TIMEOUT, "Intentional failure for testing purposes", "testOnDownloadFailed");
            logger.assertLogsWithLevels(LogLevel.ERROR);
            assertEquals(exception.getClass(), DownloadException.class);
        } else if (operationName.equals(OPERATION_DOWNLOAD_READ))
        {
            // IOException thrown from InputStream.read method is always retried
            logger.awaitLogs(TIMEOUT, "Call failed - will retry", "testOnDownloadFinished");
            logger.assertNoLogsWithLevels(LogLevel.ERROR);
            assertEquals(exception, null);
        } else if (operationName.equals(OPERATION_GET_ITEM_PATH))
        {
            // getItemPath is only used when executing the listeners and its failure does not influence the download itself
            logger.awaitLogs(TIMEOUT, "Intentional failure for testing purposes", "testOnDownloadFinished");
            logger.assertNoLogsWithLevels(LogLevel.ERROR);
            assertEquals(exception, null);
        } else if (operationName.equals(OPERATION_FINISH_DOWNLOAD_SESSION))
        {
            // Failing to finish a download session does not make the download fail
            logger.awaitLogs(TIMEOUT, "Intentional failure for testing purposes", "testOnDownloadFinished");
            logger.assertNoLogsWithLevels(LogLevel.ERROR);
            assertEquals(exception, null);
        } else
        {
            logger.awaitLogs(TIMEOUT, "Intentional failure for testing purposes", "testOnDownloadFailed");
            logger.assertLogsWithLevels(LogLevel.ERROR);
            assertEquals(exception, null);
        }

        assertion.assertOK();
    }

    @Test(invocationCount = INVOCATION_COUNT)
    public void testDownloadWithInconsistentCRCInHeader() throws DownloadException
    {
        testDownloadWithInconsistentCRC(1, "Error in header data detected");
    }

    @Test(invocationCount = INVOCATION_COUNT)
    public void testDownloadWithInconsistentCRCInPayload() throws DownloadException
    {
        testDownloadWithInconsistentCRC(80, "Error in payload data detected");
    }

    private void testDownloadWithInconsistentCRC(int byteIndexToFail, String expectedErrorMessage) throws DownloadException
    {
        TestAssertions assertion = new TestAssertions();

        TestDownloadClientConfig config = new TestDownloadClientConfig();
        config.setServer(new InconsistentCRCDownloadServer(new HttpDownloadServer(logger, config.getServer()), 0, byteIndexToFail));
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
                                assertFilesEqual(TEST_ITEM_1_ID.getFilePath(), itemPaths.get(TEST_ITEM_1_ID));
                            }
                        });
                    logger.log(getClass(), LogLevel.INFO, "testOnDownloadFinished");
                }
            });

        download.start();
        logger.awaitLogs(TIMEOUT, expectedErrorMessage, "Retrying (1/1)", "testOnDownloadFinished");
        logger.assertNoLogsWithLevels(LogLevel.ERROR);
        assertion.assertOK();
    }

    private class TestDownloadServerConfig extends DownloadServerConfig
    {

        public TestDownloadServerConfig()
        {
            setLogger(logger);
            setSessionManager(new TestUserSessionManager());
            setChunkProvider(new TestChunkProvider(logger, 5));
            setConcurrencyProvider(new TestConcurrencyProvider());
            setSerializerProvider(new DefaultSerializerProvider(new TestDownloadItemIdSerializer(), logger));
        }

    }

    private class TestDownloadClientConfig extends DownloadClientConfig
    {

        public TestDownloadClientConfig()
        {
            setLogger(logger);
            setServer(new DownloadServer(new TestDownloadServerConfig()));
            setStore(new TestDownloadStore(logger, TEST_STORE_PATH));
            setRetryProvider(new DefaultRetryProvider(logger, 0, 1, 1));
            setDeserializerProvider(new DefaultDeserializerProvider(logger, new TestDownloadItemIdDeserializer()));
        }

    }

    private void assertFilesEqual(Path sourceFilePath, Path downloadedFilePath) throws Exception
    {
        assertTrue(downloadedFilePath.startsWith(TEST_STORE_PATH));

        ByteBuffer testFileBuffer = ByteBuffer.allocate((int) FileUtils.ONE_MB);
        ByteBuffer downloadedFileBuffer = ByteBuffer.allocate((int) FileUtils.ONE_MB);

        try (
                FileChannel testFileChannel = FileChannel.open(sourceFilePath, StandardOpenOption.READ);
                FileChannel downloadedFileChannel = FileChannel.open(downloadedFilePath, StandardOpenOption.READ))
        {

            while (true)
            {
                int testFileCount = testFileChannel.read(testFileBuffer);
                int downloadedFileCount = downloadedFileChannel.read(downloadedFileBuffer);

                if (testFileCount != downloadedFileCount)
                {
                    fail("Downloaded file contents was different from the source file contents. Source file path: " + sourceFilePath
                            + ". Downloaded file path: "
                            + downloadedFilePath);
                } else if (testFileCount == -1 && downloadedFileCount == -1)
                {
                    break;
                }

                testFileBuffer.flip();
                downloadedFileBuffer.flip();

                for (int i = 0; i < testFileCount; i++)
                {
                    byte testFileByte = testFileBuffer.get();
                    byte downloadedFileByte = downloadedFileBuffer.get();

                    if (testFileByte != downloadedFileByte)
                    {
                        fail("Downloaded file contents was different from the source file contents. Source file path: " + sourceFilePath
                                + ". Downloaded file path: "
                                + downloadedFilePath);
                    }
                }
            }
        }
    }

    private void deleteDirectory(Path path) throws IOException
    {
        if (path.toFile().exists())
        {
            FileUtils.deleteDirectory(path.toFile());
        }
    }

    private void createRandomFile(Path path, long estimatedSize) throws IOException
    {
        File folder = path.toFile().getParentFile();

        if (false == folder.exists())
        {
            folder.mkdirs();
        }

        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE))
        {
            ByteBuffer buffer = ByteBuffer.allocate((int) Math.min(estimatedSize, FileUtils.ONE_MB));
            Random random = new Random();
            long currentSize = 0;

            while (currentSize < estimatedSize)
            {
                random.nextBytes(buffer.array());
                currentSize += channel.write(buffer);
                buffer.clear();
            }

            // add some additional bytes to the buffer for the last chunk not to be full (most common scenario)
            buffer.limit((int) (Math.random() * buffer.capacity()));
            channel.write(buffer);
        }
    }

    @DataProvider(name = OPERATIONS_PROVIDER)
    private Object[][] provideOperationNames()
    {
        Object[][] array = new Object[OPERATIONS.size()][];
        int i = 0;

        for (String operation : OPERATIONS)
        {
            array[i++] = new String[] { operation };
        }

        return array;
    }

}
