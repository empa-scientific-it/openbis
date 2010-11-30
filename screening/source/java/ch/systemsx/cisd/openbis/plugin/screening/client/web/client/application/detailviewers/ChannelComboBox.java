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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;

import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Combobox storing channel names.
 * 
 * @author Izabela Adamczyk
 */
public class ChannelComboBox extends SimpleComboBox<String>
{

    private IDefaultChannelState defaultChannelState;

    /**
     * Creates empty {@link ChannelComboBox}.
     */
    public ChannelComboBox(final IDefaultChannelState defaultChannelState)
    {
        setTriggerAction(TriggerAction.ALL);
        setAllowBlank(false);
        setEditable(false);
        setEmptyText("Choose...");
        this.defaultChannelState = defaultChannelState;
        addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>()
            {
                @Override
                public void selectionChanged(SelectionChangedEvent<SimpleComboValue<String>> se)
                {
                    String selectedValueOrNull = se.getSelectedItem().getValue();
                    if (selectedValueOrNull != null)
                    {
                        defaultChannelState.setDefaultChannel(selectedValueOrNull);
                    }
                }
            });
    }

    /**
     * Creates {@link ChannelComboBox} with initial list of values and selects given initial value.
     */
    public ChannelComboBox(List<String> names, IDefaultChannelState defaultChannelState)
    {
        this(defaultChannelState);
        addUniqueCodes(names);
        autoselect();
    }

    /**
     * Selects default channel if such an information was saved before. Otherwise first element if
     * nothing was selected before.
     */
    private void autoselect()
    {
        if (getStore().getModels().size() > 0 && getValue() == null)
        {
            if (defaultChannelState != null && defaultChannelState.tryGetDefaultChannel() != null)
            {
                setSimpleValue(defaultChannelState.tryGetDefaultChannel());
            } else
            {
                setValue(getStore().getModels().get(0));
            }
        }
    }

    /**
     * Adds names to the combo box if they were not yet present.
     */
    private void addUniqueCodes(List<String> codes)
    {
        List<String> withMerged = new ArrayList<String>();
        withMerged.add(ScreeningConstants.MERGED_CHANNELS);
        withMerged.addAll(codes);
        for (String s : withMerged)
        {
            if (findModel(s) == null)
            {
                add(s);
            }
        }
    }

    public void addCodesAndListener(List<String> newCodes,
            SelectionChangedListener<SimpleComboValue<String>> listener)
    {
        addUniqueCodes(newCodes);
        addSelectionChangedListener(listener);
        autoselect();
    }

}