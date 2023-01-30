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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Vocabulary term and its usage statistics.
 * 
 * @author Tomasz Pylak
 */
public class VocabularyTermWithStats implements Serializable, Comparable<VocabularyTermWithStats>
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private VocabularyTermPE term;

    // how many times is this term used as a value for each entity kind
    private Map<EntityKind, Long> usageCounter;

    public VocabularyTermWithStats(VocabularyTermPE term)
    {
        this.term = term;
        this.usageCounter = new HashMap<EntityKind, Long>();
    }

    public VocabularyTermPE getTerm()
    {
        return term;
    }

    public void registerUsage(EntityKind entityKind, long used)
    {
        usageCounter.put(entityKind, used);
    }

    public long getUsageCounter(EntityKind entityKind)
    {
        Long usage = usageCounter.get(entityKind);
        if (usage == null)
        {
            return 0;
        } else
        {
            return usage.intValue();
        }
    }

    //
    // Comparable
    //

    @Override
    public int compareTo(VocabularyTermWithStats o)
    {
        return this.getTerm().compareTo(o.getTerm());
    }
}
