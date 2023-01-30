/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.tag;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author pkupczyk
 */
public class TagAuthorization
{

    private IOperationContext context;

    public TagAuthorization(IOperationContext context)
    {
        this.context = context;
    }

    public void checkAccess(MetaprojectPE tag)
    {
        if (false == canAccess(tag.getOwner().getUserId()))
        {
            throw new UnauthorizedObjectAccessException(new TagPermId(tag.getOwner().getUserId(), tag.getName()));
        }
    }

    public void checkAccess(MetaprojectIdentifier tagIdentifier)
    {
        if (false == canAccess(tagIdentifier.getMetaprojectOwnerId()))
        {
            throw new UnauthorizedObjectAccessException(new TagPermId(tagIdentifier.getMetaprojectOwnerId(), tagIdentifier.getMetaprojectName()));
        }
    }

    public boolean canAccess(MetaprojectPE tag)
    {
        return canAccess(tag.getOwner().getUserId());
    }

    private boolean canAccess(String owner)
    {
        return owner.equals(context.getSession().tryGetPerson().getUserId());
    }

}
