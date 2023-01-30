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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * A <i>static</i> class for translating {@link DeletionPE} into {@link Deletion}.
 * 
 * @author Christian Ribeaud
 */
public final class DeletionTranslator
{

    private DeletionTranslator()
    {
        // Can not be instantiated.
    }

    public final static List<Deletion> translate(final List<DeletionPE> deletions)
    {
        final List<Deletion> result = new ArrayList<Deletion>();
        for (final DeletionPE deletion : deletions)
        {
            result.add(translate(deletion));
        }
        return result;
    }

    public final static Deletion translate(final DeletionPE deletion)
    {
        if (deletion == null)
        {
            return null;
        }
        final Deletion newDeletion = new Deletion();
        // NOTE: we should always translate Id in this way
        // because getId() on HibernateProxy object always returns null
        newDeletion.setId(HibernateUtils.getId(deletion));
        newDeletion.setReason(deletion.getReason());
        newDeletion.setRegistrationDate(deletion.getRegistrationDate());
        newDeletion.setRegistrator(PersonTranslator.translate(deletion.getRegistrator()));
        return newDeletion;
    }
}
