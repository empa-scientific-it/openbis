/*
 * Copyright 2009 ETH Zuerich, CISD
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

import java.io.PrintStream;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * A <code>ICommand</code> encapsulates one action that gets called on the client side using the prompt or terminal window.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface ICommand
{

    /**
     * Calls this <code>ICommand</code> with given <code>arguments</code>.
     * <p>
     * The arguments are the <code>main(String[])</code> method ones.
     * </p>
     * Note that this method is expected to throw given <code>RuntimeException</code> (<i>unchecked</i>) exceptions. So do not catch them and let the
     * <i>caller</i> handle them.
     * 
     * @return exit code, will be used in <code>System.exit()</code>.
     */
    public ResultCode execute(final String[] args) throws UserFailureException,
            EnvironmentFailureException;

    /**
     * Returns the name of this command.
     * <p>
     * On the client side, this <code>ICommand</code> is registered with this name. This is kind of unique identifier of this <code>ICommand</code>.
     * </p>
     */
    public String getName();

    /**
     * Prints usage information for this command.
     * 
     * @param out The stream to which help is printed
     */
    public void printUsage(PrintStream out);

}
