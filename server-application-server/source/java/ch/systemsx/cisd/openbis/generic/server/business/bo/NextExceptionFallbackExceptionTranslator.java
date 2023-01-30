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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.sql.SQLException;

import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;

/**
 * A translator that takes into includes the message from the next exception in the chain as this often has the real cause.
 * 
 * @author Bernd Rinn
 */
public class NextExceptionFallbackExceptionTranslator extends SQLStateSQLExceptionTranslator
{
    @Override
    protected String buildMessage(String task, String sql, SQLException ex)
    {
        String msg = ex.getMessage();
        if (msg.contains("Call getNextException to see the cause."))
        {
            final SQLException exNext = ex.getNextException();
            if (exNext != null)
            {
                msg +=
                        " {Next exception " + ex.getClass().getSimpleName() + ": "
                                + exNext.getMessage() + "}";
            }
        }
        return task + "; SQL [" + sql + "]; " + msg;
    }

}
