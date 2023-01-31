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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ExceptionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.ISimpleTableModelBuilderAdaptor;

/**
 * Utility functions.
 * 
 * @author Franz-Josef Elmer
 */
class Utils
{
    static TableModel generateTableModel(ITableModelCreator tableModelCreator,
            String scriptPath, Logger notifyLog)
    {
        try
        {
            ISimpleTableModelBuilderAdaptor builder = SimpleTableModelBuilderAdaptor.create();
            tableModelCreator.create(builder);
            return (TableModel) builder.getTableModel();
        } catch (RuntimeException ex)
        {
            notifyLog.error(createErrorMessage(scriptPath), ex);
            throw createUserFailureException(ex);
        }
    }

    private static String createErrorMessage(String scriptPath)
    {
        return "Could not run report script " + scriptPath;
    }

    private static UserFailureException createUserFailureException(RuntimeException ex)
    {
        return new UserFailureException("Chosen plugin failed to create a report: "
                + ExceptionUtils.getEndOfChain(ex), ex);
    }

}
