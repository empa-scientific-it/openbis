/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.db.SQLStateUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.exception.SampleUniqueCodeViolationException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.exception.SampleUniqueSubcodeViolationException;

/**
 * Extracts information about an actual cause of sample related DataAccessException.
 * 
 * @author pkupczyk
 */
public class SampleDataAccessExceptionTranslator
{

    private static final String CODE_CONSTRAINT_NAME = "samp_code_unique_check_uk";

    private static final String SUBCODE_CONSTRAINT_NAME = "samp_subcode_unique_check_uk";

    private static final Pattern MESSAGE_PATTERN = Pattern.compile(
            ".*constraint \"(.*)\".*=\\((.*)\\).*", Pattern.DOTALL);

    public static void translateAndThrow(DataAccessException exception)
    {
        if (isUniqueCodeViolationException(exception))
        {
            throwUniqueCodeViolationException(exception);
        } else if (isUniqueSubcodeViolationException(exception))
        {
            throwUniqueSubcodeViolationException(exception);
        } else
        {
            throw exception;
        }
    }

    public static boolean isUniqueCodeViolationException(DataAccessException exception)
    {
        UniqueViolationMessage message = UniqueViolationMessage.get(exception);
        return message != null
                && CODE_CONSTRAINT_NAME.equalsIgnoreCase(message.getConstraintName());
    }

    public static boolean isUniqueSubcodeViolationException(DataAccessException exception)
    {
        UniqueViolationMessage message = UniqueViolationMessage.get(exception);
        return message != null
                && SUBCODE_CONSTRAINT_NAME.equalsIgnoreCase(message.getConstraintName());
    }

    public static void throwUniqueCodeViolationException(DataAccessException exception)
    {
        UniqueViolationMessage message = UniqueViolationMessage.get(exception);
        if (message != null)
        {
            throw new SampleUniqueCodeViolationException(message.getSampleCode());
        }
    }

    public static void throwUniqueSubcodeViolationException(DataAccessException exception)
    {
        UniqueViolationMessage message = UniqueViolationMessage.get(exception);
        if (message != null)
        {
            throw new SampleUniqueSubcodeViolationException(message.getSampleCode());
        }
    }

    private static class UniqueViolationMessage
    {

        private String constraintName;

        private String columnValue;

        public String getConstraintName()
        {
            return constraintName;
        }

        public String getSampleCode()
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

}
