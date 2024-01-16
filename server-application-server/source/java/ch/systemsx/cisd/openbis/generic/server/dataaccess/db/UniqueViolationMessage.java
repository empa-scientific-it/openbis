/*
 *  Copyright ETH 2024 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import ch.systemsx.cisd.common.db.SQLStateUtils;
import org.springframework.dao.DataAccessException;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UniqueViolationMessage
{

    private static final Pattern MESSAGE_PATTERN = Pattern.compile(
            ".*constraint \"(.*)\".*=\\((.*)\\).*", Pattern.DOTALL);

    private String constraintName;

    private String columnValue;

    public String getConstraintName()
    {
        return constraintName;
    }

    public String getCode(int index)
    {
        if (columnValue != null)
        {
            String[] parts = columnValue.split(",");
            if (parts != null && parts.length > index)
            {
                return parts[index];
            }
        }
        return null;
    }

    public String getCode() //getSampleCode
    {
        if (columnValue != null)
        {
            String[] parts = columnValue.split(",");
            if (parts != null && parts.length > 0)
            {
                return parts[0];
            }
        }
        return null;
    }

    public static final UniqueViolationMessage get(DataAccessException exception)
    {
        final SQLException sqlException =
                SQLStateUtils.tryGetNextExceptionWithNonNullState(exception);

        if (sqlException != null)
        {
            final String sqlState = sqlException.getSQLState();
            if (SQLStateUtils.isUniqueViolation(sqlState))
            {
                String message = sqlException.getMessage();
                Matcher matcher = MESSAGE_PATTERN.matcher(message);

                if (matcher.find())
                {
                    UniqueViolationMessage result = new UniqueViolationMessage();
                    result.constraintName = matcher.group(1);
                    result.columnValue = matcher.group(2);
                    return result;
                }
            }
        }
        return null;
    }
}
