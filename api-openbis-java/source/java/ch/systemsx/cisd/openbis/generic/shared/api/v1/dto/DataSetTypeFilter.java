/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A class that filters out data set types based on a whitelist or blacklist.
 * <p>
 * Only one list can be applied. If both the whitelist and blacklist are specified, then the whitelist is used. If neither are specified, then no
 * filtering is done.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetTypeFilter
{
    /**
     * Takes a List of patterns and serializes them to a single string that this class can later reread.
     */
    public static String convertPatternListToString(List<String> dataSetTypePatterns)
    {
        if (null == dataSetTypePatterns)
        {
            return "";
        }
        StringBuilder patternSb = new StringBuilder();
        for (String pattern : dataSetTypePatterns)
        {
            patternSb.append(pattern);
            patternSb.append(",");
        }
        if (patternSb.length() > 0)
        {
            patternSb.deleteCharAt(patternSb.length() - 1);
        }

        return patternSb.toString();
    }

    private static List<Pattern> convertPatternListStringToList(String patternListString)
    {
        String patternsString = patternListString == null ? "" : patternListString;
        String[] patterns = patternsString.split(",");
        ArrayList<Pattern> patternsList = new ArrayList<Pattern>();
        for (String pattern : patterns)
        {
            String trimmedPattern = pattern.trim();
            if (trimmedPattern.length() > 0)
            {
                patternsList.add(Pattern.compile(trimmedPattern));
            }
        }
        return patternsList;
    }

    private final List<Pattern> whitelistPatterns;

    private final List<Pattern> blacklistPatterns;

    /**
     * Constructor
     * 
     * @param whitelistPatternString A string serialized using {@link #convertPatternListToString}.
     * @param blacklistPatternString A string serialized using {@link #convertPatternListToString}.
     */
    public DataSetTypeFilter(String whitelistPatternString, String blacklistPatternString)
    {
        this.whitelistPatterns = convertPatternListStringToList(whitelistPatternString);
        this.blacklistPatterns = convertPatternListStringToList(blacklistPatternString);
    }

    public List<DataSetType> filterDataSetTypes(List<DataSetType> typesToFilter)
    {
        // Figure out which set of patterns to use then apply them
        if (whitelistPatterns.size() > 0)
        {
            return filterUsingWhitelist(typesToFilter);
        }

        if (blacklistPatterns.size() > 0)
        {
            return filterUsingBlacklist(typesToFilter);
        }
        // No filter patterns were specified -- return the original list
        return typesToFilter;
    }

    /**
     * Filter using the whitelist, assuming it is non-empty
     */
    private List<DataSetType> filterUsingWhitelist(List<DataSetType> typesToFilter)
    {
        ArrayList<DataSetType> filteredList = new ArrayList<DataSetType>();
        for (DataSetType type : typesToFilter)
        {
            ifMatchesAddToList(type, filteredList);
        }
        return filteredList;
    }

    /**
     * Filter using the blacklist, assuming it is non-empty
     */
    private List<DataSetType> filterUsingBlacklist(List<DataSetType> typesToFilter)
    {
        ArrayList<DataSetType> filteredList = new ArrayList<DataSetType>();
        for (DataSetType type : typesToFilter)
        {
            ifNoMatchAddToList(type, filteredList);
        }
        return filteredList;
    }

    /**
     * If the type matches, add it to the filtered List
     */
    private void ifMatchesAddToList(DataSetType type, ArrayList<DataSetType> filteredList)
    {
        for (Pattern pattern : whitelistPatterns)
        {
            if (pattern.matcher(type.getCode()).matches())
            {
                filteredList.add(type);
                return;
            }
        }
    }

    /**
     * If the type does not match, add it to the filtered List
     */
    private void ifNoMatchAddToList(DataSetType type, ArrayList<DataSetType> filteredList)
    {
        for (Pattern pattern : blacklistPatterns)
        {
            if (pattern.matcher(type.getCode()).matches())
            {
                return;
            }
        }

        filteredList.add(type);
    }

}
