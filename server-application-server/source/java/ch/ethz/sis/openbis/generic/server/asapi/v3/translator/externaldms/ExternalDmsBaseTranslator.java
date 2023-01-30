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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.externaldms;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.List;

import net.lemnik.eodsql.QueryTool;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectBaseTranslator;

/**
 * @author pkupczyk
 */
@Component
public class ExternalDmsBaseTranslator extends ObjectBaseTranslator<ExternalDmsBaseRecord> implements IExternalDmsBaseTranslator
{

    @Override
    protected List<ExternalDmsBaseRecord> loadRecords(LongOpenHashSet objectIds)
    {
        ExternalDmsQuery query = QueryTool.getManagedQuery(ExternalDmsQuery.class);
        return query.getExternalDmses(objectIds);
    }

}