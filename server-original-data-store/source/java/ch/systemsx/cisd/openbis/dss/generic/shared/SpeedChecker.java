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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Enumeration of {@link ISpeedChecker} ordered in accordance to strictness.
 * 
 * @author Franz-Josef Elmer
 */
public enum SpeedChecker implements ISpeedChecker
{
    MATCHING_CHECKER()
    {
        @Override
        public boolean check(SimpleDataSetInformationDTO dataSet, Share share)
        {
            return Math.abs(dataSet.getSpeedHint()) == share.getSpeed();
        }
    },

    RESPECTING_SPEED_HINT_CHECKER()
    {
        @Override
        public boolean check(SimpleDataSetInformationDTO dataSet, Share share)
        {
            int speedHint = dataSet.getSpeedHint();
            int speed = share.getSpeed();
            return speedHint < 0 ? speed < Math.abs(speedHint) : speed > speedHint;
        }
    },

    IGNORING_SPEED_HINT_CHECKER()
    {
        @Override
        public boolean check(SimpleDataSetInformationDTO dataSet, Share share)
        {
            return true;
        }
    }
}