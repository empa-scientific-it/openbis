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

package ch.systemsx.cisd.common.parser;

/**
 * A <code>ParsingException</code> for column size mismatching between the header columns and line columns.
 * 
 * @author Christian Ribeaud
 */
public final class ColumnSizeMismatchException extends ParsingException
{

    private static final long serialVersionUID = 1L;

    private final int headerLength;

    private final String message;

    public ColumnSizeMismatchException(final String[] tokens, final int lineNumber,
            final int headerLength)
    {
        super(tokens, lineNumber);
        assert tokens.length != headerLength : "Tokens length and header length must be different (otherwise no reason to throw this exception).";
        this.headerLength = headerLength;
        this.message = createMessage();
    }

    private final String createMessage()
    {
        final String[] tokens = getTokens();
        final String moreLessStr = getMoreOrLessString(tokens, headerLength);
        final StringBuilder lineStructure = new StringBuilder();
        for (int i = 0; i < tokens.length; i++)
        {
            lineStructure.append(tokens[i]);
            if (i + 1 < tokens.length)
            {
                lineStructure.append(" <TAB> ");
            }
        }
        lineStructure.append(" <END_OF_LINE>");
        return String.format("Line <%s> has %s columns (%s) than the header (%s):\n  %s",
                getLineNumber(), moreLessStr, String.valueOf(tokens.length), String
                        .valueOf(headerLength), lineStructure.toString());
    }

    private final static String getMoreOrLessString(final String[] tokens, final int headerLength)
    {
        return tokens.length > headerLength ? "more" : "less";
    }

    //
    // ParsingException
    //

    @Override
    public final String getMessage()
    {
        return message;
    }
}
