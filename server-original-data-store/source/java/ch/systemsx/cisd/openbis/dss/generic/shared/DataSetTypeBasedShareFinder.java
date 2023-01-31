/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ShareFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Share finder for shares associated to certain data set types as specified in the property {@link ShareFactory#DATA_SET_TYPES_PROP} of
 * {@link ShareFactory#SHARE_PROPS_FILE} file.
 * <p>
 * Returns the first share which has enough space and where the data set type of the data set to be shuffled matches one of the data set types
 * associated with the share.
 * 
 * @author Jakub Straszewski
 */
public class DataSetTypeBasedShareFinder implements IShareFinder
{
    public DataSetTypeBasedShareFinder(Properties properties)
    {
    }

    @Override
    public Share tryToFindShare(SimpleDataSetInformationDTO dataSet, List<Share> shares)
    {
        for (Share share : shares)
        {
            if (share.getDataSetTypes().contains(dataSet.getDataSetType())
                    && share.calculateFreeSpace() > dataSet.getDataSetSize())
            {
                return share;
            }
        }
        return null;
    }

}
