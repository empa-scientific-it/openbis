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

package ch.systemsx.cisd.openbis.dss.etl.lmc;

import java.util.Properties;

import ch.systemsx.cisd.hcs.Location;

/**
 * A <code>IHCSImageFileExtractor</code> implementation suitable for <i>LMC</i>.
 * 
 * @author Tomasz Pylak
 */
public class HCSImageFileExtractor extends ch.systemsx.cisd.openbis.dss.etl.HCSImageFileExtractor
{
    public HCSImageFileExtractor(final Properties properties)
    {
        super(properties);
    }

    @Override
    protected Location tryGetTileLocation(final String wellLocation)
    {
        int tileNumber;
        try
        {
            tileNumber = Integer.parseInt(wellLocation);
        } catch (final NumberFormatException ex)
        {
            // Nothing to do here. Rest of the code can handle this.
            return null;
        }
        return Location.tryCreateLocationFromColumnwisePosition(tileNumber, tileGeometry);
    }
}
