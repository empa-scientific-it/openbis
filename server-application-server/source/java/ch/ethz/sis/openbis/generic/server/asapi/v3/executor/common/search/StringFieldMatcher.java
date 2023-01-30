/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AnyStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringContainsValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringEndsWithValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringStartsWithValue;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;

public abstract class StringFieldMatcher<OBJECT> extends Matcher<OBJECT>
{

    @Override
    public List<OBJECT> getMatching(IOperationContext context, List<OBJECT> objects, ISearchCriteria criteria)
    {
        AbstractStringValue searchValueObject = ((StringFieldSearchCriteria) criteria).getFieldValue();

        if (searchValueObject == null || searchValueObject.getValue() == null || searchValueObject instanceof AnyStringValue)
        {
            return objects;
        }

        String searchValue = searchValueObject.getValue().toLowerCase();
        Pattern equalsPattern = createPattern(searchValue);
        Pattern startsPattern = createPattern(searchValue + "*");
        Pattern endsPattern = createPattern("*" + searchValue);
        Pattern containsPattern = createPattern("*" + searchValue + "*");

        List<OBJECT> matches = new ArrayList<OBJECT>();

        for (OBJECT object : objects)
        {
            String actualValue = getFieldValue(object);

            if (actualValue == null)
            {
                actualValue = "";
            } else
            {
                actualValue = actualValue.toLowerCase();
            }

            boolean match;

            if (searchValueObject instanceof StringEqualToValue)
            {
                match = equalsPattern.matcher(actualValue).matches();
            } else if (searchValueObject instanceof StringContainsValue)
            {
                match = containsPattern.matcher(actualValue).matches();
            } else if (searchValueObject instanceof StringStartsWithValue)
            {
                match = startsPattern.matcher(actualValue).matches();
            } else if (searchValueObject instanceof StringEndsWithValue)
            {
                match = endsPattern.matcher(actualValue).matches();
            } else
            {
                throw new IllegalArgumentException("Unknown string value: " + criteria.getClass());
            }

            if (match)
            {
                matches.add(object);
            }
        }

        return matches;
    }

    // Logic:
    //
    // - replace '*' (matches any characters) and '?' (matches exactly one character) wild cards with appropriate regexp wild cards
    // - quote all other characters from the searched value to be treated as a normal text (even if they are regarded special in regexp world)
    //
    // Examples:
    //
    // abc => \Qabc\E
    // *abc => .*\Qabc\E
    // ?abc => .\Qabc\E
    // .abc => \Q.abc\E
    // *ab?c?d => .*\Qab\E.\Qc\E.\Qd\E
    //

    private static Pattern createPattern(String searchValue)
    {
        StringBuilder pattern = new StringBuilder();
        StringBuilder part = new StringBuilder();

        for (char c : searchValue.toCharArray())
        {
            if (c == '*' || c == '?')
            {
                if (part.length() > 0)
                {
                    pattern.append(Pattern.quote(part.toString()));
                    part = new StringBuilder();
                }
                if (c == '*')
                {
                    pattern.append(".*");
                } else
                {
                    pattern.append(".");
                }
            } else
            {
                part.append(c);
            }
        }

        if (part.length() > 0)
        {
            pattern.append(Pattern.quote(part.toString()));
        }

        return Pattern.compile(pattern.toString());
    }

    protected abstract String getFieldValue(OBJECT object);

}