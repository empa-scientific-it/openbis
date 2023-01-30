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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import java.util.ArrayList;
import java.util.List;

/**
 * Useful static methods for expressions.
 * 
 * @author Franz-Josef Elmer
 */
public class ExpressionUtil
{
    public static final String START = "${";

    public static final String END = "}";

    private static final int START_LENGTH = START.length();

    /**
     * Extracts list of all parameters in the specified expression
     */
    public static List<String> extractParameters(String expression)
    {
        List<String> list = new ArrayList<String>();
        int index = 0;
        while (true)
        {
            int indexOfStart = expression.indexOf(START, index);
            if (indexOfStart < 0)
            {
                break;
            }
            indexOfStart += START_LENGTH;
            int indexOfEnd = expression.indexOf(END, indexOfStart);
            if (indexOfEnd < 0)
            {
                break;
            }
            list.add(expression.substring(indexOfStart, indexOfEnd));
            index = indexOfEnd + 1;
        }
        return list;
    }

    /**
     * Returns a list which contains only distinct members of the specified list.
     */
    public static List<String> createDistinctParametersList(List<String> allParameters)
    {
        List<String> result = new ArrayList<String>();
        for (String parameter : allParameters)
        {
            if (result.contains(parameter) == false)
            {
                result.add(parameter);
            }
        }
        return result;
    }

    private ExpressionUtil()
    {
    }

}
