/*
 * Copyright 2021 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.business;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDeletionTable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author Franz-Josef Elmer
 *
 */
public class DeletionUtils
{

    public static UserFailureException createException(Session session, ICommonBusinessObjectFactory boFactory, 
            List<Long> deletionIds)
    {
        IDeletionTable deletionTable = boFactory.createDeletionTable(session);
        deletionTable.load(deletionIds, true);
        StringBuilder builder = new StringBuilder();
        for (Deletion deletion : deletionTable.getDeletions())
        {
            String entities = ch.systemsx.cisd.openbis.generic.shared.basic.DeletionUtils
                    .createDescriptionOfDeletedEntities(deletion);
            builder.append(String.format("\nDeletion Set %s: ("
                    + "deletion date: %2$tY-%2$tm-%2$td %2$tH:%2$tM:%2$tS, reason: %3$s, entities: %4$s)",
                    deletion.getId(), deletion.getRegistrationDate(), deletion.getReason(), entities));
        }
        UserFailureException userFailureException 
                = new UserFailureException("Permanent deletion not possible because the following "
                + "deletion sets have to be deleted first:" + builder);
        return userFailureException;
    }

}
