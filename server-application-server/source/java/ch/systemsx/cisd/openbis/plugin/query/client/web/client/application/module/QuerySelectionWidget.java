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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;

/**
 * {@link ComboBox} containing list of queries loaded from the server.
 * 
 * @author Piotr Buczek
 */
public final class QuerySelectionWidget extends DropDownList<QueryModel, QueryExpression>
{
    private static final String LIST_ITEMS_CALLBACK = "ListItemsCallback";

    public static final String SUFFIX = "query";

    private final IViewContext<IQueryClientServiceAsync> viewContext;

    private final String initialQueryNameOrNull;

    private final QueryType queryType;

    private final BasicEntityType entityTypeOrNull;

    public QuerySelectionWidget(final IViewContext<IQueryClientServiceAsync> viewContext,
            String initialQueryNameOrNull, QueryType queryType, BasicEntityType entityTypeOrNull)
    {
        super(viewContext, SUFFIX, Dict.QUERY, ModelDataPropertyNames.NAME, Dict.QUERY, "queries");
        this.viewContext = viewContext;
        this.initialQueryNameOrNull = initialQueryNameOrNull;
        this.queryType = queryType;
        this.entityTypeOrNull = entityTypeOrNull;
        setCallbackId(createCallbackId());
        setTemplate(GWTUtils.getTooltipTemplate(ModelDataPropertyNames.NAME,
                ModelDataPropertyNames.TOOLTIP));
    }

    public static String createCallbackId()
    {
        return QuerySelectionWidget.class + LIST_ITEMS_CALLBACK;
    }

    @Override
    protected List<QueryModel> convertItems(List<QueryExpression> result)
    {
        return QueryModel.convert(result);
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<QueryExpression>> callback)
    {
        viewContext.getService().listQueries(queryType, entityTypeOrNull,
                new ListQueriesCallback(viewContext));
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
        if (initialQueryNameOrNull != null)
        {
            trySelectByName(initialQueryNameOrNull);
            updateOriginalValue();
        }
    }

    private void trySelectByName(String queryName)
    {
        try
        {
            GWTUtils.setSelectedItem(this, ModelDataPropertyNames.NAME, queryName);
        } catch (IllegalArgumentException ex)
        {
            GWTUtils.alert("Error", "Query '" + queryName + "' doesn't exist.");
        }
    }

    private class ListQueriesCallback extends QuerySelectionWidget.ListItemsCallback
    {

        protected ListQueriesCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        public void process(List<QueryExpression> result)
        {
            super.process(result);
            selectInitialValue();
        }
    }
}
