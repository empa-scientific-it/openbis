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

import java.util.List;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public interface ICommandFactory
{

    /**
     * Find the command that matches the name.
     */
    public ICommand tryCommandForName(String name);

    /**
     * List all the commands supported by this command factory (not including help).
     */
    public List<String> getKnownCommands();

    /**
     * Get the help command.
     */
    public ICommand getHelpCommand();

    /**
     * Set the parent of this command factory.
     * 
     * @param parentCommandFactoryOrNull A command factory, or null if this one has no parent.
     */
    public void setParentCommandFactory(ICommandFactory parentCommandFactoryOrNull);
}