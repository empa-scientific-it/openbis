/*
 * Copyright 2002-2006 the original author or authors.
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
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

/**
 * A representation of an uploaded file received in a multipart request.
 * <p>
 * This is almost a copy of {@link MultipartFile} without the checked exceptions.
 * </p>
 * 
 * @author Christian Ribeaud
 * @see org.springframework.web.multipart.MultipartFile
 */
public interface IUncheckedMultipartFile
{

    /**
     * @see MultipartFile#getName()
     */
    String getName();

    /**
     * @see MultipartFile#getOriginalFilename()
     */
    String getOriginalFilename();

    /**
     * @see MultipartFile#getContentType()
     */
    String getContentType();

    /**
     * @see MultipartFile#isEmpty()
     */
    boolean isEmpty();

    /**
     * @see MultipartFile#getSize()
     */
    long getSize();

    /**
     * @see MultipartFile#getBytes()
     */
    byte[] getBytes();

    /**
     * @see MultipartFile#getInputStream()
     */
    InputStream getInputStream();

    /**
     * @see MultipartFile#transferTo(File)
     */
    void transferTo(final File dest);
}
