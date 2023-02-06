/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.common.get.GetObjectsOperationResult")
public abstract class GetObjectsOperationResult<ID extends IObjectId, OBJECT> implements IOperationResult
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private List<ID> ids;

    @JsonProperty
    private List<OBJECT> objects;

    protected GetObjectsOperationResult()
    {
    }

    public GetObjectsOperationResult(Map<ID, OBJECT> objectMap)
    {
        if (objectMap != null)
        {
            ids = new ArrayList<ID>();
            objects = new ArrayList<OBJECT>();

            for (Map.Entry<ID, OBJECT> entry : objectMap.entrySet())
            {
                ids.add(entry.getKey());
                objects.add(entry.getValue());
            }
        }
    }

    @JsonIgnore
    public Map<ID, OBJECT> getObjectMap()
    {
        Map<ID, OBJECT> objectMap = new LinkedHashMap<ID, OBJECT>();

        if (ids != null && objects != null)
        {
            Iterator<ID> idIter = ids.iterator();
            Iterator<OBJECT> objectIter = objects.iterator();

            while (idIter.hasNext() && objectIter.hasNext())
            {
                objectMap.put(idIter.next(), objectIter.next());
            }
        }

        return objectMap;
    }

    @Override
    public String getMessage()
    {
        return toString();
    }

    @Override
    public String toString()
    {
        int idsSize = ids != null ? ids.size() : 0;
        int objectsSize = objects != null ? objects.size() : 0;
        int size = Math.min(idsSize, objectsSize);
        return getClass().getSimpleName() + " " + size + " object(s)";
    }

}
