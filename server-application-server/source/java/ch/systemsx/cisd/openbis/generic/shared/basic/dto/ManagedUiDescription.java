/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedOutputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.ITableModel;

/**
 * Object that declaratively describes a UI (labels, fields, their ordering, table content).
 * 
 * @author Piotr Buczek
 */
public class ManagedUiDescription implements IManagedUiDescription, Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private IManagedOutputWidgetDescription outputWidget = new ManagedHtmlWidgetDescription();

    private List<IManagedUiAction> actions = new ArrayList<IManagedUiAction>();

    public ManagedUiDescription()
    {
    }

    @Override
    public IManagedOutputWidgetDescription getOutputWidgetDescription()
    {
        return outputWidget;
    }

    public void setOutputWidgetDescription(IManagedOutputWidgetDescription outputWidget)
    {
        this.outputWidget = outputWidget;
    }

    @Override
    public IManagedUiAction addAction(String id)
    {
        IManagedUiAction action = new ManagedUiActionDescription(id);
        actions.add(action);
        return action;
    }

    @Override
    public ManagedUiTableActionDescription addTableAction(String id)
    {
        ManagedUiTableActionDescription action = new ManagedUiTableActionDescription(id);
        actions.add(action);
        return action;
    }

    @Override
    public List<IManagedUiAction> getActions()
    {
        return actions;
    }

    @Override
    public void useTableOutput(ITableModel tableModel)
    {
        ManagedTableWidgetDescription tableWidget = new ManagedTableWidgetDescription();
        if (tableModel instanceof TableModel)
        {
            tableWidget.setTableModel((TableModel) tableModel);
        } else
        {
            throw new IllegalArgumentException("Unsupported implementation of ITableModel");
        }
        setOutputWidgetDescription(tableWidget);
    }

    @Override
    public void useHtmlOutput(String htmlText)
    {
        setOutputWidgetDescription(new ManagedHtmlWidgetDescription(htmlText));
    }
}
