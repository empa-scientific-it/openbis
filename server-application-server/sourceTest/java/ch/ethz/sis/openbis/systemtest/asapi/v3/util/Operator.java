/*
 * Copyright 2020 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.systemtest.asapi.v3.util;

import java.util.Map;
import java.util.TreeMap;

public enum Operator
{
    CONTAINS("contains"),
    STARTS_WITH("startsWith"),
    ENDS_WITH("endsWith"),
    EQUAL("=="),
    LESS("<"),
    LESS_OR_EQUAL("<="),
    GREATER(">"),
    GREATER_OR_EQUAL(">="),
    ;
    private String symbol;

    private Operator(String symbol)
    {
        this.symbol = symbol;
    }

    public String getSymbol()
    {
        return symbol;
    }

    public static Map<String, Operator> asMap()
    {
        Map<String, Operator> map = new TreeMap<>();
        for (Operator operator : Operator.values())
        {
            map.put(operator.getSymbol(), operator);
        }
        return map;
    }
}