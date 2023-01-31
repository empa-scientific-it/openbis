package ch.ethz.sis.openbis.generic.dss.systemtest.api.v3;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.filetransfer.DownloadException;
import ch.ethz.sis.filetransfer.DownloadListenerAdapter;
import ch.ethz.sis.filetransfer.DownloadStatus;
import ch.ethz.sis.filetransfer.IDownloadItemId;
import ch.ethz.sis.filetransfer.IDownloadListener;
import ch.ethz.sis.filetransfer.ILogger;
import ch.ethz.sis.filetransfer.IRetryAction;
import ch.ethz.sis.filetransfer.IRetryProvider;
import ch.ethz.sis.filetransfer.IRetryProviderFactory;
import ch.ethz.sis.filetransfer.LogLevel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.create.PersonalAccessTokenCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.PersonalAccessTokenPermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownload;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadReader;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload.FastDownloadSession;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload.FastDownloadSessionOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.DataSetFilePermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;
import ch.ethz.sis.openbis.generic.dssapi.v3.fastdownload.FastDownloadResult;
import ch.ethz.sis.openbis.generic.dssapi.v3.fastdownload.FastDownloader;
import ch.systemsx.cisd.common.collection.SimpleComparator;
import ch.systemsx.cisd.common.exceptions.ExceptionUtils;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

public class DownloadFileTest extends AbstractFileTest
{

    private File target;

    @Override
    @BeforeClass
    protected void beforeClass() throws Exception
    {
        super.beforeClass();
        registerDataSet();
    }

    @BeforeMethod
    public void setUp()
    {
        target = new File(workingDirectory, "file-downloads");
        FileUtilities.deleteRecursively(target);
    }

    @Test
    public void testFastDownloadOfACompleteDataSet()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        DataSetFilePermId root = new DataSetFilePermId(new DataSetPermId(dataSetCode), "");
        List<DataSetFilePermId> fileIds = Arrays.asList(root);
        FastDownloadSessionOptions options = new FastDownloadSessionOptions().withWishedNumberOfStreams(1);

        // When
        FastDownloadSession downloadSession = dss.createFastDownloadSession(sessionToken, fileIds, options);
        FastDownloadResult downloadResult = new FastDownloader(downloadSession).downloadTo(target);

        // Then
        assertEquals(DownloadStatus.FINISHED, downloadResult.getStatus());
        assertDownloads(sessionToken, downloadResult.getPathsById(), fileIds);
    }

    @Test
    public void testFastDownloadUsingPersonalAccessToken()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);

        PersonalAccessTokenCreation patCreation = new PersonalAccessTokenCreation();
        patCreation.setSessionName("test session" + UUID.randomUUID());
        patCreation.setValidFromDate(new Date());
        patCreation.setValidToDate(new Date(System.currentTimeMillis() + DateUtils.MILLIS_PER_DAY));

        List<PersonalAccessTokenPermId> patIds = as.createPersonalAccessTokens(sessionToken, Collections.singletonList(patCreation));

        DataSetFilePermId root = new DataSetFilePermId(new DataSetPermId(dataSetCode), "");
        List<DataSetFilePermId> fileIds = Arrays.asList(root);
        FastDownloadSessionOptions options = new FastDownloadSessionOptions().withWishedNumberOfStreams(1);

        // When
        FastDownloadSession downloadSession = dss.createFastDownloadSession(patIds.get(0).getPermId(), fileIds, options);
        FastDownloadResult downloadResult = new FastDownloader(downloadSession).downloadTo(target);

        // Then
        assertEquals(DownloadStatus.FINISHED, downloadResult.getStatus());
        assertDownloads(sessionToken, downloadResult.getPathsById(), fileIds);
    }

    @Test
    public void testFastDownloadOfAFileWithLogger()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        DataSetFilePermId fileId = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("subdir3/file6.txt"));
        List<DataSetFilePermId> fileIds = Arrays.asList(fileId);
        FastDownloadSessionOptions options = new FastDownloadSessionOptions().withWishedNumberOfStreams(1);
        ILogger logger = new RecordingLogger();

        // When
        FastDownloadSession downloadSession = dss.createFastDownloadSession(sessionToken, fileIds, options);
        FastDownloadResult downloadResult = new FastDownloader(downloadSession).withLogger(logger)
                .downloadTo(target);

        // Then
        assertEquals(DownloadStatus.FINISHED, downloadResult.getStatus());
        assertDownloads(sessionToken, downloadResult.getPathsById(), fileIds);
        assertEquals("log: class ch.ethz.sis.filetransfer.DownloadClientDownload INFO Download state changed to: STARTED\n"
                + "log: class ch.ethz.sis.filetransfer.DownloadClientDownload INFO Download state changed to: FINISHED\n",
                logger.toString());
    }

    @Test
    public void testFastDownloadAFileWithListener()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        DataSetFilePermId file = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("file1.txt"));
        List<DataSetFilePermId> fileIds = Arrays.asList(file);
        FastDownloadSessionOptions options = new FastDownloadSessionOptions().withWishedNumberOfStreams(1);
        IDownloadListener listener = new RecordingListener();

        // When
        FastDownloadSession downloadSession = dss.createFastDownloadSession(sessionToken, fileIds, options);
        FastDownloadResult downloadResult = new FastDownloader(downloadSession).withListener(listener).downloadTo(target);

        // Then
        assertEquals(DownloadStatus.FINISHED, downloadResult.getStatus());
        assertDownloads(sessionToken, downloadResult.getPathsById(), fileIds);
        String id1 = "DownloadItemId[id=" + dataSetCode + "/" + file.getFilePath() + "]";
        String path1 = "targets/unit-test-wd/SystemTests/file-downloads/" + dataSetCode + "/" + file.getFilePath();
        assertEquals("onDownloadStarted:\n"
                + "onItemStarted: " + id1 + "\n"
                + "onChunkDownloaded: 0\n"
                + "onChunkDownloaded: 1\n"
                + "onItemFinished: " + id1 + " " + path1 + "\n"
                + "onDownloadFinished: [" + id1 + "=" + path1 + "]\n", listener.toString());
    }

    @Test
    public void testFastDownloadAFolderWithListener()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        DataSetFilePermId folder = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("subdir1"));
        List<DataSetFilePermId> fileIds = Arrays.asList(folder);
        FastDownloadSessionOptions options = new FastDownloadSessionOptions().withWishedNumberOfStreams(1);
        IDownloadListener listener = new RecordingListener();

        // When
        FastDownloadSession downloadSession = dss.createFastDownloadSession(sessionToken, fileIds, options);
        FastDownloadResult downloadResult = new FastDownloader(downloadSession).withListener(listener).downloadTo(target);

        // Then
        assertEquals(DownloadStatus.FINISHED, downloadResult.getStatus());
        assertDownloads(sessionToken, downloadResult.getPathsById(), fileIds);
        String id2 = "DownloadItemId[id=" + dataSetCode + "/" + folder.getFilePath() + "]";
        String path2 = "targets/unit-test-wd/SystemTests/file-downloads/" + dataSetCode + "/" + folder.getFilePath();
        assertEquals("onDownloadStarted:\n"
                + "onItemStarted: " + id2 + "\n"
                + "onChunkDownloaded: 0\n"
                + "onChunkDownloaded: 1\n"
                + "onChunkDownloaded: 2\n"
                + "onChunkDownloaded: 3\n"
                + "onChunkDownloaded: 4\n"
                + "onChunkDownloaded: 5\n"
                + "onItemFinished: " + id2 + " " + path2 + "\n"
                + "onDownloadFinished: [" + id2 + "=" + path2 + "]\n", listener.toString());
    }

    @Test
    public void testInterruptedFastDownloadAFolder()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        DataSetFilePermId file1 = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("subdir1/file3.txt"));
        DataSetFilePermId file2 = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("subdir1/file4.txt"));
        List<DataSetFilePermId> fileIds = Arrays.asList(file1, file2);
        FastDownloadSessionOptions options = new FastDownloadSessionOptions().withWishedNumberOfStreams(1);
        List<Exception> exceptions = new ArrayList<>();
        IDownloadListener listener = new DownloadListenerAdapter()
            {
                @Override
                public void onDownloadFailed(Collection<Exception> e)
                {
                    exceptions.addAll(e);
                }
            };
        IRetryProviderFactory retryProviderFactory = new IRetryProviderFactory()
            {
                @Override
                public IRetryProvider createRetryProvider(ILogger logger)
                {
                    return new IRetryProvider()
                        {
                            int counter = 3;
                            @Override
                            public <T> T executeWithRetry(IRetryAction<T> action) throws DownloadException
                            {
                                T result = action.execute();
                                if (counter-- > 0)
                                {
                                    return result;
                                }
                                throw new DownloadException("Download failed!!!!", false);
                            }
                        };
                }
            };

        // When
        FastDownloadSession downloadSession = dss.createFastDownloadSession(sessionToken, fileIds, options);
        FastDownloadResult downloadResult = new FastDownloader(downloadSession).withListener(listener)
                .withRetryProviderFactory(retryProviderFactory ).downloadTo(target);

        // Then
        assertEquals(DownloadStatus.FAILED, downloadResult.getStatus());
        assertEquals("[ch.ethz.sis.filetransfer.DownloadException: Download failed!!!!]", exceptions.toString());
    }

    @Test
    public void testFastDownloadUnauthorized()
    {
        // Given
        String sessionToken = as.login(TEST_SPACE_USER, PASSWORD);
        DataSetFilePermId folder = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("subdir1"));
        List<DataSetFilePermId> fileIds = Arrays.asList(folder);
        FastDownloadSessionOptions options = new FastDownloadSessionOptions().withWishedNumberOfStreams(1);

        // When
        FastDownloadSession downloadSession = dss.createFastDownloadSession(sessionToken, fileIds, options);

        // Then
        assertEquals("[]", downloadSession.getFiles().toString());
    }

    @Test
    public void testFastDownloadUnauthorizedByCheating()
    {
        // Given
        String sessionToken = as.login(TEST_SPACE_USER, PASSWORD);
        DataSetFilePermId folder = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("subdir1"));
        List<DataSetFilePermId> fileIds = Arrays.asList(folder);
        FastDownloadSessionOptions options = new FastDownloadSessionOptions().withWishedNumberOfStreams(1);
        FastDownloadSession downloadSession = dss.createFastDownloadSession(sessionToken, fileIds, options);
        assertEquals(0, downloadSession.getFiles().size());
        downloadSession.getFiles().add(folder);

        try
        {
            // When
            new FastDownloader(downloadSession).downloadTo(target);
            fail("DownloadException expected");
        } catch (DownloadException e)
        {
            // Then
            assertEquals("java.lang.IllegalArgumentException: Item ids cannot be null or empty", ExceptionUtils.getEndOfChain(e).getMessage());
        }
    }

    @Test
    public void testDownloadUnauthorized()
    {
        String spaceSessionToken = gis.tryToAuthenticateForAllServices(TEST_SPACE_USER, PASSWORD);

        InputStream stream = dss.downloadFiles(spaceSessionToken, Arrays.asList(new DataSetFilePermId(new DataSetPermId(dataSetCode))),
                new DataSetFileDownloadOptions());

        DataSetFileDownloadReader reader = new DataSetFileDownloadReader(stream);

        assertNull(reader.read());
    }

    @Test
    public void testDownloadAllFiles() throws Exception
    {
        IDataSetFileId root = new DataSetFilePermId(new DataSetPermId(dataSetCode));
        DataSetFileDownloadOptions options = new DataSetFileDownloadOptions();
        options.setRecursive(true);

        Map<String, String> contentMap = download(Arrays.asList(root), options);

        assertEquals(filesAndDirectories.size() + 3, contentMap.size());
        assertEmptyContent(contentMap, getPath("subdir1"));
        assertContent(contentMap, getPath("subdir1/file3.txt"));
    }

    @Test
    public void testDownloadMultipleFiles() throws Exception
    {
        IDataSetFileId file1 = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("file1.txt"));
        IDataSetFileId file6 = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("subdir3/file6.txt"));

        DataSetFileDownloadOptions options = new DataSetFileDownloadOptions();

        Map<String, String> contentMap = download(Arrays.asList(file1, file6), options);

        assertEquals(2, contentMap.size());
        assertContent(contentMap, getPath("file1.txt"));
        assertContent(contentMap, getPath("subdir3/file6.txt"));
    }

    @Test
    public void testDownloadFolderRecursively() throws Exception
    {
        IDataSetFileId folder = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("subdir1/subdir2"));
        DataSetFileDownloadOptions options = new DataSetFileDownloadOptions();
        options.setRecursive(true);

        Map<String, String> contentMap = download(Arrays.asList(folder), options);

        assertEquals(2, contentMap.size());
        assertEmptyContent(contentMap, getPath("subdir1/subdir2"));
        assertContent(contentMap, getPath("subdir1/subdir2/file5.txt"));
    }

    @Test
    public void testDownloadFolderOnly() throws Exception
    {
        IDataSetFileId folder = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("subdir1/subdir2"));
        DataSetFileDownloadOptions options = new DataSetFileDownloadOptions();
        options.setRecursive(false);

        Map<String, String> contentMap = download(Arrays.asList(folder), options);

        assertEquals(1, contentMap.size());
        assertEmptyContent(contentMap, getPath("subdir1/subdir2"));
    }

    @Test(expectedExceptions = { IllegalStateException.class }, expectedExceptionsMessageRegExp = "Input stream no longer valid")
    public void testDownloadStreamReturnedByOldRead() throws Exception
    {
        IDataSetFileId file1 = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("file1.txt"));
        IDataSetFileId file2 = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("file2.txt"));
        DataSetFileDownloadOptions options = new DataSetFileDownloadOptions();

        String sessionToken = gis.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);
        InputStream stream = dss.downloadFiles(sessionToken, Arrays.asList(file1, file2), options);
        DataSetFileDownloadReader reader = new DataSetFileDownloadReader(stream);

        DataSetFileDownload download1 = reader.read();
        reader.read();

        IOUtils.toString(download1.getInputStream());
    }

    @Test
    public void testDownloadStreamReturnedAfterUnconsumedReads() throws Exception
    {
        IDataSetFileId file1 = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("file1.txt"));
        IDataSetFileId file2 = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("file2.txt"));
        IDataSetFileId file3 = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("subdir1/file3.txt"));

        DataSetFileDownloadOptions options = new DataSetFileDownloadOptions();

        String sessionToken = gis.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);
        InputStream stream = dss.downloadFiles(sessionToken, Arrays.asList(file1, file2, file3), options);
        DataSetFileDownloadReader reader = new DataSetFileDownloadReader(stream);

        reader.read();
        reader.read();
        DataSetFileDownload download3 = reader.read();

        assertContent(IOUtils.toString(download3.getInputStream()), download3.getDataSetFile().getPath());
    }

    @Test
    public void testDownloadWithFileLengthsAndChecksum() throws Exception
    {
        IDataSetFileId file1 = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("file1.txt"));
        IDataSetFileId file2 = new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("file2.txt"));

        DataSetFileDownloadOptions options = new DataSetFileDownloadOptions();

        String sessionToken = gis.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);
        InputStream stream = dss.downloadFiles(sessionToken, Arrays.asList(file1, file2), options);
        DataSetFileDownloadReader reader = new DataSetFileDownloadReader(stream);

        DataSetFileDownload download1 = reader.read();
        DataSetFileDownload download2 = reader.read();

        assertEquals(getContent(download1.getDataSetFile().getPath()).length(), download1.getDataSetFile().getFileLength());
        assertEquals(1940359475, download1.getDataSetFile().getChecksumCRC32());
        assertEquals("SHA1", download1.getDataSetFile().getChecksumType());
        assertEquals("522a98b1b45d596ac30fc01aae98350b1d967404", download1.getDataSetFile().getChecksum());
        assertEquals(getContent(download2.getDataSetFile().getPath()).length(), download2.getDataSetFile().getFileLength());
        assertEquals(2140459533, download2.getDataSetFile().getChecksumCRC32());
        assertEquals("SHA1", download2.getDataSetFile().getChecksumType());
        assertEquals("948256a1074656ef54655d53004c622bc77d95f1", download2.getDataSetFile().getChecksum());
    }

    @Test
    public void testLogging()
    {
        String sessionToken = gis.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);

        DataSetFileDownloadOptions options = new DataSetFileDownloadOptions();
        options.setRecursive(true);

        dss.downloadFiles(sessionToken, Arrays.asList(new DataSetFilePermId(new DataSetPermId(dataSetCode), getPath("file1.txt"))), options);

        assertAccessLog(
                "download-files  FILE_IDS('[DataSetFilePermId[dataSetId=" + dataSetCode + ",filePath=" + getPath("file1.txt")
                        + "]]') DOWNLOAD_OPTIONS('DataSetFileDownloadOptions[recursive=true]')");
    }

    private Map<String, String> download(@SuppressWarnings("hiding") List<IDataSetFileId> files, DataSetFileDownloadOptions options)
    {
        try
        {
            String sessionToken = gis.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);

            InputStream stream = dss.downloadFiles(sessionToken, files, options);
            DataSetFileDownloadReader reader = new DataSetFileDownloadReader(stream);

            Map<String, String> contentMap = new HashMap<>();
            DataSetFileDownload download = null;

            while ((download = reader.read()) != null)
            {
                DataSetFile dataSetFile = download.getDataSetFile();
                if (dataSetFile.isDirectory() == false)
                {
                    System.err.println(dataSetFile.getPath()+"; crc32:"+dataSetFile.getChecksumCRC32());
                }
                contentMap.put(dataSetFile.getPath(), IOUtils.toString(download.getInputStream()));
            }

            return contentMap;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void assertEmptyContent(Map<String, String> contentMap, String filePath)
    {
        assertEquals("", contentMap.get(filePath));
    }

    private void assertContent(Map<String, String> contentMap, String filePath)
    {
        assertContent(contentMap.get(filePath), filePath);
    }

    private void assertContent(String content, String filePath)
    {
        assertEquals(getContent(filePath), content);
    }

    private String getContent(String path)
    {
        String relativePath = path.substring(getPathPrefix().length());
        return createRandomContent(relativePath);
    }

    private String getPath(String path)
    {
        return getPathPrefix() + path;
    }

    private String getPathPrefix()
    {
        return "original/" + dataSetCode + "/";
    }

    private void assertDownloads(String sessionToken, Map<IDataSetFileId, Path> pathsById, List<DataSetFilePermId> fileIds)
    {
        List<IDataSetId> dataSetIds = new ArrayList<>(fileIds.stream().map(DataSetFilePermId::getDataSetId).collect(Collectors.toSet()));
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withPhysicalData();
        Map<IDataSetId, DataSet> dataSets = as.getDataSets(sessionToken, dataSetIds, fetchOptions);
        for (DataSetFilePermId fileId : fileIds)
        {
            String location = dataSets.get(fileId.getDataSetId()).getPhysicalData().getLocation();
            File expectedFile = new File(store, "1/" + location + "/" + fileId.getFilePath());
            Path path = pathsById.get(fileId);
            assertNotNull("Path for file " + fileId, path);
            File actualFile = path.toFile();
            assertSameContent(expectedFile, actualFile);
        }
        assertEquals(fileIds.size(), pathsById.size());
    }

    private void assertSameContent(File expectedFile, File actualFile)
    {
        assertEquals(expectedFile.getName(), actualFile.getName());
        assertEquals(actualFile + " exists", expectedFile.exists(), actualFile.exists());
        assertEquals(actualFile + " is directory", expectedFile.isDirectory(), actualFile.isDirectory());
        if (actualFile.isDirectory())
        {
            List<File> expectedChildren = Arrays.asList(expectedFile.listFiles());
            Collections.sort(expectedChildren);
            List<String> expectedChildrenNames = expectedChildren.stream().map(File::getName).collect(Collectors.toList());
            List<File> actualChildren = Arrays.asList(actualFile.listFiles());
            Collections.sort(actualChildren);
            List<String> actualChildrenNames = actualChildren.stream().map(File::getName).collect(Collectors.toList());
            assertEquals(expectedChildrenNames, actualChildrenNames);
            for (int i = 0, n = expectedChildren.size(); i < n; i++)
            {
                assertSameContent(expectedChildren.get(i), actualChildren.get(i));
            }
        } else
        {
            assertEquals(FileUtilities.loadToString(expectedFile), FileUtilities.loadToString(actualFile));
        }
    }

    private static final class RecordingLogger implements ILogger
    {
        private List<Event> events = new ArrayList<>();

        @Override
        public boolean isEnabled(LogLevel level)
        {
            return LogLevel.INFO.compareTo(level) <= 0;
        }

        @Override
        public void log(Class<?> clazz, LogLevel level, String message)
        {
            events.add(new Event("log", clazz, level, message));
        }

        @Override
        public void log(Class<?> clazz, LogLevel level, String message, Throwable throwable)
        {
            events.add(new Event("logWithThrowable", clazz, level, message, throwable));
        }

        @Override
        public String toString()
        {
            return render(events);
        }
    }

    private static final class RecordingListener implements IDownloadListener
    {
        private List<Event> events = new ArrayList<>();

        @Override
        public void onDownloadStarted()
        {
            events.add(new Event("onDownloadStarted"));
        }

        @Override
        public void onDownloadFinished(Map<IDownloadItemId, Path> itemPaths)
        {
            List<Entry<IDownloadItemId, Path>> list = new ArrayList<>(itemPaths.entrySet());
            Collections.sort(list, new SimpleComparator<Entry<IDownloadItemId, Path>, String>()
                {
                    @Override
                    public String evaluate(Entry<IDownloadItemId, Path> item)
                    {
                        return item.getKey().getId();
                    }
                });
            events.add(new Event("onDownloadFinished", list));
        }

        @Override
        public void onDownloadFailed(Collection<Exception> e)
        {
            events.add(new Event("onDownloadFailed", e));
        }

        @Override
        public void onItemStarted(IDownloadItemId itemId)
        {
            events.add(new Event("onItemStarted", itemId));
        }

        @Override
        public void onItemFinished(IDownloadItemId itemId, Path itemPath)
        {
            events.add(new Event("onItemFinished", itemId, itemPath));
        }

        @Override
        public void onChunkDownloaded(int sequenceNumber)
        {
            events.add(new Event("onChunkDownloaded", sequenceNumber));
        }

        @Override
        public String toString()
        {
            return render(events);
        }
    }

    private static final class Event
    {
        private String type;

        private Object[] parameters;

        Event(String type, Object... parameters)
        {
            this.type = type;
            this.parameters = parameters;
        }
    }

    private static final String render(List<Event> events)
    {
        StringBuilder builder = new StringBuilder();
        for (Event event : events)
        {
            builder.append(event.type).append(":");
            for (Object parameter : event.parameters)
            {
                builder.append(" ").append(parameter);
            }
            builder.append("\n");
        }
        return builder.toString();
    }
}