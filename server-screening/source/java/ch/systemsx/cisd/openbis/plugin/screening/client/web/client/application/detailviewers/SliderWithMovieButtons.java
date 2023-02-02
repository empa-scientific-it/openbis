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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SliderEvent;
import com.extjs.gxt.ui.client.widget.Slider;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author pkupczyk
 */
public class SliderWithMovieButtons extends Composite
{

    private Slider slider;

    private MovieButtons buttons;

    private Loading loading;

    private SliderWithMovieButtonsValueLoader valueLoader;

    public SliderWithMovieButtons(int numberOfValues)
    {
        valueLoader = SliderWithMovieButtonsValueLoader.NULL_LOADER;

        loading = new Loading();

        buttons = new MovieButtons(numberOfValues);
        buttons.setFrameLoader(new MovieButtonsFrameLoader()
            {
                @Override
                public void loadFrame(final int frame, final AsyncCallback<Void> callback)
                {
                    final int value = frame + 1;
                    showLoading();
                    slider.setValue(value, true);
                    valueLoader.loadValue(value, new AsyncCallback<Void>()
                        {
                            @Override
                            public void onSuccess(Void result)
                            {
                                hideLoading(value);
                                callback.onSuccess(result);
                            }

                            @Override
                            public void onFailure(Throwable caught)
                            {
                                hideLoading(value);
                                callback.onFailure(caught);
                            }
                        });
                }
            });

        slider = new SliderWithAutoWidth(numberOfValues);
        slider.addListener(Events.Change, new Listener<SliderEvent>()
            {
                @Override
                public void handleEvent(SliderEvent be)
                {
                    final int value = be.getNewValue();
                    showLoading();
                    buttons.setFrame(value - 1);
                    valueLoader.loadValue(value, new AsyncCallback<Void>()
                        {
                            @Override
                            public void onSuccess(Void result)
                            {
                                hideLoading(value);
                            }

                            @Override
                            public void onFailure(Throwable caught)
                            {
                                hideLoading(value);
                            }
                        });
                }
            });

        initWidget(new HTML());
    }

    public Widget getWidgets()
    {
        Panel panel = new VerticalPanel();
        panel.add(getSliderWidget());
        panel.add(getButtonsWidget());
        panel.add(getLoadingWidget());
        return panel;
    }

    public Widget getSliderWidget()
    {
        return slider;
    }

    public Widget getButtonsWidget()
    {
        return buttons;
    }

    public Widget getLoadingWidget()
    {
        return loading;
    }

    public int getValue()
    {
        return slider.getValue();
    }

    public void setValue(int value)
    {
        slider.setValue(value);
    }

    public int getDelay()
    {
        return buttons.getDelay();
    }

    public void setDelay(int delay)
    {
        buttons.setDelay(delay);
    }

    public void addDelayChangeHandler(ChangeHandler handler)
    {
        buttons.addDelayChangeHandler(handler);
    }

    public SliderWithMovieButtonsValueLoader getValueLoader()
    {
        return valueLoader;
    }

    public void setValueLoader(SliderWithMovieButtonsValueLoader valueLoader)
    {
        if (valueLoader == null)
        {
            throw new IllegalArgumentException("Value loader was null");
        }
        this.valueLoader = valueLoader;
    }

    private void showLoading()
    {
        loading.showLoading();
    }

    private void hideLoading(int value)
    {
        if (slider.getValue() == value)
        {
            loading.hideLoading();
        }
    }

}
