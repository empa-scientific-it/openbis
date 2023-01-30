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

import java.util.Set;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryDatabase;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryParameterBindings;

/**
 * The toolbar of query viewer for running custom queries.
 * 
 * @author Piotr Buczek
 */
public class RunCustomQueryToolbar extends AbstractQueryProviderToolbar
{

    private static final int QUERY_FIELD_WIDTH = 600;

    private final TextArea queryField;

    private final QueryDatabaseSelectionWidget queryDatabaseSelectionWidget;

    public RunCustomQueryToolbar(final IViewContext<IQueryClientServiceAsync> viewContext)
    {
        super(viewContext);
        setAlignment(HorizontalAlignment.CENTER);
        this.queryField = createQueryField();
        this.queryDatabaseSelectionWidget = new QueryDatabaseSelectionWidget(viewContext, null);
        add(new LabelToolItem(viewContext.getMessage(Dict.SQL_QUERY)
                + GenericConstants.LABEL_SEPARATOR));
        add(queryField);
        add(new LabelToolItem(viewContext.getMessage(Dict.QUERY_DATABASE)
                + GenericConstants.LABEL_SEPARATOR));
        add(queryDatabaseSelectionWidget);
        add(executeButton);
    }

    private TextArea createQueryField()
    {
        TextArea result = new SQLQueryField(viewContext, true, 4);
        result.setWidth(QUERY_FIELD_WIDTH);
        return result;
    }

    @Override
    protected boolean isQueryValid()
    {
        return queryField.isValid() && queryDatabaseSelectionWidget.isValid();
    }

    //
    // ICustomQueryProvider
    //

    @Override
    public Long tryGetQueryId()
    {
        return null;
    }

    @Override
    public String tryGetSQLQuery()
    {
        return queryField.getValue();
    }

    @Override
    public QueryDatabase tryGetQueryDatabase()
    {
        return queryDatabaseSelectionWidget.tryGetSelected();
    }

    @Override
    public QueryParameterBindings tryGetQueryParameterBindings()
    {
        return null;
    }

    // IDatabaseModificationObserver

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[0];
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        // nothing to do
    }

}
