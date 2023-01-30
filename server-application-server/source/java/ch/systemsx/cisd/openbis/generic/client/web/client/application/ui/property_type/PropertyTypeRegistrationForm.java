/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type;

import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.SampleTypeDisplayID;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.DataTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineVarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.XmlField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.VocabularySelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.HtmlMessageElement;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;

/**
 * A {@link LayoutContainer} extension for registering a new property type.
 * 
 * @author Christian Ribeaud
 * @author Piotr Buczek
 */
public final class PropertyTypeRegistrationForm extends AbstractRegistrationForm
{
    public static final String ID = GenericConstants.ID_PREFIX + "property-type-registration_form";

    private final CodeField propertyTypeCodeField;

    private final VarcharField propertyTypeLabelField;

    private final MultilineVarcharField propertyTypeDescriptionField;

    private final DataTypeSelectionWidget dataTypeSelectionWidget;

    private final VocabularySelectionWidget vocabularySelectionWidget;

    private final MaterialTypeSelectionWidget materialTypeSelectionWidget;

    private final SampleTypeSelectionWidget sampleTypeSelectionWidget;

    private final XmlField xmlSchemaField;

    private final XmlField xslTransformationsField;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        PropertyTypeRegistrationForm form = new PropertyTypeRegistrationForm(viewContext);
        IDatabaseModificationObserver observer = form.createDatabaseModificationObserver();
        return new DatabaseModificationAwareComponent(form, observer);
    }

    private PropertyTypeRegistrationForm(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, ID, DEFAULT_LABEL_WIDTH + 20, DEFAULT_FIELD_WIDTH);
        setResetButtonVisible(true);
        this.viewContext = viewContext;

        this.propertyTypeCodeField = createPropertyTypeCodeField();
        this.propertyTypeLabelField = createPropertyTypeLabelField();
        this.propertyTypeDescriptionField = createPropertyTypeDescriptionField();
        this.dataTypeSelectionWidget = createDataTypeSelectionWidget();
        this.vocabularySelectionWidget = createVocabularySelectionWidget();
        this.materialTypeSelectionWidget = createMaterialTypeSelectionField();
        this.sampleTypeSelectionWidget = createSampleTypeSelectionField();
        this.xmlSchemaField = createXmlSchemaField();
        this.xslTransformationsField = createXslTransformationsField();

        vocabularySelectionWidget.setVisible(false);
        materialTypeSelectionWidget.setVisible(false);
        sampleTypeSelectionWidget.setVisible(false);
        xmlSchemaField.setVisible(false);
        xslTransformationsField.setVisible(false);

        formPanel.add(propertyTypeCodeField);
        formPanel.add(propertyTypeLabelField);
        formPanel.add(propertyTypeDescriptionField);
        formPanel.add(dataTypeSelectionWidget);
        formPanel.add(vocabularySelectionWidget);
        formPanel.add(materialTypeSelectionWidget);
        formPanel.add(sampleTypeSelectionWidget);
        formPanel.add(xmlSchemaField);
        formPanel.add(xslTransformationsField);
    }

    private MaterialTypeSelectionWidget createMaterialTypeSelectionField()
    {
        String label = viewContext.getMessage(Dict.ALLOW_ANY_TYPE);
        MaterialTypeSelectionWidget chooser =
                MaterialTypeSelectionWidget
                        .createWithAdditionalOption(viewContext, label, null, ID);
        FieldUtil.markAsMandatory(chooser);
        return chooser;
    }

    private SampleTypeSelectionWidget createSampleTypeSelectionField()
    {
        SampleTypeSelectionWidget chooser = new SampleTypeSelectionWidget(viewContext, ID, false, true, false, null,
                SampleTypeDisplayID.SAMPLE_QUERY);
        FieldUtil.markAsMandatory(chooser);
        return chooser;
    }

    private final CodeField createPropertyTypeCodeField()
    {
        final CodeField codeField = new CodeField(viewContext, viewContext.getMessage(Dict.CODE));
        codeField.setId(getId() + "_code");
        return codeField;
    }

    private final VarcharField createPropertyTypeLabelField()
    {
        final VarcharField varcharField =
                new VarcharField(viewContext.getMessage(Dict.LABEL), true);
        varcharField.setId(getId() + "_label");
        varcharField.setMaxLength(GenericConstants.COLUMN_LABEL);
        return varcharField;
    }

    private final MultilineVarcharField createPropertyTypeDescriptionField()
    {
        return new DescriptionField(viewContext, true, getId());
    }

    private final XmlField createXmlSchemaField()
    {
        final String label = viewContext.getMessage(Dict.XML_SCHEMA);
        final String description = viewContext.getMessage(Dict.XML_SCHEMA_INFO);
        final AbstractImagePrototype infoIcon =
                AbstractImagePrototype.create(viewContext.getImageBundle().getInfoIcon());
        final XmlField field = new XmlField(label, false);
        FieldUtil.addInfoIcon(field, description, infoIcon.createImage());
        return field;
    }

    private final XmlField createXslTransformationsField()
    {
        final String label = viewContext.getMessage(Dict.XSLT);
        final String description = viewContext.getMessage(Dict.XSLT_INFO);
        final AbstractImagePrototype infoIcon =
                AbstractImagePrototype.create(viewContext.getImageBundle().getInfoIcon());
        final XmlField field = new XmlField(label, false);
        FieldUtil.addInfoIcon(field, description, infoIcon.createImage());
        return field;
    }

    private final DataTypeSelectionWidget createDataTypeSelectionWidget()
    {
        final DataTypeSelectionWidget selectionWidget =
                new DataTypeSelectionWidget(viewContext, true);
        selectionWidget.addSelectionChangedListener(new DataTypeSelectionChangedListener());
        return selectionWidget;
    }

    private final VocabularySelectionWidget createVocabularySelectionWidget()
    {
        VocabularySelectionWidget result = new VocabularySelectionWidget(viewContext);
        FieldUtil.markAsMandatory(result);
        return result;
    }

    private final String getPropertyTypeCode()
    {
        return propertyTypeCodeField.getValue().toUpperCase();
    }

    private final PropertyType createPropertyType()
    {
        final PropertyType propertyType = new PropertyType();
        propertyType.setCode(getPropertyTypeCode());
        propertyType.setLabel(propertyTypeLabelField.getValue());
        propertyType.setDescription(propertyTypeDescriptionField.getValue());
        final DataType selectedDataType = dataTypeSelectionWidget.tryGetSelectedDataType();
        propertyType.setDataType(selectedDataType);
        switch (selectedDataType.getCode())
        {
            case MATERIAL:
                propertyType.setMaterialType(tryGetSelectedMaterialTypeProperty());
                break;
            case SAMPLE:
                propertyType.setSampleType(tryGetSelectedSampleTypeProperty());
                break;
            case CONTROLLEDVOCABULARY:
                propertyType.setVocabulary(tryGetSelectedVocabulary());
                break;
            case XML:
                propertyType.setSchema(tryGetXmlSchema());
                propertyType.setTransformation(tryGetXslTransformation());
                break;
            default:
                break;
        }
        return propertyType;
    }

    private MaterialType tryGetSelectedMaterialTypeProperty()
    {
        return materialTypeSelectionWidget.tryGetSelected();
    }

    private SampleType tryGetSelectedSampleTypeProperty()
    {
        return sampleTypeSelectionWidget.tryGetSelected();
    }

    private Vocabulary tryGetSelectedVocabulary()
    {
        return (Vocabulary) GWTUtils.tryGetSingleSelected(vocabularySelectionWidget);
    }

    private String tryGetXmlSchema()
    {
        return xmlSchemaField.getValue();
    }

    private String tryGetXslTransformation()
    {
        return xslTransformationsField.getValue();
    }

    //
    // AbstractRegistrationForm
    //

    @Override
    protected final void submitValidForm()
    {
        final PropertyType propertyType = createPropertyType();
        viewContext.getService().registerPropertyType(propertyType,
                new PropertyTypeRegistrationCallback(viewContext, propertyType));
    }

    //
    // Helper classes
    //

    private final class DataTypeSelectionChangedListener extends
            SelectionChangedListener<DataTypeModel>
    {

        //
        // SelectionChangedListener
        //

        @Override
        public final void selectionChanged(final SelectionChangedEvent<DataTypeModel> se)
        {
            hideDataTypeRelatedFields();

            DataTypeModel selectedItem = se.getSelectedItem();
            if (selectedItem != null)
            {
                DataTypeCode dataTypeCode = selectedItem.getDataType().getCode();
                switch (dataTypeCode)
                {
                    case CONTROLLEDVOCABULARY:
                        showFields(vocabularySelectionWidget);
                        break;
                    case MATERIAL:
                        showFields(materialTypeSelectionWidget);
                        break;
                    case SAMPLE:
                        showFields(sampleTypeSelectionWidget);
                        break;
                    case XML:
                        showFields(xmlSchemaField, xslTransformationsField);
                        break;
                    default:
                        break;
                }
            }

        }

        private void showFields(Field<?>... fields)
        {
            FieldUtil.setVisibility(true, fields);
        }

        private void hideDataTypeRelatedFields()
        {
            FieldUtil.setVisibility(false, vocabularySelectionWidget, materialTypeSelectionWidget,
                    sampleTypeSelectionWidget, xmlSchemaField, xslTransformationsField);
        }
    }

    private final class PropertyTypeRegistrationCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<Void>
    {
        private final PropertyType propertyType;

        PropertyTypeRegistrationCallback(final IViewContext<?> viewContext,
                final PropertyType propertyType)
        {
            super(viewContext);
            this.propertyType = propertyType;
        }

        @Override
        protected List<HtmlMessageElement> createSuccessfullRegistrationInfo(Void result)
        {
            return Arrays.asList(new HtmlMessageElement("Property type <b>" + propertyType.getCode() + "</b> successfully registered."));
        }
    }

    public IDatabaseModificationObserver createDatabaseModificationObserver()
    {
        CompositeDatabaseModificationObserver observer =
                new CompositeDatabaseModificationObserver();
        observer.addObserver(vocabularySelectionWidget);
        observer.addObserver(materialTypeSelectionWidget);
        observer.addObserver(sampleTypeSelectionWidget);
        return observer;
    }

}
