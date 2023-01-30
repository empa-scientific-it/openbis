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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Vocabulary term and its usage statistics.
 * 
 * @author Tomasz Pylak
 */
public class VocabularyTermWithStats implements Serializable
{
    private static final long serialVersionUID = 1L;

    private VocabularyTerm term;

    // how many times is this term used as a value for each entity kind
    private Map<EntityKind, Long> usageCounter;

    // GWT only
    public VocabularyTermWithStats()
    {
    }

    public VocabularyTermWithStats(VocabularyTerm term)
    {
        this.term = term;
        this.usageCounter = new HashMap<EntityKind, Long>();
    }

    public VocabularyTerm getTerm()
    {
        return term;
    }

    public void registerUsage(EntityKind entityKind, long used)
    {
        usageCounter.put(entityKind, used);
    }

    public long getUsageCounter(EntityKind entityKind)
    {
        return usageCounter.get(entityKind);
    }

    /** how many times was this term used in general for all kinds of entities */
    public int getTotalUsageCounter()
    {
        int total = 0;
        for (EntityKind entityKind : EntityKind.values())
        {
            total += getUsageCounter(entityKind);
        }
        return total;
    }
}
