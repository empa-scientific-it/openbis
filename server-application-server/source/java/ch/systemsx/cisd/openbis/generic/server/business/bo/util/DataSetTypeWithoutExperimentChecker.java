/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;

/**
 * Helper class to check data set codes agains a list of regular expressions.
 *
 * @author Franz-Josef Elmer
 */
public class DataSetTypeWithoutExperimentChecker
{
    public static final String PROPERTY_KEY = "data-set-types-with-no-experiment-needed";

    private final String regularExpressions;

    private List<Pattern> patternsForDataSetTypesWithoutExperiment = new ArrayList<Pattern>();

    public DataSetTypeWithoutExperimentChecker(Properties properties)
    {
        this(properties.getProperty(PROPERTY_KEY));
    }

    public DataSetTypeWithoutExperimentChecker(String regularExpressions)
    {
        this.regularExpressions = regularExpressions;
        if (StringUtils.isNotBlank(regularExpressions))
        {
            String[] splittedRegexes = regularExpressions.split(",");
            for (String regex : splittedRegexes)
            {
                try
                {
                    patternsForDataSetTypesWithoutExperiment.add(Pattern.compile(regex.trim()));
                } catch (PatternSyntaxException ex)
                {
                    throw new ConfigurationFailureException("Invalid regular expression in property "
                            + "'" + PROPERTY_KEY + "': " + regex, ex);
                }
            }
        }
    }

    public String getRegularExpressions()
    {
        return regularExpressions;
    }

    public boolean isDataSetTypeWithoutExperiment(String dataSetTypeCode)
    {
        for (Pattern pattern : patternsForDataSetTypesWithoutExperiment)
        {
            if (pattern.matcher(dataSetTypeCode).matches())
            {
                return true;
            }
        }
        return false;
    }

}
