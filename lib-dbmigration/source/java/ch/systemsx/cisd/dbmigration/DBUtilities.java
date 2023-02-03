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

package ch.systemsx.cisd.dbmigration;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.db.SQLStateUtils;

/**
 * Utility database methods.
 * 
 * @author Franz-Josef Elmer
 */
public final class DBUtilities
{

    private DBUtilities()
    {
        // Can not be instantiated.
    }

    /**
     * Checks whether given <code>DataAccessException</code> is caused by a "database does not exist" exception.
     * <p>
     * This is database specific.
     * </p>
     */
    public static boolean isDBNotExistException(final DataAccessException ex)
    {
        // 3D000: INVALID CATALOG NAME
        return SQLStateUtils.isInvalidCatalogName(SQLStateUtils.tryGetSqlState(ex));
    }

    /**
     * Checks whether given <code>DataAccessException</code> is caused by a "duplicate object" exception.
     * <p>
     * This is database specific.
     * </p>
     */
    public static boolean isDuplicateObjectException(final DataAccessException ex)
    {
        // 42710 DUPLICATE OBJECT
        return SQLStateUtils.isDuplicateObject(SQLStateUtils.tryGetSqlState(ex));
    }

    public static boolean isDuplicateDatabaseException(final DataAccessException ex)
    {
        // 42P04 DUPLICATE DATABASE
        return SQLStateUtils.isDuplicateDatabase(SQLStateUtils.tryGetSqlState(ex));
    }

    /**
     * Splits SQL statements in <var>sqlScript</var> by ';'.
     * 
     * @return A list of SQL statements, one per list entry.
     */
    public final static List<String> splitSqlStatements(final String sqlScript)
    {
        final String[] lines = StringUtils.split(sqlScript, '\n');
        final List<String> statements = new ArrayList<String>(lines.length);
        final StringBuilder statement = new StringBuilder();
        for (int i = 0; i < lines.length; ++i)
        {
            String line = lines[i];
            final int commentStart = line.indexOf("--");
            if (commentStart >= 0)
            {
                line = line.substring(0, commentStart);
            }
            int startIdx = 0;
            for (int endIdx = line.indexOf(';') + 1; endIdx > 0; startIdx = endIdx, endIdx =
                    line.indexOf(';', startIdx) + 1)
            {
                statement.append(line.substring(startIdx, endIdx).trim());
                addToStatements(statements, statement);
            }
            statement.append(line.substring(startIdx).trim());
            statement.append(' ');
            if (statement.length() > 1 && statement.charAt(statement.length() - 2) == ';')
            {
                addToStatements(statements, statement);
            }
        }
        return statements;
    }

    private final static void addToStatements(final List<String> statements,
            final StringBuilder statement)
    {
        final String statementStr =
                statement.toString().trim().replaceAll("\\s+", " ").replaceAll("\\)(\\w)", ") $1")
                        .replaceAll("\\s+;", ";");
        statements.add(statementStr);
        statement.setLength(0);
    }
}
