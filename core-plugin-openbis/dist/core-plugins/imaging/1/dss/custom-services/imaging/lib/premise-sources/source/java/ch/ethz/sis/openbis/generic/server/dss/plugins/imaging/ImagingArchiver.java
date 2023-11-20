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
import ch.systemsx.cisd.openbis.dss.generic.server.TarDataSetPackager;
import ch.systemsx.cisd.openbis.dss.generic.server.ZipDataSetPackager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.ISessionWorkspaceProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

class ImagingArchiver
{
    private final String sessionToken;
    private final String exportDirName;
    private final String exportArchiveName;
    private final Function<InputStream, Long> checksumFunction;



    private ArchiveBuilder archive;
    
    private final String archiveFormat;
    
    ImagingArchiver(String sessionToken, Map<String, Serializable> exportConfig) {
        this.sessionToken = sessionToken;
        archiveFormat = exportConfig.get("archive-format").toString();

        String currentTimeMs = String.valueOf(System.currentTimeMillis());
        exportDirName = "imaging_export_" + currentTimeMs;

        if (archiveFormat.equalsIgnoreCase("zip"))
        {
            exportArchiveName = "export.zip";
            checksumFunction = Util::getCRC32Checksum;
        } else if (archiveFormat.equalsIgnoreCase("tar"))
        {
            exportArchiveName = "export.tar.gz";
            checksumFunction = (x) -> 0L;
        } else
        {
            throw new UserFailureException("Unknown archive format!");
        }
        
        
    }
    
    void prepare() {

//        try
//        {
//            Path tempDir = Files.createDirectory(Path.of(tempDirectory.getAbsolutePath(), token));
//            if (archiveFormat.equalsIgnoreCase("zip"))
//            {
//                File archiveFile =
//                        Files.createFile(Path.of(tempDir.toAbsolutePath().toString(), "export.zip"))
//                                .toFile();
//                checksumFunction = Util::getCRC32Checksum;
//                packager = new ZipDataSetPackager(archiveFile, true, null, null);
//            } else if (archiveFormat.equalsIgnoreCase("tar"))
//            {
//                archiveFile =
//                        Files.createFile(
//                                        Path.of(tempDir.toAbsolutePath().toString(), "export.tar.gz"))
//                                .toFile();
//                checksumFunction = (x) -> 0L;
//                packager = new TarDataSetPackager(archiveFile, null, null, DEFAULT_BUFFER_SIZE,
//                        5L * DEFAULT_BUFFER_SIZE);
//            } else
//            {
//                throw new UserFailureException("Unknown archive format!");
//            }
//
//        } catch (IOException exception)
//        {
//            throw new UserFailureException("Could not export data!", exception);
//        }
        
        
    }

    ArchiveBuilder newArchive() {
        archive = new ArchiveBuilder();
        return archive;
    }

    ArchiveBuilder addToArchive() {
        if(archive == null) {
            newArchive();
        }
        
        
//        File archiveFile;
//        String token = "export_" + currentTimeMs;
//
//        ISessionWorkspaceProvider sessionWorkspaceProvider =
//                getSessionWorkspaceProvider(sessionToken);
//        File tempDirectory = sessionWorkspaceProvider.getSessionWorkspace();
        
        return new ArchiveBuilder("");
    }


    private ISessionWorkspaceProvider getSessionWorkspaceProvider(String sessionToken)
    {
        return ServiceProvider.getDataStoreService().getSessionWorkspaceProvider(sessionToken);
    }
    
    
    class ArchiveBuilder {

        private AbstractDataSetPackager packager;

        private ArchiveBuilder(String exportArchiveName) {
            if(exportArchiveName.equalsIgnoreCase("export.zip")) {

            } else if(exportArchiveName.equalsIgnoreCase("export.tar.gz")) {

            }
        }
        
        public String build() {
            return "URL";
        }
        
    }
    
    
}
