/*
 * Copyright 2011 ETH Zuerich, CISD
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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share.ShufflePriority;
import ch.systemsx.cisd.openbis.generic.shared.Constants;

/**
 * A factory that constructs {@link Share} objects.
 * <p>
 * 
 * @author Kaloyan Enimanev
 */
public class ShareFactory
{
    @Private
    static final String SPEED_FILE = "speed";

    public static final String SHARE_PROPS_FILE = "share.properties";

    @Private
    static final String SPEED_HINT_PROP = "speed";

    @Private
    static final String SHUFFLE_PRIORITY_PROP = "shuffle-priority";

    public static final String WITHDRAW_SHARE_PROP = "withdraw-share";

    public static final String IGNORED_FOR_SHUFFLING_PROP = "ignored-for-shuffling";

    public static final String UNARCHIVING_SCRATCH_SHARE_PROP = "unarchiving-scratch-share";

    public static final String UNARCHIVING_SCRATCH_SHARE_MAXIMUM_SIZE_IN_GB_PROP = "unarchiving-scratch-share-maximum-size-in-GB";

    public static final String EXPERIMENTS_PROP = "experiments";

    public static final String DATA_SET_TYPES_PROP = "data-set-types";

    private int speed = Math.abs(Constants.DEFAULT_SPEED_HINT);

    private ShufflePriority shufflePriority = ShufflePriority.SPEED;

    private boolean withdrawShare;

    private boolean ignoredForShuffling;

    private boolean unarchivingScratchShare;

    private Set<String> experimentIdentifiers = Collections.emptySet();

    private Set<String> dataSetTypes = Collections.emptySet();

    private long unarchivingScratchShareMaximumSize;

    Share createShare(final SharesHolder sharesHolder, File shareRoot,
            IFreeSpaceProvider freeSpaceProvider, ISimpleLogger log)
    {
        readSpeedFile(shareRoot, log);
        readSharePropertiesFile(shareRoot, log);
        Share share = new Share(sharesHolder, shareRoot, speed, freeSpaceProvider);
        share.setShufflePriority(shufflePriority);
        share.setWithdrawShare(withdrawShare);
        share.setUnarchivingScratchShare(unarchivingScratchShare);
        share.setUnarchivingScratchShareMaximumSize(unarchivingScratchShareMaximumSize);
        share.setIgnoredForShuffling(ignoredForShuffling);
        share.setExperimentIdentifiers(experimentIdentifiers);
        share.setDataSetTypes(dataSetTypes);
        return share;

    }

    Share createShare(File shareRoot,
            IFreeSpaceProvider freeSpaceProvider, ISimpleLogger log)
    {
        return createShare(null, shareRoot, freeSpaceProvider, log);

    }

    private void readSharePropertiesFile(File shareRoot, ISimpleLogger log)
    {
        File propsFile = new File(shareRoot, SHARE_PROPS_FILE);
        if (propsFile.isFile())
        {
            Properties props = loadShareProperties(propsFile, log);

            if (props.containsKey(SPEED_HINT_PROP))
            {
                String speedHintProp = props.getProperty(SPEED_HINT_PROP);
                try
                {
                    speed = SpeedUtils.trim(Integer.parseInt(speedHintProp));
                } catch (IllegalArgumentException iae)
                {
                    String errorMsg =
                            String.format(
                                    "The value for property (%s) in file (%s) must be a number : (%s)",
                                    SPEED_HINT_PROP, propsFile.getAbsolutePath(), speedHintProp);
                    log.log(LogLevel.WARN, errorMsg);
                }
            }

            if (props.containsKey(SHUFFLE_PRIORITY_PROP))
            {
                String shufflePriorityProp = props.getProperty(SHUFFLE_PRIORITY_PROP);
                try
                {
                    shufflePriority = ShufflePriority.valueOf(shufflePriorityProp.toUpperCase());
                } catch (IllegalArgumentException iae)
                {
                    String errorMsg =
                            String.format("Invalid value for property (%s) in file (%s) : (%s)",
                                    SHUFFLE_PRIORITY_PROP, propsFile.getAbsolutePath(),
                                    shufflePriorityProp);
                    log.log(LogLevel.WARN, errorMsg);
                }
            }

            withdrawShare = PropertyUtils.getBoolean(props, WITHDRAW_SHARE_PROP, false);
            ignoredForShuffling = PropertyUtils.getBoolean(props, IGNORED_FOR_SHUFFLING_PROP, false);
            unarchivingScratchShare = PropertyUtils.getBoolean(props, UNARCHIVING_SCRATCH_SHARE_PROP, false);
            unarchivingScratchShareMaximumSize = getShareMaximumSize(props);
            experimentIdentifiers =
                    new HashSet<String>(Arrays.asList(PropertyParametersUtil.parseItemisedProperty(
                            props.getProperty(EXPERIMENTS_PROP, ""), EXPERIMENTS_PROP)));
            dataSetTypes =
                    new HashSet<String>(Arrays.asList(PropertyParametersUtil.parseItemisedProperty(
                            props.getProperty(DATA_SET_TYPES_PROP, ""), DATA_SET_TYPES_PROP)));
        }

    }

    private long getShareMaximumSize(Properties props)
    {
        int maxSize = PropertyUtils.getInt(props, UNARCHIVING_SCRATCH_SHARE_MAXIMUM_SIZE_IN_GB_PROP, 0);
        return maxSize <= 0 ? Long.MAX_VALUE : FileUtils.ONE_GB * maxSize;
    }

    private Properties loadShareProperties(File propsFile, ISimpleLogger log)
    {
        Properties props = new Properties();
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(propsFile);
            props.load(fis);
        } catch (IOException ioex)
        {
            log.log(LogLevel.WARN, "Error while reading from " + propsFile.getAbsolutePath()
                    + " : " + ioex.getMessage());
        } finally
        {
            IOUtils.closeQuietly(fis);
        }
        return props;
    }

    private void readSpeedFile(File shareRoot, ISimpleLogger log)
    {
        File speedFile = new File(shareRoot, SPEED_FILE);
        if (speedFile.isFile())
        {
            String value = FileUtilities.loadToString(speedFile).trim();
            try
            {
                speed = SpeedUtils.trim(Integer.parseInt(value));
            } catch (NumberFormatException ex)
            {
                log.log(LogLevel.WARN, "Speed file " + speedFile + " doesn't contain a number: "
                        + value);
            }
        }
    }
}
