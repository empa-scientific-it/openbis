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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.common.parser.ParsingException;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetFileLines;
import ch.systemsx.cisd.openbis.generic.shared.basic.TableCellUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.util.SimpleTableModelBuilder;

/**
 * Common super class of all tsv-based data merging reporting plugins.
 * 
 * @author Bernd Rinn
 */
public abstract class AbstractDataMergingReportingPlugin extends AbstractFileTableReportingPlugin
{
    private static final long serialVersionUID = 1L;

    private static final String FILE_INCLUDE_PATTERN = "file-include-pattern";

    private static final String FILE_EXCLUDE_PATTERN = "file-exclude-pattern";

    /** pattern for files that should be excluded (e.g. data set properties files) */
    public final static String DEFAULT_EXCLUDED_FILE_NAMES_PATTERN = ".*\\.tsv";

    private final String excludePattern;

    private final String includePatternOrNull;

    protected AbstractDataMergingReportingPlugin(Properties properties, File storeRoot)
    {
        this(properties, storeRoot, TAB_SEPARATOR);
    }

    protected AbstractDataMergingReportingPlugin(Properties properties, File storeRoot,
            char defaultSeparator)
    {
        super(properties, storeRoot, defaultSeparator);
        final String excludePatternOrNull = properties.getProperty(FILE_EXCLUDE_PATTERN);
        if (excludePatternOrNull == null)
        {
            this.excludePattern = DEFAULT_EXCLUDED_FILE_NAMES_PATTERN;
        } else
        {
            this.excludePattern = excludePatternOrNull;
        }
        this.includePatternOrNull = properties.getProperty(FILE_INCLUDE_PATTERN);
    }

    protected String[] getHeaderTitles(DatasetDescription dataset,
            IDataSetDirectoryProvider dirProvider)
    {
        File dir = getDataSubDir(dirProvider, dataset);
        final DatasetFileLines lines = loadFromDirectory(dataset, dir);
        return lines.getHeaderLabels();
    }

    /**
     * @param addFileNameColumn if true the second column will contain file name from which the row was generated
     */
    protected static void addDataRows(SimpleTableModelBuilder builder, DatasetDescription dataset,
            DatasetFileLines lines, boolean addFileNameColumn)
    {
        String datasetCode = dataset.getDataSetCode();
        String fileNameOrNull = addFileNameColumn ? lines.getFile().getName() : null;
        for (String[] dataTokens : lines.getDataLines())
        {
            addDataRow(builder, datasetCode, dataTokens, fileNameOrNull);
        }
    }

    protected static void addDataRow(SimpleTableModelBuilder builder, String datasetCode,
            String[] dataTokens, String fileNameOrNull)
    {
        List<ISerializableComparable> row = new ArrayList<ISerializableComparable>();
        row.add(new StringTableCell(datasetCode));
        if (fileNameOrNull != null)
        {
            row.add(new StringTableCell(fileNameOrNull));
        }
        for (String token : dataTokens)
        {
            row.add(TableCellUtil.createTableCell(token));
        }
        builder.addRow(row);
    }

    /**
     * Loads {@link DatasetFileLines} from the file found in the specified directory.
     * 
     * @throws IOExceptionUnchecked if a {@link IOException} has occurred.
     */
    protected DatasetFileLines loadFromDirectory(DatasetDescription dataset, final File dir)
            throws ParserException, ParsingException, IllegalArgumentException,
            IOExceptionUnchecked
    {
        List<File> datasetFilesToMerge = findMatchingFiles(dataset, dir);
        if (datasetFilesToMerge.size() != 1)
        {
            throw UserFailureException
                    .fromTemplate(
                            "Directory with Data Set '%s' data ('%s') should contain exactly 1 file with data but %s files were found.",
                            dataset.getDataSetCode(), dir.getAbsolutePath(),
                            datasetFilesToMerge.size());
        } else
        {
            return loadFromFile(dataset, datasetFilesToMerge.get(0));
        }
    }

    /**
     * Scan the specified directory to find files that match the dataset description.
     * 
     * @throws IOExceptionUnchecked if a {@link IOException} has occurred.
     */
    protected List<File> findMatchingFiles(DatasetDescription dataset, final File dir)
            throws IllegalArgumentException, IOExceptionUnchecked
    {
        assert dir != null : "Given file must not be null";
        assert dir.isDirectory() : "Given file '" + dir.getAbsolutePath() + "' is not a directory.";

        File[] datasetFiles = FileUtilities.listFiles(dir);
        List<File> matchingFiles = new ArrayList<File>();
        for (File datasetFile : datasetFiles)
        {
            if (datasetFile.isDirectory())
            {
                // recursively go down the directories
                List<File> matched = findMatchingFiles(dataset, datasetFile);
                matchingFiles.addAll(matched);
            } else
            {
                // exclude files with properties
                if (isFileExcluded(datasetFile) == false)
                {
                    matchingFiles.add(datasetFile);
                }
            }

        }
        return matchingFiles;
    }

    protected boolean isFileExcluded(File file)
    {
        if (includePatternOrNull != null)
        {
            return file.getName().matches(includePatternOrNull) == false;
        } else
        {
            return file.getName().matches(excludePattern);
        }
    }
}
