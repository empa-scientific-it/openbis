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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * Translator for {@link MetaprojectPE} into {@link Metaproject} and other way around.
 * 
 * @author Pawel Glyzewski
 */
public class MetaprojectTranslator
{
    public static List<Metaproject> translate(Collection<MetaprojectPE> metaprojectPEs)
    {
        List<Metaproject> metaprojects = new ArrayList<Metaproject>(metaprojectPEs.size());

        for (MetaprojectPE metaprojectPE : metaprojectPEs)
        {
            metaprojects.add(translate(metaprojectPE));
        }

        return metaprojects;
    }

    public static Metaproject translate(MetaprojectPE metaprojectPE)
    {
        Metaproject metaproject = new Metaproject();

        metaproject.setId(metaprojectPE.getId());
        metaproject.setName(metaprojectPE.getName());
        metaproject.setOwnerId(metaprojectPE.getOwner().getUserId());
        metaproject.setDescription(metaprojectPE.getDescription());
        metaproject.setPrivate(metaprojectPE.isPrivate());
        metaproject.setCreationDate(metaprojectPE.getCreationDate());

        return metaproject;
    }

    public static MetaprojectPE translate(Metaproject metaproject, MetaprojectPE metaprojectPE)
    {
        metaprojectPE.setName(metaproject.getName().toUpperCase());
        metaprojectPE.setDescription(metaproject.getDescription());
        metaprojectPE.setPrivate(true);

        return metaprojectPE;
    }

    public static Map<Long, Set<Metaproject>> translateMetaprojectAssignments(
            Collection<MetaprojectAssignmentPE> assignments)
    {
        Map<Long, Metaproject> metaprojects = new HashMap<Long, Metaproject>();
        for (MetaprojectAssignmentPE assignment : assignments)
        {
            Long metaprojectId = assignment.getMetaproject().getId();
            if (false == metaprojects.containsKey(metaprojectId))
            {
                metaprojects.put(metaprojectId, translate(assignment.getMetaproject()));
            }
        }

        Map<Long, Set<Metaproject>> translatedAssignments = new HashMap<Long, Set<Metaproject>>();
        for (MetaprojectAssignmentPE assignment : assignments)
        {
            Long entityId = tryGetEntityId(assignment);
            Set<Metaproject> metaprojectsSet = translatedAssignments.get(entityId);
            if (metaprojectsSet == null)
            {
                metaprojectsSet = new HashSet<Metaproject>();
                translatedAssignments.put(entityId, metaprojectsSet);
            }
            metaprojectsSet.add(metaprojects.get(assignment.getMetaproject().getId()));
        }

        return translatedAssignments;
    }

    private static final Long tryGetEntityId(MetaprojectAssignmentPE assignment)
    {
        if (assignment.getExperiment() != null)
        {
            return assignment.getExperiment().getId();
        } else if (assignment.getSample() != null)
        {
            return assignment.getSample().getId();
        } else if (assignment.getDataSet() != null)
        {
            return assignment.getDataSet().getId();
        } else if (assignment.getMaterial() != null)
        {
            return assignment.getMaterial().getId();
        }

        return null;
    }
}
