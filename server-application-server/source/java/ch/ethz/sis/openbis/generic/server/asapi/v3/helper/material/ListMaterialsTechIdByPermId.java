/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.material;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListTechIdById;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.TechIdStringIdentifierRecord;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;

import net.lemnik.eodsql.QueryTool;

/**
 * @author Franz-Josef Elmer
 */
public class ListMaterialsTechIdByPermId extends AbstractListTechIdById<MaterialPermId>
{

    @Override
    public Class<MaterialPermId> getIdClass()
    {
        return MaterialPermId.class;
    }

    @Override
    protected Map<Long, MaterialPermId> createIdsByTechIdsMap(IOperationContext context, List<MaterialPermId> ids)
    {
        Map<String, Map<String, MaterialPermId>> groupedIdentifiers = new HashMap<>();
        for (MaterialPermId permId : ids)
        {
            String code = CodeConverter.tryToDatabase(permId.getCode());
            String typeCode = CodeConverter.tryToDatabase(permId.getTypeCode());
            Map<String, MaterialPermId> identifiersByCode = groupedIdentifiers.get(typeCode);
            if (identifiersByCode == null)
            {
                identifiersByCode = new HashMap<>();
                groupedIdentifiers.put(typeCode, identifiersByCode);
            }
            identifiersByCode.put(code, permId);
        }
        Map<Long, MaterialPermId> result = new HashMap<>();
        MaterialQuery query = QueryTool.getManagedQuery(MaterialQuery.class);
        Set<Entry<String, Map<String, MaterialPermId>>> entrySet = groupedIdentifiers.entrySet();
        for (Entry<String, Map<String, MaterialPermId>> entry : entrySet)
        {
            String typeCode = entry.getKey();
            Map<String, MaterialPermId> identifiersByCode = entry.getValue();
            Set<String> codes = identifiersByCode.keySet();
            String[] codesAsArray = codes.toArray(new String[codes.size()]);
            for (TechIdStringIdentifierRecord record : query.listMaterialTechIdsByCodes(typeCode, codesAsArray))
            {
                result.put(record.id, identifiersByCode.get(record.identifier));
            }
        }
        return result;
    }

}
