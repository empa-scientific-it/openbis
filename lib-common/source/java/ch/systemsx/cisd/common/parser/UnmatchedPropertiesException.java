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

import java.util.Set;

import ch.systemsx.cisd.common.collection.CollectionStyle;
import ch.systemsx.cisd.common.collection.CollectionUtils;

/**
 * A <code>ParserException</code> extension which signalizes unmatched properties.
 * 
 * @author Christian Ribeaud
 */
public final class UnmatchedPropertiesException extends ParserException
{
    private static final long serialVersionUID = 1L;

    public UnmatchedPropertiesException(final Set<String> mandatoryNames,
            final Set<String> optionalNames, final Set<String> propertyNames)
    {
        super(createMessage(propertyNames, mandatoryNames, optionalNames));
    }

    private final static String createMessage(Set<String> propertyNames,
            Set<String> mandatoryNames, Set<String> optionalNames)
    {
        assert propertyNames != null : "Property names can not be null.";
        assert propertyNames.size() > 0 : "There is no reason to throw this exception.";
        final StringBuilder builder = new StringBuilder();
        builder.append("Columns ").append(toString(propertyNames)).append(
                " specified in the header are not expected (");
        final boolean hasMandatory = mandatoryNames.size() > 0;
        if (hasMandatory)
        {
            builder.append("mandatory colums are ");
            builder.append(toString(mandatoryNames));
        }
        if (optionalNames.size() > 0)
        {
            if (hasMandatory)
            {
                builder.append(", ");
            }
            builder.append("optional colums are ");
            builder.append(toString(optionalNames));
        }
        builder.append(")");
        return builder.toString();

    }

    private final static String toString(final Set<String> set)
    {
        return CollectionUtils.abbreviate(set, -1, CollectionStyle.SINGLE_QUOTE_BOUNDARY);
    }

}
