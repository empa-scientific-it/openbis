/*
 * Copyright 2007 ETH Zuerich, CISD
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

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetLocationUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;

/**
 * This <code>IDataStoreStrategy</code> implementation if for data set that has been <i>identified</i>, meaning that kind of connection to this data
 * set could be found in the database (through the derived <i>Master Plate</i> or through the experiment specified).
 * 
 * @author Christian Ribeaud
 */
public final class IdentifiedDataStrategy implements IDataStoreStrategy
{
    static final String DATA_SET_TYPE_PREFIX = "DataSetType_";

    static final String UNEXPECTED_PATHS_MSG_FORMAT =
            "There are unexpected paths '%s' in data store '%s'. I'll proceed anyway.";

    static final String STORAGE_LAYOUT_ERROR_MSG_PREFIX = "Serious error in data store layout: ";

    public IdentifiedDataStrategy()
    {

    }

    final static String createDataSetTypeDirectory(final DataSetType dataSetType)
    {
        final String dataSetTypeCode = dataSetType.getCode();
        assert dataSetTypeCode != null : "Data set type code can not be null.";
        return DATA_SET_TYPE_PREFIX + dataSetTypeCode;
    }

    /**
     * Computes the base directory with given <var>baseDir</var> and given <var>dataSetInfo</var> and returns it as <code>File</code>.
     * <p>
     * Note that this method does not call {@link File#mkdirs()} on returned <code>File</code>.
     * </p>
     */
    @Private
    static File createBaseDirectory(final File baseDir, final DataSetInformation dataSetInfo)
    {
        String dataSetCode = dataSetInfo.getDataSetCode();
        String shareId = dataSetInfo.getShareId();
        final String instanceUUID = dataSetInfo.getInstanceUUID();
        return DatasetLocationUtil.getDatasetLocationPath(baseDir, dataSetCode, shareId,
                instanceUUID);
    }

    //
    // IDataStoreStrategy
    //

    @Override
    public final DataStoreStrategyKey getKey()
    {
        return DataStoreStrategyKey.IDENTIFIED;
    }

    @Override
    public final File getBaseDirectory(final File storeRoot, final DataSetInformation dataSetInfo,
            final DataSetType dataSetType)
    {
        assert storeRoot != null : "Store root can not be null";
        assert dataSetInfo != null : "Data set information can not be null";
        final File baseDirectory = createBaseDirectory(storeRoot, dataSetInfo);
        if (baseDirectory.exists())
        {
            throw EnvironmentFailureException.fromTemplate(STORAGE_LAYOUT_ERROR_MSG_PREFIX
                    + "Data set directory '%s' exists but has been designed to be unique.",
                    baseDirectory.getPath());
        }
        if (baseDirectory.isFile())
        {
            throw EnvironmentFailureException.fromTemplate(STORAGE_LAYOUT_ERROR_MSG_PREFIX
                    + "Base directory '%s' is a file.", baseDirectory);
        }
        return baseDirectory;
    }

    @Override
    public final File getTargetPath(final File baseDirectory, final File incomingDataSetPath)
            throws IllegalStateException
    {
        assert baseDirectory != null : "Base directory can not be null";
        assert incomingDataSetPath != null : "Incoming data set can not be null";
        final File targetPath = new File(baseDirectory, incomingDataSetPath.getName());
        if (targetPath.exists())
        {
            throw new IllegalStateException(String.format(
                    "Target path '%s' of identified incoming data set already exists "
                            + "(which it shouldn't), bailing out.", targetPath.getAbsolutePath()));
        }
        return targetPath;
    }
}
