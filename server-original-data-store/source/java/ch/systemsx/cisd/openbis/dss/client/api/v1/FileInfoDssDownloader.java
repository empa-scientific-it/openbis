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

package ch.systemsx.cisd.openbis.dss.client.api.v1;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;

/**
 * Utility class for downloading an array of file infos.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public final class FileInfoDssDownloader
{
    /**
     * Interface for listeners to events from the downloader.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static interface FileInfoDssDownloaderListener
    {
        public void willCreateDirectory(FileInfoDssDTO fileInfo);

        public void willDownload(FileInfoDssDTO fileInfo);

        public void didFinish();
    }

    private final IDataSetDss dataSetDss;

    private final FileInfoDssDTO[] fileInfos;

    private final File outputDir;

    private final FileInfoDssDownloaderListener listener;

    public FileInfoDssDownloader(IDataSetDss dataSetDss, FileInfoDssDTO[] fileInfos, File outputDir)
    {
        // use a no-op listener
        this(dataSetDss, fileInfos, outputDir, new FileInfoDssDownloaderListener()
            {

                @Override
                public void willCreateDirectory(FileInfoDssDTO fileInfo)
                {

                }

                @Override
                public void willDownload(FileInfoDssDTO fileInfo)
                {

                }

                @Override
                public void didFinish()
                {

                }

            });
    }

    public FileInfoDssDownloader(IDataSetDss dataSetDss, FileInfoDssDTO[] fileInfos,
            File outputDir, FileInfoDssDownloaderListener listenerOrNull)
    {
        this.dataSetDss = dataSetDss;
        this.fileInfos = fileInfos;
        this.outputDir = outputDir;
        this.listener = listenerOrNull;
    }

    public final void downloadFiles()
    {
        // Download file in this thread -- could spawn threads for d/l in a future iteration
        for (FileInfoDssDTO fileInfo : fileInfos)
        {
            if (fileInfo.isDirectory())
            {
                listener.willCreateDirectory(fileInfo);
                File dir = new File(outputDir, fileInfo.getPathInListing());
                dir.mkdirs();
            } else
            {
                listener.willDownload(fileInfo);
                File file = new File(outputDir, fileInfo.getPathInListing());
                // Make sure the parent exists
                file.getParentFile().mkdirs();

                // TODO 2011-05-30, Piotr Buczek: performance of this is quite poor
                // we should have a method that handles a collection of inputStreams
                downloadFile(fileInfo, file);
            }
        }

        listener.didFinish();
    }

    private void downloadFile(FileInfoDssDTO fileInfo, File file)
    {
        FileOutputStream fos = null;
        InputStream is = null;
        try
        {
            fos = new FileOutputStream(file);
            is = dataSetDss.getFile(fileInfo.getPathInDataSet());
            IOUtils.copyLarge(is, fos);
        } catch (IOException e)
        {
            if (file.exists())
            {
                file.delete();
            }
            throw new IOExceptionUnchecked(e);
        } catch (Exception e)
        {
            if (file.exists())
            {
                file.delete();
            }
            throw new RuntimeException(e);
        } finally
        {
            IOUtils.closeQuietly(fos);
            IOUtils.closeQuietly(is);
        }
    }
}
