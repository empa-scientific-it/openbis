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

package ch.systemsx.cisd.openbis.plugin.query.client.web.client.application;

import java.util.HashMap;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.Component;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableTabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.module.AbstractQueryProviderToolbar;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.module.QueryViewer;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.module.RunCannedQueryToolbar;

/**
 * Section panel presenting query results for given entity.
 * 
 * @author Izabela Adamczyk
 */
final class QuerySectionPanel extends DisposableTabContent
{
    private static final String PARAMETER_PREFIX = "_";

    private static final String TYPE = "type";

    private static final String KEY = "key";

    private final IViewContext<IQueryClientServiceAsync> queryModuleContext;

    private final IEntityInformationHolderWithPermId entity;

    public QuerySectionPanel(IViewContext<IQueryClientServiceAsync> queryModuleContext,
            final IEntityInformationHolderWithPermId entity)
    {
        super(
                queryModuleContext
                        .getMessage(ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Dict.QUERY_MODULE_MENU_TITLE),
                queryModuleContext, entity);
        this.queryModuleContext = queryModuleContext;
        this.entity = entity;
        setIds(DisplayTypeIDGenerator.QUERY_SECTION);
    }

    @Override
    protected IDisposableComponent createDisposableContent()
    {
        HashMap<String, QueryParameterValue> parameters = extractFixedQueryParameters(entity);
        AbstractQueryProviderToolbar toolbar =
                RunCannedQueryToolbar.createTyped(queryModuleContext, null, parameters,
                        translate(entity.getEntityKind()), entity.getEntityType());
        final DatabaseModificationAwareComponent viewer =
                QueryViewer.create(queryModuleContext, toolbar);
        return new IDisposableComponent()
            {
                @Override
                public void dispose()
                {
                    IDatabaseModificationObserver mobserver = viewer.getModificationObserver();
                    if (mobserver instanceof IDisposableComponent)
                    {
                        ((IDisposableComponent) mobserver).dispose();
                    }
                }

                @Override
                public Component getComponent()
                {
                    return viewer.get();
                }

                @Override
                public DatabaseModificationKind[] getRelevantModifications()
                {
                    return viewer.getRelevantModifications();
                }

                @Override
                public void update(Set<DatabaseModificationKind> observedModifications)
                {
                    viewer.update(observedModifications);
                }
            };
    }

    /**
     * Extracts fixed query parameters from given entity.
     * <ul>
     * <li>material: code (<code>${key}</code>) + type (<code>${type}</code>)
     * <li>data set: code (<code>${key}</code>)
     * <li>sample, experiment: perm id (<code>${key}</code>)
     * </ul>
     */
    private static HashMap<String, QueryParameterValue> extractFixedQueryParameters(
            final IEntityInformationHolderWithPermId entity)
    {
        HashMap<String, QueryParameterValue> parameters =
                new HashMap<String, QueryParameterValue>();
        if (entity.getEntityKind().equals(EntityKind.MATERIAL))
        {
            parameters.put(asParameter(KEY), new QueryParameterValue(entity.getCode(), true));
            parameters.put(asParameter(TYPE), new QueryParameterValue(entity.getEntityType()
                    .getCode(), true));
        } else
        {
            parameters.put(asParameter(KEY), new QueryParameterValue(entity.getPermId(), true));
        }
        return parameters;
    }

    /**
     * Adds prefix to given parameter name.
     */
    private static String asParameter(String parameterName)
    {
        return PARAMETER_PREFIX + parameterName;
    }

    /**
     * Translates {@link EntityKind} to {@link QueryType}.
     */
    private static QueryType translate(EntityKind entityKind)
    {
        switch (entityKind)
        {
            case DATA_SET:
                return QueryType.DATA_SET;
            case EXPERIMENT:
                return QueryType.EXPERIMENT;
            case MATERIAL:
                return QueryType.MATERIAL;
            case SAMPLE:
                return QueryType.SAMPLE;
        }
        return null;
    }

}