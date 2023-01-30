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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonGroup;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IParameterField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IParameterValuesLoader;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ParameterField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ParameterValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.QueryParameterValue;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryDatabase;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryParameterBindings;

/**
 * The toolbar of query viewer for running predefined queries.
 * 
 * @author Piotr Buczek
 */
public class RunCannedQueryToolbar extends AbstractQueryProviderToolbar
{

    // 6 parameter fields fit into browser with 1024px width
    private static final int MAX_PARAMETER_COLUMNS = 6;

    private static final String INITIAL_PARAMETER_NAME_PREFIX = "_";

    public static final RunCannedQueryToolbar createGeneric(
            IViewContext<IQueryClientServiceAsync> viewContext, String initialQueryNameOrNull,
            Map<String, QueryParameterValue> initialParameterValues)
    {
        return new RunCannedQueryToolbar(viewContext, initialQueryNameOrNull,
                initialParameterValues, QueryType.GENERIC, null);
    }

    public static final RunCannedQueryToolbar createTyped(
            IViewContext<IQueryClientServiceAsync> viewContext, String initialQueryNameOrNull,
            Map<String, QueryParameterValue> initialParameterValues, QueryType queryType,
            BasicEntityType entityTypeOrNull)
    {
        return new RunCannedQueryToolbar(viewContext, initialQueryNameOrNull,
                initialParameterValues, queryType, entityTypeOrNull);
    }

    private final ContentPanel parameterContainer;

    private final QuerySelectionWidget querySelectionWidget;

    private final Button resetButton;

    private final Collection<IParameterField> parameterFields;

    // <name, value> where name starts with additional INITIAL_PARAMETER_NAME_PREFIX
    private final Map<String, QueryParameterValue> initialParameterValues;

    private final Map<String, String> initialFixedParameters;

    private RunCannedQueryToolbar(IViewContext<IQueryClientServiceAsync> viewContext,
            String initialQueryNameOrNull, Map<String, QueryParameterValue> initialParameterValues,
            QueryType queryType, BasicEntityType entityTypeOrNull)
    {
        super(viewContext);
        this.initialParameterValues = initialParameterValues;
        initialFixedParameters = new HashMap<String, String>();
        querySelectionWidget =
                new QuerySelectionWidget(viewContext, initialQueryNameOrNull, queryType,
                        entityTypeOrNull);
        parameterContainer = new ButtonGroup(MAX_PARAMETER_COLUMNS);
        parameterFields = new HashSet<IParameterField>();
        resetButton = new Button(viewContext.getMessage(ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict.BUTTON_RESET));
        add(new LabelToolItem(viewContext.getMessage(Dict.QUERY) + ": "));
        add(querySelectionWidget);
        add(parameterContainer);
        add(executeButton);
        add(resetButton);

        querySelectionWidget.addSelectionChangedListener(new SelectionChangedListener<QueryModel>()
            {
                @Override
                public void selectionChanged(SelectionChangedEvent<QueryModel> se)
                {
                    updateParameterFields();
                    tryExecuteQuery();
                }

            });
        querySelectionWidget.addPostRefreshCallback(new IDataRefreshCallback()
            {
                @Override
                public void postRefresh(boolean wasSuccessful)
                {
                    updateParameterFields();
                    // TODO 2009-11-24, Tomasz Pylak: IMPR this apply is usually unnecessary and
                    // causes screen flickering
                    tryExecuteQuery();
                }

            });
        resetButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    resetParameterFields();
                    tryExecuteQuery();
                }
            });
    }

    protected void updateParameterFields()
    {
        // Only show the filter selection widget if there are user choices
        boolean queriesAvailable = querySelectionWidget.getStore().getCount() > 0;
        setEnabled(queriesAvailable);

        executeButton.hide();

        removeAllParameterFields();
        QueryExpression queryOrNull = querySelectionWidget.tryGetSelected();
        if (queryOrNull != null)
        {
            createAndAddQueryParameterFields(queryOrNull);
            executeButton.show();
            updateExecuteButtonEnabledState();
        }

        boolean parametersAvailable = parameterContainer.getItemCount() > 0;
        resetButton.setVisible(parametersAvailable);
        parameterContainer.setVisible(parametersAvailable);

        layout();
    }

    private void createAndAddQueryParameterFields(final QueryExpression query)
    {
        parameterContainer.hide();
        final IDelegatedAction updateExecuteButtonAction = new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    updateExecuteButtonEnabledState();
                }
            };
        final IParameterValuesLoader parameterValuesloader = new IParameterValuesLoader()
            {
                @Override
                public void loadData(String queryExpression,
                        AbstractAsyncCallback<List<ParameterValue>> listParameterValuesCallback)
                {
                    viewContext.getService().listParameterValues(query.getQueryDatabase(),
                            queryExpression, listParameterValuesCallback);
                }
            };
        for (String parameterName : query.getParameters())
        {
            final String strippedParameterName = stripMetadata(parameterName);
            final QueryParameterValue initialValueOrNull =
                    tryGetInitialValue(strippedParameterName);
            if (initialValueOrNull != null && initialValueOrNull.isFixed())
            {
                addInitialBinding(strippedParameterName, initialValueOrNull.getValue());
            } else
            {
                addParameterField(ParameterField.create(viewContext, parameterName,
                        initialValueOrNull == null ? null : initialValueOrNull.getValue(),
                        updateExecuteButtonAction, parameterValuesloader));
            }
        }
    }

    private void addInitialBinding(String parameter, String value)
    {
        initialFixedParameters.put(parameter, value);
    }

    private QueryParameterValue tryGetInitialValue(String parameter)
    {
        return initialParameterValues.get(INITIAL_PARAMETER_NAME_PREFIX + parameter);
    }

    private void addParameterField(IParameterField parameterField)
    {
        parameterFields.add(parameterField);
        parameterContainer.add(parameterField.asWidget());
    }

    private void removeAllParameterFields()
    {
        parameterContainer.removeAll();
        parameterFields.clear();
    }

    private void updateExecuteButtonEnabledState()
    {
        executeButton.setEnabled(isQueryValid());
    }

    private void resetParameterFields()
    {
        for (IParameterField field : parameterFields)
        {
            field.asWidget().reset();
        }
        updateExecuteButtonEnabledState();
    }

    @Override
    protected boolean isQueryValid()
    {
        if (querySelectionWidget.isValid() == false)
        {
            return false;
        } else
        {
            boolean valid = true;
            for (IParameterField field : parameterFields)
            {
                valid = field.asWidget().isValid() && valid;
            }
            return valid;
        }
    }

    //
    // ICustomQueryProvider
    //

    @Override
    public Long tryGetQueryId()
    {
        QueryExpression selectedQueryOrNull = querySelectionWidget.tryGetSelected();
        return selectedQueryOrNull == null ? null : selectedQueryOrNull.getId();
    }

    @Override
    public String tryGetSQLQuery()
    {
        QueryExpression selectedQueryOrNull = querySelectionWidget.tryGetSelected();
        return selectedQueryOrNull == null ? null : selectedQueryOrNull.getExpression();
    }

    @Override
    public QueryDatabase tryGetQueryDatabase()
    {
        QueryExpression selectedQueryOrNull = querySelectionWidget.tryGetSelected();
        return selectedQueryOrNull == null ? null : selectedQueryOrNull.getQueryDatabase();
    }

    @Override
    public QueryParameterBindings tryGetQueryParameterBindings()
    {
        QueryParameterBindings bindings = new QueryParameterBindings();
        for (String key : initialFixedParameters.keySet())
        {
            bindings.addBinding(key, initialFixedParameters.get(key));
        }
        for (IParameterField field : parameterFields)
        {
            bindings.addBinding(field.getParameterWithValue());
        }
        return bindings;
    }

    // IDatabaseModificationObserver

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return querySelectionWidget.getRelevantModifications();
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        querySelectionWidget.update(observedModifications);
    }

    // helpers

    private static String stripMetadata(String parameterName)
    {
        int indexOfSeparator = parameterName.indexOf(ParameterField.PARAMETER_METADATA_SEPARATOR);
        return indexOfSeparator > -1 ? parameterName.substring(0, indexOfSeparator) : parameterName;
    }

}
