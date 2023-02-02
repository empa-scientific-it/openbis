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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.NotScrollableContainer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ClientPluginFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ChannelWidgetWithListener.ISimpleChanneledViewerFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.utils.MaterialComponentUtils;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.utils.PropertiesUtil;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.IntensityRange;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellReplicaImage;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;

/**
 * Component which for a specified material and experiment presents 1. feature vectors (detailed and aggregated across all replicas of one assay) 2.
 * images grouped by biological and technical replicates.
 * 
 * @author Kaloyan Enimanev
 * @author Tomasz Pylak
 */
public class MaterialReplicaSummaryComponent
{
    private static final String LOADING_IMAGES_DICT_MSG = "Loading images...";

    private static final String REPLICATE_ABBREV_DICT_MSG = "repl.";

    private static final String NO_IMAGES_AVAILABLE_DICT_MSG = "No images available.";

    private static final int ONE_IMAGE_SIZE_FACTOR_PX = 60;

    public static IDisposableComponent createViewer(
            IViewContext<IScreeningClientServiceAsync> screeningViewContext, Experiment experiment,
            Material material, boolean restrictGlobalScopeLinkToProject,
            AnalysisProcedureListenerHolder analysisProcedureListenerHolder)
    {
        return new MaterialReplicaSummaryComponent(screeningViewContext,
                restrictGlobalScopeLinkToProject, experiment, null).createViewer(material,
                analysisProcedureListenerHolder);
    }

    public static IDisposableComponent createViewer(
            IViewContext<IScreeningClientServiceAsync> screeningViewContext, Experiment experiment,
            Material material, boolean restrictGlobalScopeLinkToProject,
            AnalysisProcedureCriteria analysisProcedureCriteria)
    {
        return new MaterialReplicaSummaryComponent(screeningViewContext,
                restrictGlobalScopeLinkToProject, experiment, analysisProcedureCriteria)
                .createViewer(material);
    }

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private final boolean restrictGlobalScopeLinkToProject;

    private final Experiment experiment;

    private final AnalysisProcedureCriteria initialAnalysisProcedureCriteriaOrNull;

    private MaterialReplicaSummaryComponent(
            IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            boolean restrictGlobalScopeLinkToProject, Experiment experiment,
            AnalysisProcedureCriteria initialAnalysisProcedureCriteriaOrNull)
    {
        this.screeningViewContext = screeningViewContext;
        this.restrictGlobalScopeLinkToProject = restrictGlobalScopeLinkToProject;
        this.experiment = experiment;
        this.initialAnalysisProcedureCriteriaOrNull = initialAnalysisProcedureCriteriaOrNull;
    }

    private class ImagesFoundCallback extends AbstractAsyncCallback<List<WellReplicaImage>>
    {
        private final LayoutContainer imagesPanel;

        ImagesFoundCallback(LayoutContainer imagesPanel)
        {
            super(screeningViewContext);
            this.imagesPanel = imagesPanel;
        }

        @Override
        protected void process(List<WellReplicaImage> images)
        {
            imagesPanel.removeAll();
            imagesPanel.add(createImagePanel(images), createImagePanelLayoutData());
            imagesPanel.layout();
        }
    }

    private static RowData createImagePanelLayoutData()
    {
        return new RowData(-1, -1, new Margins(10, 0, 10, 10));
    }

    private Widget createImagePanel(List<WellReplicaImage> images)
    {
        if (images.isEmpty())
        {
            return new Text(NO_IMAGES_AVAILABLE_DICT_MSG);
        }
        String displayTypeId = ScreeningDisplayTypeIDGenerator.EXPERIMENT_CHANNEL.createID(null);
        final IDefaultChannelState defaultChannelState =
                new DefaultChannelState(screeningViewContext, displayTypeId);
        ChannelChooserPanel channelChooser =
                new ChannelChooserPanel(screeningViewContext, defaultChannelState);
        LayoutContainer panel = new NotScrollableContainer();
        panel.setLayout(new RowLayout());
        panel.add(channelChooser);

        Map<String, List<WellReplicaImage>> labelToReplicasMap = createSortedImageMap(images);
        String orphanGroupKey = null;
        List<WellReplicaImage> orphanTechnicalReplicates = labelToReplicasMap.get(orphanGroupKey);
        if (orphanTechnicalReplicates != null)
        {
            Widget technicalReplicatesPanel =
                    createOrphanTechnicalReplicatesPanel(orphanTechnicalReplicates, channelChooser);
            panel.add(technicalReplicatesPanel);
            labelToReplicasMap.remove(orphanGroupKey);
        }
        LayoutContainer biologicalReplicatesImagesPanel =
                createBiologicalReplicatesImagesPanel(labelToReplicasMap, channelChooser);
        panel.add(biologicalReplicatesImagesPanel);
        return panel;
    }

    private Widget createOrphanTechnicalReplicatesPanel(
            List<WellReplicaImage> orphanTechnicalReplicates, ChannelChooserPanel channelChooser)
    {
        LayoutContainer imagePanel = new LayoutContainer();
        boolean shouldDisplayLabels = orphanTechnicalReplicates.size() > 1;
        int oneImageSizeFactorPx = getOneImageSizeFactorPx(1);

        for (WellReplicaImage image : orphanTechnicalReplicates)
        {
            LayoutContainer imageWithLabel = new LayoutContainer();
            imageWithLabel.setLayout(new RowLayout());

            if (shouldDisplayLabels)
            {
                Widget label =
                        createTechnicalReplicateLabel(image.getTechnicalReplicateSequenceNumber());
                imageWithLabel.add(label);
            }

            Widget imageViewer =
                    createImageViewer(image.getWellImage(), channelChooser, oneImageSizeFactorPx);
            imageWithLabel.add(imageViewer);

            imagePanel.add(imageWithLabel);
        }
        return imagePanel;
    }

    private LayoutContainer createBiologicalReplicatesImagesPanel(
            Map<String, List<WellReplicaImage>> labelToReplicasMap,
            ChannelChooserPanel channelChooser)
    {
        LayoutContainer imagePanel = new LayoutContainer();

        int maxReplicaNumber = calcMaxReplicaNumber(labelToReplicasMap);
        if (maxReplicaNumber == 0)
        {
            // nothing to display
            return imagePanel;
        }

        TableLayout layout = new TableLayout(maxReplicaNumber + 1);
        layout.setBorder(1);
        layout.setCellPadding(5);
        imagePanel.setLayout(layout);

        addImageTableHeader(maxReplicaNumber, imagePanel);
        List<String> sortedLabels = sortCopy(labelToReplicasMap.keySet());
        for (String label : sortedLabels)
        {
            List<WellReplicaImage> sortedTechnicalReplicates = labelToReplicasMap.get(label);

            imagePanel.add(new Text(label));
            for (int i = 0; i < maxReplicaNumber; i++)
            {
                if (i < sortedTechnicalReplicates.size())
                {
                    WellContent wellImage = sortedTechnicalReplicates.get(i).getWellImage();
                    Widget imageViewer =
                            createImageViewer(wellImage, channelChooser,
                                    getOneImageSizeFactorPx(maxReplicaNumber));
                    imagePanel.add(imageViewer);
                } else
                {
                    imagePanel.add(createEmptyBox());
                }
            }
        }
        return imagePanel;
    }

    // The image size is dependent on number of replicas (each is displayed in one column) in a
    // following way:
    // replicas number -> magnification
    // 1 -> 1.5
    // 2 -> 1.25
    // 3 -> 1
    // other -> 0.75
    private int getOneImageSizeFactorPx(int maxReplicaNumber)
    {
        double columnFactor = 1 + 0.25 * (3 - maxReplicaNumber);
        columnFactor = Math.max(columnFactor, 0.75);
        return Math.max(64, (int) (ONE_IMAGE_SIZE_FACTOR_PX * columnFactor));
    }

    private static List<String> sortCopy(Set<String> values)
    {
        List<String> sorted = new ArrayList<String>(values);
        Collections.sort(sorted);
        return sorted;
    }

    private void addImageTableHeader(int maxReplicaNumber, LayoutContainer imagePanel)
    {
        for (int i = 0; i <= maxReplicaNumber; i++)
        {
            if (i == 0)
            {
                imagePanel.add(createEmptyBox());
            } else
            {
                imagePanel.add(createTechnicalReplicateLabel(i));
            }
        }
    }

    private Widget createTechnicalReplicateLabel(int technicalReplicateSequence)
    {
        return new Text(REPLICATE_ABBREV_DICT_MSG + " " + technicalReplicateSequence);
    }

    private Widget createEmptyBox()
    {
        return new Text();
    }

    private static int calcMaxReplicaNumber(Map<String, List<WellReplicaImage>> labelToReplicasMap)
    {
        int max = 0;
        for (List<WellReplicaImage> technicalReplicates : labelToReplicasMap.values())
        {
            max = Math.max(max, technicalReplicates.size());
        }
        return max;
    }

    private static Map<String/* label */, List<WellReplicaImage>> createSortedImageMap(
            List<WellReplicaImage> images)
    {
        Map<String, List<WellReplicaImage>> map = new HashMap<String, List<WellReplicaImage>>();
        for (WellReplicaImage image : images)
        {
            String label = image.tryGetBiologicalReplicateLabel();
            List<WellReplicaImage> technicalReplicas = map.get(label);
            if (technicalReplicas == null)
            {
                technicalReplicas = new ArrayList<WellReplicaImage>();
            }
            technicalReplicas.add(image);
            map.put(label, technicalReplicas);
        }
        sortTechnicalReplicas(map);
        return map;
    }

    private static void sortTechnicalReplicas(Map<String, List<WellReplicaImage>> map)
    {
        for (List<WellReplicaImage> technicalReplicas : map.values())
        {
            Collections.sort(technicalReplicas, new Comparator<WellReplicaImage>()
                {
                    @Override
                    public int compare(WellReplicaImage arg1, WellReplicaImage arg2)
                    {
                        Integer s1 = arg1.getTechnicalReplicateSequenceNumber();
                        Integer s2 = arg2.getTechnicalReplicateSequenceNumber();
                        return s1.compareTo(s2);
                    }
                });
        }
    }

    private Widget createImageViewer(final WellContent image, ChannelChooserPanel channelChooser,
            final int oneImageSizeFactorPx)
    {
        assert image.tryGetImageDataset() != null;
        final ISimpleChanneledViewerFactory viewerFactory = new ISimpleChanneledViewerFactory()
            {
                @Override
                public Widget create(List<String> channels, String imageTransformationCodeOrNull,
                        Map<String, IntensityRange> rangesOrNull)
                {
                    return WellContentDialog.createImageViewerForChannel(screeningViewContext,
                            image, oneImageSizeFactorPx, channels, imageTransformationCodeOrNull,
                            rangesOrNull);
                }
            };
        ChannelWidgetWithListener widgetWithListener = new ChannelWidgetWithListener(viewerFactory);
        widgetWithListener.selectionChanged(channelChooser.getSelectedValues(),
                channelChooser.tryGetSelectedTransformationCode(false),
                channelChooser.tryGetSelectedIntensityRanges());

        ImageDatasetParameters imageParameters = image.tryGetImageDataset().getImageParameters();
        channelChooser.addSelectionChangedListener(widgetWithListener);
        channelChooser.addChannels(imageParameters);

        return widgetWithListener.asWidget();
    }

    private IDisposableComponent createViewer(Material material)
    {
        final IDisposableComponent gridComponent =
                MaterialReplicaFeatureSummaryGrid.createForEmbeddedMode(screeningViewContext,
                        new TechId(experiment), new TechId(material),
                        initialAnalysisProcedureCriteriaOrNull);
        return createViewer(material, gridComponent);
    }

    private IDisposableComponent createViewer(Material material,
            AnalysisProcedureListenerHolder analysisProcedureListenerHolderOrNull)
    {
        final IDisposableComponent gridComponent =
                MaterialReplicaFeatureSummaryGrid.create(screeningViewContext, new TechId(
                        experiment), new TechId(material), analysisProcedureListenerHolderOrNull);
        return createViewer(material, gridComponent);
    }

    private IDisposableComponent createViewer(Material material,
            final IDisposableComponent gridComponent)
    {
        final LayoutContainer panel = new LayoutContainer();
        panel.setLayout(new RowLayout(Orientation.VERTICAL));
        panel.setScrollMode(Scroll.AUTO);

        Widget materialInfo = createMaterialInfo(material);
        panel.add(materialInfo, new RowData(-1, -1, PropertiesUtil.createHeaderInfoMargin()));

        TechId materialTechId = new TechId(material);
        TechId experimentTechId = new TechId(experiment);
        // NOTE: if the width is 100% then the vertical scrollbar of the grid is not visible
        panel.add(gridComponent.getComponent(), new RowData(0.97, 400));

        LayoutContainer imagesPanel = new NotScrollableContainer();
        imagesPanel.setLayout(new RowLayout());
        imagesPanel.add(new Text(LOADING_IMAGES_DICT_MSG), createImagePanelLayoutData());
        panel.add(imagesPanel);
        screeningViewContext.getService().listWellImages(materialTechId, experimentTechId,
                new ImagesFoundCallback(imagesPanel));

        return new IDisposableComponent()
            {

                @Override
                public void update(Set<DatabaseModificationKind> observedModifications)
                {
                }

                @Override
                public DatabaseModificationKind[] getRelevantModifications()
                {
                    return new DatabaseModificationKind[0];
                }

                @Override
                public Component getComponent()
                {
                    return panel;
                }

                @Override
                public void dispose()
                {
                    gridComponent.dispose();
                }
            };
    }

    private Widget createMaterialInfo(final Material material)
    {
        LayoutContainer panel = new LayoutContainer();
        panel.setLayout(new RowLayout());

        Widget headerWidget = createHeaderWithLinks(material);
        panel.add(headerWidget, PropertiesUtil.createHeaderTitleLayoutData());

        LayoutContainer materialPropertiesPanel = createMaterialPropertiesPanel(material);
        panel.add(materialPropertiesPanel);

        return panel;
    }

    private Widget createHeaderWithLinks(final Material material)
    {
        LayoutContainer headerPanel = new LayoutContainer();
        headerPanel.setLayout(new TableLayout(2));

        Widget headingWidget = createHeaderTitle(experiment, material, screeningViewContext);
        headerPanel.add(headingWidget);

        LayoutContainer rightLinksPanel = new LayoutContainer();
        rightLinksPanel.setLayout(new RowLayout());
        RowData linkMargins = new RowData(-1, -1, new Margins(0, 0, 0, 50));

        // in non-embedded mode there is a separate tab in material detail view with all assays
        // information, so we do not display this link there
        if (screeningViewContext.getModel().isEmbeddedMode()
                && initialAnalysisProcedureCriteriaOrNull != null)
        {
            Widget materialInAllAssaysSummaryLink =
                    createMaterialInAllAssaysSummaryLink(material,
                            initialAnalysisProcedureCriteriaOrNull);
            rightLinksPanel.add(materialInAllAssaysSummaryLink, linkMargins);
        }

        Widget assayAnalysisSummaryLink = createAssayAnalysisSummaryLink(material);
        rightLinksPanel.add(assayAnalysisSummaryLink, linkMargins);

        headerPanel.add(rightLinksPanel);
        return headerPanel;
    }

    private Widget createMaterialInAllAssaysSummaryLink(final Material material,
            final AnalysisProcedureCriteria analysisProcedureCriteria)
    {
        final ExperimentSearchCriteria experimentCriteria = createAllAssaysExperimentCriteria();
        String linkUrl =
                ScreeningLinkExtractor.createMaterialDetailsLink(material, experimentCriteria);
        String linkText =
                screeningViewContext.getMessage(Dict.FIND_IN_ALL_ASSAYS,
                        MaterialComponentUtils.getMaterialFullName(material, false));
        Widget linkWidget = LinkRenderer.getLinkWidget(linkText, new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {
                    ClientPluginFactory.openImagingMaterialViewer(material, experimentCriteria,
                            analysisProcedureCriteria, false, screeningViewContext);
                }
            }, linkUrl);
        return linkWidget;
    }

    private ExperimentSearchCriteria createAllAssaysExperimentCriteria()
    {
        if (restrictGlobalScopeLinkToProject)
        {
            Project project = experiment.getProject();
            BasicProjectIdentifier projectIdentifier =
                    new BasicProjectIdentifier(project.getSpace().getCode(), project.getCode());
            return ExperimentSearchCriteria.createAllExperimentsForProject(projectIdentifier);
        } else
        {
            return ExperimentSearchCriteria.createAllExperiments();
        }
    }

    private static Html createHeaderTitle(final IEntityInformationHolderWithPermId experiment,
            final Material material, IViewContext<IScreeningClientServiceAsync> screeningViewContext)
    {
        String materialDesc = MaterialComponentUtils.getMaterialFullName(material, true);
        String headingText =
                screeningViewContext.getMessage(Dict.MATERIAL_IN_ASSAY, materialDesc,
                        experiment.getCode());
        return PropertiesUtil.createHeaderTitle(headingText);
    }

    private Widget createAssayAnalysisSummaryLink(final Material material)
    {
        // add link to feature vector summary for the experiment
        String linkUrl =
                ClientPluginFactory.createImagingExperimentViewerLink(experiment,
                        restrictGlobalScopeLinkToProject, screeningViewContext);
        String linkText = screeningViewContext.getMessage(Dict.SHOW_ASSAY, experiment.getCode());
        Widget linkWidget = LinkRenderer.getLinkWidget(linkText, new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {
                    ClientPluginFactory.openImagingExperimentViewer(experiment,
                            restrictGlobalScopeLinkToProject, screeningViewContext);
                }
            }, linkUrl);
        return linkWidget;
    }

    private static LayoutContainer createMaterialPropertiesPanel(final Material material)
    {
        LayoutContainer propertiesPanel = new LayoutContainer();
        propertiesPanel.setLayout(new RowLayout());
        PropertiesUtil.addProperties(material, propertiesPanel,
                MaterialComponentUtils.createAdditionalMaterialProperties(material),
                MaterialComponentUtils.getExcludedMatrialProperties());
        return propertiesPanel;
    }

}
