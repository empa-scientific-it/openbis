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

import java.util.Arrays;

import ch.systemsx.cisd.common.exceptions.HighLevelException;

/**
 * Signals that an error has been unexpectedly reached while parsing.
 * 
 * @author Christian Ribeaud
 */
public class ParsingException extends HighLevelException
{

    static final String MESSAGE_FORMAT = "Creating an object with following tokens '%s' failed.";

    private static final long serialVersionUID = 1L;

    private final int lineNumber;

    private final String[] tokens;

    public ParsingException(final String[] tokens, final int lineNumber)
    {
        this(null, tokens, lineNumber);
    }

    public ParsingException(final RuntimeException cause, final String[] tokens,
            final int lineNumber)
    {
        super(createMessage(tokens), cause);
        this.lineNumber = lineNumber;
        this.tokens = tokens;
    }

    private final static String createMessage(final String[] tokens)
    {
        assert tokens != null : "Tokens can not be null.";
        return String.format(MESSAGE_FORMAT, Arrays.asList(tokens));
    }

    /**
     * Returns the line where the error was found.
     */
    public final int getLineNumber()
    {
        return lineNumber;
    }

    public final String[] getTokens()
    {
        return tokens;
    }

    public final RuntimeException getCauseRuntimeException()
    {
        return (RuntimeException) getCause();
    }
}
