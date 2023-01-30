/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property;

import java.util.List;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectBaseTranslator;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;

/**
 * @author pkupczyk
 */
@Component
public class PropertyTypeBaseTranslator extends ObjectBaseTranslator<PropertyTypeRecord> implements IPropertyTypeBaseTranslator
{

    @Override
    protected List<PropertyTypeRecord> loadRecords(LongOpenHashSet objectIds)
    {
        PropertyTypeQuery query = QueryTool.getManagedQuery(PropertyTypeQuery.class);
        return query.getPropertyTypes(objectIds);
    }
}
