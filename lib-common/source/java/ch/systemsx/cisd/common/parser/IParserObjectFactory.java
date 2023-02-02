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

/**
 * A <code>IParserObjectFactory</code> implementation knows how to deal with given line tokens and convert them into an appropriate
 * <code>Object</code>.
 * <p>
 * A <code>IParserObjectFactory</code> is typically registered in {@link IParser}.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public interface IParserObjectFactory<E>
{

    /**
     * This <code>IParserObjectFactory</code> implementation does nothing and returns the passed <code>lineTokens</code> as <code>String[]</code>.
     * <p>
     * This implementation could be used to debugging purposes.
     * </p>
     * 
     * @author Christian Ribeaud
     */
    public final static IParserObjectFactory<String[]> STRING_ARRAY_OBJECT_FACTORY =
            new IParserObjectFactory<String[]>()
                {

                    //
                    // IParserObjectFactory
                    //

                    @Override
                    public final String[] createObject(final String[] lineTokens)
                            throws ParserException
                    {
                        return lineTokens;
                    }
                };

    /**
     * Parses given text line and returns an appropriate <i>Object</i>. If null is return, the value is skipped. If an error is encountered, throw a
     * ParserException.
     */
    public E createObject(final String[] lineTokens) throws ParserException;
}