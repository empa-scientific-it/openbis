/*
 * Copyright 2009 ETH Zuerich, CISD
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

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Helper class for get table data out of a TSV file.
 *
 * @author Franz-Josef Elmer
 */
public class TabSeparatedValueTable
{
    private static final class RowLineIterator implements Iterator<String>
    {
        private final LineIterator lineIterator;

        private final boolean ignoreEmptyLines;

        private final boolean ignoreHashedLines;

        private String currentLine;

        private int currentLineNumber;

        RowLineIterator(LineIterator lineIterator, boolean ignoreEmptyLines, boolean ignoreHashedLines)
        {
            this.lineIterator = lineIterator;
            this.ignoreEmptyLines = ignoreEmptyLines;
            this.ignoreHashedLines = ignoreHashedLines;

        }

        @Override
        public boolean hasNext()
        {
            if (currentLine == null)
            {
                currentLine = getNextLine();
            }
            return currentLine != null;
        }

        @Override
        public String next()
        {
            if (currentLine == null)
            {
                currentLine = getNextLine();
            }
            try
            {
                return currentLine;
            } finally
            {
                currentLine = null;
            }
        }

        public final int getCurrentLineNumber()
        {
            return currentLineNumber;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        private String getNextLine()
        {
            while (true)
            {
                if (lineIterator.hasNext() == false)
                {
                    return null;
                }
                String line = lineIterator.nextLine();
                currentLineNumber++;
                if ((ignoreEmptyLines == false || line == null || StringUtils.isNotBlank(line))
                        && (ignoreHashedLines == false || line == null || line.startsWith("#") == false))
                {
                    return line;
                }
            }
        }
    }

    private final RowLineIterator rowLineIterator;

    private final List<String> headers;

    private final boolean strictRowSize;

    /**
     * Creates a new instance. Short cut for <code>new TabSeparatedValueTable(reader, nameOfReadingSource, ignoreEmptyLines, false)</code>.
     */
    public TabSeparatedValueTable(Reader reader, String nameOfReadingSource,
            boolean ignoreEmptyLines)
    {
        this(reader, nameOfReadingSource, ignoreEmptyLines, false, false);
    }

    /**
     * Creates an instance for the specified reader. The constructor already reads the header line. It will be immediately available via
     * {@link #getHeaders()}.
     * 
     * @param reader Reader pointing to the header line of the table.
     * @param nameOfReadingSource Source (usually file name) from which the table will be read. This is used for error messages only.
     * @param ignoreEmptyLines If <code>true</code> lines with only white spaces will be ignored.
     * @param strictRowSize If <code>true</code> the number of row cells have to be equal the number of headers and trailing tabs in the header and
     *            rows are not allowed.
     * @param ignoreHashedLines If <code>true</code> lines starting with '#' will be ignored.
     */
    public TabSeparatedValueTable(Reader reader, String nameOfReadingSource,
            boolean ignoreEmptyLines, boolean strictRowSize, boolean ignoreHashedLines)
    {
        this.strictRowSize = strictRowSize;
        rowLineIterator = new RowLineIterator(IOUtils.lineIterator(reader), ignoreEmptyLines, ignoreHashedLines);
        if (rowLineIterator.hasNext() == false)
        {
            throw new IllegalArgumentException("Empty file '" + nameOfReadingSource + "'.");
        }
        String headerLine = rowLineIterator.next();
        if (strictRowSize)
        {
            int countTrailingTabs = 0;
            for (int i = headerLine.length() - 1; i >= 0; i--)
            {
                if (headerLine.charAt(i) != '\t')
                {
                    break;
                }
                countTrailingTabs++;
            }
            if (countTrailingTabs > 0)
            {
                throw new UserFailureException(countTrailingTabs
                        + " trailing tab characters detected in headers line: '" + headerLine + "'.");
            }
        }
        headers = getRowCells(headerLine);
    }

    /**
     * Returns header line.
     */
    public List<String> getHeaders()
    {
        return headers;
    }

    /**
     * Returns all columns. This method returns only rows which are not consumed by previous invocations of {@link #tryToGetNextRow()}.
     */
    public List<Column> getColumns()
    {
        TableBuilder builder = new TableBuilder(headers);
        while (hasMoreRows())
        {
            List<String> row = tryToGetNextRow();
            builder.addRow(row);
        }
        return builder.getColumns();
    }

    /**
     * Returns <code>true</code> if more rows available. This method returns always <code>false</code> if {@link #getColumns()} has already been
     * invoked.
     */
    public boolean hasMoreRows()
    {
        return rowLineIterator.hasNext();
    }

    /**
     * Returns the next row as a list of its cell values. Missing cells are returned as empty strings. The size of returned list is the size of the
     * header list.
     * 
     * @return <code>null</code> if there are no more rows.
     */
    public List<String> tryToGetNextRow()
    {
        String line = rowLineIterator.next();
        if (line == null)
        {
            return null;
        }
        List<String> row = getRowCells(line);
        if (strictRowSize && row.size() != headers.size())
        {
            throw new UserFailureException(rowLineIterator.getCurrentLineNumber() - 1
                    + ". data row has " + row.size() + " instead of " + headers.size() + " cells.");
        }
        for (int i = row.size(); i < headers.size(); i++)
        {
            row.add("");
        }
        return row;
    }

    private List<String> getRowCells(String line)
    {
        String[] cells = StringUtils.splitByWholeSeparatorPreserveAllTokens(line, "\t");
        if (cells == null)
        {
            cells = new String[0];
        }
        List<String> row = new ArrayList<String>();
        row.addAll(Arrays.asList(cells));
        return row;
    }

}
