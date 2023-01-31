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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * Encapsulation of {@link InputStream} that will be sent as a a servlet response.
 * <p>
 * Additional content information:
 * <ul>
 * <li>Content-Type
 * <li>Content-Length
 * <li>Content-Disposition
 * </ul>
 * 
 * @author Piotr Buczek
 */
public class ResponseContentStream
{

    protected static final String CONTENT_TYPE_PNG = "image/png";

    /**
     * @param image is the content of the response
     * @param fileNameOrNull specified if image was generated from one file
     */
    public final static ResponseContentStream createPNG(BufferedImage image, String fileNameOrNull)
            throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        InputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        long responseSize = output.size();
        String contentType = CONTENT_TYPE_PNG;

        return ResponseContentStream.create(inputStream, responseSize, contentType, fileNameOrNull);
    }

    public final static ResponseContentStream create(InputStream inputStream, long responseSize,
            String contentType, String fileNameOrNull)
    {
        String headerContentDisposition = "inline;";
        if (fileNameOrNull != null)
        {
            headerContentDisposition += " filename=\"" + fileNameOrNull + "\"";
        }
        return new ResponseContentStream(inputStream, responseSize, contentType,
                headerContentDisposition);
    }

    private final InputStream inputStream;

    private final long size;

    private final String contentType;

    private final String headerContentDisposition;

    private ResponseContentStream(InputStream inputStream, long size, String contentType,
            String headerContentDisposition)
    {
        this.inputStream = inputStream;
        this.size = size;
        this.contentType = contentType;
        this.headerContentDisposition = headerContentDisposition;
    }

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public long getSize()
    {
        return size;
    }

    public String getContentType()
    {
        return contentType;
    }

    public String getHeaderContentDisposition()
    {
        return headerContentDisposition;
    }
}
