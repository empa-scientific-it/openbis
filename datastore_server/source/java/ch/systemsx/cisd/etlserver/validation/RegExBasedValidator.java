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

    RegExBasedValidator(boolean allowEmptyValues, String regularExpression)
    {
        super(allowEmptyValues);
        pattern = Pattern.compile(regularExpression);
    }

    @Override
    protected void assertValidNonEmptyValue(String value)
    {
        if (isValidHeader(value) == false)
        {
            throw new UserFailureException("'" + value
                    + "' doesn't match the following regular expression: " + pattern);
        }
    }

    public boolean isValidHeader(String header)
    {
        return pattern.matcher(header).matches();
    }

}
