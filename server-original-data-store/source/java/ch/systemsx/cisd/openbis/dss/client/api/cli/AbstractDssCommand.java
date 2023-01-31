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

package ch.systemsx.cisd.openbis.dss.client.api.cli;

import ch.systemsx.cisd.openbis.dss.client.api.v1.DssComponentFactory;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;

/**
 * Superclass for dss command-line client commands.
 * <p>
 * Provides services used by most subclasses. In order for the AbstractCommand to work, subclasses must do the following:
 * <ul>
 * <li>Set the parser ivar in their constructor
 * </ul>
 * 
 * @author Chandrasekhar Ramakrishnan
 */
abstract class AbstractDssCommand<T extends GlobalArguments> extends AbstractCommand<T>
{
    /**
     * @param arguments
     */
    public AbstractDssCommand(T arguments)
    {
        super(arguments);
    }

    /**
     * How is this program invoked from the command line?
     */
    @Override
    protected String getProgramCallString()
    {
        return DssCommandFactory.PROGRAM_CALL_STRING;
    }

    @Override
    protected String getRequiredArgumentsString()
    {
        return "<data set code> <path>";
    }

    /**
     * Creates the DSS Component object and logs into the server.
     */
    protected IDssComponent login(GlobalArguments args)
    {
        IDssComponent component =
                DssComponentFactory.tryCreate(arguments.getUsername(), arguments.getPassword(),
                        arguments.getServerBaseUrl(), args.getTimeoutInMillis());
        if (null == component)
        {
            System.out.println("Username / password is invalid");
        }
        return component;
    }

}
