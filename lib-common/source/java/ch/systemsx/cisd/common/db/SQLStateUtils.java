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

import java.sql.SQLException;

import ch.systemsx.cisd.common.exceptions.ExceptionUtils;

/**
 * Some utility methods regarding <i>SQL State</i>.
 * <p>
 * This class does the job done in <code>org.springframework.jdbc.support.SQLStateSQLExceptionTranslator</code> back.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class SQLStateUtils
{
    /** SQL State. */
    public static final String NULL_VALUE_VIOLATION = "23502";

    /** SQL State. */
    public static final String FOREIGN_KEY_VIOLATION = "23503";

    /** SQL State. */
    public static final String UNIQUE_VIOLATION = "23505";

    /** SQL State. */
    public static final String INVALID_CATALOG_NAME = "3D000";

    /** SQL State. */
    public static final String DUPLICATE_OBJECT = "42710";

    /** SQL State. */
    public static final String DUPLICATE_DATABASE = "42P04";

    private SQLStateUtils()
    {
        // This class can not be instantiated.
    }

    /**
     * Tries to get the SQL state of given <code>Throwable</code>.
     */
    public final static String tryGetSqlState(final Throwable ex)
    {
        assert ex != null : "Throwable unspecified";
        final SQLException nextExceptionOrNull = tryGetNextExceptionWithNonNullState(ex);
        if (nextExceptionOrNull != null)
        {
            return nextExceptionOrNull.getSQLState();
        }
        return null;
    }

    /**
     * Try to find a {@link SQLException} with a non-<code>null</code> SQL state (using {@link SQLException#getSQLState()}) in given <var>ex</var>.
     */
    public final static SQLException tryGetNextExceptionWithNonNullState(final Throwable ex)
    {
        assert ex != null : "Throwable unspecified";
        final SQLException sqlExceptionOrNull =
                ExceptionUtils.tryGetThrowableOfClass(ex, SQLException.class);
        if (sqlExceptionOrNull != null)
        {
            SQLException nextException = sqlExceptionOrNull;
            String sqlStateOrNull = nextException.getSQLState();
            while (sqlStateOrNull == null && nextException.getNextException() != null)
            {
                nextException = nextException.getNextException();
                sqlStateOrNull = nextException.getSQLState();
            }
            return nextException;
        }
        return null;
    }

    /** Whether given SQL state stands for <i>DUPLICATE OBJECT</i>. */
    public final static boolean isDuplicateObject(final String sqlState)
    {
        assert sqlState != null : "SQL state unspecified";
        return DUPLICATE_OBJECT.equalsIgnoreCase(sqlState);
    }

    /** Whether given SQL state stands for <i>DUPLICATE DATABASE</i>. */
    public final static boolean isDuplicateDatabase(final String sqlState)
    {
        assert sqlState != null : "SQL state unspecified";
        return DUPLICATE_DATABASE.equalsIgnoreCase(sqlState);
    }

    /** Whether given SQL state stands for <i>INVALID CATALOG NAME</i>. */
    public final static boolean isInvalidCatalogName(final String sqlState)
    {
        assert sqlState != null : "SQL state unspecified";
        return INVALID_CATALOG_NAME.equalsIgnoreCase(sqlState);
    }

    /** Whether given SQL state stands for <i>UNIQUE VIOLATION</i>. */
    public final static boolean isUniqueViolation(final String sqlState)
    {
        assert sqlState != null : "SQL state unspecified";
        return UNIQUE_VIOLATION.equalsIgnoreCase(sqlState);
    }

    /** Whether given SQL state stands for <i>FOREIGN KEY VIOLATION</i>. */
    public final static boolean isForeignKeyViolation(final String sqlState)
    {
        assert sqlState != null : "SQL state unspecified";
        return FOREIGN_KEY_VIOLATION.equalsIgnoreCase(sqlState);
    }

    /** Whether given SQL state stands for <i>NULL_VALUE_VIOLATION</i>. */
    public final static boolean isNullValueConstraintViolation(final String sqlState)
    {
        assert sqlState != null : "SQL state unspecified";
        return NULL_VALUE_VIOLATION.equalsIgnoreCase(sqlState);
    }

}
