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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TriggerField;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ParameterValueModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ParameterValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ParameterWithValue;

/**
 * {@link TriggerField} extension for providing values for parameters.
 * 
 * @author Piotr Buczek
 */
public class ParameterField extends TriggerField<ModelData> implements IParameterField
{

    public static String PARAMETER_METADATA_SEPARATOR = "::";

    private static String ENUM_LIST_EXPRESSION_PREFIX = "list=";

    private static String ENUM_LIST_SEPARATOR = ",";

    private static String QUERY_LIST_EXPRESSION_PREFIX = "query=";

    private static String TYPE_EXPRESSION_PREFIX = "type=";

    public static IParameterField create(IViewContext<?> viewContextOrNull, String parameterName,
            String initialValueOrNull, IDelegatedAction onValueChangeAction,
            IParameterValuesLoader loaderOrNull)
    {
        String[] split = parameterName.split(PARAMETER_METADATA_SEPARATOR, 2);
        final String name = split[0];
        if (split.length == 2)
        {
            final String[] expressionParts = split[1].split(PARAMETER_METADATA_SEPARATOR);
            final String idSuffix = name.replaceAll(" ", "_");
            checkMetadata(name, expressionParts);
            for (String expressionPart : expressionParts)
            {
                if (expressionPart.startsWith(ENUM_LIST_EXPRESSION_PREFIX))
                {
                    String itemList =
                            expressionPart.substring(ENUM_LIST_EXPRESSION_PREFIX.length());
                    String[] values = itemList.split(ENUM_LIST_SEPARATOR);
                    List<ParameterValue> parameterValues = new ArrayList<ParameterValue>();
                    for (String value : values)
                    {
                        parameterValues.add(new ParameterValue(value, null));
                    }
                    return ParameterSelectionField.createWithValues(name, idSuffix,
                            parameterValues, initialValueOrNull, onValueChangeAction);
                } else if (expressionPart.startsWith(QUERY_LIST_EXPRESSION_PREFIX))
                {
                    String queryExpression =
                            expressionPart.substring(QUERY_LIST_EXPRESSION_PREFIX.length());
                    return ParameterSelectionField.createWithLoader(name, queryExpression,
                            idSuffix, viewContextOrNull, loaderOrNull, initialValueOrNull,
                            onValueChangeAction);
                }
            }
        }
        return new ParameterField(name, onValueChangeAction, initialValueOrNull);
    }

    static void checkMetadata(final String namePart, final String[] expressionParts)
    {
        for (String expressionPart : expressionParts)
        {
            if (expressionPart.startsWith(ENUM_LIST_EXPRESSION_PREFIX))
            {
                // OK
            } else if (expressionPart.startsWith(QUERY_LIST_EXPRESSION_PREFIX))
            {
                // OK
            } else if (expressionPart.startsWith(TYPE_EXPRESSION_PREFIX))
            {
                // OK
            } else
            {
                GWTUtils.alert("Error", "Filter parameter '" + namePart + "' is not defined properly.");
            }
        }
    }

    private final String parameterName;

    private final IDelegatedAction onValueChangeAction;

    private final String initialValueOrNull;

    private ParameterField(String parameterName, IDelegatedAction onValueChangeAction,
            String initialValueOrNull)
    {
        this.parameterName = parameterName;
        this.onValueChangeAction = onValueChangeAction;
        this.initialValueOrNull = initialValueOrNull;
        setEmptyText(parameterName);
        setToolTip(parameterName);
        setFieldLabel(parameterName); // used in custom query testing dialog
        setTriggerStyle("x-form-clear-trigger");
        FieldUtil.setMandatoryFlag(this, true);
        setAutoValidate(true);
        setValidateOnBlur(true);
        setWidth(100);
    }

    @Override
    public ParameterWithValue getParameterWithValue()
    {
        return new ParameterWithValue(parameterName, getRawValue());
    }

    @Override
    public Field<?> asWidget()
    {
        return this;
    }

    @Override
    protected void onRender(Element target, int index)
    {
        super.onRender(target, index);
        setRawValue(initialValueOrNull);
    }

    @Override
    protected void onKeyUp(FieldEvent fe)
    {
        super.onKeyUp(fe);
        onValueChangeAction.execute();
    }

    @Override
    protected void onTriggerClick(ComponentEvent ce)
    {
        super.onTriggerClick(ce);
        setValue(null);
        onValueChangeAction.execute();
    }

    private static class ParameterSelectionField extends
            DropDownList<ParameterValueModel, ParameterValue> implements IParameterField
    {

        private static final String VALUE_NOT_IN_LIST_MSG = "Value not in the list";

        private static final String EMPTY_MSG = "- No values found -";

        private final IViewContext<?> viewContextOrNull;

        private final IParameterValuesLoader loaderOrNull;

        private final String parameterName;

        private String initialValueOrNull;

        private final String queryExpressionOrNull;

        private final IDelegatedAction onValueChangeAction;

        /**
         * Allows to choose one of the values received from server.
         */
        public static IParameterField createWithLoader(final String parameterName,
                String queryExpression, String idSuffix, IViewContext<?> viewContextOrNull,
                IParameterValuesLoader loader, String initialValueOrNull,
                IDelegatedAction onValueChangeAction)
        {
            return new ParameterSelectionField(parameterName, idSuffix, viewContextOrNull, loader,
                    queryExpression, null, initialValueOrNull, onValueChangeAction);
        }

        /**
         * Allows to choose one of the specified values.
         */
        public static IParameterField createWithValues(final String parameterName, String idSuffix,
                List<ParameterValue> initialValues, String initialValueOrNull,
                IDelegatedAction onValueChangeAction)
        {
            return new ParameterSelectionField(parameterName, idSuffix, null, null, null,
                    initialValues, initialValueOrNull, onValueChangeAction);
        }

        protected ParameterSelectionField(final String parameterName, String idSuffix,
                IViewContext<?> viewContextOrNull, IParameterValuesLoader loaderOrNull,
                String queryExpressionOrNull, List<ParameterValue> valuesOrNull,
                String initialValueOrNull, final IDelegatedAction onValueChangeAction)
        {
            super(idSuffix, ModelDataPropertyNames.CODE, parameterName, parameterName, EMPTY_MSG,
                    VALUE_NOT_IN_LIST_MSG, true, viewContextOrNull, valuesOrNull == null);
            this.parameterName = parameterName;
            this.queryExpressionOrNull = queryExpressionOrNull;
            this.viewContextOrNull = viewContextOrNull;
            this.loaderOrNull = loaderOrNull;
            this.initialValueOrNull = initialValueOrNull;
            this.onValueChangeAction = onValueChangeAction;
            if (valuesOrNull != null)
            {
                setValues(valuesOrNull);
            }
            setTemplate(GWTUtils.getTooltipTemplate(ModelDataPropertyNames.CODE,
                    ModelDataPropertyNames.TOOLTIP));
            FieldUtil.setMandatoryFlag(this, true);
            setAllowValueNotFromList(true);
            setToolTip(parameterName);
            setWidth(100);

            addSelectionChangedListener(new SelectionChangedListener<ParameterValueModel>()
                {
                    @Override
                    public void selectionChanged(SelectionChangedEvent<ParameterValueModel> se)
                    {
                        onValueChangeAction.execute();
                    }
                });
        }

        private void setValues(List<ParameterValue> values)
        {
            final List<ParameterValueModel> models = new ArrayList<ParameterValueModel>();
            models.addAll(convertItems(values));
            updateStore(models);
            getPropertyEditor().setList(store.getModels());
            selectInitialValue();
        }

        @Override
        protected List<ParameterValueModel> convertItems(List<ParameterValue> result)
        {
            return ParameterValueModel.convert(result);
        }

        @Override
        protected void loadData(AbstractAsyncCallback<List<ParameterValue>> callback)
        {
            if (viewContextOrNull != null && loaderOrNull != null && queryExpressionOrNull != null)
            {
                loaderOrNull.loadData(queryExpressionOrNull, new ListParameterValuesCallback(
                        viewContextOrNull));
            }
            callback.ignore();
        }

        @Override
        public DatabaseModificationKind[] getRelevantModifications()
        {
            return new DatabaseModificationKind[0];
        }

        public void selectInitialValue()
        {
            if (initialValueOrNull != null)
            {
                trySelectByValue(initialValueOrNull);
                updateOriginalValue();
            }
        }

        public void trySelectByValue(String parameterValue)
        {
            GWTUtils.setSelectedItem(this, ModelDataPropertyNames.CODE, parameterValue);
        }

        private class ListParameterValuesCallback extends ParameterSelectionField.ListItemsCallback
        {

            protected ListParameterValuesCallback(IViewContext<?> viewContext)
            {
                super(viewContext);
            }

            @Override
            public void process(List<ParameterValue> result)
            {
                super.process(result);
                selectInitialValue();
            }
        }

        @Override
        public ParameterWithValue getParameterWithValue()
        {
            return new ParameterWithValue(parameterName, getRawValue());
        }

        @Override
        public Field<?> asWidget()
        {
            return this;
        }

        @Override
        protected void onKeyUp(FieldEvent fe)
        {
            super.onKeyUp(fe);
            onValueChangeAction.execute();
        }
    }
}
