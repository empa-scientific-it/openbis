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

import ch.systemsx.cisd.openbis.generic.shared.Constants;

/**
 * Utility method for speed.
 *
 * @author Franz-Josef Elmer
 */
public class SpeedUtils
{
    /**
     * Returns trimmed speed. The trimmed speed is has value between 0 and {@link Constants#MAX_SPEED}.
     */
    public static int trim(int speed)
    {
        return Math.max(0, Math.min(Constants.MAX_SPEED, speed));
    }

    /**
     * Returns trimmed speed hint. If the absolute value of speed hint is larger than maximum speed the maximum value multiplied with the sign of the
     * original value is returned.
     */
    public static int trimSpeedHint(int speedHint)
    {
        if (Math.abs(speedHint) > Constants.MAX_SPEED)
        {
            int absSpeed = Math.min(Math.abs(speedHint), Constants.MAX_SPEED);
            return speedHint < 0 ? -absSpeed : absSpeed;
        }
        return speedHint;
    }

}
