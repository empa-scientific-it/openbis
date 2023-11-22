/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.server.dss.plugins.imaging;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDataSetPackager;
import ch.systemsx.cisd.openbis.dss.generic.server.DataStoreServer;
import ch.systemsx.cisd.openbis.dss.generic.server.TarDataSetPackager;
import ch.systemsx.cisd.openbis.dss.generic.server.ZipDataSetPackager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.ISessionWorkspaceProvider;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

class ImagingArchiver
{
    private final String sessionToken;
    private final String exportDirName;
    private final String exportArchiveName;
    private final Function<InputStream, Long> checksumFunction;
    private final AbstractDataSetPackager packager;
    private final File archiveFile;
    private boolean isFinished;
    private final long archiveDate;

    private static final int DEFAULT_BUFFER_SIZE = (int) (10 * FileUtils.ONE_MB);

    ImagingArchiver(String sessionToken, String archiveFormat) throws IOException {
        this.sessionToken = sessionToken;
        isFinished = false;

        archiveDate = System.currentTimeMillis();
        exportDirName = "imaging_export_" + String.valueOf(archiveDate);

        ISessionWorkspaceProvider sessionWorkspaceProvider = getSessionWorkspaceProvider(sessionToken);
        File rootDirectory = sessionWorkspaceProvider.getSessionWorkspace();

        Path tempDir = Files.createDirectory(
                Path.of(rootDirectory.getAbsolutePath(), exportDirName));


        if (archiveFormat.equalsIgnoreCase("zip"))
        {
            exportArchiveName = "export.zip";
            archiveFile =
                    Files.createFile(Path.of(tempDir.toAbsolutePath().toString(), exportArchiveName))
                            .toFile();
            packager = new ZipDataSetPackager(archiveFile, true, null, null);
            checksumFunction = Util::getCRC32Checksum;
        } else if (archiveFormat.equalsIgnoreCase("tar"))
        {
            exportArchiveName = "export.tar.gz";
            archiveFile =
                    Files.createFile(Path.of(tempDir.toAbsolutePath().toString(), exportArchiveName))
                            .toFile();
            packager = new TarDataSetPackager(archiveFile, null, null, DEFAULT_BUFFER_SIZE,
                    5L * DEFAULT_BUFFER_SIZE);
            checksumFunction = (x) -> 0L;
        } else
        {
            throw new UserFailureException("Unknown archive format!");
        }


    }

    void addToArchive(String folderName, String fileName, byte[] byteArray) {
        assertNotFinished();
        long size = byteArray.length;
        packager.addEntry(Paths.get(folderName, fileName).toString(),
                archiveDate,
                size,
                checksumFunction.apply(new ByteArrayInputStream(byteArray)),
                new ByteArrayInputStream(byteArray));
    }

    void addToArchive(String folderName, File fileOrDirectoryToArchive) {
        assertNotFinished();

        Deque<Map.Entry<String, File>> queue = new LinkedList<>();

        queue.add(new AbstractMap.SimpleImmutableEntry<>(folderName, fileOrDirectoryToArchive));
        while (!queue.isEmpty())
        {
            Map.Entry<String, File> element = queue.pollFirst();
            String prefixPath = element.getKey();
            File file = element.getValue();
            String path = Paths.get(prefixPath, file.getName()).toString();
            if (file.isDirectory())
            {
                for (File f : file.listFiles())
                {
                    queue.add(new AbstractMap.SimpleImmutableEntry<>(path, f));
                }
                packager.addDirectoryEntry(path);
            } else
            {
                try
                {
                    packager.addEntry(path,
                            file.lastModified(),
                            file.getTotalSpace(),
                            checksumFunction.apply(new FileInputStream(file)),
                            new FileInputStream(file));
                } catch (IOException exc)
                {
                    throw new UserFailureException("Failed during export!", exc);
                }
            }
        }

    }

    String build() {
        if(!isFinished) {
            isFinished = true;
            packager.close();
        }
        String url = DataStoreServer.getConfigParameters().getDownloadURL() + "/datastore_server/session_workspace_file_download?sessionID=" + sessionToken + "&filePath=";
        return url + Path.of(exportDirName, exportArchiveName);
    }

    private ISessionWorkspaceProvider getSessionWorkspaceProvider(String sessionToken)
    {
        return ServiceProvider.getDataStoreService().getSessionWorkspaceProvider(sessionToken);
    }

    private void assertNotFinished()
    {
        if(isFinished){
            throw new UserFailureException("Archive file is already closed!");
        }
    }

    
}
