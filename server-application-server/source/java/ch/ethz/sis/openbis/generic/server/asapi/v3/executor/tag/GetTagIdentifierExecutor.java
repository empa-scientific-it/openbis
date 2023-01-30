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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagCode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectIdentifier;

/**
 * @author pkupczyk
 */
@Component
public class GetTagIdentifierExecutor implements IGetTagIdentifierExecutor
{

    public GetTagIdentifierExecutor()
    {
    }

    @Override
    public MetaprojectIdentifier getIdentifier(IOperationContext context, ITagId tagId)
    {
        if (tagId instanceof TagCode)
        {
            String code = ((TagCode) tagId).getCode();
            String owner = context.getSession().tryGetPerson().getUserId();
            return new MetaprojectIdentifier(owner, code);
        }
        if (tagId instanceof TagPermId)
        {
            TagPermId tagPermId = (TagPermId) tagId;
            MetaprojectIdentifier tagIdentifier =
                    MetaprojectIdentifier.parse(tagPermId.getPermId());
            return tagIdentifier;
        }
        throw new NotImplementedException("Tag id [" + tagId + "] is of unknown type: "
                + tagId.getClass().getName());
    }

}
