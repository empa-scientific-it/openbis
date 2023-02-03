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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.dbcp.BasicDataSource;

/**
 * Configuration context for database operations.
 * 
 * @author Piotr Buczek
 */
public class VerboseDataSource extends BasicDataSource
{
    Set<Integer> connections = new HashSet<Integer>();

    List<Integer> list = new ArrayList<Integer>();

    @Override
    public Connection getConnection() throws SQLException
    {
        Connection c = super.getConnection();
        if (connections.add(c.hashCode()))
        {
            list.add(c.hashCode());
        }
        List<Integer> listCopy = new ArrayList<Integer>(list);
        System.err.println(c.hashCode() + ", " + (listCopy.indexOf(c.hashCode()) + 1) + "/"
                + listCopy.size() + ": " + listCopy);
        return c;
    }

}