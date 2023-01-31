/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.datasource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * @author Franz-Josef Elmer
 */
class DataSourceUtils
{

    static String convertToW3CDate(Date date)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(date) + "Z";
    }

    /**
     * Return the sub set from the full where non of the regexs from the specified black lists matches and at least
     * one regex of the white lists matches. An empty or null whiteLists means that only the check against black lists
     * are done.
     */
    static Set<String> getRequestedAndAllowedSubSet(Collection<String> fullSet, List<String> whiteLists, List<String> blackLists)
    {
        List<Pattern> allowedPatterns = getRegexs(whiteLists);
        List<Pattern> disallowedPatterns = getRegexs(blackLists);
        Set<String> subSet = new LinkedHashSet<>();
        for (String item : fullSet)
        {
            if (matchesARegex(disallowedPatterns, item))
            {
                continue;
            }
            if (allowedPatterns.isEmpty() || matchesARegex(allowedPatterns, item))
            {
                subSet.add(item);
            }
        }
        return subSet;

    }

    private static boolean matchesARegex(List<Pattern> patterns, String item)
    {
        for (Pattern pattern : patterns)
        {
            if (pattern.matcher(item).matches())
            {
                return true;
            }
        }
        return false;
    }

    private static List<Pattern> getRegexs(List<String> lists)
    {
        List<Pattern> regexs = new ArrayList<>();
        if (lists != null)
        {
            for (String commaSeparatedList : lists)
            {
                String[] splitted = commaSeparatedList.split(",");
                for (String string : splitted)
                {
                    regexs.add(Pattern.compile(string.trim()));
                }
            }
        }
        return regexs;
    }
}
