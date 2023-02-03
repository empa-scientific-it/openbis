/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.installer.izpack;

import static ch.systemsx.cisd.openbis.installer.izpack.GlobalInstallationContext.POSTGRES_BIN_VARNAME;

import java.io.File;

import com.izforge.izpack.api.data.AutomatedInstallData;

/**
 * Validates the user input for the location of the postgres installation.
 * 
 * @author Kaloyan Enimanev
 */
public class PostgresToolsPathValidator extends AbstractDataValidator
{

    @Override
    public boolean getDefaultAnswer()
    {
        return true;
    }

    @Override
    public String getErrorMessageId()
    {
        return "'psql' and 'pg_dump' must be available on the specified path.";
    }

    @Override
    public String getWarningMessageId()
    {
        return getErrorMessageId();
    }

    @Override
    public Status validateData(AutomatedInstallData data)
    {
        String selectedPath = data.getVariable(POSTGRES_BIN_VARNAME);
        boolean valid = PostgresInstallationDetectorUtils.areCommandLineToolsInDir(selectedPath);
        if (valid == false)
        {
            selectedPath = new File(selectedPath, "bin").getAbsolutePath();
            valid = PostgresInstallationDetectorUtils.areCommandLineToolsInDir(selectedPath);
        }

        data.setVariable(POSTGRES_BIN_VARNAME, selectedPath);

        if (valid)
        {
            return Status.OK;
        } else
        {
            setErrorMessage("'psql' and 'pg_dump' must be available on the specified path: " + selectedPath);
            return Status.ERROR;
        }

    }

}
