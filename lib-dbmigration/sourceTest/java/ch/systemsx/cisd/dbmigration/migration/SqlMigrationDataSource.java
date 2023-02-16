/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.dbmigration.migration;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.springframework.beans.factory.DisposableBean;

public class SqlMigrationDataSource implements DataSource, DisposableBean
{
    private final String driver;

    private final String url;

    private final String owner;

    private final String password;

    private Connection connection;

    SqlMigrationDataSource(final String driver, final String url, final String owner,
            final String password)
    {
        this.driver = driver;
        this.url = url;
        this.owner = owner;
        this.password = password;
    }

    @Override
    public Connection getConnection() throws SQLException
    {
        if (connection != null && connection.isClosed() || connection == null)
        {
            try
            {
                Class.forName(driver);
            } catch (final ClassNotFoundException ex)
            {
                throw new SQLException("Couldn't load driver " + driver);
            }
            final Connection c = DriverManager.getConnection(url, owner, password);
            connection = c;
        }
        return connection;
    }

    @Override
    public Connection getConnection(final String username, final String pw) throws SQLException
    {
        if (owner.equals(username) && password.equals(pw))
        {
            return getConnection();
        }
        throw new SQLException("Forbidden");
    }

    @Override
    public int getLoginTimeout() throws SQLException
    {
        return 0;
    }

    @Override
    public void setLoginTimeout(final int timeout) throws SQLException
    {
        throw new UnsupportedOperationException("setLoginTimeout");
    }

    @Override
    public PrintWriter getLogWriter()
    {
        throw new UnsupportedOperationException("getLogWriter");
    }

    @Override
    public void setLogWriter(final PrintWriter pw) throws SQLException
    {
        throw new UnsupportedOperationException("setLogWriter");
    }

    @Override
    public void destroy() throws SQLException
    {
        if (connection != null)
        {
            connection.close();
            connection = null;
        }
    }

    @Override
    public String toString()
    {
        return "MyDataSource[" + driver + ", " + url + ", " + owner + "]";
    }

    @Override
    public boolean isWrapperFor(Class<?> arg0) throws SQLException
    {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> arg0) throws SQLException
    {
        return null;
    }

    // @Override -- un-comment when switching to Java 1.7
    public Logger getParentLogger() throws SQLFeatureNotSupportedException
    {
        return null;
    }

}