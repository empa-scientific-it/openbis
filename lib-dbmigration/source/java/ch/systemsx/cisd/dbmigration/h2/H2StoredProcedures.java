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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The implementation of the stored procedures for H2.
 * 
 * @author Bernd Rinn
 */
public class H2StoredProcedures
{

    /**
     * Renames the sequence <var>oldName</var> into <var>newName</var> ensuring the <code>NEXTVAL()</code> will return the right value.
     * 
     * @return The next value the sequence will deliver.
     */
    public static int renameSequence(Connection conn, String oldName, String newName)
            throws SQLException
    {
        final ResultSet rs =
                conn.createStatement().executeQuery(String.format("SELECT NEXTVAL('%s')", oldName));
        if (rs.first() == false)
        {
            throw new SQLException("Cannot get next value of sequence '" + oldName + "'");
        }
        final int currSeqVal = rs.getInt(1);
        rs.close();
        conn.createStatement().execute("CREATE SEQUENCE " + newName + " START WITH " + currSeqVal);
        conn.createStatement().execute("DROP SEQUENCE " + oldName);
        return currSeqVal;
    }

}
