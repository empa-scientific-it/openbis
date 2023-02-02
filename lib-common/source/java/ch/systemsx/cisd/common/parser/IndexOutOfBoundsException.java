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

import ch.systemsx.cisd.common.collection.CollectionStyle;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.string.StringUtilities;

/**
 * A <code>ParserException</code> extension which signalizes a lookup index outside of the currently available tokens.
 * 
 * @author Christian Ribeaud
 */
public final class IndexOutOfBoundsException extends ParserException
{
    private static final String MESSAGE_FORMAT =
            "Not enough columns available. Looking for %s column but we have only %d columns (%s).";

    private static final long serialVersionUID = 1L;

    public IndexOutOfBoundsException(final int index, final String[] lineTokens)
    {
        super(createMessage(index, lineTokens));
    }

    private final static String createMessage(final int index, final String[] lineTokens)
    {
        assert lineTokens != null : "Line tokens can not be null.";
        assert index >= lineTokens.length : "Index must be out of range (otherwise no reason "
                + "to call this exception).";
        return String.format(MESSAGE_FORMAT, StringUtilities.getOrdinal(index + 1),
                lineTokens.length, CollectionUtils.abbreviate(lineTokens, -1,
                        CollectionStyle.SINGLE_QUOTE_BOUNDARY));
    }

}
