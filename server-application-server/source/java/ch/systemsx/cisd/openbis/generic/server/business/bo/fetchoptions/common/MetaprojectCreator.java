/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.common;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author Pawel Glyzewski
 */
public class MetaprojectCreator
{
    public static Metaproject createMetaproject(MetaprojectRecord metaprojectRecord, PersonPE owner)
    {
        Metaproject metaproject = new Metaproject();

        metaproject.setId(metaprojectRecord.id);
        metaproject.setName(metaprojectRecord.name);
        metaproject.setDescription(metaprojectRecord.description);
        metaproject.setPrivate(metaprojectRecord.is_private);
        metaproject.setCreationDate(metaprojectRecord.creation_date);

        metaproject.setOwnerId(owner.getUserId());

        return metaproject;
    }
}
