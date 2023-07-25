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
package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.managed_property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedTableWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedUiTableActionDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ReportRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiTableAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.ManagedTableActionRowSelectionType;

public final class ManagedPropertyGridActionDialog extends
        AbstractDataConfirmationDialog<List<TableModelRowWithObject<ReportRowModel>>>
{

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final AsyncCallback<Void> callback;

    private final IEntityInformationHolder entity;

    private final IManagedProperty managedProperty;

    private final IManagedUiTableAction managedAction;

    private final Map<String, TextField<?>> inputFieldsByCode =
            new LinkedHashMap<String, TextField<?>>();

    private final ManagedPropertyFormHelper formHelper;

    public ManagedPropertyGridActionDialog(IViewContext<ICommonClientServiceAsync> viewContext,
            String editTitle, List<TableModelRowWithObject<ReportRowModel>> data,
            AsyncCallback<Void> callback, IEntityInformationHolder entity,
            IManagedProperty managedProperty, IManagedUiTableAction managedAction)
    {
        super(viewContext, data, editTitle);
        this.viewContext = viewContext;
        this.entity = entity;
        this.managedProperty = managedProperty;
        this.callback = callback;
        this.managedAction = managedAction;
        setWidth(400);
        formHelper = new ManagedPropertyFormHelper(viewContext, formPanel, inputFieldsByCode)
            {
                @Override
                protected void trySetBoundedValue(IManagedInputWidgetDescription inputDescription)
                {
                    ManagedPropertyGridActionDialog.this.trySetBoundedValue(inputDescription);
                }
            };
    }

    @Override
    protected String createMessage()
    {
        return managedAction.getDescription() == null ? "Provide data" : managedAction
                .getDescription();
    }

    @Override
    protected void executeConfirmedAction()
    {
        if (managedAction instanceof ManagedUiTableActionDescription)
        {
            ManagedUiTableActionDescription ma = (ManagedUiTableActionDescription) managedAction;
            List<Integer> selectedRows = new ArrayList<Integer>();
            for (TableModelRowWithObject<ReportRowModel> rowModel : data)
            {
                selectedRows.add(rowModel.getObjectOrNull().getRowNumber());
            }
            Collections.sort(selectedRows);
            ma.setSelectedRows(selectedRows);
        }

        for (IManagedInputWidgetDescription inputDescription : managedAction
                .getInputWidgetDescriptions())
        {
            TextField<?> field = inputFieldsByCode.get(inputDescription.getCode());
            Object fieldValue = field.getValue();
            String value = fieldValue == null ? null : field.getValue().toString().trim();
            if (fieldValue instanceof SimpleComboValue)
            {
                value = ((SimpleComboValue<?>) fieldValue).getValue().toString();
            }
            inputDescription.setValue(value);
        }
        // old value was escaped going to the client - unescape it before sending back to the server
        managedProperty.setValue(StringEscapeUtils.unescapeHtml((String)managedProperty.getValue()));
        viewContext.getService().updateManagedProperty(TechId.create(entity),
                entity.getEntityKind(), managedProperty, managedAction, callback);
    }

    @Override
    protected void extendForm()
    {
        formPanel.setLabelWidth(100);
        formPanel.setFieldWidth(200);

        formHelper.fillForm(managedAction.getInputWidgetDescriptions());
    }

    /**
     * If the managed action requires single row to be selected and there is a binding between given input field and a table column it tries to get
     * the value from the selected table row and sets it in the input model.
     */
    private void trySetBoundedValue(IManagedInputWidgetDescription inputDescription)
    {
        if (managedAction.getSelectionType() == ManagedTableActionRowSelectionType.REQUIRED_SINGLE
                && data.size() == 1)
        {
            String boundedColumnTitleOrNull =
                    managedAction.getBindings().get(inputDescription.getCode());
            if (boundedColumnTitleOrNull != null)
            {
                if (viewContext.isLoggingEnabled())
                {
                    GWTUtils.displayInfo("found binding", inputDescription.getCode() + "->"
                            + boundedColumnTitleOrNull);
                }
                TableModelRowWithObject<ReportRowModel> selectedRow = data.get(0);
                TableModel tableModel =
                        ((ManagedTableWidgetDescription) managedProperty.getUiDescription()
                                .getOutputWidgetDescription()).getTableModel();

                for (TableModelColumnHeader header : tableModel.getHeader())
                {
                    if (header.getTitle().equals(boundedColumnTitleOrNull))
                    {
                        ISerializableComparable value =
                                selectedRow.getValues().get(header.getIndex());
                        inputDescription.setValue(value.toString());
                        if (viewContext.isLoggingEnabled())
                        {
                            Info.display("bounded value", inputDescription.getCode() + "=" + value);
                        }
                        break;
                    }
                }
            }
        }
    }

}
