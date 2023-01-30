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
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryDatabase;

/**
 * {@link ModelData} for {@link QueryDatabase}.
 * 
 * @author Piotr Buczek
 */
public class QueryDatabaseModel extends SimplifiedBaseModelData
{
    private static final long serialVersionUID = 1L;

    public QueryDatabaseModel(final QueryDatabase database)
    {
        set(ModelDataPropertyNames.NAME, database.getLabel());
        set(ModelDataPropertyNames.DESCRIPTION, database.getKey());
        set(ModelDataPropertyNames.OBJECT, database);
        set(ModelDataPropertyNames.TOOLTIP, TooltipRenderer.renderAsTooltip(database.getLabel(),
                "key: " + database.getKey()));
    }

    public final static List<QueryDatabaseModel> convert(final List<QueryDatabase> databases)
    {
        final List<QueryDatabaseModel> result = new ArrayList<QueryDatabaseModel>();

        for (final QueryDatabase database : databases)
        {
            result.add(new QueryDatabaseModel(database));
        }

        return result;
    }
}
