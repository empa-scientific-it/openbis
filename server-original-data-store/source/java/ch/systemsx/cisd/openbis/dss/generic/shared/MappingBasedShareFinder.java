/*
 * Copyright ETH 2013 - 2023 Zürich, Scientific IT Services
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Share finde which is based on a mapping file mapping space/project/experiment identifiers onto a list of possible shares. The first share having
 * enough space for the data set is returned.
 *
 * @author Franz-Josef Elmer
 */
public class MappingBasedShareFinder implements IShareFinder
{
    public static final String MAPPING_FILE_KEY = "mapping-file";

    private IdentifierAttributeMappingManager mapping;

    public MappingBasedShareFinder(Properties properties)
    {
        String mappingFilePath = PropertyUtils.getMandatoryProperty(properties, MAPPING_FILE_KEY);
        mapping = new IdentifierAttributeMappingManager(mappingFilePath, false, null);
    }

    @Override
    /**
     * Returns the first share defined in the mapping file which has enough space for specified data set 
     * and is in the list of specified shares.
     * 
     * @return <code>null</code> if no matching entry in the mapping file found or no share has enough space.
     */
    public Share tryToFindShare(SimpleDataSetInformationDTO dataSet, List<Share> shares)
    {
        Map<String, Share> shareId2Shares = new HashMap<String, Share>();
        for (Share share : shares)
        {
            if (share.calculateFreeSpace() > dataSet.getDataSetSize())
            {
                shareId2Shares.put(share.getShareId(), share);
            }
        }
        List<String> shareIds = mapping.getShareIds(dataSet);
        for (String shareId : shareIds)
        {
            Share share = shareId2Shares.get(shareId);
            if (share != null)
            {
                return share;
            }
        }
        return null;
    }

}
