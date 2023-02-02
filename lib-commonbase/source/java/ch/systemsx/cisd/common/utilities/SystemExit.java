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

package ch.systemsx.cisd.common.utilities;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.SystemExitException;

/**
 * Exit handler based on <code>System.exit()</code>.
 * 
 * @author Franz-Josef Elmer
 */
public class SystemExit implements IExitHandler
{
    public static final String EXIT_MESSAGE = "Exit called with exit code %d";

    /** The one and only one instance. */
    public static final IExitHandler SYSTEM_EXIT = new SystemExit();

    private static boolean throwException;

    private SystemExit()
    {
    }

    @Override
    public void exit(final int exitCode) throws SystemExitException
    {
        if (throwException)
        {
            throw new SystemExitException(String.format(EXIT_MESSAGE, exitCode));

        }
        System.exit(exitCode);
    }

    @Private
    public static void setThrowException(final boolean throwException)
    {
        SystemExit.throwException = throwException;
    }

}
