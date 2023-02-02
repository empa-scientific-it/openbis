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

import java.util.Iterator;
import java.util.List;

import ch.systemsx.cisd.common.parser.filter.ILineFilter;

/**
 * <code>IReaderParser</code> is able to parse a given text lines and to return a list of objects of type <code>E</code>.
 * 
 * @author Christian Ribeaud
 */
public interface IParser<E, T>
{

    /**
     * Parses the lines delivered by the specified <var>lineIterator</var>, creating elements of type <code>E</code>.
     * 
     * @param lineFilter A filter lines have to pass in order to be parsed.
     * @param headerLength number of columns in the header
     * @return a <code>List</code> of elements.
     */
    public List<E> parse(final Iterator<ILine<T>> lineIterator, final ILineFilter lineFilter,
            final int headerLength) throws ParsingException;

    /**
     * Parses the lines delivered by the specified <var>lineIterator</var> iteratively, creating elements of type <code>E</code>.
     * <p>
     * <i>Use this method for parsing of large input data sets in streaming mode.</i>
     * 
     * @param lineFilter A filter lines have to pass in order to be parsed.
     * @param headerLength number of columns in the header
     * @return an <code>List</code> of elements.
     */
    public Iterator<E> parseIteratively(final Iterator<ILine<T>> lineIterator,
            final ILineFilter lineFilter, final int headerLength) throws ParsingException;

    /**
     * Sets the <code>IParserObjectFactory</code>.
     * <p>
     * Typically, the given <code>factory</code> transforms a line into an element.
     * </p>
     */
    public void setObjectFactory(final IParserObjectFactory<E> factory);
}
