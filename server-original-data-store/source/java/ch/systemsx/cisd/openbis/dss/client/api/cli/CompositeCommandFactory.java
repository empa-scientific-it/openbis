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

import java.util.ArrayList;
import java.util.List;

/**
 * A command factory that wraps sub-command factories.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class CompositeCommandFactory extends AbstractCommandFactory
{

    private final String programCallString;

    private final List<ICommandFactory> factories;

    /**
     * Constructor.
     * 
     * @param factories An ordered list of factories this factory composites.
     */
    public CompositeCommandFactory(String programCallString, List<ICommandFactory> factories)
    {
        this.programCallString = programCallString;
        this.factories = factories;
        assert factories.size() > 0 : "CompositeCommandFactory must have a factory to wrap.";
        for (ICommandFactory factory : factories)
        {
            factory.setParentCommandFactory(this);
        }
    }

    @Override
    public List<String> getKnownCommands()
    {
        ArrayList<String> commands = new ArrayList<String>();
        for (ICommandFactory factory : factories)
        {
            commands.addAll(factory.getKnownCommands());
        }
        return commands;
    }

    @Override
    public ICommand getHelpCommand()
    {
        return new CommandHelp(this, programCallString);
    }

    @Override
    public ICommand tryCommandForName(String name)
    {
        for (ICommandFactory factory : factories)
        {
            ICommand command = factory.tryCommandForName(name);
            if (command != null)
            {
                return command;
            }
        }
        return null;
    }
}
