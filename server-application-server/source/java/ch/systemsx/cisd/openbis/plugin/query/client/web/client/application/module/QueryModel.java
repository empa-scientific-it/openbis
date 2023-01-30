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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SimplifiedBaseModelData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.renderer.TooltipRenderer;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;

/**
 * {@link ModelData} for {@link QueryExpression}.
 * 
 * @author Piotr Buczek
 */
public class QueryModel extends SimplifiedBaseModelData
{
    private static final long serialVersionUID = 1L;

    public QueryModel(final QueryExpression query)
    {
        set(ModelDataPropertyNames.NAME, query.getName());
        set(ModelDataPropertyNames.DESCRIPTION, query.getDescription());
        set(ModelDataPropertyNames.OBJECT, query);
        set(ModelDataPropertyNames.TOOLTIP, TooltipRenderer.renderAsTooltip(query.getName(), query
                .getDescription()));
    }

    public final static List<QueryModel> convert(final List<QueryExpression> queries)
    {
        final List<QueryModel> result = new ArrayList<QueryModel>();

        for (final QueryExpression query : queries)
        {
            result.add(new QueryModel(query));
        }

        return result;
    }
}
