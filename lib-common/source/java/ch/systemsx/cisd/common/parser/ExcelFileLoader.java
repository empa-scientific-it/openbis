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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.common.parser.filter.AlwaysAcceptLineFilter;
import ch.systemsx.cisd.common.parser.filter.ILineFilter;

/**
 * Convenient class to load (or iterate over) a tab file, a reader or a stream. The loader delivers either a list or an iterator of beans of type
 * <code>T</code>. The following formats for the column headers are recognized.
 * <ol>
 * <li>Column headers in first line:
 * 
 * <pre>
 *     column1  column2 column2
 * </pre>
 * <li>Comment section:
 * 
 * <pre>
 *     # 1. line of comment
 *     # 2. line of comment
 *     # ...
 *     column1  column2 column2
 * </pre>
 * <li>Column headers at the end of the comment section:
 * 
 * <pre>
 *     # 1. line of comment
 *     # 2. line of comment
 *     # ...
 *     #
 *     #column1  column2 column2
 * </pre>
 * 
 * </ol>
 * 
 * @author Franz-Josef Elmer
 */
public class ExcelFileLoader<T>
{
    public static final String COMMENT_PREFIX = "#";

    // Excel can put comments in double quotes (") if they contain quote character. Multiple saving
    // of the can cause multiple '"' to occure before the '#' character.
    public static final Pattern COMMENT_REGEXP_PATTERN = Pattern.compile("\"*#.*");

    public static final String DEFAULT_SECTION = "[DEFAULT]";

    private final IParserObjectFactoryFactory<T> factory;

    /**
     * Creates a new instance based on the factory which uses only bean annotations.
     */
    public ExcelFileLoader(final Class<T> beanClass)
    {
        this.factory = new IParserObjectFactoryFactory<T>()
            {
                @Override
                public IParserObjectFactory<T> createFactory(IPropertyMapper propertyMapper)
                        throws ParserException
                {
                    return new AbstractParserObjectFactory<T>(beanClass, propertyMapper)
                        {
                        };
                }
            };
    }

    /**
     * Creates a new instance based on the specified factory.
     */
    public ExcelFileLoader(final IParserObjectFactoryFactory<T> factory)
    {
        assert factory != null : "Undefined factory";
        this.factory = factory;
    }

    /**
     * Loads data from the specified reader.
     * <p>
     * The header can contain comments which are ignored. The column names can be the first uncommented line or the last commented line. The latter
     * case is determined by the fact, that the one before the last line is a single hash.
     * </p>
     */
    public List<T> load(final Sheet sheet, int begin, int end, Map<String, String> defaults)
            throws ParserException, ParsingException, IllegalArgumentException
    {
        assert sheet != null : "Unspecified reader";

        final Iterator<ILine<Row>> rowIterator = createRowIterator(sheet, begin, end);
        return load(rowIterator, defaults);
    }

    public static final Map<String, String> parseDefaults(Sheet sheet, int begin, int end)
    {
        final Iterator<ILine<Row>> lineIterator = createRowIterator(sheet, begin, end);
        final Map<String, String> defaults = new HashMap<String, String>();
        return parseDefaults(lineIterator, defaults);
    }

    private static final Map<String, String> parseDefaults(final Iterator<ILine<Row>> rowIterator,
            final Map<String, String> defaults)
    {
        while (rowIterator.hasNext())
        {
            ILine<Row> row = rowIterator.next();
            if (startsDefaultSection(row))
            {
                break;
            }

            String[] tokens = ExcelRowTokenizer.tokenizeRow(row.getObject());
            if (tokens.length >= 2)
            {
                String name = tokens[0];
                String value = tokens[1];
                defaults.put(name.toLowerCase(), value);
            }
        }

        return defaults;
    }

    private List<T> load(final Iterator<ILine<Row>> lineIterator, Map<String, String> fileDefaults)
    {
        ILine<Row> previousLine = null;
        ILine<Row> line = null;
        ILine<Row> headerLine = null;
        ILine<Row> firstContentLine = null;

        Map<String, String> defaults = new HashMap<String, String>(fileDefaults);

        while (lineIterator.hasNext())
        {
            if (previousLine != null && isEmptyComment(previousLine) && line != null
                    && !isEmptyComment(line))
            {
                headerLine = line;
            }

            previousLine = line;
            line = lineIterator.next();

            if (isComment(line))
            {
                continue;
            }
            if (startsDefaultSection(line))
            {
                parseDefaults(lineIterator, defaults);
            } else
            {
                if (headerLine == null)
                {
                    headerLine = line;
                } else
                {
                    firstContentLine = line;
                }
                break;
            }
        }

        if (line == null) // no lines present
        {
            return new ArrayList<T>();
        }

        final IParser<T, Row> parser = createParser();
        final String[] tokens =
                headerLine != null ? ExcelRowTokenizer.tokenizeRow(trimComment(headerLine))
                        : new String[0];
        final int headerLength = tokens.length;
        notUnique(tokens);

        final IPropertyMapper propertyMapper = new DefaultPropertyMapper(tokens, defaults);
        parser.setObjectFactory(factory.createFactory(propertyMapper));

        Iterator<ILine<Row>> contentLineIterator =
                createContentIterator(firstContentLine, lineIterator);
        final ILineFilter filter = AlwaysAcceptLineFilter.INSTANCE;
        return parser.parse(contentLineIterator, filter, headerLength);
    }

    private static boolean startsDefaultSection(Row row)
    {
        Cell cell = row.getCell(0);
        if (cell == null || cell.getCellType() != Cell.CELL_TYPE_STRING)
        {
            return false;
        } else
        {
            return DEFAULT_SECTION.equals(cell.getStringCellValue().trim());
        }
    }

    private static boolean startsDefaultSection(ILine<Row> row)
    {
        return startsDefaultSection(row.getObject());
    }

    private static Row trimComment(ILine<Row> previousLine)
    {
        if (isComment(previousLine))
        {
            Cell firstCell = previousLine.getObject().getCell(0);
            firstCell.setCellValue(firstCell.getStringCellValue()
                    .substring(COMMENT_PREFIX.length()));
        }
        return previousLine.getObject();
    }

    private static boolean isComment(ILine<Row> line)
    {
        Row row = line.getObject();
        return row.getCell(0) != null && row.getCell(0).toString().startsWith(COMMENT_PREFIX);
    }

    private static boolean isEmptyComment(ILine<Row> line)
    {
        if (isComment(line))
        {
            Row row = trimComment(line);
            return row.getCell(0) == null || row.getCell(0).getStringCellValue() == null
                    || row.getCell(0).getStringCellValue().trim().length() == 0;
        } else
        {
            return false;
        }
    }

    /**
     * @param firstContentLineOrNull if not null, it will be returned as the first iterator element, followed by all iterator elements from the second
     *            parameter
     */
    private static Iterator<ILine<Row>> createContentIterator(
            final ILine<Row> firstContentLineOrNull, final Iterator<ILine<Row>> lineIterator)
    {
        return new Iterator<ILine<Row>>()
            {
                private ILine<Row> firstLineOrNull = firstContentLineOrNull;

                @Override
                public boolean hasNext()
                {
                    return firstLineOrNull != null || lineIterator.hasNext();
                }

                @Override
                public ILine<Row> next()
                {
                    return nextLine();
                }

                private ILine<Row> nextLine()
                {
                    if (firstLineOrNull != null)
                    {
                        ILine<Row> line = firstLineOrNull;
                        firstLineOrNull = null;
                        return line;
                    } else
                    {
                        return lineIterator.next();
                    }
                }

                @Override
                public void remove()
                {
                    throw new NotImplementedException();
                }
            };
    }

    private static Iterator<ILine<Row>> createRowIterator(Sheet sheet, int begin, int end)
    {
        return new ExcelFileRowIterator(sheet, begin, end);
    }

    /**
     * Checks given <var>tokens</var> whether there is no duplicate.
     * <p>
     * Note that the search is case-insensitive.
     * </p>
     * 
     * @throws IllegalArgumentException if there is at least one duplicate in the given <var>tokens</var>.
     */
    private final static void notUnique(final String[] tokens)
    {
        assert tokens != null : "Given tokens can not be null.";
        final Set<String> unique = new HashSet<String>();
        for (final String token : tokens)
        {
            if (unique.add(token.toLowerCase()) == false)
            {
                throw new IllegalArgumentException(String.format("Duplicated column name '%s': %s",
                        token, Arrays.asList(tokens)));
            }
        }
    }

    private final <E> IParser<E, Row> createParser()
    {
        ExcelRowTokenizer tokenizer = new ExcelRowTokenizer();
        return new DefaultParser<E, Row>(tokenizer);
    }

    //
    // Helper classes
    //
    private final static class ExcelFileRowIterator implements Iterator<ILine<Row>>
    {
        private final Sheet sheet;

        private int current;

        private final int end;

        private ExcelFileRowIterator(Sheet sheet, int begin, int end)
        {
            this.sheet = sheet;
            this.current = begin;
            this.end = end;
            getFirstNonEmptyCurrent();
        }

        private void getFirstNonEmptyCurrent()
        {
            while (sheet.getRow(current) == null && current <= end)
            {
                current++;
            }
        }

        @Override
        public final void remove()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public final ILine<Row> next()
        {
            try
            {
                return new ExcelRow(sheet.getRow(current));
            } finally
            {
                current++;
                getFirstNonEmptyCurrent();
            }
        }

        @Override
        public final boolean hasNext()
        {
            return current <= end;
        }
    }
}