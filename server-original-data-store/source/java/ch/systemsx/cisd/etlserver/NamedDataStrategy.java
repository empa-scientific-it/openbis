/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.regex.Pattern;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;

/**
 * A <code>IDataStoreStrategy</code> implementation that creates a named directory and put the candidates (data sets) using numbered subdirectories.
 * 
 * @author Christian Ribeaud
 */
final class NamedDataStrategy implements IDataStoreStrategy
{
    /**
     * A pattern used in {@link FileUtilities#createNextNumberedFile(File, Pattern, String)} to number the files in a given directory.
     */
    private final static Pattern multipleFilesPatterns = Pattern.compile("_\\[([0-9]+)\\]");

    private final DataStoreStrategyKey key;

    final static File createTargetPath(final File targetPath)
    {
        assert targetPath != null : "Given target path can not be null.";
        final String defaultFileName = targetPath.getName() + "_[1]";
        return FileUtilities.createNextNumberedFile(targetPath, multipleFilesPatterns,
                defaultFileName);
    }

    NamedDataStrategy(final DataStoreStrategyKey key)
    {
        super();
        this.key = key;
    }

    static final String getDirectoryName(final DataStoreStrategyKey key)
    {
        return key.name().toLowerCase();
    }

    private final String getDirectoryName()
    {
        return getDirectoryName(key);
    }

    private final static void assertBaseDirectory(final File baseDirectory)
    {
        assert baseDirectory != null : "Missing base directory.";
    }

    //
    // IDataStoreStrategy
    //

    @Override
    public final DataStoreStrategyKey getKey()
    {
        return key;
    }

    @Override
    public final File getBaseDirectory(final File baseDirectory,
            final DataSetInformation dataSetInfo, final DataSetType dataSetType)
    {
        assertBaseDirectory(baseDirectory);
        assert dataSetType != null : "Missing data set type.";
        File share = new File(baseDirectory, dataSetInfo.getShareId());
        return new File(new File(share, getDirectoryName()), IdentifiedDataStrategy
                .createDataSetTypeDirectory(dataSetType));
    }

    @Override
    public final File getTargetPath(final File baseDirectory, final File incomingDataSetPath)
    {
        assertBaseDirectory(baseDirectory);
        assert incomingDataSetPath != null : "Missing incoming data set path";
        return createTargetPath(new File(baseDirectory, incomingDataSetPath.getName()));
    }
}
