/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.asapi.v3.exceptions;

import java.util.Iterator;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author pkupczyk
 */
public class UnauthorizedObjectAccessException extends UserFailureException
{

    private static final long serialVersionUID = 1L;

    private IObjectId objectId;

    private List<? extends IObjectId> objectIds;

    public UnauthorizedObjectAccessException(IObjectId id)
    {
        super("Access denied to object with " + id.getClass().getSimpleName() + " = [" + id + "].");
        this.objectId = id;
    }

    public UnauthorizedObjectAccessException(List<? extends IObjectId> ids)
    {
        super("Access denied to at least one of the " + ids.size() + " = [" + abbreviate(ids, 100) + "].");
        this.objectIds = ids;
    }

    private static String abbreviate(List<? extends IObjectId> ids, int maxSize)
    {
        StringBuilder builder = new StringBuilder();
        Iterator<? extends IObjectId> iterator = ids.iterator();
        for (int i = 0; iterator.hasNext() && i < maxSize; i++)
        {
            if (i > 0)
            {
                builder.append(", ");
            }
            builder.append(iterator.next());
        }
        int size = ids.size();
        if (maxSize < size)
        {
            builder.append(", ... (").append(size - maxSize).append(" left)");
        }
        return builder.toString();
    }

    public IObjectId getObjectId()
    {
        return objectId;
    }

    public List<? extends IObjectId> getObjectIds()
    {
        return objectIds;
    }

}