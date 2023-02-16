/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.xls.importer.helper;

import ch.ethz.sis.openbis.generic.server.xls.importer.utils.IAttribute;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractImportHelper
{
    protected Map<String, Integer> parseHeader(List<String> header, boolean needValidation)
    {
        Map<String, Integer> headerMap = new HashMap<>();

        for (int i = 0; i < header.size(); ++i)
        {
            /*
             * Header rows can contain values that do not have corresponding elements in the properties row (can be left empty).
             * Empty columns are allowed between each attribute. However, it is possible that this feature might be disabled in future.
             * Source - https://unlimited.ethz.ch/display/openBISDoc2010/Excel+Import+Service
             */
            if (header.get(i) != null && !header.get(i).trim().isEmpty())
            {
                // The case of letters in header rows is ignored.
                String key = header.get(i).trim();
                if (headerMap.containsKey(key))
                {
                    throw new UserFailureException("Repeated headers are not allowed.");
                }
                headerMap.put(key, i);
            }
        }

        // may throw an exception, if header contains errors
        if (needValidation)
        {
            validateHeader(headerMap);
        }

        return headerMap;
    }

    // If attribute does not exist, it will be ignored.
    // Source - https://unlimited.ethz.ch/display/openBISDoc2010/Excel+Import+Service
    protected abstract void validateHeader(Map<String, Integer> header);

    // This is a non-mandatory method. If the helper needs to validate the line before executing, override.
    protected void validateLine(Map<String, Integer> header, List<String> values) {

    }

    protected static void checkKeyExistence(Map<String, Integer> header, String key)
    {
        String keyFix = Character.toUpperCase(key.charAt(0)) + key.substring(1);
        if (!header.containsKey(key) && !header.containsKey(keyFix))
        {
            throw new UserFailureException("Header should contain '" + key + "'.");
        }
    }

    protected static String getValueByColumnName(Map<String, Integer> header, List<String> values, IAttribute columnName)
    {
        if (header.containsKey(columnName.getHeaderName()))
        {
            int column = header.get(columnName.getHeaderName());
            if (column < values.size())
            {
                return values.get(column);
            }
        }
        return null;
    }

    protected static String getValueByColumnName(Map<String, Integer> header, List<String> values, String columnName)
    {
        if (header.containsKey(columnName))
        {
            int column = header.get(columnName);
            if (column < values.size())
            {
                return values.get(column);
            }
        }
        return null;
    }
}
