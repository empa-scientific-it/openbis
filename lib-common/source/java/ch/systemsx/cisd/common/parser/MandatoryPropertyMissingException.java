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
 * A <code>ParserException</code> extension which signalizes missing of a mandatory property.
 * 
 * @author Christian Ribeaud
 */
public final class MandatoryPropertyMissingException extends ParserException
{
    private static final String MISSING_PROPERTY_COLUMN =
            "Mandatory column(s) %s are missing (mandatory columns are %s).";

    private static final String MISSING_PROPERTY_VALUE =
            "Missing value for the mandatory column '%s'.";

    private static final long serialVersionUID = 1L;

    public MandatoryPropertyMissingException(final String propertyField)
    {
        super(String.format(MISSING_PROPERTY_VALUE, propertyField));
    }

    public MandatoryPropertyMissingException(final Set<String> mandatoryFields,
            final Set<String> missingMandatoryProperties)
    {
        super(createMessage(missingMandatoryProperties, mandatoryFields));
    }

    private final static String createMessage(final Set<String> missingMandatoryProperties,
            Set<String> mandatoryFields)
    {
        assert missingMandatoryProperties != null : "Missing mandatory properties can not be null.";
        assert missingMandatoryProperties.size() > 0 : "No reason to throw this exception.";
        return String.format(MISSING_PROPERTY_COLUMN, toString(missingMandatoryProperties),
                toString(mandatoryFields));
    }

    final static String toString(final Set<String> set)
    {
        return CollectionUtils.abbreviate(set, -1, CollectionStyle.SINGLE_QUOTE_BOUNDARY);
    }

}
