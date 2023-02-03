/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.dbmigration.h2;

import java.sql.SQLException;

/**
 * A class for stored procedures used in SQL unit tests.
 * 
 * @author Bernd Rinn
 */
public class H2TestStoredProcedures
{

    /**
     * Compares <var>expected</var> with <var>actual</var> and throws an exception if they are not equal.
     * 
     * @return 0.
     */
    public static int assertEquals(String message, long expected, long actual) throws SQLException
    {
        if (actual != expected)
        {
            throw new SQLException(String.format("%s: expected:%d, but actual: %d", message,
                    expected, actual));
        }
        return 0;
    }

    /**
     * Compares <var>minExpected</var> with <var>actual</var> and throws an exception if <code>actual &lt; minExpected<code>.
     * 
     * @return 0.
     */
    public static int assertMinimum(String message, long minExpected, long actual)
            throws SQLException
    {
        if (actual < minExpected)
        {
            throw new SQLException(String.format("%s: Minimum expected:%d, but actual: %d",
                    message, minExpected, actual));
        }
        return 0;
    }

}
