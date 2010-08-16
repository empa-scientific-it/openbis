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
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.renderers.SimpleImageHtmlRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabClickListener;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ChannelChooser.DefaultChannelState;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ChannelChooser.IChanneledViewerFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ChannelStackImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria.SingleExperimentSearchCriteria;

/**
 * A dialog which shows the content of the well.
 * 
 * @author Tomasz Pylak
 */
public class WellContentDialog extends Dialog
{
    private static final int ONE_IMAGE_WIDTH_PX = 200;

    private static final int ONE_IMAGE_HEIGHT_PX = 120;

    private static final int NO_IMAGES_DIALOG_WIDTH_PX = 300;

    private static final int NO_IMAGES_DIALOG_HEIGHT_PX = 160;

    public static void showContentDialog(final WellData wellData, DefaultChannelState channelState,
            final ScreeningViewContext viewContext)
    {

        final WellContentDialog contentDialog =
                new WellContentDialog(wellData.tryGetMetadata(), getExperiment(wellData),
                        viewContext);

        final WellImages imagesOrNull = wellData.tryGetImages();
        if (imagesOrNull != null && imagesOrNull.isMultidimensional())
        {
            showTimepointImageViewer(contentDialog, wellData, imagesOrNull, channelState,
                    viewContext);
        } else
        {
            if (imagesOrNull != null)
            {
                LayoutContainer imageViewer =
                        createImageViewer(imagesOrNull, channelState, viewContext);
                contentDialog.addComponent(imageViewer);
            }
            contentDialog.setupContentAndShow(wellData);
        }
    }

    private static void showTimepointImageViewer(final WellContentDialog contentDialog,
            final WellData wellData, final WellImages images,
            final DefaultChannelState channelState, final ScreeningViewContext viewContext)
    {
        viewContext.getService().listChannelStackImages(images.getDatasetCode(),
                images.getDatastoreCode(), images.getWellLocation(),
                new AbstractAsyncCallback<List<ChannelStackImageReference>>(viewContext)
                    {
                        @Override
                        protected void process(List<ChannelStackImageReference> channelStackImages)
                        {
                            LayoutContainer imageViewer =
                                    createTimepointImageViewer(channelStackImages, images,
                                            channelState, viewContext);
                            contentDialog.addComponent(imageViewer);
                            contentDialog.setupContentAndShow(wellData);
                        }
                    });
    }

    private static LayoutContainer createTimepointImageViewer(
            List<ChannelStackImageReference> channelStackImages, WellImages images,
            DefaultChannelState channelState, IViewContext<?> viewContext)
    {
        System.out.println("result: " + channelStackImages);
        // TODO 2010-08-16, Tomasz Pylak: implement me!
        return new LayoutContainer();
    }

    private static int getDialogWidth(final WellImages images)
    {
        float imageSizeMultiplyFactor = getImageSizeMultiplyFactor(images);
        return (int) (ONE_IMAGE_WIDTH_PX * imageSizeMultiplyFactor) * images.getTileColsNum() + 100;
    }

    private static int getDialogHeight(final WellImages images)
    {
        float imageSizeMultiplyFactor = getImageSizeMultiplyFactor(images);
        return Math.max((int) (ONE_IMAGE_HEIGHT_PX * imageSizeMultiplyFactor)
                * images.getTileRowsNum() + 100, 300);
    }

    private static SingleExperimentSearchCriteria getExperiment(WellData wellData)
    {
        return new SingleExperimentSearchCriteria(wellData.getExperimentId().getId(), wellData
                .getExperimentDisplayIdentifier());
    }

    private static float getImageSizeMultiplyFactor(WellImages images)
    {
        float dim = Math.max(images.getTileRowsNum(), images.getTileColsNum());
        // if there are more than 3 tiles, make them smaller, if there are less, make them bigger
        return 3.0F / dim;
    }

    // ----------------

    private final WellMetadata metadataOrNull;

    private final SingleExperimentSearchCriteria experiment;

    private final ScreeningViewContext viewContext;

    private final LayoutContainer dialogContent;

    private WellContentDialog(WellMetadata metadataOrNull,
            SingleExperimentSearchCriteria experiment, ScreeningViewContext viewContext)
    {
        this.metadataOrNull = metadataOrNull;
        this.experiment = experiment;
        this.viewContext = viewContext;

        this.dialogContent = new LayoutContainer();
        dialogContent.setLayout(new RowLayout());
        dialogContent.setScrollMode(Scroll.AUTO);

        LayoutContainer descriptionContainer = createContentDescription();
        dialogContent.add(descriptionContainer);

    }

    public void addComponent(LayoutContainer component)
    {
        dialogContent.add(component);
    }

    private void setupContentAndShow(WellData wellData)
    {
        String title = "Well Content: " + wellData.getWellDescription();
        setHeading(title);
        setLayout(new FitLayout());
        setScrollMode(Scroll.AUTO);
        setHideOnButtonClick(true);
        add(dialogContent);

        final WellImages imagesOrNull = wellData.tryGetImages();
        if (imagesOrNull != null)
        {
            setWidth(getDialogWidth(imagesOrNull));
            setHeight(getDialogHeight(imagesOrNull));
        } else
        {
            setWidth(NO_IMAGES_DIALOG_WIDTH_PX);
            setHeight(NO_IMAGES_DIALOG_HEIGHT_PX);
        }

        show();
    }

    private LayoutContainer createContentDescription()
    {
        LayoutContainer container = new LayoutContainer();
        TableLayout tableLayout = new TableLayout(2);
        tableLayout.setCellPadding(2);
        container.setLayout(tableLayout);
        TableData cellLayout = new TableData();
        cellLayout.setMargin(2);
        if (metadataOrNull != null)
        {
            container.add(new Text("Well: "), cellLayout);
            container.add(createEntityLink(metadataOrNull.getWellSample()));

            addProperties(container, cellLayout, metadataOrNull.getWellSample().getProperties());
        } else
        {
            container.add(new Text("No metadata available."));
        }
        return container;
    }

    private void addProperties(LayoutContainer container, TableData cellLayout,
            List<IEntityProperty> properties)
    {
        for (IEntityProperty property : properties)
        {
            addProperty(container, cellLayout, property);
        }
    }

    private void addProperty(LayoutContainer container, TableData cellLayout,
            IEntityProperty property)
    {
        String propertyLabel = property.getPropertyType().getLabel();
        String propertyValue = property.tryGetAsString();

        container.add(new Text(propertyLabel + ": "), cellLayout);
        Material material = property.getMaterial();
        if (material != null)
        {
            container.add(createPlateLocationsMaterialViewerLink(material));

            if (material.getMaterialType().getCode().equalsIgnoreCase(
                    ScreeningConstants.GENE_PLUGIN_TYPE_CODE))
            {
                container.add(new Text("Gene details: "), cellLayout);
                container.add(createEntityExternalLink(material));
            }
        } else
        {
            container.add(new Text(propertyValue), cellLayout);
        }

    }

    private Widget createEntityExternalLink(Material gene)
    {
        String url = viewContext.getMessage(Dict.GENE_LIBRARY_URL, gene.getCode());
        return new Html(LinkRenderer.renderAsLinkWithAnchor("gene database", url, true));
    }

    private Widget createPlateLocationsMaterialViewerLink(final IEntityInformationHolder material)
    {
        return LinkRenderer.getLinkWidget(material.getCode(), new ClickHandler()
            {
                public void onClick(ClickEvent event)
                {
                    WellContentDialog.this.hide();
                    ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ClientPluginFactory
                            .openPlateLocationsMaterialViewer(material, ExperimentSearchCriteria
                                    .createExperiment(experiment), viewContext);
                }
            });
    }

    private Widget createEntityLink(IEntityInformationHolder entity)
    {
        final ClickHandler listener = new OpenEntityDetailsTabClickListener(entity, viewContext);
        return LinkRenderer.getLinkWidget(entity.getCode(), listener);
    }

    // -------------

    private static LayoutContainer createImageViewer(final WellImages images,
            DefaultChannelState channelState, final IViewContext<?> viewContext)
    {
        final float imageSizeMultiplyFactor = getImageSizeMultiplyFactor(images);
        final IChanneledViewerFactory viewerFactory = new IChanneledViewerFactory()
            {
                public LayoutContainer create(String channel)
                {
                    String sessionId = getSessionId(viewContext);
                    return createTilesGrid(images, channel, sessionId,
                            (int) (ONE_IMAGE_WIDTH_PX * imageSizeMultiplyFactor),
                            (int) (ONE_IMAGE_HEIGHT_PX * imageSizeMultiplyFactor));
                }
            };
        return ChannelChooser.createViewerWithChannelChooser(viewerFactory, channelState, images
                .getChannelsNames());
    }

    /**
     * Creates a view for the specified channel.
     * 
     * @param channel Channel numbers start with 1. Channel 0 consists of all other channels merged.
     */
    public static Widget createImageViewerForChannel(IViewContext<?> viewContext,
            WellContent wellContent, int imageWidthPx, int imageHeightPx, String channel)
    {
        DatasetImagesReference images = wellContent.tryGetImages();
        if (images == null)
        {
            return new Text("Images not acquired.");
        }
        WellLocation locationOrNull = wellContent.tryGetLocation();
        if (locationOrNull == null)
        {
            return new Text("Incorrect well code.");
        }
        if (images.getImageParameters().getChannelsNames().contains(channel) == false
                && channel.equals(ScreeningConstants.MERGED_CHANNELS) == false)
        {
            return new Text("No images available for this channel.");
        }
        WellImages wellImages = new WellImages(images, locationOrNull);
        String sessionId = getSessionId(viewContext);
        return WellContentDialog.createTilesGrid(wellImages, channel, sessionId, imageWidthPx,
                imageHeightPx);
    }

    private static LayoutContainer createTilesGrid(WellImages images, String channel,
            String sessionId, int imageWidth, int imageHeight)
    {
        LayoutContainer container = new LayoutContainer(new TableLayout(images.getTileColsNum()));
        for (int row = 1; row <= images.getTileRowsNum(); row++)
        {
            for (int col = 1; col <= images.getTileColsNum(); col++)
            {
                Component tileContent;
                String imageURL =
                        createDatastoreImageUrl(images, channel, row, col, imageWidth, imageHeight,
                                sessionId);
                tileContent = new Html(imageURL);
                tileContent.setHeight("" + imageHeight);
                PlateStyleSetter.setPointerCursor(tileContent);
                container.add(tileContent);
            }
        }
        return container;
    }

    /** generates URL of an image on Data Store server */
    // TODO 2010-08-16, Tomasz Pylak: implement and use me!!!!
    @SuppressWarnings("unused")
    private static String createDatastoreImageUrl(WellImages images, String channel,
            ChannelStackImageReference channelStackRef, int width, int height, String sessionID)
    {
        URLMethodWithParameters methodWithParameters =
                createBasicImageURL(images, channel, sessionID);

        methodWithParameters
                .addParameter("channelStackId", channelStackRef.getChannelStackTechId());
        String linkURL = methodWithParameters.toString();
        methodWithParameters.addParameter("mode", "thumbnail" + width + "x" + height);

        String imageURL = methodWithParameters.toString();
        return SimpleImageHtmlRenderer.createEmbededImageHtml(imageURL, linkURL);
    }

    /** generates URL of an image on Data Store server */
    private static String createDatastoreImageUrl(WellImages images, String channel, int tileRow,
            int tileCol, int width, int height, String sessionID)
    {
        URLMethodWithParameters methodWithParameters =
                createBasicImageURL(images, channel, sessionID);

        methodWithParameters.addParameter("wellRow", images.getWellLocation().getRow());
        methodWithParameters.addParameter("wellCol", images.getWellLocation().getColumn());
        methodWithParameters.addParameter("tileRow", tileRow);
        methodWithParameters.addParameter("tileCol", tileCol);
        String linkURL = methodWithParameters.toString();
        methodWithParameters.addParameter("mode", "thumbnail" + width + "x" + height);

        String imageURL = methodWithParameters.toString();
        return SimpleImageHtmlRenderer.createEmbededImageHtml(imageURL, linkURL);
    }

    private static URLMethodWithParameters createBasicImageURL(WellImages images, String channel,
            String sessionID)
    {
        URLMethodWithParameters methodWithParameters =
                new URLMethodWithParameters(images.getDownloadUrl() + "/"
                        + ScreeningConstants.DATASTORE_SCREENING_SERVLET_URL);
        methodWithParameters.addParameter("sessionID", sessionID);
        methodWithParameters.addParameter("dataset", images.getDatasetCode());
        methodWithParameters.addParameter("channel", channel);
        if (channel.equals(ScreeningConstants.MERGED_CHANNELS))
        {
            methodWithParameters.addParameter("mergeChannels", "true");
        }
        return methodWithParameters;
    }

    private static String getSessionId(IViewContext<?> viewContext)
    {
        return viewContext.getModel().getSessionContext().getSessionID();
    }
}
