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

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SliderEvent;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.Slider;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.SliderWithAutoWidth;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.SliderWithMovieButtons;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.SliderWithMovieButtonsValueLoader;

/**
 * @author pkupczyk
 */
public class LogicalImageSeriesTimeAndDepthControls extends LogicalImageSeriesControls
{

    private Label timeLabel;

    private Label depthLabel;

    private SliderWithMovieButtons timeSliderWithButtons;

    private Slider depthSlider;

    public LogicalImageSeriesTimeAndDepthControls(
            IViewContext<IScreeningClientServiceAsync> viewContext, String displayTypeId,
            LogicalImageSeriesDownloader imageDownloader, LogicalImageSeriesModel imageModel)
    {
        super(viewContext, displayTypeId, imageDownloader, imageModel);

        Panel timeSliderWithLabel = new VerticalPanel();
        timeSliderWithLabel.add(getTimeLabel());
        timeSliderWithLabel.add(getTimeSliderWithButtons().getSliderWidget());

        Panel depthSliderWithLabel = new VerticalPanel();
        depthSliderWithLabel.add(getDepthLabel());
        depthSliderWithLabel.add(getDepthSlider());

        Panel timeAndDepthSlidersWithLabels = new HorizontalPanel();
        timeAndDepthSlidersWithLabels.add(timeSliderWithLabel);
        timeAndDepthSlidersWithLabels.add(getSpacer());
        timeAndDepthSlidersWithLabels.add(depthSliderWithLabel);

        Panel mainPanel = new VerticalPanel();
        mainPanel.add(timeAndDepthSlidersWithLabels);
        mainPanel.add(getTimeSliderWithButtons().getButtonsWidget());
        mainPanel.add(getTimeSliderWithButtons().getLoadingWidget());

        add(mainPanel);

        refreshLabels();
    }

    private SliderWithMovieButtons getTimeSliderWithButtons()
    {
        if (timeSliderWithButtons == null)
        {
            timeSliderWithButtons =
                    new SliderWithMovieButtons(getImageModel().getNumberOfTimepoints());
            timeSliderWithButtons.setValue(1);
            timeSliderWithButtons.setDelay(getSettingsManager().getDefaultMovieDelay(
                    getDisplayTypeId()));
            timeSliderWithButtons.addDelayChangeHandler(new ChangeHandler()
                {
                    @Override
                    public void onChange(ChangeEvent event)
                    {
                        getSettingsManager().setDefaultMovieDelay(getDisplayTypeId(),
                                timeSliderWithButtons.getDelay());
                    }
                });
            timeSliderWithButtons.setValueLoader(new SliderWithMovieButtonsValueLoader()
                {
                    @Override
                    public void loadValue(int value, AsyncCallback<Void> callback)
                    {
                        refreshValue(callback);
                        refreshLabels();
                    }
                });
        }
        return timeSliderWithButtons;
    }

    private Label getTimeLabel()
    {
        if (timeLabel == null)
        {
            timeLabel = new Label();
        }
        return timeLabel;
    }

    private Slider getDepthSlider()
    {
        if (depthSlider == null)
        {
            depthSlider = new SliderWithAutoWidth(getImageModel().getNumberOfDepthLevels());
            depthSlider.setValue(1);
            depthSlider.addListener(Events.Change, new Listener<SliderEvent>()
                {
                    @Override
                    public void handleEvent(SliderEvent be)
                    {
                        refreshValue(null);
                        refreshLabels();
                    }
                });
        }
        return depthSlider;
    }

    private Label getDepthLabel()
    {
        if (depthLabel == null)
        {
            depthLabel = new Label();
        }
        return depthLabel;
    }

    private Widget getSpacer()
    {
        HTML spacer = new HTML();
        spacer.getElement().getStyle().setWidth(20, Unit.PX);
        return spacer;
    }

    private void refreshValue(AsyncCallback<Void> callback)
    {
        int timeIndex = getTimeSliderWithButtons().getValue();
        int depthIndex = getDepthSlider().getValue();

        int frame = (timeIndex - 1) * getImageModel().getNumberOfDepthLevels() + (depthIndex - 1);
        getImageDownloader().setSelectedFrame(frame, callback);

        layout();
    }

    private void refreshLabels()
    {
        int timeIndex = getTimeSliderWithButtons().getValue();
        int depthIndex = getDepthSlider().getValue();

        int numberOfTimePoints = getImageModel().getNumberOfTimepoints();
        int numberOfDepthLevels = getImageModel().getNumberOfDepthLevels();

        LogicalImageSeriesPoint point = getImageModel().get(timeIndex - 1, depthIndex - 1);

        getTimeLabel().setText(
                "Time: " + point.getTimePointOrNull() + " sec (" + timeIndex + "/"
                        + numberOfTimePoints + ")");
        getDepthLabel().setText(
                "Depth: " + point.getDepthOrNull() + " (" + depthIndex + "/" + numberOfDepthLevels
                        + ")");
    }

}
