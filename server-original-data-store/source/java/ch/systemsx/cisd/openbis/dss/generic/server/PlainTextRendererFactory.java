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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.PrintWriter;

/**
 * Factory for rendering file system view in plain text.
 * 
 * @author Tomasz Pylak
 */
public class PlainTextRendererFactory implements IRendererFactory
{

    @Override
    public IDirectoryRenderer createDirectoryRenderer(RenderingContext context)
    {
        return new PlainTextDirectoryRenderer();
    }

    @Override
    public IErrorRenderer createErrorRenderer()
    {
        return new IErrorRenderer()
            {
                private PrintWriter writer;

                @Override
                public void printErrorMessage(String errorMessage)
                {
                    writer.println("Error:");
                    writer.println(errorMessage);
                }

                @Override
                public void setWriter(PrintWriter writer)
                {
                    this.writer = writer;
                }
            };
    }

    @Override
    public String getContentType()
    {
        return "text";
    }

    private static class PlainTextDirectoryRenderer implements IDirectoryRenderer
    {
        private PrintWriter writer;

        @Override
        public void printDirectory(String name, String relativePath, long size, Boolean disableLinks)
        {
            writer.format("%s\t%s\n", name, DirectoryRendererUtil.renderFileSize(size));
        }

        @Override
        public void printFile(String name, String relativePath, long size, Integer checksumOrNull, final Boolean disableLinks)
        {
            writer.format("%s\t%s\t%s\n", name, DirectoryRendererUtil.renderFileSize(size),
                    DirectoryRendererUtil.renderCRC32Checksum(checksumOrNull));
        }

        @Override
        public void printFooter()
        {
        }

        @Override
        public void printHeader()
        {
            writer.println("Directory content:");
        }

        @Override
        public void printLinkToParentDirectory(String relativePath, Boolean disableLinks)
        {
        }

        @Override
        public void setWriter(PrintWriter writer)
        {
            this.writer = writer;
        }

    }

    @Override
    public IMessageRenderer createMessageRenderer()
    {
        return new IMessageRenderer()
            {
                private PrintWriter writer;

                @Override
                public void printMessage(String message)
                {
                    writer.println(message);
                }

                @Override
                public void setWriter(PrintWriter writer)
                {
                    this.writer = writer;
                }
            };
    }
}
