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

package ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess;

import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellReference;

/**
 * Basic information about well content which allows to compute rankings.
 * 
 * @author Tomasz Pylak
 */
public class BasicWellContentQueryResult implements IWellReference
{
    private String well_code;

    private String plate_perm_id;

    public String exp_perm_id;

    public long material_content_id;

    @Override
    public String getPlatePermId()
    {
        return plate_perm_id;
    }

    @Override
    public WellReference getWellReference()
    {
        WellLocation wellLocation = WellLocation.parseLocationStr(well_code);
        return new WellReference(wellLocation, plate_perm_id);
    }
}