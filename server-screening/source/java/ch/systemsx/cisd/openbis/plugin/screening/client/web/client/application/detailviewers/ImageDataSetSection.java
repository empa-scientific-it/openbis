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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import java.util.Collections;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.LayoutUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LogicalImageInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * @author Pawel Glyzewski
 */
public class ImageDataSetSection extends TabContent
{
    private final AbstractExternalData dataSet;

    private final WellLocation wellLocationOrNull;

    private static final String WELL_IMAGE_SECTION_TITLE_PREFIX = "Well ";

    private static final String WELL_IMAGE_SECTION_TITLE_SUFFIX = " Images";

    public ImageDataSetSection(final ScreeningViewContext viewContext, final AbstractExternalData dataSet,
            WellLocation wellLocationOrNull)
    {
        super(WELL_IMAGE_SECTION_TITLE_PREFIX
                + (wellLocationOrNull == null ? "" : wellLocationOrNull.toWellIdString())
                + WELL_IMAGE_SECTION_TITLE_SUFFIX, viewContext, dataSet);

        this.dataSet = dataSet;
        this.wellLocationOrNull = wellLocationOrNull;
        setIds(DisplayTypeIDGenerator.LOGICAL_IMAGE_WELL_DATASET_SECTION);
    }

    private ScreeningViewContext getViewContext()
    {
        return (ScreeningViewContext) viewContext;
    }

    @Override
    protected void showContent()
    {
        final ScreeningViewContext context = getViewContext();
        add(new Text(context.getMessage(ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict.LOAD_IN_PROGRESS)));
        context.getService().getImageDatasetInfo(dataSet.getCode(),
                dataSet.getDataStore().getCode(), wellLocationOrNull,
                createDisplayImagesCallback(context));
    }

    private AsyncCallback<LogicalImageInfo> createDisplayImagesCallback(ScreeningViewContext context)
    {
        return new AbstractAsyncCallback<LogicalImageInfo>(context)
            {
                @Override
                protected void process(LogicalImageInfo imageDataSet)
                {
                    removeAll();
                    setLayout(new RowLayout());
                    setScrollMode(Scroll.AUTO);

                    addVisualisation(imageDataSet);
                    layout();
                }
            };
    }

    private void addVisualisation(LogicalImageInfo imageDataSet)
    {
        RowData margins = LayoutUtils.createRowLayoutSurroundingData();

        final ScreeningViewContext context = getViewContext();
        final LogicalImageLayouter logicalImageLayouter =
                new LogicalImageLayouter(context, wellLocationOrNull,
                        Collections.singletonList(imageDataSet));

        ImageDatasetEnrichedReference firstImageDataset = imageDataSet.getImageDataset();
        logicalImageLayouter.changeDisplayedImageDataset(firstImageDataset);

        add(logicalImageLayouter, margins);
    }
}
