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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.image;

import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants.ImageServletUrlParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * @author pkupczyk
 */
public class TileImage extends Image
{

    public TileImage(TileImageInitializer initializer)
    {
        super(initializer);
    }

    @Override
    protected URLMethodWithParameters createUrl()
    {
        URLMethodWithParameters url = super.createUrl();
        WellLocation wellLocation =
                getInitializer().getChannelReferences().getBasicImage().tryGetWellLocation();

        if (wellLocation != null)
        {
            url.addParameter(ImageServletUrlParameters.WELL_ROW_PARAM, wellLocation.getRow());
            url.addParameter(ImageServletUrlParameters.WELL_COLUMN_PARAM, wellLocation.getColumn());
        }
        url.addParameter(ImageServletUrlParameters.TILE_ROW_PARAM, getInitializer().getTileRow());
        url.addParameter(ImageServletUrlParameters.TILE_COL_PARAM, getInitializer().getTileColumn());
        return url;
    }

    @Override
    protected TileImageInitializer getInitializer()
    {
        return (TileImageInitializer) super.getInitializer();
    }

}
