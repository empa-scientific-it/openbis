/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.common.spring.IUncheckedMultipartFile;
import ch.systemsx.cisd.openbis.common.spring.MultipartFileAdapter;
import ch.systemsx.cisd.openbis.generic.shared.ISessionWorkspaceProvider;

/**
 * A bean that contains the uploaded files.
 * 
 * @author Christian Ribeaud
 */
public final class UploadedFilesBean
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, UploadedFilesBean.class);

    private static final String CLASS_SIMPLE_NAME = UploadedFilesBean.class.getSimpleName();

    private List<IUncheckedMultipartFile> multipartFiles = new ArrayList<IUncheckedMultipartFile>();

    private final File createTempFile(String sessionToken, ISessionWorkspaceProvider sessionWorkspaceProvider) throws IOException
    {
        File tempFolder = sessionWorkspaceProvider.getSessionWorkspace(sessionToken);
        final File tempFile = File.createTempFile(CLASS_SIMPLE_NAME, null, tempFolder);
        tempFile.deleteOnExit();
        return tempFile;
    }

    public final void addMultipartFile(String sessionToken, final MultipartFile multipartFile, ISessionWorkspaceProvider sessionWorkspaceProvider)
    {
        addMultipartFile(sessionToken, multipartFile, sessionWorkspaceProvider, false);
    }

    public final void addMultipartFile(String sessionToken, final MultipartFile multipartFile, ISessionWorkspaceProvider sessionWorkspaceProvider, boolean keepOriginalFileName)
    {
        assert multipartFile != null : "Unspecified multipart file.";
        try
        {
            File tempFile = null;
            if (keepOriginalFileName) {
                File sessionWorkspaceFolder = sessionWorkspaceProvider.getSessionWorkspace(sessionToken);
                tempFile = new File(sessionWorkspaceFolder, multipartFile.getOriginalFilename());
                if (tempFile.exists()) {
                    tempFile.delete();
                }
                tempFile.createNewFile();
                tempFile.deleteOnExit();
            } else {
                tempFile = createTempFile(sessionToken, sessionWorkspaceProvider);
            }

            multipartFile.transferTo(tempFile);

            operationLog.info("Uploaded file '" + multipartFile.getOriginalFilename() + "' to session workspace");

            final FileMultipartFileAdapter multipartFileAdapter =
                    new FileMultipartFileAdapter(multipartFile, tempFile);
            multipartFiles.add(multipartFileAdapter);
        } catch (final IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
    }

    public final Iterable<IUncheckedMultipartFile> iterable()
    {
        return multipartFiles;
    }

    /**
     * Returns the number of files uploaded.
     */
    public final int size()
    {
        return multipartFiles.size();
    }

    /**
     * Deletes the transferred files.
     */
    public final void deleteTransferredFiles()
    {
        for (final IUncheckedMultipartFile multipartFile : iterable())
        {
            ((FileMultipartFileAdapter) multipartFile).destFile.delete();
        }
    }

    //
    // Helper classes
    //

    private final static class FileMultipartFileAdapter extends MultipartFileAdapter
    {
        private final File destFile;

        FileMultipartFileAdapter(final MultipartFile multipartFile, final File destFile)
        {
            super(multipartFile);
            this.destFile = destFile;
        }

        //
        // MultipartFileAdapter
        //

        @Override
        public final byte[] getBytes()
        {
            try
            {
                return FileUtils.readFileToByteArray(destFile);
            } catch (final IOException ex)
            {
                throw new IOExceptionUnchecked(ex);
            }
        }

        @Override
        public final InputStream getInputStream()
        {
            try
            {
                return FileUtils.openInputStream(destFile);
            } catch (final IOException ex)
            {
                throw new IOExceptionUnchecked(ex);
            }
        }

        @Override
        public final void transferTo(final File dest)
        {
            throw new UnsupportedOperationException();
        }
    }
}
