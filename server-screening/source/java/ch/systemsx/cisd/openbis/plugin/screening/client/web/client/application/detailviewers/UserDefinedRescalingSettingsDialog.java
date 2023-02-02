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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.ui.Grid;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IntegerField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.LabeledItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.IntensityRange;

/**
 * @author Pawel Glyzewski
 */
public class UserDefinedRescalingSettingsDialog extends Dialog
{
    private static class SingleChannelIntesityRange
    {
        private final LabeledItem<String> channelCode;

        private final Label labelMin;

        private final Label labelMax;

        private final IntegerField minTextField;

        private final IntegerField maxTextField;

        public SingleChannelIntesityRange(IMessageProvider messageProvider,
                LabeledItem<String> channelCode)
        {
            this.channelCode = channelCode;
            labelMin =
                    new Label(messageProvider.getMessage(Dict.RESCALING_DIALOG_MIN,
                            channelCode.getLabel()));
            labelMax =
                    new Label(messageProvider.getMessage(Dict.RESCALING_DIALOG_MAX,
                            channelCode.getLabel()));
            minTextField =
                    new IntegerField(messageProvider.getMessage(Dict.RESCALING_DIALOG_MIN,
                            channelCode), true);
            maxTextField =
                    new IntegerField(messageProvider.getMessage(Dict.RESCALING_DIALOG_MAX,
                            channelCode), true);
        }
    }

    private final List<SingleChannelIntesityRange> intensitiesPerChannel;

    private final IMessageProvider messageProvider;

    private final IDefaultChannelState defaultChannelState;

    private final List<LabeledItem<String>> channelCodes;

    public UserDefinedRescalingSettingsDialog(IMessageProvider messageProvider,
            Map<String, IntensityRange> intensitiesPerChannel,
            IDefaultChannelState defaultChannelState, List<LabeledItem<String>> channelCodes)
    {
        super();

        this.messageProvider = messageProvider;
        this.defaultChannelState = defaultChannelState;
        this.channelCodes = channelCodes;

        setHeading(this.messageProvider.getMessage(Dict.TITLE_USER_DEFINED_RESCALING_DIALOG));
        setButtons(Dialog.OKCANCEL);

        this.intensitiesPerChannel = new ArrayList<SingleChannelIntesityRange>(channelCodes.size());
        for (LabeledItem<String> channelCode : channelCodes)
        {
            this.intensitiesPerChannel.add(new SingleChannelIntesityRange(messageProvider,
                    channelCode));
        }

        setInitialValues(intensitiesPerChannel);

        Grid grid = new Grid(channelCodes.size() * 2, 2);
        int counter = 0;
        for (SingleChannelIntesityRange scir : this.intensitiesPerChannel)
        {
            grid.setWidget(2 * counter, 0, scir.labelMax);
            grid.setWidget(2 * counter, 1, scir.maxTextField);
            grid.setWidget(2 * counter + 1, 0, scir.labelMin);
            grid.setWidget(2 * counter + 1, 1, scir.minTextField);
            counter++;
        }

        add(grid);

        setAutoWidth(true);
        setAutoHeight(true);

        addListener(Events.Show, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    center();
                }
            });
    }

    private void setInitialValues(Map<String, IntensityRange> intensitiesPerChannel)
    {
        for (SingleChannelIntesityRange scir : this.intensitiesPerChannel)
        {
            scir.minTextField.setValue(0);
            scir.maxTextField.setValue(255);

            IntensityRange range = intensitiesPerChannel.get(scir.channelCode.getItem());
            if (range != null)
            {
                scir.minTextField.setValue(range.getBlackPoint());
                scir.maxTextField.setValue(range.getWhitePoint());
            }
        }
    }

    @Override
    protected final void onButtonPressed(final Button button)
    {
        if (button.getItemId().equals(Dialog.OK))
        {
            button.disable();
            if (areIntensitiesValid())
            {
                super.onButtonPressed(button);
                updateIntensityRescaling();
                fireEvent(Events.OnChange);
                hide();
            } else
            {
                button.enable();
            }
        } else
        {
            super.onButtonPressed(button);
            hide();
        }
    }

    private boolean areIntensitiesValid()
    {
        for (SingleChannelIntesityRange scir : intensitiesPerChannel)
        {
            if ((false == scir.minTextField.isValid()) || (false == scir.maxTextField.isValid()))
            {
                return false;
            }
        }

        return true;
    }

    public void updateIntensityRescaling()
    {
        Map<String, IntensityRange> result = new HashMap<String, IntensityRange>();

        try
        {
            for (SingleChannelIntesityRange scir : intensitiesPerChannel)
            {
                int min = Integer.parseInt(scir.minTextField.getValue().toString());
                int max = Integer.parseInt(scir.maxTextField.getValue().toString());

                result.put(scir.channelCode.getItem(), new IntensityRange(min, max));
            }

            for (LabeledItem<String> channelCode : channelCodes)
            {
                defaultChannelState.setIntensityRange(channelCode.getItem(),
                        result.get(channelCode.getItem()));
            }
        } catch (NumberFormatException e)
        {
        }
    }
}
