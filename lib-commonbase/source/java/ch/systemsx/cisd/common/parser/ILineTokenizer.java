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
 * A <code>ILineTokenizer</code> implementation is able to split a given <code>T</code> line in an array of tokens.
 * 
 * @author Christian Ribeaud
 */
public interface ILineTokenizer<T>
{

    /**
     * Inits this <code>ILineTokenizer</code>.
     * <p>
     * Should be called before parsing starts.
     * </p>
     */
    public void init();

    /** Splits given <code>line</code> into an array of tokens. */
    public String[] tokenize(T line) throws ParserException;

    /**
     * Cleans up resources used by this <code>ILineTokenizer</code>.
     * <p>
     * Should be called when parsing has finished.
     * </p>
     */
    public void destroy();
}