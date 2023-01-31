/*
 * Copyright 2012 ETH Zuerich, CISD
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
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * Share finder for shares associated to certain experiments as specified in the property {@link ShareFactory#EXPERIMENTS_PROP} of
 * {@link ShareFactory#SHARE_PROPS_FILE} file.
 * <p>
 * Returns the first share which has enough space and where the experiment identifier of the data set to be shuffled matches on of the experiments
 * associated with the share.
 * 
 * @author Franz-Josef Elmer
 */
public class ExperimentBasedShareFinder implements IShareFinder
{
    public ExperimentBasedShareFinder(Properties properties)
    {
    }

    @Override
    public Share tryToFindShare(SimpleDataSetInformationDTO dataSet, List<Share> shares)
    {
        if (dataSet.getExperimentCode() != null)
        {
            String experimentIdentifier =
                    new ExperimentIdentifier(dataSet.getSpaceCode(), dataSet.getProjectCode(),
                            dataSet.getExperimentCode()).toString();
            for (Share share : shares)
            {
                if (share.getExperimentIdentifiers().contains(experimentIdentifier)
                        && share.calculateFreeSpace() > dataSet.getDataSetSize())
                {
                    return share;
                }
            }
        }
        return null;
    }

}
