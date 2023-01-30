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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField.wrapUnaware;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.SampleTypeDisplayID;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetParentsArea;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetsArea;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetsContainedArea;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.FileFormatTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.FileFormatTypeSelectionWidget.FileFormatTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField.ExperimentChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserField.SampleChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.HtmlMessageElement;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetUpdates;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelationshipRole;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractGenericEntityRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.PropertiesEditor;

/**
 * The <i>generic</i> data set edit form.
 * 
 * @author Piotr Buczek
 */
public final class GenericDataSetEditForm extends
        AbstractGenericEntityRegistrationForm<DataSetType, DataSetTypePropertyType>
{
    private static final String SAMPLE_FIELD_ID_SUFFIX = "sample_field";

    private static final String EXPERIMENT_FIELD_ID_SUFFIX = "experiment_field";

    private final String simpleId;

    private CheckBoxField connectedWithSampleCheckbox;

    private DataSetsArea parentsArea;

    // two options:
    // 1. connected with sample
    private SampleChooserFieldAdaptor sampleChooser;

    // 2. not connected with sample
    private ExperimentChooserFieldAdaptor experimentChooser;

    //

    private AbstractExternalData originalDataSet;

    private IDataSetEditorBuilder builder;

    public static DatabaseModificationAwareComponent create(
            IViewContext<IGenericClientServiceAsync> viewContext, IIdAndCodeHolder identifiable)
    {
        GenericDataSetEditForm form = new GenericDataSetEditForm(viewContext, identifiable);
        return new DatabaseModificationAwareComponent(form, form);
    }

    private GenericDataSetEditForm(IViewContext<IGenericClientServiceAsync> viewContext,
            IIdAndCodeHolder identifiable)
    {
        super(viewContext, null, identifiable, EntityKind.DATA_SET);
        setRevertButtonVisible(true);
        simpleId = createSimpleId(identifiable, EntityKind.DATA_SET);
    }

    private String createChildId(String childSuffix)
    {
        return getId() + childSuffix;
    }

    @Override
    public final void submitValidForm()
    {
        viewContext.getService().updateDataSet(createUpdates(),
                new UpdateDataSetCallback(viewContext));
    }

    private DataSetUpdates createUpdates()
    {
        final DataSetUpdates result = new DataSetUpdates();
        result.setDatasetId(techIdOrNull);
        result.setProperties(extractProperties());
        result.setVersion(originalDataSet.getVersion());
        if (isConnectedWithSample())
        {
            result.setSampleIdentifierOrNull(extractSampleIdentifier());
        } else
        {
            result.setExperimentIdentifierOrNull(extractExperimentIdentifier());
        }
        result.setModifiedParentDatasetCodesOrNull(extractParentDatasetCodes());
        result.setMetaprojectsOrNull(metaprojectArea.tryGetModifiedMetaprojects());
        builder.fillUpdates(result);
        return result;
    }

    private Boolean isConnectedWithSample()
    {
        return connectedWithSampleCheckbox.getValue();
    }

    private String extractSampleIdentifier()
    {
        return sampleChooser.getValue();
    }

    private String extractExperimentIdentifier()
    {
        ExperimentIdentifier identifierOrNull = experimentChooser.tryToGetValue();
        return identifierOrNull == null ? null : identifierOrNull.getIdentifier();
    }

    private String[] extractParentDatasetCodes()
    {
        return parentsArea.tryGetModifiedDataSetCodes();
    }

    // public only for tests
    public final class UpdateDataSetCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<DataSetUpdateResult>
    {

        UpdateDataSetCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final DataSetUpdateResult result)
        {
            updateOriginalValues(result);
            super.process(result);
        }

        @Override
        protected List<HtmlMessageElement> createSuccessfullRegistrationInfo(DataSetUpdateResult result)
        {
            return Arrays.asList(new HtmlMessageElement("Data set successfully updated"));
        }
    }

    private void updateOriginalValues(final DataSetUpdateResult result)
    {
        originalDataSet.setVersion(result.getVersion());
        updatePropertyFieldsOriginalValues();
        final List<String> parentCodes = result.getParentCodes();
        parentsArea.setDataSetCodes(parentCodes);
        connectedWithSampleCheckbox.updateOriginalValue(connectedWithSampleCheckbox.getValue());
        sampleChooser.updateOriginalValue();
        experimentChooser.updateOriginalValue();
        updateFieldOriginalValue(metaprojectArea);
        builder.updateOriginalValues(result);
    }

    @Override
    protected PropertiesEditor<DataSetType, DataSetTypePropertyType> createPropertiesEditor(
            String id, Map<String, List<IManagedInputWidgetDescription>> inputWidgetDescriptions,
            IViewContext<ICommonClientServiceAsync> context)
    {
        DataSetPropertyEditor editor =
                new DataSetPropertyEditor(id, inputWidgetDescriptions, context);
        return editor;
    }

    @Override
    protected List<DatabaseModificationAwareField<?>> getEntitySpecificFormFields()
    {
        ArrayList<DatabaseModificationAwareField<?>> fields =
                new ArrayList<DatabaseModificationAwareField<?>>();
        fields.add(wrapUnaware(connectedWithSampleCheckbox));
        fields.add(wrapUnaware(sampleChooser.getField()));
        fields.add(wrapUnaware(experimentChooser.getField()));
        fields.add(wrapUnaware(parentsArea));
        builder.addEntitySpecificFormFields(fields);
        return fields;
    }

    @Override
    protected void createEntitySpecificFormFields()
    {
        this.connectedWithSampleCheckbox = createConnectedWithSampleCheckbox();
        this.sampleChooser = createSampleField();
        this.experimentChooser = createExperimentChooserField();
        this.parentsArea = createParentsArea();
        builder.createEntitySpecificFormFields();
    }

    private CheckBoxField createConnectedWithSampleCheckbox()
    {
        CheckBoxField result = new CheckBoxField("Connected with " + viewContext.getMessage(Dict.SAMPLE), false);
        result.addListener(Events.Change, new Listener<FieldEvent>()
            {
                @Override
                public void handleEvent(FieldEvent be)
                {
                    updateFieldsVisibility();
                }
            });
        return result;
    }

    private SampleChooserFieldAdaptor createSampleField()
    {
        String label = viewContext.getMessage(Dict.SAMPLE);
        String originalSampleOrNull = originalDataSet.getSampleIdentifier();
        // one cannot select a sample from shared group or a sample that has no experiment
        final SampleChooserFieldAdaptor result =
                SampleChooserField.create(label, true, originalSampleOrNull, false, false, false,
                        viewContext.getCommonViewContext(),
                        SampleTypeDisplayID.DATA_SET_EDIT_SAMPLE_CHOOSER, false);
        result.getField().setId(createChildId(SAMPLE_FIELD_ID_SUFFIX));
        return result;
    }

    private ExperimentChooserFieldAdaptor createExperimentChooserField()
    {
        String label = viewContext.getMessage(Dict.EXPERIMENT);
        Experiment experiment = originalDataSet.getExperiment();
        ExperimentIdentifier originalExperiment = experiment == null ? null :
                ExperimentIdentifier.createIdentifier(experiment);
        final ExperimentChooserFieldAdaptor result =
                ExperimentChooserField.create(label, true, originalExperiment,
                        viewContext.getCommonViewContext());
        result.getField().setId(createChildId(EXPERIMENT_FIELD_ID_SUFFIX));
        return result;
    }

    private DataSetsArea createParentsArea()
    {
        return new DataSetParentsArea(viewContext, simpleId);
    }

    private FileFormatTypeSelectionWidget createFileFormatTypeField()
    {
        final FileFormatTypeSelectionWidget result =
                new FileFormatTypeSelectionWidget(viewContext.getCommonViewContext(), getId());
        FieldUtil.markAsMandatory(result);
        return result;
    }

    @Override
    protected void initializeFormFields()
    {
        boolean connectedWithSample = StringUtils.isBlank(sampleChooser.getValue()) == false;
        connectedWithSampleCheckbox.setValue(connectedWithSample);
        updateFieldsVisibility();

        propertiesEditor.initWithProperties(originalDataSet.getDataSetType()
                .getAssignedPropertyTypes(), originalDataSet.getProperties());
        codeField.setValue(originalDataSet.getCode());
        metaprojectArea.setMetaprojects(originalDataSet.getMetaprojects());
        // data set fields are initialized when they are created
        parentsArea.setValue(viewContext.getMessage(Dict.LOAD_IN_PROGRESS));
        builder.initializeFormFields();
        loadDataInBackground();
    }

    private void loadDataInBackground()
    {
        // not best performance but the same solution that is done for experiments
        // only codes are needed but we extract 'full' object
        DefaultResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> config =
                DefaultResultSetConfig.createFetchAll();
        viewContext.getCommonService().listDataSetRelationships(techIdOrNull,
                DataSetRelationshipRole.CHILD, config, new ListParentsCallback(viewContext));
        builder.loadDataInBackground(new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    updateDirtyCheck();
                }
            });
    }

    private void updateFieldsVisibility()
    {
        boolean connectedWithSample = isConnectedWithSample();
        FieldUtil.setVisibility(connectedWithSample, sampleChooser.getField());
        FieldUtil.setVisibility(connectedWithSample == false, experimentChooser.getField());
    }

    private void setOriginalData(AbstractExternalData data)
    {
        this.originalDataSet = data;

        if (data.isContainer())
        {
            this.builder = new ContainerDataSetEditFormBuilder(data.tryGetAsContainerDataSet());
        } else if (data.isLinkData())
        {
            this.builder = new LinkDataSetEditFormBuilder(data.tryGetAsLinkDataSet());
        } else
        {
            this.builder = new ExternalDataEditFormBuilder(data.tryGetAsDataSet());
        }
    }

    @Override
    protected void loadForm()
    {
        viewContext.getService().getDataSetInfo(techIdOrNull, new DataSetInfoCallback(viewContext));

    }

    private List<AbstractExternalData> extractDataSets(List<TableModelRowWithObject<AbstractExternalData>> rows)
    {
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        for (TableModelRowWithObject<AbstractExternalData> row : rows)
        {
            dataSets.add(row.getObjectOrNull());
        }
        return dataSets;
    }

    private final class DataSetInfoCallback extends AbstractAsyncCallback<AbstractExternalData>
    {

        private DataSetInfoCallback(final IViewContext<IGenericClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected final void process(final AbstractExternalData result)
        {
            setOriginalData(result);
            initGUI();
        }
    }

    private class ListParentsCallback extends
            AbstractAsyncCallback<TypedTableResultSet<AbstractExternalData>>
    {

        public ListParentsCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(TypedTableResultSet<AbstractExternalData> result)
        {
            List<TableModelRowWithObject<AbstractExternalData>> rows =
                    result.getResultSet().getList().extractOriginalObjects();
            List<AbstractExternalData> dataSets = extractDataSets(rows);
            parentsArea.setDataSets(dataSets);
            updateDirtyCheck();
            if (parentsArea.isVisible())
            {
                parentsArea.setEnabled(true);
            }
        }
    }

    interface IDataSetEditorBuilder
    {

        void updateOriginalValues(DataSetUpdateResult result);

        void loadDataInBackground(Listener<BaseEvent> listener);

        void fillUpdates(DataSetUpdates result);

        void addEntitySpecificFormFields(List<DatabaseModificationAwareField<?>> fields);

        void createEntitySpecificFormFields();

        void initializeFormFields();

    }

    /** {@link IDataSetEditorBuilder} implementation for {@link PhysicalDataSet}-s */
    private class ExternalDataEditFormBuilder implements IDataSetEditorBuilder
    {
        private PhysicalDataSet dataSet;

        private FileFormatTypeSelectionWidget fileFormatTypeSelectionWidget;

        public ExternalDataEditFormBuilder(PhysicalDataSet dataSet)
        {
            this.dataSet = dataSet;
        }

        @Override
        public void createEntitySpecificFormFields()
        {
            this.fileFormatTypeSelectionWidget = createFileFormatTypeField();
        }

        @Override
        public void updateOriginalValues(DataSetUpdateResult result)
        {
            fileFormatTypeSelectionWidget.updateOriginalValue(fileFormatTypeSelectionWidget
                    .getValue());
        }

        @Override
        public void fillUpdates(DataSetUpdates result)
        {
            result.setFileFormatTypeCode(extractFileFormatTypeCode());
        }

        private String extractFileFormatTypeCode()
        {
            return fileFormatTypeSelectionWidget.tryGetSelectedFileFormatType().getCode();
        }

        @Override
        public void addEntitySpecificFormFields(List<DatabaseModificationAwareField<?>> fields)
        {
            fields.add(wrapUnaware(fileFormatTypeSelectionWidget));
        }

        @Override
        public void initializeFormFields()
        {
            fileFormatTypeSelectionWidget.setValue(new FileFormatTypeModel(dataSet
                    .getFileFormatType()));
        }

        @Override
        public void loadDataInBackground(Listener<BaseEvent> listener)
        {
            // nothing to do
        }

    }

    /** {@link IDataSetEditorBuilder} implementation for {@link ContainerDataSet}-s */
    private class ContainerDataSetEditFormBuilder implements IDataSetEditorBuilder
    {
        private DataSetsArea containedArea;

        public ContainerDataSetEditFormBuilder(ContainerDataSet dataSet)
        {
        }

        @Override
        public void createEntitySpecificFormFields()
        {
            this.containedArea = createContainsArea();
        }

        private DataSetsArea createContainsArea()
        {
            return new DataSetsContainedArea(viewContext, simpleId);
        }

        @Override
        public void updateOriginalValues(DataSetUpdateResult result)
        {
            final List<String> containedCodes = result.getContainedDataSetCodes();
            containedArea.setDataSetCodes(containedCodes);
        }

        @Override
        public void fillUpdates(DataSetUpdates result)
        {
            result.setModifiedContainedDatasetCodesOrNull(extractContainedDatasetCodes());
        }

        private String[] extractContainedDatasetCodes()
        {
            return containedArea.tryGetModifiedDataSetCodes();
        }

        @Override
        public void addEntitySpecificFormFields(List<DatabaseModificationAwareField<?>> fields)
        {
            fields.add(wrapUnaware(containedArea));
        }

        @Override
        public void initializeFormFields()
        {
            // data set fields are initialized when they are created
            containedArea.setValue(viewContext.getMessage(Dict.LOAD_IN_PROGRESS));
        }

        @Override
        public void loadDataInBackground(Listener<BaseEvent> listener)
        {
            // not best performance but the same solution that is done for experiments
            // only codes are needed but we extract 'full' object
            DefaultResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> config =
                    DefaultResultSetConfig.createFetchAll();
            viewContext.getCommonService().listDataSetRelationships(techIdOrNull,
                    DataSetRelationshipRole.CONTAINER, config,
                    new ListContainedDataSetsCallback(viewContext, listener));
        }

        private class ListContainedDataSetsCallback extends
                AbstractAsyncCallback<TypedTableResultSet<AbstractExternalData>>
        {

            private Listener<BaseEvent> listener;

            public ListContainedDataSetsCallback(IViewContext<?> viewContext,
                    Listener<BaseEvent> listener)
            {
                super(viewContext);
                this.listener = listener;
            }

            @Override
            protected void process(TypedTableResultSet<AbstractExternalData> result)
            {
                List<TableModelRowWithObject<AbstractExternalData>> rows =
                        result.getResultSet().getList().extractOriginalObjects();
                containedArea.setDataSets(extractDataSets(rows));
                listener.handleEvent(null);
                if (containedArea.isVisible())
                {
                    containedArea.setEnabled(true);
                }
            }
        }

    }

    /** {@link IDataSetEditorBuilder} implementation for {@link LinkDataSet}-s */
    private class LinkDataSetEditFormBuilder implements IDataSetEditorBuilder
    {
        private LinkDataSet dataSet;

        private CodeField externalCodeField;

        public LinkDataSetEditFormBuilder(LinkDataSet dataSet)
        {
            this.dataSet = dataSet;
        }

        @Override
        public void createEntitySpecificFormFields()
        {
            externalCodeField =
                    new CodeField(viewContext, viewContext.getMessage(Dict.EXTERNAL_CODE));
            externalCodeField.setId(getId() + "_externalCode");
            externalCodeField.setReadOnly(true);
            externalCodeField.setHideTrigger(true);
            externalCodeField.disable();
        }

        @Override
        public void updateOriginalValues(DataSetUpdateResult result)
        {
            // nothing to do
        }

        @Override
        public void fillUpdates(DataSetUpdates result)
        {
            // nothing to do
        }

        @Override
        public void addEntitySpecificFormFields(List<DatabaseModificationAwareField<?>> fields)
        {
            fields.add(0, wrapUnaware(externalCodeField));
        }

        @Override
        public void initializeFormFields()
        {
            externalCodeField.setValue(StringEscapeUtils.unescapeHtml(dataSet.getExternalCode()));
        }

        @Override
        public void loadDataInBackground(Listener<BaseEvent> listener)
        {
            // nothing to do
        }

    }

}
