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

package ch.systemsx.cisd.openbis.dss.etl.featurevector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.CsvFileReaderHelper.ICsvFileReaderConfiguration;

class FeatureVectorStorageProcessorConfiguration implements
        ICsvFileReaderConfiguration
{
    private static final String DEFAULT_COLUMNS_TO_BE_IGNORED = "barcode";

    @Private
    static final String COLUMNS_TO_BE_IGNORED_KEY = "columns-to-be-ignored";

    private static final String SEPARATOR_PROPERTY_KEY = "separator";

    private static final String IGNORE_COMMENTS_PROPERTY_KEY = "ignore-comments";

    private static final String WELL_NAME_ROW_PROPERTY_KEY = "well-name-row";

    private static final String WELL_NAME_COL_PROPERTY_KEY = "well-name-col";

    private static final char DEFAULT_DELIMITER = ';';

    private static final String DEFAULT_WELL_ROW = "WellName";

    private static final String DEFAULT_WELL_COL = "WellName";

    private final char columnDelimiter;

    private final boolean ignoreComments;

    private final char comment;

    private final String wellRow;

    private final String wellColumn;

    private final Set<String> columnsToBeIgnored;

    FeatureVectorStorageProcessorConfiguration(Properties properties)
    {
        comment = '#';

        this.columnDelimiter =
                PropertyUtils.getChar(properties, SEPARATOR_PROPERTY_KEY, DEFAULT_DELIMITER);
        this.ignoreComments =
                PropertyUtils.getBoolean(properties, IGNORE_COMMENTS_PROPERTY_KEY, true);

        this.wellRow = properties.getProperty(WELL_NAME_ROW_PROPERTY_KEY, DEFAULT_WELL_ROW);

        this.wellColumn = properties.getProperty(WELL_NAME_COL_PROPERTY_KEY, DEFAULT_WELL_COL);

        columnsToBeIgnored =
                new HashSet<String>(Arrays.asList(properties.getProperty(COLUMNS_TO_BE_IGNORED_KEY,
                        DEFAULT_COLUMNS_TO_BE_IGNORED).split(", *")));
    }

    @Override
    public char getColumnDelimiter()
    {
        return columnDelimiter;
    }

    @Override
    public char getCommentDelimiter()
    {
        return comment;
    }

    @Override
    public boolean isIgnoreComments()
    {
        return ignoreComments;
    }

    @Override
    public boolean isSkipEmptyRecords()
    {
        return true;
    }

    public String getWellRow()
    {
        return wellRow;
    }

    public String getWellColumn()
    {
        return wellColumn;
    }

    public final Set<String> getColumnsToBeIgnored()
    {
        return columnsToBeIgnored;
    }
}