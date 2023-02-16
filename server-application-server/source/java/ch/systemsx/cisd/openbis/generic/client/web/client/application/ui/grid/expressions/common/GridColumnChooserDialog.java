/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.expressions.common;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Event;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractColumnSettingsDataModelProvider;

/**
 * {@link Dialog} displaying {@link GridColumnChooser}. Used to insert selected columns into an expression to build the custom grid filter or column.
 * 
 * @author Izabela Adamczyk
 */
class GridColumnChooserDialog extends Dialog
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public static void show(IViewContext<ICommonClientServiceAsync> viewContext,
            AbstractColumnSettingsDataModelProvider columnDataModelProvider, String gridId,
            IExpressionHolder expressionField)
    {
        new GridColumnChooserDialog(viewContext, gridId).show(columnDataModelProvider,
                expressionField);
    }

    private GridColumnChooserDialog(IViewContext<ICommonClientServiceAsync> viewContext,
            String gridId)
    {
        this.viewContext = viewContext;
        setHeight(450);
        setWidth(700);
        setLayout(new FitLayout());
        setHeading(viewContext.getMessage(Dict.COLUMNS) + " [" + gridId + "]");
        setModal(true); // without it the dialog goes under filter edit/register dialog
    }

    /**
     * Shows window containing {@link GridColumnChooser} based on given {@link ColumnModel}.
     */
    private void show(final AbstractColumnSettingsDataModelProvider columnDataModelProvider,
            final IExpressionHolder expressionField)
    {
        assert columnDataModelProvider != null : "columnModels not specified";
        removeAll();
        final GridColumnChooser columnChooser =
                new GridColumnChooser(columnDataModelProvider, viewContext);
        final Component columnChooserComponent = columnChooser.getComponent();
        add(columnChooserComponent);

        columnChooserComponent.sinkEvents(Event.ONDBLCLICK);
        columnChooserComponent.addListener(Events.OnDoubleClick, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    insertColumnsIntoExpression(expressionField, columnChooser);
                }

            });
        super.show();
        getButtonById(OK).addListener(Events.Select, new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {
                    insertColumnsIntoExpression(expressionField, columnChooser);
                }
            });

    }

    private void insertColumnsIntoExpression(IExpressionHolder expressionField,
            GridColumnChooser columnChooser)
    {
        String expression = expressionField.getValue() != null ? expressionField.getValue() : "";
        int cursor = expressionField.getCursorPos();
        for (String column : columnChooser.getSelectedItems())
        {

            String addressWithSeparator = column + " ";
            expression =
                    expression.substring(0, cursor) + addressWithSeparator
                            + expression.substring(cursor);
            cursor += addressWithSeparator.length();
        }
        expressionField.setValue(expression);
        expressionField.setCursorPos(cursor);
        hide();
    }

}
