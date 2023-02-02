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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.logicalimage;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.LayoutContainer;

import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.LazyImageSeriesFrame;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannelStack;

/**
 * Allows to view logical image which has series (e.g. timepoints).
 * 
 * @author Tomasz Pylak
 */
public class LogicalImageSeriesGrid extends LayoutContainer
{

    private LogicalImageSeriesGridInitializer initializer;

    public LogicalImageSeriesGrid(LogicalImageSeriesGridInitializer initializer)
    {
        if (initializer == null)
        {
            throw new IllegalArgumentException("Initializer was null");
        }

        this.initializer = initializer;

        LogicalImageSeriesModel model =
                new LogicalImageSeriesModel(initializer.getChannelStackImages());
        List<LazyImageSeriesFrame> frames = createFrames(model);
        LogicalImageSeriesDownloader downloader =
                new LogicalImageSeriesDownloader(frames, initializer.getImageDownloadListener());

        LayoutContainer controls = null;

        if (model.isMatrixViewPossible())
        {
            controls =
                    new LogicalImageSeriesTimeAndDepthControls(initializer.getViewContext(),
                            initializer.getDisplayTypeId(), downloader, model);
        } else
        {
            controls =
                    new LogicalImageSeriesMovieControls(initializer.getViewContext(),
                            initializer.getDisplayTypeId(), downloader, model);
        }

        for (LazyImageSeriesFrame frame : frames)
        {
            controls.add(frame);
        }

        add(controls);
    }

    private List<LazyImageSeriesFrame> createFrames(LogicalImageSeriesModel model)
    {
        final List<LazyImageSeriesFrame> frames = new ArrayList<LazyImageSeriesFrame>();
        final String sessionId =
                initializer.getViewContext().getModel().getSessionContext().getSessionID();

        for (List<ImageChannelStack> seriesPointStacks : model.getSortedChannelStackSeriesPoints())
        {
            final LazyImageSeriesFrame frame =
                    new LazyImageSeriesFrame(seriesPointStacks, initializer.getChannelReferences(),
                            sessionId, initializer.getImageSize().getWidth(), initializer
                                    .getImageSize().getHeight());
            frame.setImageClickHandler(initializer.getImageClickHandler());

            boolean isFirstFrame = frames.isEmpty();
            if (isFirstFrame)
            {
                frame.downloadImagesFromServer();
            }
            frame.setVisible(isFirstFrame);
            frames.add(frame);
        }
        return frames;
    }

}
