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

package ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.module;

import java.util.List;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryDatabase;

/**
 * {@link ComboBox} containing list of databases loaded from the server.
 * 
 * @author Piotr Buczek
 */
public final class QueryDatabaseSelectionWidget extends
        DropDownList<QueryDatabaseModel, QueryDatabase>
{
    private static final String LIST_ITEMS_CALLBACK = "ListItemsCallback";

    public static final String SUFFIX = "database";

    private final IViewContext<IQueryClientServiceAsync> viewContext;

    private final QueryDatabase initialDatabaseOrNull;

    public QueryDatabaseSelectionWidget(final IViewContext<IQueryClientServiceAsync> viewContext,
            QueryDatabase initialDatabaseOrNull)
    {
        super(viewContext, SUFFIX, Dict.QUERY_DATABASE, ModelDataPropertyNames.NAME,
                Dict.QUERY_DATABASE, "databases");
        this.viewContext = viewContext;
        this.initialDatabaseOrNull = initialDatabaseOrNull;
        setEditable(false);
        setCallbackId(createCallbackId());
        setTemplate(GWTUtils.getTooltipTemplate(ModelDataPropertyNames.NAME,
                ModelDataPropertyNames.TOOLTIP));
    }

    public static String createCallbackId()
    {
        return QueryDatabaseSelectionWidget.class + LIST_ITEMS_CALLBACK;
    }

    @Override
    protected List<QueryDatabaseModel> convertItems(List<QueryDatabase> result)
    {
        return QueryDatabaseModel.convert(result);
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<QueryDatabase>> callback)
    {
        viewContext.getService().listQueryDatabases(new ListDatabasesCallback(viewContext));
        callback.ignore();
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.QUERY);
    }

    //
    // initial value support
    //

    private void selectInitialValue()
    {
        if (initialDatabaseOrNull != null)
        {
            trySelect(initialDatabaseOrNull);
            updateOriginalValue();
        }
    }

    private void trySelect(QueryDatabase database)
    {
        try
        {
            GWTUtils.setSelectedItem(this, ModelDataPropertyNames.NAME, database.getLabel());
        } catch (IllegalArgumentException ex)
        {
            viewContext.log(ex.getMessage());
            GWTUtils.alert("Error", "Query Database '" + database.getLabel()
                    + "' isn't configured.");
        }
    }

    private class ListDatabasesCallback extends QueryDatabaseSelectionWidget.ListItemsCallback
    {

        protected ListDatabasesCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        public void process(List<QueryDatabase> result)
        {
            super.process(result);
            selectInitialValue();
        }
    }
}
