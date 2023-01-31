/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Checks uniqueness on selected groups of values matching given regular expression.
 * 
 * @author Izabela Adamczyk
 */
class UniqueGroupValidator extends AbstractValidator
{
    static final int MIN_GROUP = 1;

    private final Pattern pattern;

    private final List<Integer> groups;

    private final UniquenessChecker checker;

    UniqueGroupValidator(String regularExpression, List<Integer> groups)
    {
        super(false, Collections.<String> emptySet());
        this.groups = groups;
        checker = new UniquenessChecker(groups.size());
        pattern = Pattern.compile(regularExpression);
    }

    @Override
    protected void assertValidNonEmptyValue(String value)
    {
        Matcher m = pattern.matcher(value);
        List<String> list = new ArrayList<String>();
        if (m.find())
        {
            for (int g : groups)
            {
                if (g <= m.groupCount() || g > MIN_GROUP)
                {
                    list.add(m.group(g));
                } else
                {
                    String message =
                            String.format("Group '%s' not found. Value: '%s', pattern: '%s'", g,
                                    value, pattern.pattern());
                    throw new UserFailureException(message);
                }
            }
            Result result = checker.check(list, value);
            if (result.isValid() == false)
            {
                throw new UserFailureException(result.toString());
            }
        } else
        {
            throw new UserFailureException(String.format(
                    "Value '%s' does not match the pattern '%s'", value, pattern.pattern()));

        }
    }

    private static class UniquenessChecker
    {
        Set<List<String>> values;

        private final int expectedSize;

        public UniquenessChecker(int expectedSize)
        {
            this.expectedSize = expectedSize;
            values = new HashSet<List<String>>();
        }

        public Result check(List<String> list, String oryginalValue)
        {
            if (list.size() != expectedSize)
            {
                String message =
                        String.format("Wrong number of elements (%s instead of %s)", list.size(),
                                expectedSize);
                return Result.failure(message);
            }
            if (values.contains(list))
            {
                String message =
                        String.format("Record '%s' breaks group uniqueness (repeated group: '%s')",
                                oryginalValue, list);
                return Result.failure(message);
            }
            values.add(list);
            return Result.OK;
        }
    }

}
