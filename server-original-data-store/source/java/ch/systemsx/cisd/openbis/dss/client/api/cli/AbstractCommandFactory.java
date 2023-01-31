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

/**
 * @author Chandrasekhar Ramakrishnan
 */
abstract public class AbstractCommandFactory implements ICommandFactory
{
    // If non-null, this command factory is a "sub-factory" of another factory.
    private ICommandFactory parentCommandFactoryOrNull;

    /**
     *
     *
     */
    public AbstractCommandFactory()
    {
        super();
    }

    public ICommand tryHelpCommandForName(String name)
    {
        // special handling of "-h" and "--help"
        if ("-h".equals(name) || "--help".equals(name))
        {
            return getHelpCommand();
        }

        return null;
    }

    /**
     * If this command factory has a parent command factory, return it.
     * 
     * @return A command factory or null
     */
    protected ICommandFactory tryParentCommandFactory()
    {
        return parentCommandFactoryOrNull;
    }

    /**
     * Set the parent of this command factory
     * 
     * @param parentCommandFactoryOrNull A command factory, or null if this one has no parent.
     */
    @Override
    public void setParentCommandFactory(ICommandFactory parentCommandFactoryOrNull)
    {
        this.parentCommandFactoryOrNull = parentCommandFactoryOrNull;
    }

    protected CommandHelp getHelpCommand(String programCallString)
    {
        ICommandFactory helpFactory = tryParentCommandFactory();
        if (null == helpFactory)
        {
            helpFactory = this;
        }

        return new CommandHelp(helpFactory, programCallString);
    }
}