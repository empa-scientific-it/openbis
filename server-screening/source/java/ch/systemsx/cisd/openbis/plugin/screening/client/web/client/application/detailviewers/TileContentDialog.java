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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.google.gwt.user.client.ui.Grid;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.TileLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * @author pkupczyk
 */
public class TileContentDialog extends ImageDialog
{

    public TileContentDialog(IViewContext<IScreeningClientServiceAsync> viewContext,
            ImageDatasetEnrichedReference imageDataset, WellLocation wellLocation,
            TileLocation tileLocation, String experimentIdentifier, String experimentPermId)
    {
        setScrollMode(Scroll.AUTO);
        setHideOnButtonClick(true);
        setButtons(CLOSE);
        setHeading(viewContext.getMessage(Dict.TILE_CONTENT_DIALOG_TITLE, tileLocation.getRow(),
                tileLocation.getColumn()));

        final LogicalImageReference imageReference =
                new LogicalImageReference(imageDataset, wellLocation, tileLocation);
        LogicalImageViewer imageViewer =
                new LogicalImageViewer(imageReference, viewContext, experimentIdentifier,
                        experimentPermId, false);

        final Grid grid = new Grid(1, 1);
        grid.setWidget(0, 0, imageViewer.getViewerWidget("TileContentDialog"));
        add(grid);

        imageViewer.setLogicalImageRefreshHandler(new LogicalImageRefreshHandler()
            {
                @Override
                public void onRefresh()
                {
                    WindowUtils.resize(TileContentDialog.this, grid.getElement());
                }
            });

    }
}
