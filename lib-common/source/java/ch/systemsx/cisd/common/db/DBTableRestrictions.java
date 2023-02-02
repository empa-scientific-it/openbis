/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.db;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A class that holds the restrictions put upon columns in a database table.
 * 
 * @author Bernd Rinn
 */
public class DBTableRestrictions
{
    final Map<String, Integer> columnLengthMap = new HashMap<String, Integer>();

    final Map<String, Set<String>> checkedConstraintsMap = new HashMap<String, Set<String>>();

    final Set<String> notNullSet = new HashSet<String>();

    public int getLength(String columnName)
    {
        final Integer columnLength = columnLengthMap.get(columnName);
        assert columnLength != null : "Illegal column '" + columnName + "'.";
        return columnLength;
    }

    public Set<String> tryGetCheckedConstaint(String columnName)
    {
        final Set<String> checkedConstraint = checkedConstraintsMap.get(columnName);
        return (checkedConstraint == null) ? null : Collections.unmodifiableSet(checkedConstraint);
    }

    public boolean hasNotNullConstraint(String columnName)
    {
        return notNullSet.contains(columnName);
    }
}