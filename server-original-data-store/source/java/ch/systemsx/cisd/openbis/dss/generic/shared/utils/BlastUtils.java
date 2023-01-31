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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.fasta.SequenceType;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginScanner.ScannerType;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginsUtils;

/**
 * Constants and method about BLAST support.
 *
 * @author Franz-Josef Elmer
 */
public class BlastUtils
{
    public final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, BlastUtils.class);

    private final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, BlastUtils.class);

    private static final String[] BLASTN_OPTIONS = { "task", "evalue", "word_size", "ungapped" };

    private static final String[] BLASTP_OPTIONS = { "task", "evalue", "word_size" };

    private final String BLAST_ROOT = "eln-lims/bin/blast";

    private final String LINUX = "linux/bin/";

    private final String MAC = "mac/bin/";

    public final static String BLAST_TOOLS_DIRECTORY_PROPERTY = "blast-tools-directory";

    public final static String BLAST_DATABASES_FOLDER_PROPERTY = "blast-databases-folder";

    public final static String DEFAULT_BLAST_DATABASES_FOLDER = "blast-databases";

    private String blastn;

    private String blastp;

    private boolean available;

    private File databaseFolder;

    public BlastUtils(Properties properties, File storeRoot)
    {
        String blastToolDirectory = getBLASTToolDirectory(properties);
        blastn = blastToolDirectory + "blastn";
        blastp = blastToolDirectory + "blastp";
        databaseFolder = getBlastDatabaseFolder(properties, storeRoot);

        available = process(blastn, "-version");

        if (false == available)
        {
            logMissingTools(getConfiguredBlastPath(properties));
        }
    }

    public boolean available()
    {
        return available;
    }

    public boolean process(String... command)
    {
        ProcessResult processResult = run(Arrays.asList(command));
        if (processResult.isOK())
        {
            processResult.logAsInfo();
        } else
        {
            processResult.log();
        }
        return processResult.isOK();
    }

    @Private
    protected ProcessResult run(List<String> command)
    {
        return ProcessExecutionHelper.run(command, operationLog, machineLog);
    }

    protected List<String> createCommand(SequenceType sequenceType, File queryFile, String parameterPrefix, Map<String, String> parameters)
    {
        List<String> command = new ArrayList<String>();
        String[] options;
        String defaultTask;
        if (sequenceType == SequenceType.NUCL)
        {
            command.add(blastn);
            defaultTask = "blastn";
            options = BLASTN_OPTIONS;
        } else
        {
            command.add(blastp);
            defaultTask = "blastp";
            options = BLASTP_OPTIONS;
        }
        command.add("-db");
        command.add(databaseFolder.getAbsolutePath() + "/all-" + sequenceType.toString().toLowerCase());
        command.add("-query");
        command.add(queryFile.getAbsolutePath());
        command.add("-outfmt");
        command.add("6 stitle bitscore score evalue sstart send qstart qend mismatch gaps");
        if (parameters.containsKey("task") == false)
        {
            parameters.put("task", defaultTask);
        }
        for (String option : options)
        {
            String value = tryGetOption(option, sequenceType, parameterPrefix, parameters);
            if (value != null)
            {
                command.add("-" + option);
                if (StringUtils.isNotBlank(value))
                {
                    command.add(value);
                }
            }
        }
        return command;
    }

    private String tryGetOption(String option, SequenceType sequenceType, String name, Map<String, String> parameters)
    {
        String value = parameters.get(option);
        if (value != null)
        {
            return value;
        }
        String prefixedOption = (sequenceType == SequenceType.NUCL ? "blastn." : "blastp.") + option;
        value = parameters.get(prefixedOption);
        if (value != null)
        {
            return value;
        }
        value = parameters.get(name + "." + option);
        if (value != null)
        {
            return value;
        }
        return parameters.get(name + "." + prefixedOption);
    }

    public List<String> processAndDeliverOutput(SequenceType sequenceType, File queryFile, String parameterPrefix, Map<String, String> parameters)
    {
        List<String> command = createCommand(sequenceType, queryFile, parameterPrefix, parameters);
        ProcessResult processResult = run(command);
        List<String> output = processResult.getOutput();
        if (processResult.isOK())
        {
            return output;
        }
        processResult.log();
        String message = "Couldn't find any results. The reason is most likely that the BLAST database hasn't been populated";
        throw new UserFailureException(message);
    }

    public File getBlastDatabaseFolder(Properties properties, File storeRoot)
    {
        return getFile(properties, BLAST_DATABASES_FOLDER_PROPERTY, DEFAULT_BLAST_DATABASES_FOLDER, storeRoot);
    }

    public String getBLASTToolDirectory(Properties properties)
    {
        String blastToolsDirectory = getConfiguredBlastPath(properties);

        if (false == blastToolsDirectory.isEmpty())
        {

            if (blastToolsDirectory.endsWith(File.separator))
            {
                return blastToolsDirectory;
            }
            return blastToolsDirectory + File.separator;
        }

        return getBestGuessBLASTToolDirectory(properties);
    }

    private String getConfiguredBlastPath(Properties properties)
    {
        String blastToolsDirectory = properties.getProperty(BLAST_TOOLS_DIRECTORY_PROPERTY, "");
        return blastToolsDirectory;
    }

    private String getBestGuessBLASTToolDirectory(Properties properties)
    {
        String corePluginsFolder = CorePluginsUtils.getCorePluginsFolder(properties, ScannerType.DSS);
        Path path = Paths.get(corePluginsFolder, BLAST_ROOT).toAbsolutePath();

        if (false == path.toFile().exists())
        {
            return "";
        }

        if (SystemUtils.IS_OS_LINUX)
        {
            return path.resolve(LINUX).toString() + File.separator;
        }

        return path.resolve(MAC).toString() + File.separator;
    }

    public File getFile(Properties properties, String pathProperty, String defaultPath, File storeRoot)
    {
        String path = properties.getProperty(pathProperty);
        return path == null ? new File(storeRoot, defaultPath) : new File(path);
    }

    public void logMissingTools(String configuredBlastPath)
    {
        if (StringUtils.isBlank(configuredBlastPath))
        {
            operationLog.error("Property '" + BLAST_TOOLS_DIRECTORY_PROPERTY
                    + "' is not specified and BLAST isn't found in default location.");
        } else
        {
            operationLog.error("BLAST isn't installed in location '" + configuredBlastPath + "' specified by the property '"
                    + BLAST_TOOLS_DIRECTORY_PROPERTY + ".");
        }
    }

    public static String createDatabaseName(String baseName, SequenceType seqType)
    {
        return baseName + "-" + seqType.toString().toLowerCase();
    }
}
