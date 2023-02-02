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

import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author pkupczyk
 */
public class Loading extends Composite
{

    private static final int LOADING_WAIT_PERIOD = 100;

    private static final int LOADING_SHOW_PERIOD = 1000;

    private Widget msg;

    private LoadingState state;

    public Loading()
    {
        msg = new Label("Loading...");
        setState(new LoadingHiddenState());
        initWidget(msg);
    }

    public void showLoading()
    {
        state.handleShowLoading();
    }

    public void hideLoading()
    {
        state.handleHideLoading();
    }

    private void setState(LoadingState state)
    {
        this.state = state;
        state.init();
    }

    private abstract class LoadingState
    {

        public abstract void init();

        public abstract void handleShowLoading();

        public abstract void handleHideLoading();

    }

    private class LoadingHiddenState extends LoadingState
    {

        private Timer showTimer;

        @Override
        public void init()
        {
            msg.getElement().getStyle().setVisibility(Visibility.HIDDEN);
        }

        @Override
        public void handleShowLoading()
        {
            if (showTimer == null)
            {
                showTimer = new Timer()
                    {
                        @Override
                        public void run()
                        {
                            setState(new LoadingShownState());
                        }
                    };
                showTimer.schedule(LOADING_WAIT_PERIOD);
            }
        }

        @Override
        public void handleHideLoading()
        {
            if (showTimer != null)
            {
                showTimer.cancel();
                showTimer = null;
            }
        }

    }

    private class LoadingShownState extends LoadingState
    {

        private long showTime;

        private Timer hideTimer;

        @Override
        public void init()
        {
            msg.getElement().getStyle().setVisibility(Visibility.VISIBLE);
            showTime = System.currentTimeMillis();
        }

        @Override
        public void handleShowLoading()
        {
            if (hideTimer != null)
            {
                hideTimer.cancel();
                hideTimer = null;
            }
        }

        @Override
        public void handleHideLoading()
        {
            long showPeriod = System.currentTimeMillis() - showTime;

            if (showPeriod >= LOADING_SHOW_PERIOD)
            {
                setState(new LoadingHiddenState());
            } else
            {
                if (hideTimer == null)
                {
                    hideTimer = new Timer()
                        {
                            @Override
                            public void run()
                            {
                                setState(new LoadingHiddenState());
                            }
                        };
                    hideTimer.schedule((int) (LOADING_SHOW_PERIOD - showPeriod));
                }
            }
        }
    }

}
