/*
 * Copyright 2010 ETH Zuerich, CISD
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

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.DateRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.LabeledItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SimpleModelComboBox;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.GuiUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference;

/**
 * GUI utilities to create links and choosers of connected datasets: image datasets, feature vector
 * datasets and all the other unknown datasets.
 * 
 * @author Tomasz Pylak
 */
class ImagingDatasetGuiUtils
{
    private static final String UNKNOWN_DATASETS_LABEL = "Other connected datasets:";

    // --------

    private static final String LABEL_WIDTH_PX = "160";

    private static final int DATASET_COMBOBOX_CHOOSER_WIDTH_PX = 350;

    protected static interface IFeatureVectorDatasetReferenceUpdater
    {
        /** changes the feature vector dataset presented on the plate layout */
        public void changeDisplayedFeatureVectorDataset(FeatureVectorDataset dataset);
    }

    protected static interface IDatasetImagesReferenceUpdater
    {
        /** changes the image dataset from which images on the well detail view are displayed */
        void changeDisplayedImageDataset(ImageDatasetEnrichedReference dataset);
    }

    // ---

    private final IViewContext<?> viewContext;

    public ImagingDatasetGuiUtils(IViewContext<?> viewContext)
    {
        this.viewContext = viewContext;
    }

    public final Widget tryCreateUnknownDatasetsLinks(List<DatasetReference> unknownDatasets)
    {
        if (unknownDatasets.isEmpty())
        {
            return null;
        }
        LayoutContainer c = new LayoutContainer();
        c.setLayout(new RowLayout());
        c.add(new Text(UNKNOWN_DATASETS_LABEL));
        for (DatasetReference dataset : unknownDatasets)
        {
            String label = createUnknownDatasetLabel(dataset);
            Widget detailsLink = createDatasetDetailsLink(dataset, label, viewContext);
            // WORKAROUND without wrapping in table all links are rendered in the same line )in
            // spite of the row layout)
            c.add(warpInTable(detailsLink));
        }
        return c;
    }

    private static LayoutContainer warpInTable(Widget detailsLink)
    {
        LayoutContainer container = new LayoutContainer();
        container.setLayout(new TableLayout(1));
        container.add(detailsLink);
        return container;
    }

    private static String createUnknownDatasetLabel(DatasetReference datasetReference)
    {
        return datasetReference.getEntityType().getCode() + ", registered on "
                + createDatasetLabel(datasetReference);
    }

    public Widget createFeatureVectorDatasetDetailsRow(
            List<FeatureVectorDataset> featureVectorDatasets,
            IFeatureVectorDatasetReferenceUpdater datasetUpdater)
    {
        return ImageAnalysisDatasetDetails.createFeatureVectorDatasetDetailsRow(
                featureVectorDatasets, datasetUpdater, viewContext);
    }

    private static final class ImageAnalysisDatasetDetails
    {
        public static Widget createFeatureVectorDatasetDetailsRow(
                final List<FeatureVectorDataset> featureVectorDatasets,
                IFeatureVectorDatasetReferenceUpdater datasetUpdater, IViewContext<?> viewContext)
        {
            return new ImageAnalysisDatasetDetails(viewContext)
                    .createFeatureVectorDatasetDetailsRow(featureVectorDatasets, datasetUpdater);
        }

        // --- GUI messages (to be moved to the dictionary)

        private static final String SHOW_CHOSEN_ANALYSIS_DATASET_BUTTON = "Show Report";

        private static final String NO_IMAGE_ANALYSIS_DATASET_LABEL =
                "No image analysis data is available.";

        private static final String IMAGE_ANALYSIS_DATASET_CHOOSER_LABEL =
                "Images analysis results: ";

        // ---

        private final IViewContext<?> viewContext;

        private ImageAnalysisDatasetDetails(IViewContext<?> viewContext)
        {
            this.viewContext = viewContext;
        }

        private Widget createFeatureVectorDatasetDetailsRow(
                final List<FeatureVectorDataset> featureVectorDatasets,
                IFeatureVectorDatasetReferenceUpdater datasetUpdater)
        {
            if (featureVectorDatasets.size() == 0)
            {
                return new Text(NO_IMAGE_ANALYSIS_DATASET_LABEL);
            } else if (featureVectorDatasets.size() == 1)
            {
                FeatureVectorDataset featureVectorDataset = featureVectorDatasets.get(0);
                return createAndConnectFeatureVectorDatasetInfo(featureVectorDataset,
                        datasetUpdater);
            } else
            {
                return createAndConnectFeatureVectorDatasetChooser(featureVectorDatasets,
                        datasetUpdater);
            }
        }

        private Widget createAndConnectFeatureVectorDatasetChooser(
                List<FeatureVectorDataset> featureVectorDatasets,
                final IFeatureVectorDatasetReferenceUpdater datasetUpdater)
        {
            final SimpleModelComboBox<FeatureVectorDataset> datasetChooser =
                    createDatasetChooserComboBox(viewContext, featureVectorDatasets,
                            createDatasetLabels(asFeatureVectorReferences(featureVectorDatasets)));
            final Anchor datasetDetailsButton = createImageAnalysisDetailsButton(datasetChooser);
            datasetChooser
                    .addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<LabeledItem<FeatureVectorDataset>>>()
                        {
                            @Override
                            public void selectionChanged(
                                    SelectionChangedEvent<SimpleComboValue<LabeledItem<FeatureVectorDataset>>> se)
                            {
                                FeatureVectorDataset chosenDataset =
                                        SimpleModelComboBox.getChosenItem(se);
                                datasetUpdater.changeDisplayedFeatureVectorDataset(chosenDataset);
                                updateFeatureVectorDatasetSimpleViewModeLink(datasetChooser,
                                        datasetDetailsButton);
                            }
                        });
            FeatureVectorDataset chosenDataset = datasetChooser.getChosenItem();
            datasetUpdater.changeDisplayedFeatureVectorDataset(chosenDataset);

            return GuiUtils.renderInRow(
                    withLabel(datasetChooser, IMAGE_ANALYSIS_DATASET_CHOOSER_LABEL),
                    datasetDetailsButton);
        }

        private Widget createAndConnectFeatureVectorDatasetInfo(
                FeatureVectorDataset featureVectorDataset,
                IFeatureVectorDatasetReferenceUpdater datasetUpdater)
        {
            datasetUpdater.changeDisplayedFeatureVectorDataset(featureVectorDataset);

            Widget datasetDetailsLink =
                    createDatasetDetailsLink(featureVectorDataset.getDatasetReference(),
                            SHOW_CHOSEN_ANALYSIS_DATASET_BUTTON, viewContext);
            return withLabel(datasetDetailsLink, IMAGE_ANALYSIS_DATASET_CHOOSER_LABEL);
        }

        private static List<DatasetReference> asFeatureVectorReferences(
                List<FeatureVectorDataset> featureVectorDatasets)
        {
            List<DatasetReference> refs = new ArrayList<DatasetReference>();
            for (FeatureVectorDataset dataset : featureVectorDatasets)
            {
                refs.add(dataset.getDatasetReference());
            }
            return refs;
        }

        private Anchor createImageAnalysisDetailsButton(
                final SimpleModelComboBox<FeatureVectorDataset> datasetChooser)
        {
            return LinkRenderer.getLinkAnchor(SHOW_CHOSEN_ANALYSIS_DATASET_BUTTON,
                    new ClickHandler()
                        {
                            public void onClick(ClickEvent event)
                            {
                                DatasetReference datasetReference =
                                        datasetChooser.getChosenItem().getDatasetReference();
                                openDatasetDetails(datasetReference, viewContext);
                            }
                        }, createDatasetSimpleViewModeHref(datasetChooser));
        }

        private void updateFeatureVectorDatasetSimpleViewModeLink(
                final SimpleModelComboBox<FeatureVectorDataset> datasetChooser, final Anchor anchor)
        {
            if (viewContext.isSimpleOrEmbeddedMode())
            {
                anchor.setHref("#" + createDatasetSimpleViewModeHref(datasetChooser));
            }
        }

        private String createDatasetSimpleViewModeHref(
                SimpleModelComboBox<FeatureVectorDataset> datasetChooser)
        {
            return LinkExtractor.tryExtract(datasetChooser.getChosenItem().getDatasetReference());
        }
    }

    public final Widget createImageDatasetDetailsRow(
            List<ImageDatasetEnrichedReference> imageDatasets,
            IDatasetImagesReferenceUpdater datasetUpdater)
    {
        return ImageDatasetDetails.createImageDatasetDetailsRow(imageDatasets, datasetUpdater,
                viewContext);
    }

    private static final class ImageDatasetDetails
    {

        public static Widget createImageDatasetDetailsRow(
                List<ImageDatasetEnrichedReference> imageDatasets,
                IDatasetImagesReferenceUpdater datasetUpdater, IViewContext<?> viewContext)
        {
            return new ImageDatasetDetails(viewContext).createImageDatasetDetailsRow(imageDatasets,
                    datasetUpdater);
        }

        // --- GUI messages (to be moved to the dictionary)

        private static final String IMAGES_DATASET_CHOOSER_LABEL = "Images acquired on: ";

        private static final String SINGLE_IMAGE_DATASET_DETAILS_LABEL =
                "Image acquisition details: ";

        private static final String SHOW_CHOSEN_IMAGE_DATASET_DETAILS_BUTTON = "Advanced";

        private static final String NO_IMAGES_DATASET_LABEL = "No images data has been acquired.";

        // ---

        private final IViewContext<?> viewContext;

        private ImageDatasetDetails(IViewContext<?> viewContext)
        {
            this.viewContext = viewContext;
        }

        private final Widget createImageDatasetDetailsRow(
                List<ImageDatasetEnrichedReference> imageDatasets,
                IDatasetImagesReferenceUpdater datasetUpdater)
        {
            if (imageDatasets.size() == 0)
            {
                return new Text(NO_IMAGES_DATASET_LABEL);
            } else if (imageDatasets.size() == 1)
            {
                ImageDatasetEnrichedReference imageDataset = imageDatasets.get(0);
                return createAndConnectImageDatasetInfo(imageDataset, datasetUpdater);
            } else
            {
                return createAndConnectImageDatasetChooser(imageDatasets, datasetUpdater);
            }
        }

        private Widget createAndConnectImageDatasetInfo(ImageDatasetEnrichedReference imageDataset,
                IDatasetImagesReferenceUpdater datasetUpdater)
        {
            Widget datasetDetailsButton =
                    createDatasetDetailsLink(imageDataset.getImageDataset().getDatasetReference(),
                            SHOW_CHOSEN_IMAGE_DATASET_DETAILS_BUTTON, viewContext);
            Widget imageDatasetDetailsRow =
                    withLabel(datasetDetailsButton, SINGLE_IMAGE_DATASET_DETAILS_LABEL);
            datasetUpdater.changeDisplayedImageDataset(imageDataset);
            return imageDatasetDetailsRow;
        }

        private Widget createAndConnectImageDatasetChooser(
                List<ImageDatasetEnrichedReference> imageDatasets,
                final IDatasetImagesReferenceUpdater datasetUpdater)
        {
            final SimpleModelComboBox<ImageDatasetEnrichedReference> datasetChooser =
                    createDatasetChooserComboBox(viewContext, imageDatasets,
                            createDatasetLabels(asReferences(imageDatasets)));

            final Anchor datasetDetailsButton = createImageDetailsButton(datasetChooser);
            datasetChooser
                    .addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<LabeledItem<ImageDatasetEnrichedReference>>>()
                        {
                            @Override
                            public void selectionChanged(
                                    SelectionChangedEvent<SimpleComboValue<LabeledItem<ImageDatasetEnrichedReference>>> se)
                            {
                                ImageDatasetEnrichedReference chosenDataset =
                                        SimpleModelComboBox.getChosenItem(se);
                                datasetUpdater.changeDisplayedImageDataset(chosenDataset);
                                updateImageDatasetSimpleViewModeLink(datasetChooser,
                                        datasetDetailsButton);
                            }
                        });
            ImageDatasetEnrichedReference chosenDataset = datasetChooser.getChosenItem();
            datasetUpdater.changeDisplayedImageDataset(chosenDataset);

            return GuiUtils.renderInRow(withLabel(datasetChooser, IMAGES_DATASET_CHOOSER_LABEL),
                    datasetDetailsButton);
        }

        private Anchor createImageDetailsButton(
                final SimpleModelComboBox<ImageDatasetEnrichedReference> imageDatasetChooser)
        {
            return LinkRenderer.getLinkAnchor(SHOW_CHOSEN_IMAGE_DATASET_DETAILS_BUTTON,
                    new ClickHandler()
                        {
                            public void onClick(ClickEvent event)
                            {
                                openDatasetDetails(getChosenDatasetReference(imageDatasetChooser),
                                        viewContext);
                            }
                        }, createImageDatasetSimpleViewModeHref(imageDatasetChooser));
        }

        private void updateImageDatasetSimpleViewModeLink(
                final SimpleModelComboBox<ImageDatasetEnrichedReference> imageDatasetChooser,
                final Anchor anchor)
        {
            if (viewContext.isSimpleOrEmbeddedMode())
            {
                anchor.setHref("#" + createImageDatasetSimpleViewModeHref(imageDatasetChooser));
            }
        }

        private static String createImageDatasetSimpleViewModeHref(
                final SimpleModelComboBox<ImageDatasetEnrichedReference> imageDatasetChooser)
        {
            return LinkExtractor.tryExtract(getChosenDatasetReference(imageDatasetChooser));
        }

        private static DatasetReference getChosenDatasetReference(
                final SimpleModelComboBox<ImageDatasetEnrichedReference> imageDatasetChooser)
        {
            return imageDatasetChooser.getChosenItem().getImageDataset().getDatasetReference();
        }

    }

    // ----------- generic helpers --------------------

    private static String createDatasetLabel(DatasetReference datasetReference)
    {
        String registrationDate =
                DateRenderer.renderDate(datasetReference.getRegistrationDate(),
                        BasicConstant.DATE_WITHOUT_TIME_FORMAT_PATTERN);
        return registrationDate + ", " + datasetReference.getCode() + " ("
                + datasetReference.getFileTypeCode() + ")";
    }

    private final static List<String> createDatasetLabels(List<DatasetReference> datasetReferences)
    {
        List<String> labels = new ArrayList<String>(datasetReferences.size());
        for (DatasetReference dataset : datasetReferences)
        {
            labels.add(createDatasetLabel(dataset));
        }
        return labels;
    }

    private static Widget createDatasetDetailsLink(final DatasetReference dataset, String label,
            final IViewContext<?> viewContext)
    {
        String href = LinkExtractor.tryExtract(dataset);
        assert href != null : "invalid link for " + dataset;
        ClickHandler listener = new ClickHandler()
            {
                public void onClick(ClickEvent event)
                {
                    openDatasetDetails(dataset, viewContext);
                }
            };
        return LinkRenderer.getLinkWidget(label, listener, href);
    }

    private static void openDatasetDetails(DatasetReference selectedDatasetReference,
            IViewContext<?> viewContext)
    {
        new OpenEntityDetailsTabAction(selectedDatasetReference, viewContext).execute();
    }

    static Widget withLabel(Widget widet, String label)
    {
        LayoutContainer container = new LayoutContainer();
        container.setLayout(new TableLayout(2));
        Text labelWidget = new Text(label);
        labelWidget.setWidth(LABEL_WIDTH_PX);
        container.add(labelWidget);
        container.add(widet);
        return container;
    }

    private static List<DatasetReference> asReferences(
            List<ImageDatasetEnrichedReference> imageDatasets)
    {
        List<DatasetReference> refs = new ArrayList<DatasetReference>();
        for (ImageDatasetEnrichedReference dataset : imageDatasets)
        {
            refs.add(dataset.getImageDataset().getDatasetReference());
        }
        return refs;
    }

    private static <T> SimpleModelComboBox<T> createDatasetChooserComboBox(
            IMessageProvider messageProvider, List<T> items, List<String> labels)
    {
        return new SimpleModelComboBox<T>(messageProvider, items, labels,
                DATASET_COMBOBOX_CHOOSER_WIDTH_PX);
    }
}
