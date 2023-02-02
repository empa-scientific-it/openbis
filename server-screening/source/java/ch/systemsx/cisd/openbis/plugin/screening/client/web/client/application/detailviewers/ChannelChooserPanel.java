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

import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants.MERGED_CHANNELS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.MultiField;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.LabeledItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SimpleModelComboBox;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.IntensityRange;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.InternalImageTransformationInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * An UI panel for selecting image channels.
 * 
 * @author Kaloyan Enimanev
 */
public class ChannelChooserPanel extends LayoutContainer
{
    private static class ComboBoxGroup extends MultiField<Widget>
    {
        private ComboBoxGroup()
        {
            super();

            this.setSpacing(5);
        }
    }

    /**
     * Can be used from external classes wishing to be notified when the channel selection changes.
     */
    public static interface ChannelSelectionListener
    {
        void selectionChanged(List<String> channels, String imageTransformationCodeOrNull,
                Map<String, IntensityRange> rangesOrNull);
    }

    public static final String DEFAULT_TRANSFORMATION_CODE = "$DEFAULT$";

    private static final LabeledItem<InternalImageTransformationInfo> DEFAULT_TRANSFORMATION =
            convertToLabeledItem(new InternalImageTransformationInfo(
                    DEFAULT_TRANSFORMATION_CODE,
                    "Optimal (image)",
                    "Grayscale images with color depth higher then 8 bits are transformed in the optimal way for a single image. Otherwise no filter is applied.",
                    "", false));

    private final IMessageProvider messageProvider;

    private IDefaultChannelState defaultChannelState;

    private CheckBoxGroup channelsCheckBoxGroup;

    private SimpleModelComboBox<String> channelsComboBox;

    private SimpleModelComboBox<InternalImageTransformationInfo> transformationsComboBox;

    private LabelField adjustLabel = new LabelField("Filter:");

    private Button userDefinedTransformationSettingsButton = new Button("Settings");

    private AdapterField userDefinedTransformationSettingsButtonField = new AdapterField(
            userDefinedTransformationSettingsButton);

    private Map<String, Set<InternalImageTransformationInfo>> transformationsForChannels =
            new HashMap<String, Set<InternalImageTransformationInfo>>();

    private List<ChannelSelectionListener> channelSelectionListeners =
            new ArrayList<ChannelSelectionListener>();

    private Map<String, IntensityRange> intensitiesPerChannel =
            new HashMap<String, IntensityRange>();

    private final Listener<BaseEvent> selectionChangeListener = new Listener<BaseEvent>()
        {
            @Override
            public void handleEvent(BaseEvent be)
            {
                selectionChanged();
            }
        };

    private final Listener<BaseEvent> transformationSelection = new Listener<BaseEvent>()
        {
            @Override
            public void handleEvent(BaseEvent be)
            {
                List<LabeledItem<String>> channelCodes = getSelectedLabeledValues();
                InternalImageTransformationInfo selectedTransformation =
                        transformationsComboBox.getSimpleValue().getItem();
                String transformationCode = selectedTransformation.getCode();
                defaultChannelState.setDefaultTransformation(getChannelCode(channelCodes),
                        transformationCode);
                changeTransformationSettingsButtonVisibility(true, false);

                Map<String, IntensityRange> intensityRanges = new HashMap<String, IntensityRange>();
                for (LabeledItem<String> channelCode : channelCodes)
                {
                    intensityRanges.put(channelCode.getItem(),
                            defaultChannelState.tryGetIntensityRange(channelCode.getItem()));
                }
                notifySelectionListeners(getSelectedValues(),
                        tryGetSelectedTransformationCode(false), intensityRanges);

                String updatedTooltip = selectedTransformation.getLabel();
                if (ScreeningConstants.USER_DEFINED_RESCALING_CODE
                        .equalsIgnoreCase(transformationCode))
                {

                    for (Map.Entry<String, IntensityRange> rangeEntry : intensityRanges.entrySet())
                    {
                        IntensityRange intensityRange = rangeEntry.getValue();
                        if (intensityRanges.size() == 1)
                        {
                            updatedTooltip +=
                                    " ["
                                            + (intensityRange == null ? "undefined"
                                                    : intensityRange.getBlackPoint() + " - "
                                                            + intensityRange.getWhitePoint()) + "]";

                        } else
                        {
                            updatedTooltip +=
                                    " "
                                            + rangeEntry.getKey()
                                            + "=["
                                            + (intensityRange == null ? "undefined"
                                                    : intensityRange.getBlackPoint() + " - "
                                                            + intensityRange.getWhitePoint())
                                            + "];";
                        }

                        ChannelChooserPanel.this.intensitiesPerChannel.put(rangeEntry.getKey(),
                                intensityRange);
                    }
                }
                transformationsComboBox.setToolTip(updatedTooltip);
            }
        };

    public ChannelChooserPanel(IMessageProvider messageProvider,
            IDefaultChannelState defChannelState)
    {
        this(messageProvider, defChannelState, Collections.<String> emptyList(), null);
    }

    public ChannelChooserPanel(final IMessageProvider messageProvider,
            IDefaultChannelState defChannelState, List<String> selectedChannelsOrNull,
            ImageDatasetParameters imageDatasetParameters)
    {
        this.messageProvider = messageProvider;
        this.defaultChannelState = defChannelState;

        setAutoHeight(true);
        setAutoWidth(true);

        channelsComboBox = createChannelsComboBox();

        transformationsComboBox =
                new SimpleModelComboBox<InternalImageTransformationInfo>(this.messageProvider,
                        new ArrayList<LabeledItem<InternalImageTransformationInfo>>(), null);
        transformationsComboBox.addListener(Events.SelectionChange, transformationSelection);

        ComboBoxGroup group = new ComboBoxGroup();
        group.add(channelsComboBox);
        group.add(adjustLabel);
        group.add(transformationsComboBox);
        group.add(userDefinedTransformationSettingsButtonField);

        userDefinedTransformationSettingsButton
                .addSelectionListener(new SelectionListener<ButtonEvent>()
                    {
                        @Override
                        public void componentSelected(ButtonEvent ce)
                        {
                            UserDefinedRescalingSettingsDialog dialog =
                                    new UserDefinedRescalingSettingsDialog(messageProvider,
                                            intensitiesPerChannel, defaultChannelState,
                                            getSelectedLabeledValues());
                            dialog.addListener(Events.OnChange, transformationSelection);
                            dialog.show();
                        }
                    });

        add(group);

        channelsCheckBoxGroup = createCheckBoxGroup();
        add(channelsCheckBoxGroup);

        addChannels(imageDatasetParameters);
        updateChannelSelection(selectedChannelsOrNull);
    }

    private final static String getChannelCode(List<LabeledItem<String>> selection)
    {
        if (selection.size() == 1)
        {
            return selection.get(0).getItem();
        }

        return ScreeningConstants.MERGED_CHANNELS;
    }

    private SimpleModelComboBox<String> createChannelsComboBox()
    {
        SimpleModelComboBox<String> comboBox =
                new SimpleModelComboBox<String>(this.messageProvider,
                        new ArrayList<LabeledItem<String>>(), null);
        comboBox.addListener(Events.SelectionChange, selectionChangeListener);

        return comboBox;
    }

    private CheckBoxGroup createCheckBoxGroup()
    {
        CheckBoxGroup group = new CheckBoxGroup();
        return group;
    }

    /**
     * adds a {@link ChannelSelectionListener} that will be receiving notifications when the selection changes.
     */
    public void addSelectionChangedListener(ChannelSelectionListener selectionListener)
    {
        channelSelectionListeners.add(selectionListener);
    }

    /**
     * a quite specific method, currently only needed by the well-search grid.
     */
    public void addChannels(ImageDatasetParameters imageParameters)
    {
        addChannelsForParameters(imageParameters);
        updateChannelSelection(null);
    }

    /**
     * @return the current channel selection. If all channels are selected, returns a list containing a single element -
     *         {@link ScreeningConstants#MERGED_CHANNELS}.
     */
    public List<String> getSelectedValues()
    {
        String comboBoxValue = channelsComboBox.tryGetChosenItem();
        if (comboBoxValue == null)
        {
            return Collections.<String> emptyList();
        }
        if (ScreeningConstants.MERGED_CHANNELS.equals(comboBoxValue))
        {
            // multiple channel selection
            return getMergedChannelSelection();
        } else
        {
            // single channel selection
            return Collections.singletonList(comboBoxValue);
        }
    }

    private List<String> getMergedChannelSelection()
    {
        boolean allSelected = true;
        List<String> channels = new ArrayList<String>();
        for (CheckBox cb : getAllCheckBoxes())
        {
            if (cb.getValue() == true)
            {
                channels.add(cb.getValueAttribute());
            } else
            {
                allSelected = false;
            }
        }

        if (allSelected)
        {
            // all channels selected
            // do not list them one-by-one. use the more general "MERGED_CHANNELS" term instead
            return Collections.singletonList(MERGED_CHANNELS);
        }
        return channels;
    }

    /**
     * @return the current channel selection. If all channels are selected, returns whole list of channels.
     */
    public List<LabeledItem<String>> getSelectedLabeledValues()
    {
        LabeledItem<String> comboBoxValue;
        if (channelsComboBox.getSelectedIndex() == -1)
        {
            comboBoxValue = null;
        } else
        {
            comboBoxValue = channelsComboBox.getSimpleValue();
        }

        if (comboBoxValue == null)
        {
            return Collections.<LabeledItem<String>> emptyList();
        }
        if (ScreeningConstants.MERGED_CHANNELS.equals(comboBoxValue.getItem()))
        {
            // multiple channel selection
            return getMergedChannelLabeledSelection();
        } else
        {
            // single channel selection
            return Collections.singletonList(comboBoxValue);
        }
    }

    private List<LabeledItem<String>> getMergedChannelLabeledSelection()
    {
        List<LabeledItem<String>> channels = new ArrayList<LabeledItem<String>>();
        for (CheckBox cb : getAllCheckBoxes())
        {
            if (cb.getValue() == true)
            {
                channels.add(new LabeledItem<String>(cb.getValueAttribute(), cb.getBoxLabel()));
            }
        }

        return channels;
    }

    public void addChannelsForParameters(ImageDatasetParameters imageParameters)
    {
        addChannelToComboBox(new LabeledItem<String>(MERGED_CHANNELS, MERGED_CHANNELS));

        List<LabeledItem<String>> channels = extractLabeledChannels(imageParameters);
        if (channels == null || channels.isEmpty())
        {
            return;
        }

        List<CheckBox> newCheckBoxes = new ArrayList<CheckBox>();
        for (LabeledItem<String> channel : channels)
        {
            boolean codeAdded = addChannelToComboBox(channel);
            String code = channel.getItem();
            Set<InternalImageTransformationInfo> transformationsForChannel =
                    transformationsForChannels.get(code);
            if (transformationsForChannel == null)
            {
                transformationsForChannel = new LinkedHashSet<InternalImageTransformationInfo>();
                transformationsForChannels.put(code, transformationsForChannel);
            }
            transformationsForChannel.addAll(imageParameters
                    .getAvailableImageTransformationsFor(code));

            intensitiesPerChannel.put(code, defaultChannelState.tryGetIntensityRange(code));

            if (codeAdded)
            {
                // also add a checkBockbox for the channel
                CheckBox checkBox = new CheckBox();
                checkBox.setBoxLabel(channel.getLabel());
                checkBox.setValueAttribute(channel.getItem());
                checkBox.addListener(Events.Change, selectionChangeListener);
                newCheckBoxes.add(checkBox);
            }
        }

        updateCheckBoxGroup(newCheckBoxes);
    }

    private List<LabeledItem<String>> extractLabeledChannels(ImageDatasetParameters imageParameters)
    {
        if (imageParameters == null || imageParameters.getChannelsNumber() == 0)
        {
            return Collections.emptyList();
        }
        List<String> codes = imageParameters.getChannelsCodes();
        List<String> labels = imageParameters.getChannelsLabels();
        List<LabeledItem<String>> result = new ArrayList<LabeledItem<String>>();
        for (int i = 0; i < codes.size(); i++)
        {
            String code = codes.get(i);
            String label = code;
            if (i < labels.size() && false == StringUtils.isBlank(labels.get(i)))
            {
                label = labels.get(i);
            }
            result.add(new LabeledItem<String>(code, label));
        }
        return result;
    }

    private boolean addChannelToComboBox(LabeledItem<String> channel)
    {
        if (channelsComboBox.findModelForVal(channel.getItem()) == null)
        {
            channelsComboBox.add(channel);
            return true;
        }
        return false;
    }

    public void updateChannelSelection(List<String> selectedChannels)
    {
        List<String> channels = selectedChannels;
        if (channels == null || channels.size() == 0)
        {
            if (defaultChannelState != null)
            {
                channels = defaultChannelState.tryGetDefaultChannels();
            }
        }

        String codeToSelect = ScreeningConstants.MERGED_CHANNELS;
        if (channels != null && channels.size() == 1)
        {
            codeToSelect = channels.get(0);
        }

        LabeledItem<String> itemToSelect = channelsComboBox.findModelForVal(codeToSelect);
        channelsComboBox.setSelection(itemToSelect);
        initializeCheckBoxValues(channels);
        updateTransformationComboBox();
    }

    private void initializeCheckBoxValues(List<String> selectedChannels)
    {
        boolean selectAllChannels = (selectedChannels == null) || (selectedChannels.size() < 2);

        for (CheckBox cb : getAllCheckBoxes())
        {
            @SuppressWarnings("null")
            boolean checked =
                    selectAllChannels || selectedChannels.contains(cb.getValueAttribute());
            cb.setValue(checked);
        }

    }

    private void selectionChanged()
    {
        List<String> selection = getSelectedValues();
        defaultChannelState.setDefaultChannels(selection);

        String selectedComboValue = channelsComboBox.tryGetChosenItem();
        boolean showCheckBoxGroup = ScreeningConstants.MERGED_CHANNELS.equals(selectedComboValue);
        channelsCheckBoxGroup.setVisible(showCheckBoxGroup);

        ensureAtLeastOneCheckboxChecked();
        updateTransformationComboBox();

        notifySelectionListeners(selection, tryGetSelectedTransformationCode(false),
                tryGetSelectedIntensityRanges());
    }

    public String tryGetSelectedTransformationCode(boolean force)
    {
        String code = null;

        if (force || transformationsComboBox.isVisible(false))
        {
            code = transformationsComboBox.getSelection().get(0).getValue().getItem().getCode();
        } else if (transformationsComboBox.getStore().getModels().size() == 1)
        {
            code =
                    transformationsComboBox.getStore().getModels().get(0).getValue().getItem()
                            .getCode();
        }

        return transformCode(code);
    }

    public Map<String, IntensityRange> tryGetSelectedIntensityRanges()
    {
        List<LabeledItem<String>> selectedValues = getSelectedLabeledValues();
        if (selectedValues != null)
        {
            Map<String, IntensityRange> ranges = new HashMap<String, IntensityRange>();
            for (LabeledItem<String> channelCode : selectedValues)
            {
                ranges.put(channelCode.getItem(), intensitiesPerChannel.get(channelCode.getItem()));
            }

            return ranges;
        }

        return null;
    }

    private static String transformCode(String code)
    {
        if (code == null || DEFAULT_TRANSFORMATION_CODE.equals(code))
        {
            return null;
        } else
        {
            return code;
        }
    }

    private void notifySelectionListeners(List<String> selection,
            String imageTransformationCodeOrNull, Map<String, IntensityRange> rangesOrNull)
    {
        for (ChannelSelectionListener listener : channelSelectionListeners)
        {
            listener.selectionChanged(selection, imageTransformationCodeOrNull, rangesOrNull);
        }
    }

    private void updateCheckBoxGroup(List<CheckBox> newCheckBoxes)
    {
        if (newCheckBoxes.isEmpty())
        {
            return;
        }

        boolean recreateCheckBoxGroup = channelsCheckBoxGroup.isRendered();

        if (recreateCheckBoxGroup)
        {
            // we must create a new CheckBoxGroup because the old one
            // wouldn't accept new checkboxes after rendering
            newCheckBoxes.addAll(0, getAllCheckBoxes());
            remove(channelsCheckBoxGroup);
            channelsCheckBoxGroup = createCheckBoxGroup();
        }

        for (CheckBox cb : newCheckBoxes)
        {
            channelsCheckBoxGroup.add(cb);
        }

        if (recreateCheckBoxGroup)
        {
            add(channelsCheckBoxGroup);
            layout(true);
        }

    }

    private void ensureAtLeastOneCheckboxChecked()
    {
        List<CheckBox> checkboxes = getAllCheckBoxes();
        int selected = 0;

        for (CheckBox cb : checkboxes)
        {
            if (cb.getValue())
            {
                selected++;
            }
        }

        if (selected > 1)
        {
            for (CheckBox cb : checkboxes)
            {
                cb.setEnabled(true);
            }
        } else
        {
            for (CheckBox cb : checkboxes)
            {
                if (cb.getValue())
                {
                    cb.setEnabled(false);
                }
            }
        }

    }

    private List<CheckBox> getAllCheckBoxes()
    {
        List<CheckBox> result = new ArrayList<CheckBox>();
        for (Field<?> field : channelsCheckBoxGroup.getAll())
        {
            if (field instanceof CheckBox)
            {
                result.add((CheckBox) field);
            }
        }
        return result;
    }

    private void updateTransformationComboBox()
    {
        List<String> selectedValues = getSelectedValues();

        transformationsComboBox.removeAll();
        transformationsComboBox.clearState();

        List<LabeledItem<InternalImageTransformationInfo>> model =
                new ArrayList<LabeledItem<InternalImageTransformationInfo>>();
        Set<InternalImageTransformationInfo> infos = null;
        if (selectedValues.size() == 1
                && (infos = transformationsForChannels.get(selectedValues.get(0))) != null
                && infos.size() > 0)
        {
            if (isDefaultTransformationDefined(infos) == false)
            {
                model.add(DEFAULT_TRANSFORMATION);
            }
            for (InternalImageTransformationInfo imageTransformationInfo : infos)
            {
                model.add(convertToLabeledItem(imageTransformationInfo));
            }
            if (model.size() <= 1)
            {
                transformationsComboBox.setVisible(false);
                adjustLabel.setVisible(false);
                userDefinedTransformationSettingsButtonField.setVisible(false);
                return;
            }
            transformationsComboBox.add(model);

            selectTransformation(selectedValues.get(0));
            setTransformationsVisible(true);
        } else if (selectedValues.size() > 0)
        {
            model.add(DEFAULT_TRANSFORMATION);
            model.add(convertToLabeledItem(new InternalImageTransformationInfo(
                    ScreeningConstants.USER_DEFINED_RESCALING_CODE, "User defined",
                    "User defined intensity rescaling", "", false)));
            transformationsComboBox.add(model);
            selectTransformation(ScreeningConstants.MERGED_CHANNELS);
            setTransformationsVisible(true);
        } else
        {
            setTransformationsVisible(false);
        }
    }

    private void changeTransformationSettingsButtonVisibility(boolean visible, boolean force)
    {
        if (visible
                && ScreeningConstants.USER_DEFINED_RESCALING_CODE
                        .equals(tryGetSelectedTransformationCode(force)))
        {
            userDefinedTransformationSettingsButtonField.setVisible(true);
        } else
        {
            userDefinedTransformationSettingsButtonField.setVisible(false);
        }
    }

    private static boolean isDefaultTransformationDefined(Set<InternalImageTransformationInfo> infos)
    {
        for (InternalImageTransformationInfo imageTransformationInfo : infos)
        {
            if (imageTransformationInfo.isDefault())
            {
                return true;
            }
        }
        return false;
    }

    private void setTransformationsVisible(boolean visible)
    {
        transformationsComboBox.setVisible(visible);
        adjustLabel.setVisible(visible);
        changeTransformationSettingsButtonVisibility(visible, true);
    }

    private void selectTransformation(String channelCode)
    {
        boolean selected = false;
        String code = defaultChannelState.tryGetDefaultTransformation(channelCode);

        SimpleModelComboBox<InternalImageTransformationInfo> combobox = transformationsComboBox;
        selected = setSelectedValue(code, combobox);
        if (false == selected)
        {
            for (SimpleComboValue<LabeledItem<InternalImageTransformationInfo>> info : transformationsComboBox
                    .getStore().getModels())
            {
                if (info.getValue().getItem().isDefault())
                {
                    transformationsComboBox.setSelection(Collections.singletonList(info));
                    selected = true;
                    break;
                }
            }
        }

        if (false == selected)
        {
            transformationsComboBox.setSelection(Collections.singletonList(transformationsComboBox
                    .getStore().getModels().get(0)));
        }

        changeTransformationSettingsButtonVisibility(true, true);
    }

    private boolean setSelectedValue(String code,
            SimpleModelComboBox<InternalImageTransformationInfo> combobox)
    {
        if (code != null)
        {
            for (SimpleComboValue<LabeledItem<InternalImageTransformationInfo>> info : combobox
                    .getStore().getModels())
            {
                if (info.getValue().getItem().getCode().equals(code))
                {
                    combobox.setSelection(Collections.singletonList(info));
                    return true;
                }
            }
        }
        return false;
    }

    private static LabeledItem<InternalImageTransformationInfo> convertToLabeledItem(
            InternalImageTransformationInfo imageTransformationInfo)
    {
        return new LabeledItem<InternalImageTransformationInfo>(imageTransformationInfo,
                imageTransformationInfo.getLabel(), imageTransformationInfo.getDescription());
    }
}