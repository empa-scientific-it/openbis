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

package ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.SampleTypeDisplayID;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IParameterField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IParameterValuesLoader;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ParameterField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.report.ReportGeneratedCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.report.ReportGeneratedCallback.IOnReportComponentGeneratedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.ExpressionUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.IReportInformationProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ParameterValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Constants;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.NewQuery;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryDatabase;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryParameterBindings;

/**
 * @author Franz-Josef Elmer
 */
public class QueryEditor extends Dialog implements IDisposableComponent
{
    public static final String ID = Constants.QUERY_ID_PREFIX + "_query_editor";

    private static final FormData FORM_DATA = new FormData("100%");

    private static Button createCancelButton(IViewContext<?> viewContext, final Window window)
    {
        return new Button(
                viewContext
                        .getMessage(ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict.BUTTON_CANCEL),
                new SelectionListener<ButtonEvent>()
                    {
                        @Override
                        public final void componentSelected(ButtonEvent ce)
                        {
                            window.hide();
                        }
                    });
    }

    private static interface QueryExecutor
    {
        public void execute(QueryParameterBindings parameterBindings);
    }

    private static final class BindingsDialog extends Dialog
    {
        private final List<IParameterField> parameterFields;

        private final QueryExecutor queryExecutor;

        public BindingsDialog(final IViewContext<IQueryClientServiceAsync> viewContext,
                final List<String> parameters, final QueryDatabase queryDatabase,
                final QueryExecutor queryExecutor)
        {
            this.queryExecutor = queryExecutor;
            setHeading(viewContext.getMessage(Dict.QUERY_PARAMETERS_BINDINGS_DIALOG_TITLE));
            setModal(true);
            setScrollMode(Scroll.AUTO);
            setHideOnButtonClick(true);
            setButtons("");
            final FormPanel form = new FormPanel();
            form.setHeaderVisible(false);
            form.setBorders(false);
            form.setBodyBorder(false);
            form.setLabelWidth(150);
            form.setFieldWidth(250);

            parameterFields = new ArrayList<IParameterField>();
            final IParameterValuesLoader parameterValuesloader = new IParameterValuesLoader()
                {
                    @Override
                    public void loadData(String queryExpression,
                            AbstractAsyncCallback<List<ParameterValue>> listParameterValuesCallback)
                    {
                        viewContext.getService().listParameterValues(queryDatabase,
                                queryExpression, listParameterValuesCallback);
                    }
                };
            for (String parameter : parameters)
            {
                final IParameterField parameterField =
                        ParameterField.create(viewContext, parameter, null,
                                IDelegatedAction.DO_NOTHING, parameterValuesloader);
                parameterFields.add(parameterField);
                form.add(parameterField.asWidget());
            }

            add(form, new BorderLayoutData(LayoutRegion.CENTER));
            addButton(new Button(
                    viewContext
                            .getMessage(ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict.BUTTON_SUBMIT),
                    new SelectionListener<ButtonEvent>()
                        {
                            @Override
                            public final void componentSelected(ButtonEvent ce)
                            {
                                if (form.isValid())
                                {
                                    prepareBindingsAndExecuteQuery();
                                    hide();
                                }
                            }
                        }));
            addButton(createCancelButton(viewContext, this));
            setWidth(500);
        }

        private void prepareBindingsAndExecuteQuery()
        {
            QueryParameterBindings bindings = new QueryParameterBindings();
            for (IParameterField field : parameterFields)
            {
                bindings.addBinding(field.getParameterWithValue());
            }
            queryExecutor.execute(bindings);
        }
    }

    private final IViewContext<IQueryClientServiceAsync> viewContext;

    private final TextField<String> nameField;

    private final TextField<String> descriptionField;

    private final SQLQueryField statementField;

    private final CheckBoxField isPublicField;

    private final QueryExpression queryOrNull;

    private final int parentHeight;

    private final SimpleComboBox<QueryType> queryTypeField;

    private final QueryDatabaseSelectionWidget queryDatabaseSelectionWidget;

    private IDisposableComponent reportAsDisposable;

    // entity type selection widgets (not more than one will be shown at the same time)

    private MaterialTypeSelectionWidget materialTypeField;

    private SampleTypeSelectionWidget sampleTypeField;

    private ExperimentTypeSelectionWidget experimentTypeField;

    private DataSetTypeSelectionWidget dataSetTypeField;

    public QueryEditor(final IViewContext<IQueryClientServiceAsync> viewContext,
            QueryExpression queryOrNull, IDelegatedAction refreshAction, int parentWidth,
            int parentHeight)
    {
        this.viewContext = viewContext;
        this.queryOrNull = queryOrNull;
        this.parentHeight = parentHeight;
        setHeading(viewContext.getMessage(queryOrNull == null ? Dict.QUERY_CREATE_TITLE
                : Dict.QUERY_EDIT_TITLE));
        setModal(true);
        setLayout(new FitLayout());
        setButtons("");

        nameField =
                AbstractRegistrationDialog
                        .createTextField(
                                viewContext
                                        .getMessage(ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict.NAME),
                                true);
        nameField.setMaxLength(200);
        descriptionField =
                AbstractRegistrationDialog
                        .createTextField(
                                viewContext
                                        .getMessage(ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict.DESCRIPTION),
                                false);
        descriptionField.setMaxLength(GenericConstants.DESCRIPTION_2000);
        statementField = createStatementField();
        isPublicField =
                new CheckBoxField(
                        viewContext
                                .getMessage(ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict.IS_PUBLIC),
                        false);
        queryDatabaseSelectionWidget =
                new QueryDatabaseSelectionWidget(viewContext,
                        (queryOrNull != null) ? queryOrNull.getQueryDatabase() : null);
        queryTypeField = new QueryTypeComboBox(viewContext);
        queryTypeField.addListener(Events.SelectionChange,
                new Listener<SelectionChangedEvent<SimpleComboValue<QueryType>>>()
                    {
                        @Override
                        public void handleEvent(
                                SelectionChangedEvent<SimpleComboValue<QueryType>> be)
                        {
                            QueryType selectedType = be.getSelectedItem().getValue();
                            statementField.updateQueryType(selectedType);
                            updateFieldsVisibility(selectedType);
                        }
                    });
        createEntityTypeFields();
        if (queryOrNull != null)
        {
            FieldUtil.setValueWithUnescaping(nameField, queryOrNull.getName());
            FieldUtil.setValueWithUnescaping(descriptionField, queryOrNull.getDescription());
            FieldUtil.setValueWithUnescaping(statementField, queryOrNull.getExpression());
            isPublicField.setValue(queryOrNull.isPublic());
            queryTypeField.setSimpleValue(queryOrNull.getQueryType());
            // initial values for entity type fields are set when they are created
        } else
        {
            queryTypeField.setSimpleValue(QueryType.GENERIC);
        }

        FormPanel form = createFormPanel();
        setTopComponent(createFormPanel());
        addButton(createSaveButton(form, refreshAction));
        addButton(createTestButton(form));
        addButton(createCancelButton(viewContext, this));

        setPosition(5, 70);
        setWidth(parentWidth);
    }

    @Override
    public Component getComponent()
    {
        return this;
    }

    @Override
    public void dispose()
    {
        if (reportAsDisposable != null)
        {
            reportAsDisposable.dispose();
        }
    }

    private void createEntityTypeFields()
    {
        final IViewContext<ICommonClientServiceAsync> commonViewContext =
                viewContext.getCommonViewContext();

        String materialTypeInitialCodeOrNull = null;
        String sampleTypeInitialCodeOrNull = null;
        String experimentTypeInitialCodeOrNull = null;
        String dataSetTypeInitialCodeOrNull = null;
        if (queryOrNull != null)
        {
            final String entityTypeCode = queryOrNull.getEntityTypeCode();
            switch (queryOrNull.getQueryType())
            {
                case DATA_SET:
                    dataSetTypeInitialCodeOrNull = entityTypeCode;
                    break;
                case EXPERIMENT:
                    experimentTypeInitialCodeOrNull = entityTypeCode;
                    break;
                case MATERIAL:
                    materialTypeInitialCodeOrNull = entityTypeCode;
                    break;
                case SAMPLE:
                    sampleTypeInitialCodeOrNull = entityTypeCode;
                    break;
                case GENERIC:
                    break;
            }
        }

        materialTypeField =
                MaterialTypeSelectionWidget.createWithAdditionalOption(commonViewContext,
                        EntityType.ALL_TYPES_CODE, materialTypeInitialCodeOrNull, ID);
        materialTypeField.setAllowValueNotFromList(true);
        materialTypeField.setAllowBlank(false);

        sampleTypeField =
                new SampleTypeSelectionWidget(commonViewContext, ID, false, true, false,
                        sampleTypeInitialCodeOrNull, SampleTypeDisplayID.SAMPLE_QUERY);
        sampleTypeField.setAllowValueNotFromList(true);
        sampleTypeField.setAllowBlank(false);

        dataSetTypeField =
                new DataSetTypeSelectionWidget(commonViewContext, ID, true,
                        dataSetTypeInitialCodeOrNull);
        dataSetTypeField.setAllowValueNotFromList(true);
        dataSetTypeField.setAllowBlank(false);

        experimentTypeField =
                new ExperimentTypeSelectionWidget(commonViewContext, ID, true,
                        experimentTypeInitialCodeOrNull);
        experimentTypeField.setAllowValueNotFromList(true);
        experimentTypeField.setAllowBlank(false);
    }

    private FormPanel createFormPanel()
    {
        FormPanel form = new FormPanel();
        form.setHeaderVisible(false);
        form.setBorders(true);
        form.setBodyBorder(false);
        form.add(nameField, FORM_DATA);
        form.add(queryDatabaseSelectionWidget, FORM_DATA);
        form.add(queryTypeField, FORM_DATA);
        form.add(materialTypeField, FORM_DATA);
        form.add(sampleTypeField, FORM_DATA);
        form.add(dataSetTypeField, FORM_DATA);
        form.add(experimentTypeField, FORM_DATA);
        form.add(descriptionField, FORM_DATA);
        form.add(statementField, FORM_DATA);
        form.add(isPublicField);
        form.setPadding(20);
        return form;
    }

    private void updateFieldsVisibility(QueryType selectedType)
    {
        FieldUtil.setVisibility(selectedType == QueryType.DATA_SET, dataSetTypeField);
        FieldUtil.setVisibility(selectedType == QueryType.EXPERIMENT, experimentTypeField);
        FieldUtil.setVisibility(selectedType == QueryType.MATERIAL, materialTypeField);
        FieldUtil.setVisibility(selectedType == QueryType.SAMPLE, sampleTypeField);
    }

    private DropDownList<?, ? extends EntityType> tryGetEntityTypeField(QueryType selectedQueryType)
    {
        switch (selectedQueryType)
        {
            case DATA_SET:
                return dataSetTypeField;
            case EXPERIMENT:
                return experimentTypeField;
            case MATERIAL:
                return materialTypeField;
            case SAMPLE:
                return sampleTypeField;
            default:
                return null;
        }
    }

    private String tryExtractEntityTypeCode(QueryType selectedQueryType)
    {
        DropDownList<?, ? extends EntityType> entityTypeFieldOrNull =
                tryGetEntityTypeField(selectedQueryType);
        if (entityTypeFieldOrNull == null)
        {
            return null;
        } else
        {
            EntityType entityTypeOrNull = entityTypeFieldOrNull.tryGetSelected();
            String entityTypeCode =
                    entityTypeOrNull == null ? entityTypeFieldOrNull.getRawValue()
                            : entityTypeOrNull.getCode();
            return EntityType.isAllTypesCode(entityTypeCode) ? null : entityTypeCode;
        }
    }

    static class QueryTypeComboBox extends SimpleComboBox<QueryType>
    {

        public QueryTypeComboBox(IMessageProvider messages)
        {
            setFireChangeEventOnSetValue(true);
            setAllowBlank(false);
            setEditable(false);
            setTriggerAction(TriggerAction.ALL);
            setFieldLabel(messages.getMessage(Dict.QUERY_TYPE));
            add(Arrays.asList(QueryType.values()));
        }
    }

    private SQLQueryField createStatementField()
    {
        SQLQueryField field = new SQLQueryField(viewContext, true, 10);
        field.setMaxLength(2000);
        return field;
    }

    private Button createSaveButton(final FormPanel form, final IDelegatedAction refreshAction)
    {
        final Button button =
                new Button(
                        viewContext
                                .getMessage(ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict.BUTTON_SAVE),
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public final void componentSelected(final ButtonEvent ce)
                                {
                                    if (form.isValid())
                                    {
                                        register(new AbstractAsyncCallback<Void>(viewContext)
                                            {

                                                @Override
                                                protected void process(Void result)
                                                {
                                                    hide();
                                                    refreshAction.execute();
                                                }
                                            });
                                    }
                                }
                            });
        return button;
    }

    private Button createTestButton(final FormPanel form)
    {
        Button testButton = new Button(viewContext.getMessage(Dict.BUTTON_TEST_QUERY));
        testButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    if (form.isValid())
                    {
                        QueryDatabase queryDatabase = queryDatabaseSelectionWidget.tryGetSelected();
                        List<String> parameters =
                                ExpressionUtil.extractParameters(statementField.getValue());
                        parameters = ExpressionUtil.createDistinctParametersList(parameters);
                        if (parameters.size() > 0)
                        {
                            new BindingsDialog(viewContext, parameters, queryDatabase,
                                    new QueryExecutor()
                                        {
                                            @Override
                                            public void execute(
                                                    QueryParameterBindings parameterBindings)
                                            {
                                                runQuery(parameterBindings);
                                            }
                                        }).show();
                        } else
                        {
                            runQuery(new QueryParameterBindings());
                        }
                    }
                }

            });
        return testButton;
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[0];
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
    }

    protected void register(AsyncCallback<Void> registrationCallback)
    {
        final String name = nameField.getValue();
        final String description = descriptionField.getValue();
        final String statement = statementField.getValue();
        final boolean isPublic = isPublicField.getValue();
        final QueryType queryType = queryTypeField.getSimpleValue();
        final QueryDatabase queryDatabase = queryDatabaseSelectionWidget.tryGetSelected();
        final String entityTypeOrNull = tryExtractEntityTypeCode(queryType);
        if (queryOrNull == null)
        {
            NewQuery query = new NewQuery();
            query.setName(name);
            query.setDescription(description);
            query.setExpression(statement);
            query.setPublic(isPublic);
            query.setQueryType(queryType);
            query.setQueryDatabase(queryDatabase);
            query.setEntityTypeCode(entityTypeOrNull);
            viewContext.getService().registerQuery(query, registrationCallback);
        } else
        {
            queryOrNull.setName(name);
            queryOrNull.setDescription(description);
            queryOrNull.setExpression(statement);
            queryOrNull.setPublic(isPublic);
            queryOrNull.setQueryType(queryType);
            queryOrNull.setQueryDatabase(queryDatabase);
            queryOrNull.setEntityTypeCode(entityTypeOrNull);
            viewContext.getService().updateQuery(queryOrNull, registrationCallback);
        }
    }

    private void runQuery(QueryParameterBindings parameterBindings)
    {
        String sqlStatement = statementField.getValue();
        QueryDatabase queryDatabase = queryDatabaseSelectionWidget.tryGetSelected();
        if (sqlStatement != null && sqlStatement.length() > 0 && queryDatabase != null)
        {
            IReportInformationProvider reportInformation =
                    createReportInformationProvider(sqlStatement);

            viewContext.getService().createQueryResultsReport(
                    queryDatabase,
                    sqlStatement,
                    parameterBindings,
                    ReportGeneratedCallback.create(viewContext.getCommonViewContext(),
                            reportInformation, reportInformation.getKey(),
                            createDisplayQueryResultsAction()));
        }
    }

    private IReportInformationProvider createReportInformationProvider(final String sqlQuery)
    {
        return new IReportInformationProvider()
            {
                @Override
                public String getDownloadURL()
                {
                    return null;
                }

                @Override
                public String getKey()
                {
                    return Integer.toString(sqlQuery.hashCode());
                }
            };
    }

    private IOnReportComponentGeneratedAction createDisplayQueryResultsAction()
    {
        return new IOnReportComponentGeneratedAction()
            {
                @Override
                public void execute(final IDisposableComponent reportComponent)
                {
                    removeAll();
                    dispose();
                    add(reportComponent.getComponent());
                    reportAsDisposable = reportComponent;
                    if (getHeight() < parentHeight)
                    {
                        setHeight(parentHeight);
                    }
                    layout();
                }
            };
    }
}
