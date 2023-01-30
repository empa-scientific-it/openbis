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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author Izabela Adamczyk
 */
public class VocabularyTermTranslator
{

    public static VocabularyTerm translate(VocabularyTermPE vt)
    {
        if (vt == null)
        {
            return null;
        }
        VocabularyTerm result = new VocabularyTerm();
        result.setId(HibernateUtils.getId(vt));
        result.setCode(vt.getCode());
        result.setLabel(vt.getLabel());
        result.setDescription(vt.getDescription());
        result.setOrdinal(vt.getOrdinal());
        result.setUrl(vt.getUrl());
        result.setOfficial(vt.isOfficial());
        result.setRegistrationDate(vt.getRegistrationDate());
        result.setRegistrator(PersonTranslator.translate(vt.getRegistrator()));
        result.setModificationDate(vt.getModificationDate());

        return result;
    }

    public static List<VocabularyTerm> translateTermsList(Collection<VocabularyTermPE> terms)
    {
        final List<VocabularyTerm> result = new ArrayList<VocabularyTerm>();
        for (VocabularyTermPE term : terms)
        {
            result.add(translate(term));
        }
        Collections.sort(result, new Comparator<VocabularyTerm>()
            {
                @Override
                public int compare(VocabularyTerm arg0, VocabularyTerm arg1)
                {
                    return arg0.getOrdinal().compareTo(arg1.getOrdinal());
                }
            });

        return result;
    }

    public static Set<VocabularyTerm> translateTerms(Set<VocabularyTermPE> terms)
    {
        final Set<VocabularyTerm> result = new HashSet<VocabularyTerm>();
        for (VocabularyTermPE term : terms)
        {
            result.add(translate(term));
        }
        return result;
    }

    public static List<VocabularyTermWithStats> translate(
            Collection<ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats> terms)
    {
        List<VocabularyTermWithStats> result = new ArrayList<VocabularyTermWithStats>();
        for (ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats term : terms)
        {
            result.add(translate(term));
        }
        return result;
    }

    private static VocabularyTermWithStats translate(
            ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats term)
    {
        VocabularyTermWithStats result = new VocabularyTermWithStats(translate(term.getTerm()));
        for (EntityKind entityKind : EntityKind.values())
        {
            result.registerUsage(entityKind, term.getUsageCounter(translate(entityKind)));
        }
        return result;
    }

    private static ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind translate(
            EntityKind entityKind)
    {
        ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind origEntityKind =
                ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind
                        .valueOf(entityKind.name());
        return origEntityKind;
    }

}
