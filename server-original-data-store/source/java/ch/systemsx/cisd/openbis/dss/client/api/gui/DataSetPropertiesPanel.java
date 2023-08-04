/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.client.api.gui;

import static ch.systemsx.cisd.openbis.dss.client.api.gui.DataSetUploadClient.BUTTON_HEIGHT;
import static ch.systemsx.cisd.openbis.dss.client.api.gui.DataSetUploadClient.BUTTON_WIDTH;
import static ch.systemsx.cisd.openbis.dss.client.api.gui.DataSetUploadClient.LABEL_WIDTH;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.dss.client.api.gui.model.DataSetUploadClientModel;
import ch.systemsx.cisd.openbis.dss.client.api.gui.model.DataSetUploadClientModel.NewDataSetInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTOBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetMetadataDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationError;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyTypeGroup;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetPropertiesPanel extends JPanel
{

    public static interface Observer
    {
        public void update();
    }

    private static final long serialVersionUID = 1L;

    private final DataSetType dataSetType;

    private final DataSetUploadClientModel clientModel;

    private final HashMap<String, JComponent> formFields = new HashMap<String, JComponent>();

    private final HashMap<String, JLabel> labels = new HashMap<String, JLabel>();

    private final LinkedList<Observer> observers = new LinkedList<Observer>();

    private NewDataSetInfo newDataSetInfo;

    public DataSetPropertiesPanel(DataSetType dataSetType, DataSetUploadClientModel clientModel)
    {
        super();
        this.dataSetType = dataSetType;
        this.clientModel = clientModel;
        setLayout(new GridBagLayout());
        createGui();
    }

    public NewDataSetInfo getDataSetInfo()
    {
        return newDataSetInfo;
    }

    public void setNewDataSetInfo(NewDataSetInfo dataSetInfo)
    {
        this.newDataSetInfo = dataSetInfo;
        syncGui();
    }

    /**
     * Return all editable widgets.
     */
    protected List<JComponent> getAllEditableWidgets()
    {
        ArrayList<JComponent> editableWidgets = new ArrayList<JComponent>(formFields.values());
        return editableWidgets;
    }

    private void createGui()
    {
        // Add the fields, two per row, to the GUI; ignore the groups for now
        int row = 0;
        int col = 0;
        List<PropertyTypeGroup> groups = dataSetType.getPropertyTypeGroups();
        for (PropertyTypeGroup group : groups)
        {
            for (PropertyType propertyType : group.getPropertyTypes())
            {
                addFormFieldForPropertyType(row, col, propertyType);
                if (++col > 1)
                {
                    // advance to the next row
                    ++row;
                    col = 0;
                }
            }
        }
    }

    private void addFormFieldForPropertyType(int row, int col, final PropertyType propertyType)
    {
        String labelString = getLabelStringForPropertyType(propertyType);
        JLabel label = new JLabel(labelString + ":", SwingConstants.TRAILING);
        label.setPreferredSize(new Dimension(LABEL_WIDTH, BUTTON_HEIGHT));
        label.setToolTipText(propertyType.getDescription());
        if (propertyType.isMandatory())
        {
            // Set the font to be bold/italic for required fields.
            label.setFont(label.getFont().deriveFont(Font.BOLD | Font.ITALIC));
        }

        final JComponent formField;
        if (propertyType instanceof ControlledVocabularyPropertyType)
        {
            formField = createComboBox((ControlledVocabularyPropertyType) propertyType);
        } else if (propertyType.getDataType() == DataTypeCode.BOOLEAN)
        {
            formField = createCheckBox(propertyType);
        } else
        {
            formField = createTextField(propertyType);
        }
        formField.setToolTipText(propertyType.getDescription());
        formField.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        addFormField(col, row, label, formField);
        labels.put(propertyType.getCode(), label);
        formFields.put(propertyType.getCode(), formField);
    }

    private JTextField createTextField(final PropertyType propertyType)
    {
        final JTextField textField = new JTextField();

        class FieldListener implements ActionListener, FocusListener
        {
            @Override
            public void focusGained(FocusEvent e)
            {
            }

            @Override
            public void focusLost(FocusEvent e)
            {
                handleEvent();
            }

            @Override
            public void actionPerformed(ActionEvent e)
            {
                handleEvent();
            }

            private void handleEvent()
            {
                String newValue = StringUtils.toStringEmptyIfNull(textField.getText());
                String oldValue =
                        StringUtils.toStringEmptyIfNull(setPropertyValue(propertyType, newValue));

                if (false == newValue.equals(oldValue))
                {
                    notifyObservers();
                }
            }
        }

        FieldListener listener = new FieldListener();
        textField.addActionListener(listener);
        textField.addFocusListener(listener);
        return textField;
    }

    private VocabularyTermsComboBoxPanel createComboBox(
            final ControlledVocabularyPropertyType propertyType)
    {
        final VocabularyTermsComboBoxPanel comboBox =
                new VocabularyTermsComboBoxPanel(propertyType, clientModel);

        clientModel.registerObserver(comboBox);

        comboBox.addItemListener(new ItemListener()
            {
                @Override
                public void itemStateChanged(ItemEvent e)
                {
                    setPropertyValue(propertyType, ((VocabularyTerm) e.getItem()).getCode());
                    notifyObservers();
                }
            });

        return comboBox;
    }

    private JComponent createCheckBox(final PropertyType propertyType)
    {
        final JCheckBox checkBox = new JCheckBox();

        checkBox.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    setPropertyValue(propertyType,
                            checkBox.isSelected() ? PropertyUtils.Boolean.TRUE.toString()
                                    : PropertyUtils.Boolean.FALSE.toString());
                    notifyObservers();
                }

            });
        return checkBox;
    }

    private String getLabelStringForPropertyType(PropertyType propertyType)
    {
        StringBuilder label = new StringBuilder();
        label.append(propertyType.getLabel());
        if (propertyType.isMandatory())
        {
            label.append("*");
        }
        return label.toString();
    }

    private void addFormField(int colx, int rowy, Component label, Component field)
    {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        // Internally, we consume two columns, one for the label, one for the field
        c.gridx = colx * 2;
        c.gridy = rowy;
        c.weightx = 0;
        c.weighty = 0;
        c.insets = new Insets((rowy > 0) ? 5 : 0, 0, 0, 5);
        add(label, c);
        c.gridx = c.gridx + 1;
        c.weightx = 0.5;
        c.insets = new Insets((rowy > 0) ? 5 : 0, 0, 0, 5);
        add(field, c);
    }

    private Serializable setPropertyValue(PropertyType propertyType, String text)
    {
        if (null == newDataSetInfo)
        {
            return null;
        }

        NewDataSetDTOBuilder builder = newDataSetInfo.getNewDataSetBuilder();
        NewDataSetMetadataDTO metadata = builder.getDataSetMetadata();
        Map<String, Serializable> props = metadata.getProperties();
        HashMap<String, Serializable> newProps = new HashMap<String, Serializable>(props);
        Serializable oldValue;
        if (null == text || text.trim().length() < 1)
        {
            oldValue = newProps.remove(propertyType.getCode());
        } else
        {
            oldValue = newProps.put(propertyType.getCode(), text);
        }
        metadata.setProperties(newProps);

        clientModel.validateNewDataSetInfoAndNotifyObservers(newDataSetInfo);
        syncErrors();
        clientModel.notifyObserversOfChanges(newDataSetInfo);

        return oldValue;
    }

    private void syncGui()
    {
        NewDataSetDTOBuilder builder = newDataSetInfo.getNewDataSetBuilder();
        NewDataSetMetadataDTO metadata = builder.getDataSetMetadata();
        Map<String, Serializable> props = metadata.getProperties();
        for (String propertyTypeCode : formFields.keySet())
        {
            Serializable propertyValue = props.get(propertyTypeCode);
            JComponent formField = formFields.get(propertyTypeCode);
            formField.setEnabled(false == metadata.isUnmodifiableProperty(propertyTypeCode));
            if (formField instanceof JTextField)
            {
                JTextField textField = (JTextField) formField;
                textField.setText(serializableToString(propertyValue));
            } else if (formField instanceof VocabularyTermsComboBoxPanel)
            {
                VocabularyTermsComboBoxPanel comboBox = (VocabularyTermsComboBoxPanel) formField;
                if(propertyValue.getClass().isArray()){
                    Serializable[] values = (Serializable[]) propertyValue;
                    for(Serializable val : values) {
                        setVocabToComboBox(comboBox, (String) val);
                    }
                } else {
                    setVocabToComboBox(comboBox, (String) propertyValue);
                }

            } else if (formField instanceof JCheckBox)
            {
                JCheckBox checkBox = (JCheckBox) formField;
                checkBox.setSelected(Boolean.parseBoolean((String)propertyValue));
            }
        }
    }

    private void setVocabToComboBox(VocabularyTermsComboBoxPanel comboBox, String value) {

        for (int i = 0; i < comboBox.getItemCount(); ++i)
        {
            VocabularyTerm term = comboBox.getItemAt(i);
            if (term.getCode().equals(value))
            {
                comboBox.setSelectedIndex(i);
            }
        }
    }

    private String serializableToString(Serializable value) {
        if(value == null) {
            return null;
        }
        if(value.getClass().isArray()) {
            Serializable[] values = (Serializable[]) value;
            StringBuilder builder = new StringBuilder("[");
            for(Serializable val : values) {
                if(builder.length() > 1) {
                    builder.append(", ");
                }
                builder.append((String)val);
            }
            builder.append("]");
            return builder.toString();
        } else {
            return (String) value;
        }
    }

    public void syncErrors()
    {
        // Clear all errors first
        for (String key : labels.keySet())
        {
            clearError(labels.get(key), formFields.get(key));
        }

        List<ValidationError> errors = newDataSetInfo.getValidationErrors();
        for (ValidationError error : errors)
        {
            switch (error.getTarget())
            {
                case DATA_SET_OWNER:
                    // These are handled by the Metadata Panel
                    break;

                case DATA_SET_TYPE:
                    // These are handled by the Metadata Panel
                    break;

                case DATA_SET_FILE:
                    // These are handled by the Metadata Panel
                    break;

                case DATA_SET_PROPERTY:
                    JLabel label = labels.get(error.getPropertyCodeOrNull());
                    JComponent formField = formFields.get(error.getPropertyCodeOrNull());
                    displayError(label, formField, error);
                    break;
            }
        }
    }

    private void displayError(JLabel label, JComponent component, ValidationError error)
    {
        // Not all errors are applicable to this panel
        if (null == label || null == component)
        {
            return;
        }
        UiUtilities.displayError(label, component, null, error);
    }

    private void clearError(JLabel label, JComponent component)
    {
        UiUtilities.clearError(label, component, null);
        component.setToolTipText(label.getToolTipText());
    }

    public void registerObserver(Observer observer)
    {
        observers.add(observer);
    }

    private void notifyObservers()
    {
        for (Observer observer : observers)
        {
            observer.update();
        }
    }
}
