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

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.SliderWithMovieButtons;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.SliderWithMovieButtonsValueLoader;

/**
 * @author pkupczyk
 */
public class LogicalImageSeriesMovieControls extends LogicalImageSeriesControls
{

    public LogicalImageSeriesMovieControls(IViewContext<IScreeningClientServiceAsync> viewContext,
            String displayTypeId, LogicalImageSeriesDownloader imageDownloader,
            LogicalImageSeriesModel imageModel)
    {
        super(viewContext, displayTypeId, imageDownloader, imageModel);

        final List<LogicalImageSeriesPoint> sortedPoints = getImageModel().getSortedPoints();
        final SliderWithMovieButtons sliderWithButtons =
                new SliderWithMovieButtons(sortedPoints.size());
        sliderWithButtons.setDelay(getSettingsManager().getDefaultMovieDelay(displayTypeId));
        sliderWithButtons.addDelayChangeHandler(new ChangeHandler()
            {
                @Override
                public void onChange(ChangeEvent event)
                {
                    getSettingsManager().setDefaultMovieDelay(getDisplayTypeId(),
                            sliderWithButtons.getDelay());
                }
            });
        sliderWithButtons.setValueLoader(new SliderWithMovieButtonsValueLoader()
            {
                @Override
                public void loadValue(int value, AsyncCallback<Void> callback)
                {
                    getImageDownloader().setSelectedFrame(value - 1, callback);
                    remove(getItem(0));
                    insert(new LogicalImageSeriesPointLabel(sortedPoints, value), 0);
                    layout();
                }
            });

        add(new LogicalImageSeriesPointLabel(sortedPoints, 1));
        add(sliderWithButtons.getWidgets());
    }

}
