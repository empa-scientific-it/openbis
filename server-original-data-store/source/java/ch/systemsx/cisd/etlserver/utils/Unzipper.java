/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;

/**
 * {@link ZipFile} based utility class, unzipping files.
 * 
 * @author Izabela Adamczyk
 */
public class Unzipper
{

    /**
     * Unzipps given archive file to selected output directory.
     * 
     * @return operation {@link Status} ({@link Status#OK} in case of success and error otherwise)
     */
    public static Status unzip(File archiveFile, File outputDirectory, boolean deleteArchive)
    {
        try
        {
            ZipFile zipFile = new ZipFile(archiveFile);
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements())
            {
                ZipEntry zipEntry = zipEntries.nextElement();
                if (zipEntry.isDirectory())
                {
                    createDirectory(new File(outputDirectory, zipEntry.getName()));
                } else
                {
                    createFile(outputDirectory, zipFile, zipEntry);
                }
            }
            if (deleteArchive)
            {
                archiveFile.delete();
            }
        } catch (Exception ex)
        {
            return Status.createError(ex.getMessage());
        }
        return Status.OK;
    }

    private static void createFile(File outputDir, ZipFile zipFile, ZipEntry zipEntry)
            throws IOException, FileNotFoundException
    {
        File outputFile = new File(outputDir, zipEntry.getName());
        File parentDirectory = outputFile.getParentFile();
        if (parentDirectory.exists() == false)
        {
            createDirectory(parentDirectory);
        }
        BufferedInputStream inputStream = new BufferedInputStream(zipFile.getInputStream(zipEntry));
        BufferedOutputStream outputStream =
                new BufferedOutputStream(new FileOutputStream(outputFile));
        try
        {
            IOUtils.copy(inputStream, outputStream);
        } finally
        {
            outputStream.close();
            inputStream.close();
        }
    }

    private static void createDirectory(File directory)
    {
        boolean directoryCreated = directory.mkdirs();
        if (directoryCreated == false)
        {
            throw EnvironmentFailureException.fromTemplate("Could not create directory '%s'",
                    directory);
        }
    }
}
