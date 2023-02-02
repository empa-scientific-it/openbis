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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.parser.filter.ILineFilter;

/**
 * A default {@link IParser} implementation.
 * <p>
 * The object type returned by this implementation is generic. This implementation defines a <code>ILineFilter</code> that filters out comment and
 * empty lines.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public class DefaultParser<E, T> implements IParser<E, T>
{
    private final ILineTokenizer<T> lineTokenizer;

    private IParserObjectFactory<E> factory;

    /**
     * Creates an instance based on the {@link DefaultLineTokenizer}.
     */
    public static <E> DefaultParser<E, String> createDefaultParser()
    {
        return new DefaultParser<E, String>(new DefaultLineTokenizer());
    }

    /**
     * Creates an instance for the specified line tokenizer.
     */
    public DefaultParser(final ILineTokenizer<T> lineTokenizer)
    {
        this.lineTokenizer = lineTokenizer;
    }

    protected E createObject(final String[] tokens) throws ParserException
    {
        return factory.createObject(tokens);
    }

    @Override
    public final List<E> parse(final Iterator<ILine<T>> lineIterator, final ILineFilter lineFilter,
            final int headerLength) throws ParsingException
    {
        final List<E> elements = new ArrayList<E>();
        lineTokenizer.init();
        while (lineIterator.hasNext())
        {
            final ILine<T> line = lineIterator.next();
            final T nextLine = line.getObject();
            final int number = line.getNumber();
            if (lineFilter.acceptLine(line))
            {
                E object = null;
                String[] tokens = parseLine(number, nextLine, headerLength);
                // skip empty lines
                if (false == areAllTokensBlank(tokens))
                {
                    try
                    {
                        object = createObject(tokens);
                    } catch (final ParserException parserException)
                    {
                        throw new ParsingException(parserException, tokens, number);
                    }
                    // Skip null values
                    if (null != object)
                    {
                        elements.add(object);
                    }
                }
            }
        }
        lineTokenizer.destroy();
        return elements;
    }

    private boolean areAllTokensBlank(String[] tokens)
    {
        for (String token : tokens)
        {
            if (false == StringUtils.isBlank(token))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public final Iterator<E> parseIteratively(final Iterator<ILine<T>> lineIterator,
            final ILineFilter lineFilter, final int headerLength) throws ParsingException
    {
        lineTokenizer.init();
        return new Iterator<E>()
            {
                ILine<T> currentLine = null;

                @Override
                public boolean hasNext()
                {
                    boolean hasNext = lineIterator.hasNext();
                    while (hasNext)
                    {
                        currentLine = lineIterator.next();
                        if (lineFilter.acceptLine(currentLine))
                        {
                            break;
                        }
                        hasNext = lineIterator.hasNext();
                    }
                    if (hasNext == false)
                    {
                        currentLine = null;
                        lineTokenizer.destroy();
                    }
                    return hasNext;
                }

                @Override
                public E next()
                {
                    if (currentLine == null && hasNext() == false)
                    {
                        throw new NoSuchElementException();
                    }
                    final T nextLine = currentLine.getObject();
                    final int number = currentLine.getNumber();
                    currentLine = null;
                    String[] tokens = parseLine(number, nextLine, headerLength);
                    try
                    {
                        return createObject(tokens);
                    } catch (final ParserException parserException)
                    {
                        throw new ParsingException(parserException, tokens, number);
                    }
                }

                @Override
                public void remove()
                {
                    throw new UnsupportedOperationException();
                }

            };
    }

    @Override
    public final void setObjectFactory(final IParserObjectFactory<E> factory)
    {
        this.factory = factory;
    }

    private String[] parseLine(final int lineNumber, final T nextLine, final int headerLength)
    {
        String[] tokens = lineTokenizer.tokenize(nextLine);
        if (tokens.length > headerLength)
        {
            throw new ColumnSizeMismatchException(tokens, lineNumber, headerLength);
        }
        if (tokens.length < headerLength)
        {
            String[] newTokens = new String[headerLength];
            System.arraycopy(tokens, 0, newTokens, 0, tokens.length);
            for (int i = tokens.length; i < headerLength; i++)
            {
                newTokens[i] = "";
            }
            tokens = newTokens;
        }
        return tokens;
    }
}
