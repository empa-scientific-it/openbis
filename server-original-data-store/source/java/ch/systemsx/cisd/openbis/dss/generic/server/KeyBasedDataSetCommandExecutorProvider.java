/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IProcessingPluginTask;

/**
 * A {@link IDataSetCommandExecutorProvider} based on the processing task key.
 *
 * @author Franz-Josef Elmer
 */
public class KeyBasedDataSetCommandExecutorProvider implements IDataSetCommandExecutorProvider
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            KeyBasedDataSetCommandExecutorProvider.class);

    private static final class ExecutorNameMapping
    {
        private final String name;

        private final Pattern pattern;

        ExecutorNameMapping(String mapping)
        {
            int indexOfColon = mapping.indexOf(':');
            if (indexOfColon < 0)
            {
                throw new ConfigurationFailureException("Missing ':' in mapping definition: " + mapping);
            }
            name = mapping.substring(0, indexOfColon).trim();
            if (name.length() == 0)
            {
                throw new ConfigurationFailureException("Unspecified executor name in mapping definition: " + mapping);
            }
            try
            {
                pattern = Pattern.compile(mapping.substring(indexOfColon + 1).trim());
            } catch (PatternSyntaxException ex)
            {
                throw new ConfigurationFailureException("Invalid regex in mapping definition: " + mapping, ex);
            }
        }

        ExecutorNameMapping(String name, Pattern pattern)
        {
            this.name = name;
            this.pattern = pattern;
        }

        String getName()
        {
            return name;
        }

        Pattern getPattern()
        {
            return pattern;
        }
    }

    private final List<ExecutorNameMapping> nameMappings = new ArrayList<ExecutorNameMapping>();

    private final Map<String, IDataSetCommandExecutor> executorsByName = new HashMap<String, IDataSetCommandExecutor>();

    private final String commandQueueDirPath;

    private final IDataSetCommandExecutorFactory executorFactory;

    private IDataSetCommandExecutor defaultExecutor;

    public KeyBasedDataSetCommandExecutorProvider(String mapping, String commandQueueDir)
    {
        this(mapping, commandQueueDir, new IDataSetCommandExecutorFactory()
            {
                @Override
                public IDataSetCommandExecutor create(File store, File queueDir, String nameOrNull)
                {
                    return new DataSetCommandExecutor(store, queueDir, nameOrNull);
                }
            });
    }

    KeyBasedDataSetCommandExecutorProvider(String mapping, String commandQueueDir,
            IDataSetCommandExecutorFactory executorFactory)
    {
        this.executorFactory = executorFactory;
        if (mapping.startsWith("${") == false)
        {
            String[] definitions = mapping.split(",");
            for (String definition : definitions)
            {
                nameMappings.add(new ExecutorNameMapping(definition));
            }
        }
        nameMappings.add(new ExecutorNameMapping("", Pattern.compile(".*")));
        this.commandQueueDirPath = StringUtils.isBlank(commandQueueDir) || commandQueueDir.startsWith("${")
                ? "" : commandQueueDir;
        File commandQueueDirPathAsFile = new File(commandQueueDirPath);
        if (commandQueueDirPathAsFile.exists() == false)
        {
            commandQueueDirPathAsFile.mkdirs();
        }
    }

    @Override
    public void init(File storeRoot)
    {
        File commandQueueDir = storeRoot;
        if (StringUtils.isNotBlank(commandQueueDirPath))
        {
            commandQueueDir = new File(commandQueueDirPath);
        }
        for (ExecutorNameMapping mapping : nameMappings)
        {
            String name = mapping.getName();
            IDataSetCommandExecutor executor = executorFactory.create(storeRoot, commandQueueDir, name);
            executorsByName.put(name, executor);
            executor.start();
            if (StringUtils.isBlank(name))
            {
                defaultExecutor = executor;
                operationLog.info("Default command executor started.");
            } else
            {
                operationLog.info("Command executor '" + name + "' started.");
            }
        }
    }

    @Override
    public IDataSetCommandExecutor getDefaultExecutor()
    {
        if (defaultExecutor == null)
        {
            throw new IllegalStateException("Default executor not yet defined.");
        }
        return defaultExecutor;
    }

    @Override
    public List<IDataSetCommandExecutor> getAllExecutors()
    {
        return new ArrayList<>(executorsByName.values());
    }

    @Override
    public IDataSetCommandExecutor getExecutor(IProcessingPluginTask processingTask, String processingTaskKey)
    {
        String identifier = getIdentifier(processingTask, processingTaskKey);
        for (ExecutorNameMapping mapping : nameMappings)
        {
            if (mapping.getPattern().matcher(identifier).matches())
            {
                return executorsByName.get(mapping.getName());
            }
        }
        throw new IllegalStateException("Couldn't find executor. This is a programming error.");
    }

    protected String getIdentifier(IProcessingPluginTask processingTask, String processingTaskKey)
    {
        return processingTaskKey;
    }

}
