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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.logicalimage;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.LazyImageSeriesFrame;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.LazyImageSeriesFrame.ImagesDownloadListener;

/**
 * @author pkupczyk
 */
class LogicalImageSeriesDownloader
{

    private List<LazyImageSeriesFrame> frames;

    private int selectedFrameIndex = 0;

    private int shownFrameIndex = 0;

    LogicalImageSeriesDownloader(List<LazyImageSeriesFrame> frames,
            ImagesDownloadListener imagesDownloadListener)
    {
        this.frames = frames;
        downloadFirstFrame(imagesDownloadListener);
    }

    private void downloadFirstFrame(ImagesDownloadListener listener)
    {
        if (frames.isEmpty())
        {
            if (listener != null)
            {
                listener.imagesDownloaded(null);
            }
        } else
        {
            downloadFrame(0, listener);
        }
    }

    private void downloadFrame(int frameIndex, ImagesDownloadListener listener)
    {
        LazyImageSeriesFrame frame = frames.get(frameIndex);

        if (frame.areImagesDownloaded())
        {
            if (listener != null)
            {
                listener.imagesDownloaded(frame);
            }
        } else
        {
            if (listener != null)
            {
                frame.addImagesDownloadListener(listener);
            }
            frame.downloadImagesFromServer();
        }
    }

    public void setSelectedFrame(final int newSelectedFrameIndex, final AsyncCallback<Void> callback)
    {
        selectedFrameIndex = newSelectedFrameIndex;

        ImagesDownloadListener listener = new ImagesDownloadListener()
            {
                @Override
                public void imagesDownloaded(LazyImageSeriesFrame frame)
                {
                    // do not display the frame if selection changed during loading
                    if (newSelectedFrameIndex == selectedFrameIndex)
                    {
                        LazyImageSeriesFrame selectedFrame = frames.get(newSelectedFrameIndex);
                        LazyImageSeriesFrame shownFrame = frames.get(shownFrameIndex);
                        shownFrame.hide();
                        selectedFrame.show();
                        shownFrameIndex = newSelectedFrameIndex;
                    }
                    if (callback != null)
                    {
                        callback.onSuccess(null);
                    }
                }
            };

        downloadFrame(newSelectedFrameIndex, listener);
    }

}
