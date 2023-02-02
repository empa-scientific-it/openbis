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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.CheckBoxGroupWithModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.CheckBoxGroupWithModel.CheckBoxGroupListner;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.LabeledItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SimpleModelComboBox;
import ch.systemsx.cisd.openbis.generic.shared.basic.utils.GroupByMap;
import ch.systemsx.cisd.openbis.generic.shared.basic.utils.IGroupKeyExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.ImageDatasetChannel;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageChannelsReference;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.utils.EntityTypeLabelUtils;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.GuiUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetOverlayImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageResolution;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.IntensityRange;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.InternalImageChannel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.InternalImageTransformationInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Handles displaying images in different channels and allows to choose the overlays.
 * 
 * @author Tomasz Pylak
 */
class ChannelChooser
{

    public static interface IChanneledViewerFactory
    {
        LayoutContainer create(LogicalImageChannelsReference channelReferences,
                ImageResolution resolution);
    }

    // ---

    private static final String OVERLAYS_MSG = "Overlays:";

    private static final String CHANNEL_MSG = "Channel:";

    // ---

    private final IChanneledViewerFactory viewerFactory;

    private final IDefaultChannelState defaultChannelState;

    private final LayoutContainer imageContainer;

    private ResolutionChooser resolutionChooser;

    // --- state

    private LogicalImageReference basicImage;

    private Set<ImageDatasetChannel> selectedOverlayChannels;

    private List<String> basicChannelCodes;

    private String imageTransformationCodeOrNull;

    private Map<String, IntensityRange> rangesOrNull;

    // window id is the string identifying the kind of a view (either a tile view or well view)
    // It is used as a key to store selected image resolutions different for tile and well view
    private final String windowId;

    public ChannelChooser(LogicalImageReference basicImage, IChanneledViewerFactory viewerFactory,
            IDefaultChannelState defaultChannelState, String windowId)
    {
        this.basicImage = basicImage;
        this.viewerFactory = viewerFactory;
        this.imageContainer = new LayoutContainer();

        this.basicChannelCodes =
                getInitialChannelCodes(defaultChannelState, basicImage.getChannelsCodes());
        this.imageTransformationCodeOrNull =
                tryGetInitialImageTransformationCode(defaultChannelState, basicChannelCodes,
                        basicImage.getImagetParameters());
        this.rangesOrNull = tryGetInitialIntensityRange(defaultChannelState, basicChannelCodes);
        this.defaultChannelState = defaultChannelState;
        this.selectedOverlayChannels = new HashSet<ImageDatasetChannel>();
        this.windowId = windowId;
    }

    /** Refreshes the displayed images, but not the rest of the GUI */
    public void refresh(LogicalImageReference updatedBasicImage)
    {
        this.basicImage = updatedBasicImage;
        refresh();
    }

    /** Refreshes the displayed images, but not the rest of the GUI */
    public void refresh()
    {
        LogicalImageChannelsReference state =
                new LogicalImageChannelsReference(basicImage, basicChannelCodes,
                        imageTransformationCodeOrNull, rangesOrNull, selectedOverlayChannels);

        LayoutContainer view =
                viewerFactory.create(state,
                        resolutionChooser != null ? resolutionChooser.getResolution() : null);

        imageContainer.removeAll();
        imageContainer.add(view);
        imageContainer.layout();
    }

    public void addViewerTo(final LayoutContainer container,
            final IViewContext<IScreeningClientServiceAsync> context,
            final AsyncCallback<Void> callback)
    {
        final Widget loading =
                new Text(
                        context.getMessage(ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict.LOAD_IN_PROGRESS));
        container.add(loading);

        context.getService().getImageDatasetResolutions(basicImage.getDatasetCode(),
                basicImage.getDatastoreCode(),
                new AbstractAsyncCallback<List<ImageResolution>>(context)
                    {
                        @Override
                        protected void process(List<ImageResolution> resolutions)
                        {
                            container.remove(loading);

                            // overlays
                            List<DatasetOverlayImagesReference> overlayDatasets =
                                    basicImage.getOverlayDatasets();
                            if (overlayDatasets.size() > 0)
                            {
                                container
                                        .add(createOverlayChannelsChooser(overlayDatasets, context));
                            }

                            if (basicImage.getChannelsCodes().size() > 0)
                            {
                                Widget channelChooserWithLabel = createBasicChannelChooser(context);
                                container.add(channelChooserWithLabel);
                            }

                            container.add(createResolutionChooser(context, resolutions),
                                    new MarginData(5, 0, 5, 0));
                            container.add(imageContainer);
                            container.layout();

                            if (callback != null)
                            {
                                callback.onSuccess(null);
                            }

                            refresh();
                        }
                    });
    }

    private static Map<String, List<DatasetOverlayImagesReference>> groupByAnalysisProcedure(
            List<DatasetOverlayImagesReference> overlayDatasets)
    {
        return GroupByMap.create(overlayDatasets,
                new IGroupKeyExtractor<String, DatasetOverlayImagesReference>()
                    {
                        @Override
                        public String getKey(DatasetOverlayImagesReference dataset)
                        {
                            return dataset.tryGetAnalysisProcedure();
                        }
                    }).getMap();
    }

    public interface OverlayDataSetSelectionListener
    {
        public void overlayDataSetSelected(DatasetOverlayImagesReference reference);
    }

    public Widget createAndConnectOverlayImageDatasetChooser(String labelText,
            List<DatasetOverlayImagesReference> imageDatasets,
            final OverlayDataSetSelectionListener listener, IViewContext<?> viewContext)
    {
        List<String> labels =
                EntityTypeLabelUtils.createDatasetLabels(overlaysAsReferences(imageDatasets), true, true);
        imageDatasets.add(0, null);
        labels.add(0, "All");
        SimpleModelComboBox<DatasetOverlayImagesReference> datasetChooser =
                ImagingDatasetGuiUtils.createDatasetChooserComboBox(viewContext, imageDatasets, labels, labels);

        datasetChooser
                .addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<LabeledItem<DatasetOverlayImagesReference>>>()
                    {
                        @Override
                        public void selectionChanged(
                                SelectionChangedEvent<SimpleComboValue<LabeledItem<DatasetOverlayImagesReference>>> se)
                        {
                            DatasetOverlayImagesReference chosenDataset =
                                    SimpleModelComboBox.getChosenItem(se);
                            listener.overlayDataSetSelected(chosenDataset);
                        }
                    });
        DatasetOverlayImagesReference chosenDataset = datasetChooser.tryGetChosenItem();
        listener.overlayDataSetSelected(chosenDataset);

        return ImagingDatasetGuiUtils.withLabel(datasetChooser, labelText);
    }

    private static List<DatasetReference> overlaysAsReferences(
            List<DatasetOverlayImagesReference> imageDatasets)
    {
        List<DatasetReference> refs = new ArrayList<DatasetReference>();
        for (DatasetOverlayImagesReference dataset : imageDatasets)
        {
            DatasetReference r = dataset.getDatasetReference();
            refs.add(r);
        }
        return refs;
    }

    private Widget createOverlayChannelsChooser(
            List<DatasetOverlayImagesReference> overlayDatasets, IViewContext<?> viewContext)
    {
        LinkedList<DatasetOverlayImagesReference> sortedDataSets = new LinkedList<DatasetOverlayImagesReference>(overlayDatasets);
        Collections.sort(sortedDataSets, new ProcedureDatasetSortingOrder());

        final Map<String, List<DatasetOverlayImagesReference>> datasetsByAnalysisProcMap =
                groupByAnalysisProcedure(overlayDatasets);
        if (sortedDataSets.size() > 1)
        {
            LayoutContainer chooserPanel = new LayoutContainer();
            chooserPanel.setLayout(new RowLayout());

            final LayoutContainer objectsChooserContainer = new LayoutContainer();

            OverlayDataSetSelectionListener selectionListener =
                    createAnalysisProcedureSelectionListener(datasetsByAnalysisProcMap,
                            objectsChooserContainer);

            Widget overlaysChooser = createAndConnectOverlayImageDatasetChooser(OVERLAYS_MSG, sortedDataSets, selectionListener, viewContext);
            chooserPanel.add(overlaysChooser);
            chooserPanel.add(objectsChooserContainer);

            return chooserPanel;
        } else
        {
            final LayoutContainer objectsChooserContainer = new LayoutContainer();
            addOverlayChannelsChoosers(sortedDataSets, objectsChooserContainer);
            return objectsChooserContainer;
        }
    }

    private OverlayDataSetSelectionListener createAnalysisProcedureSelectionListener(
            final Map<String, List<DatasetOverlayImagesReference>> datasetsByAnalysisProcMap,
            final LayoutContainer objectsChooserContainer)
    {
        return new OverlayDataSetSelectionListener()
            {
                @Override
                public void overlayDataSetSelected(DatasetOverlayImagesReference reference)
                {
                    refreshObjectChooser(reference, datasetsByAnalysisProcMap, objectsChooserContainer);
                }
            };
    }

    private void refreshObjectChooser(DatasetOverlayImagesReference image,
            final Map<String, List<DatasetOverlayImagesReference>> datasetsByAnalysisProcMap,
            final LayoutContainer objectsChooserContainer)
    {
        List<DatasetOverlayImagesReference> overlayDatasetsForOneAnalysisProc = new LinkedList<DatasetOverlayImagesReference>();

        if (image == null)
        {
            for (String ap : datasetsByAnalysisProcMap.keySet())
            {
                overlayDatasetsForOneAnalysisProc.addAll(datasetsByAnalysisProcMap.get(ap));
            }
        }
        else
        {
            overlayDatasetsForOneAnalysisProc.add(image);
        }

        objectsChooserContainer.removeAll();
        if (overlayDatasetsForOneAnalysisProc.size() > 0)
        {
            addOverlayChannelsChoosers(overlayDatasetsForOneAnalysisProc, objectsChooserContainer);
        }
        objectsChooserContainer.layout();

        updateSelectedOverlayChannels(Collections.<CheckBoxGroupWithModel<ImageDatasetChannel>> emptyList());
    }

    private List<List<DatasetOverlayImagesReference>> splitIntoGroupsOfIdenticalAnalysisProcedure(List<DatasetOverlayImagesReference> overlayDatasets)
    {
        List<DatasetOverlayImagesReference> sortedDataSets = new LinkedList<DatasetOverlayImagesReference>();
        sortedDataSets.addAll(overlayDatasets);

        Collections.sort(sortedDataSets, new ProcedureDatasetSortingOrder());

        LinkedList<List<DatasetOverlayImagesReference>> result = new LinkedList<List<DatasetOverlayImagesReference>>();
        LinkedList<DatasetOverlayImagesReference> group = null;

        String currentAP = null;
        for (DatasetOverlayImagesReference dataSet : sortedDataSets)
        {
            String localAP = dataSet.tryGetAnalysisProcedure();
            if (localAP == null || localAP.isEmpty())
            {
                localAP = "Unspecified Analysis Procedure";
            }
            if (false == localAP.equals(currentAP))
            {
                group = null;
            }
            if (group == null)
            {
                group = new LinkedList<DatasetOverlayImagesReference>();
                result.add(group);
                currentAP = localAP;
            }
            group.add(dataSet);
        }
        return result;

    }

    private void addOverlayChannelsChoosers(List<DatasetOverlayImagesReference> overlayDatasets,
            LayoutContainer objectsChooserContainer)
    {

        List<List<DatasetOverlayImagesReference>> groups = splitIntoGroupsOfIdenticalAnalysisProcedure(overlayDatasets);

        if (groups.size() == 1 && overlayDatasets.size() > 1)
        {
            objectsChooserContainer.add(new HTML(OVERLAYS_MSG));
        }

        List<CheckBoxGroupWithModel<ImageDatasetChannel>> checkboxGroups = new LinkedList<CheckBoxGroupWithModel<ImageDatasetChannel>>();

        for (List<DatasetOverlayImagesReference> group : groups)
        {
            LayoutContainer container;
            if (groups.size() == 1)
            {
                container = objectsChooserContainer;
            }
            else
            {
                String analysisProcedure = group.get(0).tryGetAnalysisProcedure();
                if (analysisProcedure == null || analysisProcedure.isEmpty())
                {
                    analysisProcedure = "Unspecified Analysis Procedure";
                }
                container = new SectionFieldSet(analysisProcedure);
            }

            for (DatasetOverlayImagesReference dataSet : group)
            {
                container.add(createOverlayChannelsChooserForOneDataSet(dataSet, overlayDatasets.size() != 1, checkboxGroups));
            }

            if (container != objectsChooserContainer)
            {
                objectsChooserContainer.add(container);
            }
        }
    }

    private static class ProcedureDatasetSortingOrder implements Comparator<DatasetOverlayImagesReference>
    {
        private String key(DatasetOverlayImagesReference dataSet)
        {
            return dataSet.tryGetAnalysisProcedure() + ":" + dataSet.getDatasetCode();
        }

        @Override
        public int compare(DatasetOverlayImagesReference one, DatasetOverlayImagesReference two)
        {
            return key(one).compareTo(key(two));
        }
    }

    private static final class SectionFieldSet extends FieldSet
    {

        public SectionFieldSet(final String sectionName)
        {
            createForm(sectionName);
        }

        private void createForm(final String sectionName)
        {
            setHeading(sectionName);
            setLayout(createFormLayout());
            setAutoWidth(true);
        }

        private final FormLayout createFormLayout()
        {
            final FormLayout formLayout = new FormLayout();
            formLayout.setLabelWidth(AbstractRegistrationForm.SECTION_LABEL_WIDTH);
            formLayout.setDefaultWidth(AbstractRegistrationForm.SECTION_DEFAULT_FIELD_WIDTH);
            return formLayout;
        }
    }

    private Widget createOverlayChannelsChooserForOneDataSet(
            DatasetOverlayImagesReference overlayDataset, boolean withLabel, final List<CheckBoxGroupWithModel<ImageDatasetChannel>> checkboxes)
    {
        List<LabeledItem<ImageDatasetChannel>> overlayChannelItems =
                createOverlayChannelItems(overlayDataset);
        CheckBoxGroupWithModel<ImageDatasetChannel> checkBoxGroup =
                new CheckBoxGroupWithModel<ImageDatasetChannel>(overlayChannelItems);
        checkBoxGroup.addListener(new CheckBoxGroupListner<ImageDatasetChannel>()
            {
                @Override
                public void onChange(Set<ImageDatasetChannel> selected)
                {
                    updateSelectedOverlayChannels(checkboxes);
                }
            });
        checkboxes.add(checkBoxGroup);

        String label = OVERLAYS_MSG;

        if (withLabel)
        {
            String extraLabel =
                    overlayDataset.getDatasetReference().getLabelText() == null ? ""
                            : ", " + (overlayDataset.getDatasetReference().getLabelText());
            label = overlayDataset.getDatasetCode() + extraLabel + ":";
        }
        return GuiUtils.withLabel(checkBoxGroup, label);

    }

    private void updateSelectedOverlayChannels(List<CheckBoxGroupWithModel<ImageDatasetChannel>> checkboxes)
    {
        selectedOverlayChannels = new HashSet<ImageDatasetChannel>();

        for (CheckBoxGroupWithModel<ImageDatasetChannel> checkbox : checkboxes)
        {
            selectedOverlayChannels.addAll(checkbox.getSelected());
        }
        refresh();
    }

    private static List<LabeledItem<ImageDatasetChannel>> createOverlayChannelItems(
            DatasetImagesReference overlayDataset)
    {
        List<LabeledItem<ImageDatasetChannel>> items =
                new ArrayList<LabeledItem<ImageDatasetChannel>>();
        ImageDatasetParameters imageParams = overlayDataset.getImageParameters();
        for (int i = 0; i < imageParams.getChannelsNumber(); i++)
        {
            InternalImageChannel channel = imageParams.getInternalChannels().get(i);
            String channelCode = channel.getCode();
            String channelLabel = channel.getLabel();
            LabeledItem<ImageDatasetChannel> item =
                    createLabeledItem(overlayDataset, channelCode, channelLabel);
            items.add(item);
        }
        return items;
    }

    private static LabeledItem<ImageDatasetChannel> createLabeledItem(
            DatasetImagesReference overlayDataset, String channelCode, String channelLabel)
    {
        ImageDatasetChannel overlayChannel = createImageDatasetChannel(overlayDataset, channelCode);
        return new LabeledItem<ImageDatasetChannel>(overlayChannel, channelLabel);
    }

    private static ImageDatasetChannel createImageDatasetChannel(DatasetImagesReference dataset,
            String channelCode)
    {
        return new ImageDatasetChannel(dataset.getDatasetCode(), dataset.getDatastoreHostUrl(),
                channelCode);
    }

    private Widget createBasicChannelChooser(IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        final ChannelChooserPanel channelChooser =
                new ChannelChooserPanel(viewContext, defaultChannelState, basicChannelCodes,
                        basicImage.getImagetParameters());

        channelChooser
                .addSelectionChangedListener(new ChannelChooserPanel.ChannelSelectionListener()
                    {
                        @Override
                        public void selectionChanged(List<String> newlySelectedChannels,
                                @SuppressWarnings("hiding") String imageTransformationCodeOrNull,
                                @SuppressWarnings("hiding") Map<String, IntensityRange> rangesOrNull)
                        {
                            basicChannelCodes = newlySelectedChannels;
                            ChannelChooser.this.imageTransformationCodeOrNull =
                                    imageTransformationCodeOrNull;
                            ChannelChooser.this.rangesOrNull = rangesOrNull;
                            refresh();
                        }
                    });

        return GuiUtils.withLabel(channelChooser, CHANNEL_MSG, 0, 80);
    }

    private Widget createResolutionChooser(IViewContext<IScreeningClientServiceAsync> viewContext,
            List<ImageResolution> resolutions)
    {
        if (resolutionChooser == null)
        {
            resolutionChooser =
                    new ResolutionChooser(viewContext, resolutions,
                            defaultChannelState.tryGetDefaultResolution(windowId));
            resolutionChooser.addResolutionChangedListener(new Listener<BaseEvent>()
                {
                    @Override
                    public void handleEvent(BaseEvent be)
                    {
                        defaultChannelState.setDefaultResolution(resolutionChooser.getResolution(), windowId);
                        refresh();
                    }
                });
        }
        return GuiUtils.withLabel(resolutionChooser,
                viewContext.getMessage(Dict.RESOLUTION_CHOOSER_LABEL), 0, 80);
    }

    private static List<String> getInitialChannelCodes(IDefaultChannelState defaultChannelState,
            List<String> channels)
    {
        List<String> defaultChannels = defaultChannelState.tryGetDefaultChannels();
        if (defaultChannels == null || false == channels.containsAll(defaultChannels))
        {
            return channels;
        }
        return defaultChannels;
    }

    private static String tryGetInitialImageTransformationCode(
            IDefaultChannelState defaultChannelState, List<String> channels,
            ImageDatasetParameters imageParameters)
    {
        if (imageParameters != null)
        {
            String channel =
                    channels.size() == 1 ? channels.get(0) : ScreeningConstants.MERGED_CHANNELS;
            String initialTransformation = defaultChannelState.tryGetDefaultTransformation(channel);
            if (ChannelChooserPanel.DEFAULT_TRANSFORMATION_CODE.equals(initialTransformation))
            {
                return null;
            }

            String defaultSelection = null;
            List<InternalImageTransformationInfo> transformations =
                    imageParameters.getAvailableImageTransformationsFor(channel);
            for (InternalImageTransformationInfo transformation : transformations)
            {
                if (transformation.getCode().equals(initialTransformation))
                {
                    return initialTransformation;
                }
                if (transformation.isDefault() && defaultSelection == null)
                {
                    defaultSelection = transformation.getCode();
                }
            }

            return defaultSelection;
        }

        return null;
    }

    private static Map<String, IntensityRange> tryGetInitialIntensityRange(
            IDefaultChannelState defaultChannelState, List<String> channels)
    {
        Map<String, IntensityRange> ranges = new HashMap<String, IntensityRange>();

        for (String channel : channels)
        {
            IntensityRange rangeOrNull = defaultChannelState.tryGetIntensityRange(channel);
            ranges.put(channel, rangeOrNull);
        }

        return ranges;
    }
}
