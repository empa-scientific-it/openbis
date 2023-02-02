/*
 * Copyright 2015 ETH Zuerich, CISD
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author pkupczyk
 */
public class PercentFormatter
{

    private static final Pattern pattern = Pattern.compile("([0-9]+)\\s*\\%");

    public static int parse(String percent)
    {
        Matcher m = pattern.matcher(percent);

        if (m.matches())
        {
            return Integer.valueOf(m.group(1));
        } else
        {
            throw new IllegalArgumentException("Could not parse percent value: " + percent);
        }
    }

    public static String format(int percent)
    {
        if (percent < 0)
        {
            throw new IllegalArgumentException("Percent value has to greater than or equal to 0");
        }
        return percent + "%";
    }

}
