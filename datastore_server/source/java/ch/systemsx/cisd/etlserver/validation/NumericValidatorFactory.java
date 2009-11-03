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

import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class NumericValidatorFactory implements IValidatorFactory
{
    static final String VALUE_RANGE_KEY = "value-range";

    private static final class Range
    {
        private double minimum;
        private boolean minimumIncluded;
        private double maximum;
        private boolean maximumIncluded;
        
        Range(String rangeDescription)
        {
            if (rangeDescription.length() < 3)
            {
                throw new ConfigurationFailureException("Invalid range: " + rangeDescription);
            }
            char firstCharacter = rangeDescription.charAt(0);
            if (firstCharacter == '(')
            {
                minimumIncluded = false;
            } else if (firstCharacter == '[')
            {
                minimumIncluded = true;
            } else
            {
                throw new ConfigurationFailureException(
                        "Range has to start with either '(' or '[': " + rangeDescription);
            }
            char lastCharacter = rangeDescription.charAt(rangeDescription.length() - 1);
            if (lastCharacter == ')')
            {
                maximumIncluded = false;
            } else if (lastCharacter == ']')
            {
                maximumIncluded = true;
            } else
            {
                throw new ConfigurationFailureException("Range has to end with either ')' or ']': "
                        + rangeDescription);
            }
            int indexOfComma = rangeDescription.indexOf(',');
            if (indexOfComma < 0)
            {
                throw new ConfigurationFailureException("Missing comma in range definition: "
                        + rangeDescription);
            }
            try
            {
                minimum = new Double(rangeDescription.substring(1, indexOfComma));
            } catch (NumberFormatException ex)
            {
                throw new ConfigurationFailureException("Invalid minimum in range definition: "
                        + rangeDescription);
            }
            try
            {
                maximum =
                        new Double(rangeDescription.substring(indexOfComma + 1, rangeDescription
                                .length() - 1));
            } catch (NumberFormatException ex)
            {
                throw new ConfigurationFailureException("Invalid maximum in range definition: "
                        + rangeDescription);
            }
        }

        void assertInRange(double number)
        {
            if (number < minimum || (minimumIncluded == false && number == minimum))
            {
                throw new UserFailureException("Number to small: " + number
                        + (minimumIncluded ? " < " : " <= ") + minimum);
            }
            if (number > maximum || (maximumIncluded == false && number == maximum))
            {
                throw new UserFailureException("Number to large: " + number
                        + (maximumIncluded ? " > " : " >= ") + maximum);
            }
        }
    }
    
    private final static class NumericValidator implements IValidator
    {
        private final Range rangeOrNull;
        
        NumericValidator(Range rangeOrNull)
        {
            this.rangeOrNull = rangeOrNull;
        }
        
        public void assertValid(String value)
        {
            double number = Double.parseDouble(value);
            if (rangeOrNull != null)
            {
                rangeOrNull.assertInRange(number);
            }
        }
        
    }
    
    private NumericValidator validator;
    
    NumericValidatorFactory(Properties properties)
    {
        String valueRange = properties.getProperty(VALUE_RANGE_KEY);
        validator = new NumericValidator(valueRange == null ? null : new Range(valueRange));
    }

    public IValidator createValidator()
    {
        return validator;
    }

}
