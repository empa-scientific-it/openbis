/*
 * Copyright 2009 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.LayoutUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageSampleContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LogicalImageInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * A section of a detail view which shows images for the well or microscopy sample.
 * 
 * @author Tomasz Pylak
 */
public class ImageSampleSection extends TabContent
{

    // --- GUI messages (to be moved to the dictionary)

    private static final String NO_IMAGES_DATASET_LABEL = "No images data has been acquired.";

    private static final String WELL_IMAGE_SECTION_TITLE_SUFFIX = "Images";

    private static final String WELL_IMAGE_SECTION_TITLE_PREFIX = "Well";

    // ----

    private final TechId sampleId;

    private final WellLocation wellLocationOrNull;

    private final boolean isWell;

    private static String getTabName(boolean isWell, WellLocation wellLocationOrNull)
    {
        if (false == isWell && wellLocationOrNull != null)
        {
            return WELL_IMAGE_SECTION_TITLE_PREFIX + " " + wellLocationOrNull.toWellIdString()
                    + " " + WELL_IMAGE_SECTION_TITLE_SUFFIX;
        }
        return WELL_IMAGE_SECTION_TITLE_SUFFIX;
    }

    public ImageSampleSection(final ScreeningViewContext viewContext, final TechId sampleId,
            WellLocation wellLocationOrNull, boolean isWell)
    {
        super(getTabName(isWell, wellLocationOrNull), viewContext, sampleId);
        this.sampleId = sampleId;
        this.wellLocationOrNull = wellLocationOrNull;
        this.isWell = isWell;
        setIds(DisplayTypeIDGenerator.LOGICAL_IMAGE_WELL_SECTION);
    }

    private ScreeningViewContext getViewContext()
    {
        return (ScreeningViewContext) viewContext;
    }

    @Override
    protected void showContent()
    {
        final ScreeningViewContext context = getViewContext();
        add(new Text(
                context.getMessage(ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict.LOAD_IN_PROGRESS)));
        context.getService().getImageDatasetInfosForSample(sampleId,
                isWell ? wellLocationOrNull : null, createDisplayImagesCallback(context));
    }

    private AsyncCallback<ImageSampleContent> createDisplayImagesCallback(
            final ScreeningViewContext context)
    {
        return new AbstractAsyncCallback<ImageSampleContent>(context)
            {
                @Override
                protected void process(ImageSampleContent imageSampleContent)
                {
                    removeAll();
                    setLayout(new RowLayout());
                    setScrollMode(Scroll.AUTO);

                    addVisualisation(imageSampleContent);
                    addUnknownDatasetLinks(imageSampleContent.getUnknownDatasets());

                    layout();
                }
            };
    }

    private void addUnknownDatasetLinks(List<DatasetReference> unknownDatasets)
    {
        ImagingDatasetGuiUtils guiUtils = new ImagingDatasetGuiUtils(getViewContext());
        Widget w = guiUtils.tryCreateUnknownDatasetsLinks(unknownDatasets);
        if (w != null)
        {
            add(w, LayoutUtils.createRowLayoutSurroundingData());
        }
    }

    private void addVisualisation(ImageSampleContent imageSampleContent)
    {
        RowData margins = LayoutUtils.createRowLayoutSurroundingData();
        List<LogicalImageInfo> images = imageSampleContent.getLogicalImages();
        if (images.size() == 0)
        {
            add(new Text(NO_IMAGES_DATASET_LABEL), margins);
        } else
        {
            final ScreeningViewContext context = getViewContext();
            final LogicalImageLayouter logicalImageLayouter =
                    new LogicalImageLayouter(context, wellLocationOrNull, images);

            ImageDatasetEnrichedReference viewedImageDataSet =
                    context.tryCurrentlyViewedPlateDataSet(sampleId.getId());
            ImageDatasetEnrichedReference currentImageDataSet = images.get(0).getImageDataset();

            List<ImageDatasetEnrichedReference> list =
                    new ArrayList<ImageDatasetEnrichedReference>();

            if (null != viewedImageDataSet)
            {
                for (LogicalImageInfo imageInfos : images)
                {
                    ImageDatasetEnrichedReference ref = imageInfos.getImageDataset();
                    if (ref.getId().equals(viewedImageDataSet.getId()))
                    {
                        currentImageDataSet = ref;
                    } else
                    {
                        list.add(ref);
                    }
                }
            }
            list.add(0, currentImageDataSet);

            final Widget imageDatasetsDetails =
                    new ImagingDatasetGuiUtils(context)
                            .createImageDatasetDetailsRow(
                                    list,
                                    logicalImageLayouter);

            logicalImageLayouter.changeDisplayedImageDataset(currentImageDataSet);

            add(imageDatasetsDetails, margins);
            add(logicalImageLayouter, margins);
        }
    }
}
