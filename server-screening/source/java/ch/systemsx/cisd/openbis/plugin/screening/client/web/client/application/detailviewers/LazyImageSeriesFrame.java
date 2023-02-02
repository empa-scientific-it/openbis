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
import java.util.List;

import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.reveregroup.gwt.imagepreloader.FitImageLoadEvent;
import com.reveregroup.gwt.imagepreloader.FitImageLoadHandler;

import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageChannelsReference;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.image.StackImage;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.image.StackImageInitializer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannelStack;

/**
 * A frame wrapping all tile images for an unique time/depth/series combination. The tiles are loaded lazily. In order to download and display the
 * tile images one has to call the method {@link #downloadImagesFromServer()}.
 * 
 * @author Kaloyan Enimanev
 */
public class LazyImageSeriesFrame extends LayoutContainer
{
    public interface ImagesDownloadListener
    {
        /**
         * called when all images for a frame have been downloaded.
         */
        void imagesDownloaded(LazyImageSeriesFrame frame);
    }

    private final int imageWidth;

    private final int imageHeight;

    private final String sessionId;

    private final LogicalImageChannelsReference channelReferences;

    private ImageChannelStack[/* tileRow */][/* tileCol */] tilesMap;

    private boolean needsImageDownload;

    private boolean imagesDownloaded;

    private List<ImagesDownloadListener> imagesDownloadListeners =
            new ArrayList<ImagesDownloadListener>();

    private LogicalImageClickHandler imageClickHandler;

    public LazyImageSeriesFrame(List<ImageChannelStack> seriesPointStacks,
            LogicalImageChannelsReference channelReferences, String sessionId, int imageWidth,
            int imageHeight)
    {
        LogicalImageReference images = channelReferences.getBasicImage();

        TableLayout tableLayout = new TableLayout(images.getTileColsNum());
        tableLayout.setCellSpacing(LogicalImageViewer.TILE_SPACING);
        setLayout(tableLayout);

        tilesMap = createTilesMap(seriesPointStacks, images);
        this.channelReferences = channelReferences;
        this.sessionId = sessionId;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;

        needsImageDownload = areImagesExistingOnServer();
    }

    public boolean needsImageDownload()
    {
        return needsImageDownload;
    }

    public boolean areImagesDownloaded()
    {
        return imagesDownloaded;
    }

    public synchronized void downloadImagesFromServer()
    {
        if (false == needsImageDownload)
        {
            // images already download
            return;
        }
        needsImageDownload = false;

        FitImageLoadHandler loadAndErrorhandler = createLoadHandler();

        for (int row = 0; row < tilesMap.length; row++)
        {
            for (int col = 0; col < tilesMap[row].length; col++)
            {
                final ImageChannelStack stackRef = tilesMap[row][col];

                if (stackRef != null)
                {
                    StackImageInitializer initializer = new StackImageInitializer();
                    initializer.setSessionId(sessionId);
                    initializer.setChannelReferences(channelReferences);
                    initializer.setStack(stackRef);
                    initializer.setImageWidth(imageWidth);
                    initializer.setImageHeight(imageHeight);
                    initializer.setImageLoadHandler(loadAndErrorhandler);
                    initializer.setImageClickHandler(imageClickHandler);
                    this.add(new StackImage(initializer));

                } else
                {
                    addDummyImage(this, imageWidth, imageHeight);
                }
            }
        }
    }

    private FitImageLoadHandler createLoadHandler()
    {
        final int totalTilesToDownload = getNumberOfTilesToDownload();
        return new FitImageLoadHandler()
            {
                int tilesDownloaded = 0;

                @Override
                public void imageLoaded(FitImageLoadEvent event)
                {
                    tilesDownloaded++;
                    if (tilesDownloaded >= totalTilesToDownload)
                    {
                        imagesDownloaded = true;
                        if (imagesDownloadListeners != null)
                        {
                            for (ImagesDownloadListener listener : imagesDownloadListeners)
                            {
                                listener.imagesDownloaded(LazyImageSeriesFrame.this);
                            }
                        }
                    }
                }
            };
    }

    private int getNumberOfTilesToDownload()
    {
        int totalImagesToDownload = 0;
        for (ImageChannelStack[] stackRefArray : tilesMap)
        {
            for (ImageChannelStack stackRef : stackRefArray)
            {
                if (stackRef != null)
                {
                    totalImagesToDownload++;
                }
            }
        }
        return totalImagesToDownload;
    }

    private boolean areImagesExistingOnServer()
    {
        for (ImageChannelStack[] stackRefArray : tilesMap)
        {
            for (ImageChannelStack stackRef : stackRefArray)
            {
                if (stackRef != null)
                {
                    return true;
                }
            }
        }
        return false;
    }

    private static ImageChannelStack[][] createTilesMap(List<ImageChannelStack> stackReferences,
            LogicalImageReference images)
    {
        if (images.tryGetTileLocation() == null)
        {
            int rows = images.getTileRowsNum();
            int cols = images.getTileColsNum();

            ImageChannelStack[][] map = new ImageChannelStack[rows][cols];
            for (ImageChannelStack stackRef : stackReferences)
            {
                map[stackRef.getTileRow() - 1][stackRef.getTileCol() - 1] = stackRef;
            }
            return map;
        } else
        {
            ImageChannelStack[][] map = new ImageChannelStack[1][1];
            if (!stackReferences.isEmpty())
            {
                map[0][0] = stackReferences.get(0);
            }
            return map;
        }
    }

    private static void addDummyImage(LayoutContainer container, int imageWidth, int imageHeight)
    {
        Label dummy = new Label();
        dummy.setWidth(imageWidth);
        dummy.setHeight(imageHeight);
        container.add(dummy);
    }

    public void addImagesDownloadListener(ImagesDownloadListener imagesDownloadListener)
    {
        this.imagesDownloadListeners.add(imagesDownloadListener);
    }

    public void setImageClickHandler(LogicalImageClickHandler imageClickHandler)
    {
        this.imageClickHandler = imageClickHandler;
    }

}
