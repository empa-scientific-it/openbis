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

package ch.systemsx.cisd.openbis.common.spring;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;

/**
 * A {@link IUncheckedMultipartFile} implementation which adapts {@link MultipartFile}.
 * 
 * @author Christian Ribeaud
 */
public class MultipartFileAdapter implements IUncheckedMultipartFile
{
    private final MultipartFile multipartFile;

    public MultipartFileAdapter(final MultipartFile multipartFile)
    {
        assert multipartFile != null : "Unspecified multipart file.";
        this.multipartFile = multipartFile;
    }

    //
    // UncheckedMultipartFile
    //

    @Override
    public byte[] getBytes()
    {
        try
        {
            return multipartFile.getBytes();
        } catch (final IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
    }

    @Override
    public String getContentType()
    {
        return multipartFile.getContentType();
    }

    @Override
    public InputStream getInputStream()
    {
        try
        {
            return multipartFile.getInputStream();
        } catch (IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
    }

    @Override
    public String getName()
    {
        return multipartFile.getName();
    }

    @Override
    public String getOriginalFilename()
    {
        return multipartFile.getOriginalFilename();
    }

    @Override
    public long getSize()
    {
        return multipartFile.getSize();
    }

    @Override
    public boolean isEmpty()
    {
        return multipartFile.isEmpty();
    }

    @Override
    public void transferTo(final File dest)
    {
        try
        {
            multipartFile.transferTo(dest);
        } catch (final IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
    }
}
