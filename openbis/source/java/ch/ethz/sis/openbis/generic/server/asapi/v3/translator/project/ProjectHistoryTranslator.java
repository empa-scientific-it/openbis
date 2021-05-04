/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.project;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.fetchoptions.HistoryEntryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.id.UnknownRelatedObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.history.ProjectRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.experiment.IExperimentAuthorizationValidator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryPropertyRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryRelationshipRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.space.ISpaceAuthorizationValidator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author pkupczyk
 */
@Component
public class ProjectHistoryTranslator extends HistoryTranslator implements IProjectHistoryTranslator
{

    @Autowired
    private ISpaceAuthorizationValidator spaceValidator;

    @Autowired
    private IExperimentAuthorizationValidator experimentValidator;

    @Override
    protected List<HistoryPropertyRecord> loadPropertyHistory(Collection<Long> entityIds)
    {
        return null;
    }

    @Override
    protected List<? extends HistoryRelationshipRecord> loadRelationshipHistory(TranslationContext context, Collection<Long> entityIds)
    {
        ProjectQuery query = QueryTool.getManagedQuery(ProjectQuery.class);

        List<ProjectRelationshipRecord> records = query.getRelationshipsHistory(new LongOpenHashSet(entityIds));
        List<ProjectRelationshipRecord> validRecords = new ArrayList<ProjectRelationshipRecord>();

        Set<Long> spaceIds = new HashSet<Long>();
        Set<Long> experimentIds = new HashSet<Long>();

        for (ProjectRelationshipRecord record : records)
        {
            if (record.spaceId != null)
            {
                spaceIds.add(record.spaceId);
            } else if (record.experimentId != null)
            {
                experimentIds.add(record.experimentId);
            }
        }

        if (false == spaceIds.isEmpty())
        {
            spaceIds = spaceValidator.validate(context.getSession().tryGetPerson(), spaceIds);
        }
        if (false == experimentIds.isEmpty())
        {
            experimentIds = experimentValidator.validate(context.getSession().tryGetPerson(), experimentIds);
        }

        final boolean isSystemUser = context.getSession().tryGetPerson() != null && context.getSession().tryGetPerson().isSystemUser();

        for (ProjectRelationshipRecord record : records)
        {
            boolean isValid = false;

            if (record.spaceId != null)
            {
                isValid = spaceIds.contains(record.spaceId);
            } else if (record.experimentId != null)
            {
                isValid = experimentIds.contains(record.experimentId);
            } else
            {
                isValid = isSystemUser;
            }

            if (isValid)
            {
                validRecords.add(record);
            }
        }

        return validRecords;
    }

    @Override
    protected RelationHistoryEntry createRelationshipEntry(HistoryRelationshipRecord record, Map<Long, Person> authorMap,
            HistoryEntryFetchOptions fetchOptions)
    {
        RelationHistoryEntry entry = super.createRelationshipEntry(record, authorMap, fetchOptions);

        ProjectRelationshipRecord projectRecord = (ProjectRelationshipRecord) record;

        if (projectRecord.spaceId != null)
        {
            entry.setRelationType(ProjectRelationType.SPACE);
            entry.setRelatedObjectId(new SpacePermId(projectRecord.relatedObjectId));
        } else if (projectRecord.experimentId != null)
        {
            entry.setRelationType(ProjectRelationType.EXPERIMENT);
            entry.setRelatedObjectId(new ExperimentPermId(projectRecord.relatedObjectId));
        } else
        {
            entry.setRelatedObjectId(new UnknownRelatedObjectId(projectRecord.relatedObjectId, projectRecord.relationType));
        }

        return entry;
    }

}
