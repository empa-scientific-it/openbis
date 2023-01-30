/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.SessionFactory;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.SequenceNames;

/**
 * <i>Data Access Object</i> implementation for sequence named {@link SequenceNames#CODE_SEQUENCE}.
 * 
 * @author Piotr Buczek
 */
public class CodeSequenceDAO extends AbstractDAO implements ICodeSequenceDAO
{
    private static final Map<EntityKind, String> entityKindSequenceMap =
            new HashMap<EntityKind, String>();

    protected CodeSequenceDAO(final SessionFactory sessionFactory)
    {
        super(sessionFactory);
        entityKindSequenceMap.put(EntityKind.EXPERIMENT, SequenceNames.EXPERIMENT_CODE_SEQUENCE);
        entityKindSequenceMap.put(EntityKind.SAMPLE, SequenceNames.SAMPLE_CODE_SEQUENCE);
        entityKindSequenceMap.put(EntityKind.DATA_SET, SequenceNames.CODE_SEQUENCE);
        entityKindSequenceMap.put(EntityKind.MATERIAL, SequenceNames.CODE_SEQUENCE);
    }

    @Override
    public long getNextCodeSequenceId()
    {
        return getNextSequenceId(SequenceNames.CODE_SEQUENCE);
    }

    @Override
    public long getNextCodeSequenceId(EntityKind entityKind)
    {
        return getNextSequenceId(entityKindSequenceMap.get(entityKind));
    }
}
