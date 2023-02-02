/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.common.spring;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import net.lemnik.eodsql.EoDException;
import net.lemnik.eodsql.impl.ExceptionTranslationUtils;
import net.lemnik.eodsql.impl.ExceptionTranslator;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

/**
 * An {@link ExceptionTranslator} that uses the {@link SQLExceptionTranslator} underneath.
 * 
 * @author Bernd Rinn
 */
public class SpringEoDSQLExceptionTranslator implements ExceptionTranslator
{

    /**
     * Enables the translation of SQL Exceptions to their (unchecked) Spring pendants rather than the generic {@link EoDException}.
     */
    public static void activate()
    {
        ExceptionTranslationUtils.setExceptionTranslator(new SpringEoDSQLExceptionTranslator());
    }

    @Override
    public RuntimeException translateException(DataSource dataSource, String task, String sql,
            SQLException ex)
    {
        return new SQLErrorCodeSQLExceptionTranslator(dataSource).translate(task, sql, ex);
    }

    @Override
    public RuntimeException translateException(Connection connection, String task, String sql,
            SQLException ex)
    {
        String databaseProductName;
        try
        {
            databaseProductName = connection.getMetaData().getDatabaseProductName();
            return new SQLErrorCodeSQLExceptionTranslator(databaseProductName).translate(task, sql,
                    ex);
        } catch (SQLException ex1)
        {
            return new SQLErrorCodeSQLExceptionTranslator().translate(task, sql, ex);
        }
    }

    @Override
    public RuntimeException uniqueResultExpected()
    {
        return new IncorrectResultSizeDataAccessException(
                "A unique result was expected but the database returned multiple rows.", 1);
    }

}
