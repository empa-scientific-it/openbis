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

import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ImagingDatasetGuiUtils.IDatasetImagesReferenceUpdater;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ImagingDatasetGuiUtils.IFeatureVectorDatasetReferenceUpdater;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.LayoutUtils;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.PlateLayouter;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMetadata;

/**
 * A section of a plate detail view which shows plate's wells and allow to check the content of the well quickly.
 * 
 * @author Tomasz Pylak
 */
public class PlateLayoutSampleSection extends TabContent
{
    // --- GUI messages (to be moved to the dictionary)

    private static final String PLATE_METADATA_REPORT_LABEL = "Plate Metadata Report: ";

    // ----

    private final TechId sampleId;

    public PlateLayoutSampleSection(final ScreeningViewContext viewContext, final TechId sampleId)
    {
        super("Plate Layout", viewContext, sampleId);
        this.sampleId = sampleId;
        setIds(DisplayTypeIDGenerator.PLATE_LAYOUT_SAMPLE_SECTION);
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
        context.getService().getPlateContent(sampleId, createDisplayPlateCallback());
    }

    private AsyncCallback<PlateContent> createDisplayPlateCallback()
    {
        return new AbstractAsyncCallback<PlateContent>(getViewContext())
            {
                @Override
                protected void process(PlateContent plateContent)
                {
                    removeAll();
                    setLayout(new RowLayout());
                    setScrollMode(Scroll.AUTO);

                    addPlateVisualisation(plateContent);
                    addPlateMetadataReportLink(plateContent);
                    addUnknownDatasetLinks(plateContent);

                    layout();
                }
            };
    }

    private void addUnknownDatasetLinks(PlateContent plateContent)
    {
        ImagingDatasetGuiUtils guiUtils = new ImagingDatasetGuiUtils(getViewContext());
        Widget w = guiUtils.tryCreateUnknownDatasetsLinks(plateContent.getUnknownDatasets());
        if (w != null)
        {
            add(w, LayoutUtils.createRowLayoutSurroundingData());
        }
    }

    private void addPlateVisualisation(PlateContent plateContent)
    {
        PlateMetadata plateMetadata = plateContent.getPlateMetadata();
        PlateLayouter plateLayouter = new PlateLayouter(getViewContext(), plateMetadata);

        List<ImageDatasetEnrichedReference> imageDatasets = plateContent.getImageDatasets();
        ImagingDatasetGuiUtils guiUtils = new ImagingDatasetGuiUtils(getViewContext());
        Widget imageDatasetDetailsRow =
                guiUtils.createImageDatasetDetailsRow(imageDatasets,
                        asImageDatasetUpdater(plateLayouter));
        Widget featureVectorDatasetDatailsRow =
                guiUtils.createFeatureVectorDatasetDetailsRow(
                        plateContent.getFeatureVectorDatasets(),
                        asFeatureVectorDatasetUpdater(plateLayouter));

        Widget plateLayout = plateLayouter.getView();

        boolean manyImageDatasets = imageDatasets.size() > 1;
        boolean manyFeatureVectorDatasets = plateContent.getFeatureVectorDatasets().size() > 1;
        layoutComponents(plateLayout, imageDatasetDetailsRow, manyImageDatasets,
                featureVectorDatasetDatailsRow, manyFeatureVectorDatasets);
    }

    private IFeatureVectorDatasetReferenceUpdater asFeatureVectorDatasetUpdater(
            final PlateLayouter plateLayouter)
    {
        return new IFeatureVectorDatasetReferenceUpdater()
            {
                @Override
                public void changeDisplayedFeatureVectorDataset(FeatureVectorDataset dataset)
                {
                    plateLayouter.changeDisplayedFeatureVectorDataset(dataset);
                }

                @Override
                public void changeDisplayedFeatureVectorDataset(ScreeningViewContext context,
                        DatasetReference dataset)
                {

                    plateLayouter.changeDisplayedFeatureVectorDataset(context, dataset);
                }
            };
    }

    private static IDatasetImagesReferenceUpdater asImageDatasetUpdater(
            final PlateLayouter plateLayouter)
    {
        return new IDatasetImagesReferenceUpdater()
            {
                @Override
                public void changeDisplayedImageDataset(
                        ImageDatasetEnrichedReference newImageDatasetOrNull)
                {
                    plateLayouter.changeDisplayedImageDataset(newImageDatasetOrNull);
                }
            };
    }

    private void layoutComponents(Widget plateLayout, Widget imageDatasetDetailsRow,
            boolean manyImageDatasets, Widget featureVectorDatasetDatailsRow,
            boolean manyFeatureVectorDatasets)
    {
        RowData margin = LayoutUtils.createRowLayoutSurroundingData();
        if (manyImageDatasets)
        {
            add(imageDatasetDetailsRow, margin);
        }
        if (manyFeatureVectorDatasets)
        {
            add(featureVectorDatasetDatailsRow, margin);
        }

        add(plateLayout, margin);

        if (manyImageDatasets == false)
        {
            add(imageDatasetDetailsRow, margin);
        }
        if (manyFeatureVectorDatasets == false)
        {
            add(featureVectorDatasetDatailsRow, margin);
        }
    }

    private void addPlateMetadataReportLink(final PlateContent plateContent)
    {
        Sample plate = plateContent.getPlateMetadata().getPlate();
        Widget generateLink = createPlateMetadataLink(plate, getViewContext());
        add(ImagingDatasetGuiUtils.withLabel(generateLink, PLATE_METADATA_REPORT_LABEL),
                LayoutUtils.createRowLayoutSurroundingData());
    }

    /** @return a button which shows a grid with the plate metadata */
    private static Widget createPlateMetadataLink(final Sample plate,
            final IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        String plateLinkUrl =
                ScreeningLinkExtractor.createPlateMetadataBrowserLink(plate.getPermId());
        return LinkRenderer.getLinkWidget(viewContext.getMessage(ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict.BUTTON_SHOW),
                new ClickHandler()
                    {
                        @Override
                        public void onClick(ClickEvent event)
                        {
                            PlateMetadataBrowser.openTab(plate, viewContext);
                        }
                    }, plateLinkUrl);
    }
}
