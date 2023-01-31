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

package ch.systemsx.cisd.etlserver.plugins;

import java.util.List;

import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;

/**
 * Strategy of shuffling data sets from source shares to target shares. Source shares are incoming shares. Target shares are all shares.
 * 
 * @author Franz-Josef Elmer
 */
public interface ISegmentedStoreShuffling
{
    /**
     * Initialize this instance.
     */
    public void init(ISimpleLogger logger);

    /**
     * Moves data sets from source shares to some target shares if necessary.
     */
    public void shuffleDataSets(List<Share> sourceShares, List<Share> targetShares,
            IEncapsulatedOpenBISService service, IDataSetMover dataSetMover, ISimpleLogger logger);
}
