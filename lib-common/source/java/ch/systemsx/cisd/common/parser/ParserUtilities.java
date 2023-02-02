/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.parser;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.parser.filter.AlwaysAcceptLineFilter;
import ch.systemsx.cisd.common.parser.filter.ILineFilter;

/**
 * Utilities for parsing files.
 * 
 * @author Bernd Rinn
 */
public final class ParserUtilities
{
    private ParserUtilities()
    {
        // Can not be instantiated.
    }

    /**
     * A class for splitting text content from a <code>String</code> or <code>File</code> into lines, optionally filtering them by a
     * {@link ILineFilter}.
     */
    public static class LineSplitter implements Closeable
    {
        private final LineIterator lineIterator;

        private final ILineFilter lineFilter;

        private int lineNumber;

        public LineSplitter(final String content)
        {
            this(content, null);
        }

        public LineSplitter(final String content, final ILineFilter lineFilterOrNull)
        {
            assert content != null : "Unspecified context.";

            final Reader reader = new StringReader(content);
            lineIterator = IOUtils.lineIterator(reader);
            lineFilter = getLineFilter(lineFilterOrNull);
        }

        public LineSplitter(final File file) throws IOExceptionUnchecked
        {
            this(file, null);
        }

        public LineSplitter(final File file, final ILineFilter lineFilterOrNull)
                throws IOExceptionUnchecked
        {
            assert file != null : "Given file must not be null.";

            try
            {
                lineIterator = FileUtils.lineIterator(file);
                lineFilter = getLineFilter(lineFilterOrNull);
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }

        private final static ILineFilter getLineFilter(final ILineFilter lineFilterOrNull)
        {
            return (lineFilterOrNull == null) ? AlwaysAcceptLineFilter.INSTANCE : lineFilterOrNull;
        }

        @Override
        public void close()
        {
            try
            {
                lineIterator.close();
            } catch (IOException e)
            {
                throw new IOExceptionUnchecked(e);
            }
        }

        /**
         * Returns the next line that is accepted by the <var>lineFilter</var>.
         */
        public ILine<String> tryNextLine()
        {
            for (int line = lineNumber; lineIterator.hasNext(); line++)
            {
                final String nextLine = lineIterator.nextLine();
                final Line ret = new Line(line, nextLine);
                if (lineFilter.acceptLine(ret))
                {
                    lineNumber = line + 1;
                    return ret;
                }
            }
            return null;
        }
    }

    /**
     * Returns the first <code>Line</code> that is not filtered out by given <code>ILineFilter</code>.
     * <p>
     * You should not call this method if given <var>content</var> is <code>null</code>.
     * </p>
     * 
     * @param lineFilterOrNull could be <code>null</code>. In this case, the {@link AlwaysAcceptLineFilter} implementation will be used.
     * @param content the content that is going to be analyzed. Can not be <code>null</code>.
     * @return <code>null</code> if all lines have been filtered out.
     */
    public final static ILine<String> tryGetFirstAcceptedLine(final String content,
            final ILineFilter lineFilterOrNull)
    {
        final LineSplitter splitter = new LineSplitter(content, lineFilterOrNull);
        try
        {
            return splitter.tryNextLine();
        } finally
        {
            splitter.close();
        }
    }

    /**
     * Returns the first <code>Line</code> that is not filtered out by given <code>ILineFilter</code>.
     * <p>
     * You should not call this method if given <var>file</var> does not exist.
     * </p>
     * 
     * @param lineFilterOrNull could be <code>null</code>. In this case, the {@link AlwaysAcceptLineFilter} implementation will be used.
     * @param file the file that is going to be analyzed. Can not be <code>null</code> and must exists.
     * @return <code>null</code> if all lines have been filtered out.
     */
    public final static ILine<String> tryGetFirstAcceptedLine(final File file,
            final ILineFilter lineFilterOrNull)
    {
        final LineSplitter splitter = new LineSplitter(file, lineFilterOrNull);
        try
        {
            return splitter.tryNextLine();
        } finally
        {
            splitter.close();
        }
    }
}
