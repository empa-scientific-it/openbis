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

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Validator based on a regular expression.
 *
 * @author Franz-Josef Elmer
 */
class RegExBasedValidator extends AbstractValidator implements IColumnHeaderValidator
{
    private final Pattern pattern;

    RegExBasedValidator(String regularExpression)
    {
        this(false, Collections.<String> emptySet(), regularExpression);
    }

    RegExBasedValidator(boolean allowEmptyValues, Set<String> emptyValueSynonyms,
            String regularExpression)
    {
        super(allowEmptyValues, emptyValueSynonyms);
        pattern = Pattern.compile(regularExpression);
    }

    @Override
    protected void assertValidNonEmptyValue(String value)
    {
        Result result = validateHeader(value);
        if (result.isValid() == false)
        {
            throw new UserFailureException("'" + value
                    + "' is invalid: " + result);
        }
    }

    @Override
    public Result validateHeader(String header)
    {
        if (pattern.matcher(header).matches())
        {
            return Result.OK;
        }
        return Result.failure("Does not match the following regular expression: " + pattern);
    }

}
