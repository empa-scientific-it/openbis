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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.fetchoptions.HistoryEntryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.id.UnknownRelatedObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.history.SampleRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.IDataSetAuthorizationValidator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.experiment.IExperimentAuthorizationValidator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryPropertyRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryRelationshipRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.project.IProjectAuthorizationValidator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.space.ISpaceAuthorizationValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationType;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author pkupczyk
 */
@Component
public class SampleHistoryTranslator extends HistoryTranslator implements ISampleHistoryTranslator
{

    @Autowired
    private ISpaceAuthorizationValidator spaceValidator;

    @Autowired
    private IProjectAuthorizationValidator projectValidator;

    @Autowired
    private IExperimentAuthorizationValidator experimentValidator;

    @Autowired
    private ISampleAuthorizationValidator sampleValidator;

    @Autowired
    private IDataSetAuthorizationValidator dataSetValidator;

    @Override
    protected List<HistoryPropertyRecord> loadPropertyHistory(Collection<Long> entityIds)
    {
        SampleQuery query = QueryTool.getManagedQuery(SampleQuery.class);
        return query.getPropertiesHistory(new LongOpenHashSet(entityIds));
    }

    @Override
    protected List<? extends HistoryRelationshipRecord> loadRelationshipHistory(TranslationContext context, Collection<Long> entityIds)
    {
        SampleQuery query = QueryTool.getManagedQuery(SampleQuery.class);

        List<SampleRelationshipRecord> records = query.getRelationshipsHistory(new LongOpenHashSet(entityIds));
        List<SampleRelationshipRecord> validRecords = new ArrayList<SampleRelationshipRecord>();

        Set<Long> spaceIds = new HashSet<Long>();
        Set<Long> projectIds = new HashSet<Long>();
        Set<Long> experimentIds = new HashSet<Long>();
        Set<Long> sampleIds = new HashSet<Long>();
        Set<Long> dataSetIds = new HashSet<Long>();

        for (SampleRelationshipRecord record : records)
        {
            if (record.spaceId != null)
            {
                spaceIds.add(record.spaceId);
            } else if (record.projectId != null)
            {
                projectIds.add(record.projectId);
            } else if (record.experimentId != null)
            {
                experimentIds.add(record.experimentId);
            } else if (record.sampleId != null)
            {
                sampleIds.add(record.sampleId);
            } else if (record.dataSetId != null)
            {
                dataSetIds.add(record.dataSetId);
            }
        }

        if (false == spaceIds.isEmpty())
        {
            spaceIds = spaceValidator.validate(context.getSession().tryGetPerson(), spaceIds);
        }
        if (false == projectIds.isEmpty())
        {
            projectIds = projectValidator.validate(context.getSession().tryGetPerson(), projectIds);
        }
        if (false == experimentIds.isEmpty())
        {
            experimentIds = experimentValidator.validate(context.getSession().tryGetPerson(), experimentIds);
        }
        if (false == sampleIds.isEmpty())
        {
            sampleIds = sampleValidator.validate(context.getSession().tryGetPerson(), sampleIds);
        }
        if (false == dataSetIds.isEmpty())
        {
            dataSetIds = dataSetValidator.validate(context.getSession().tryGetPerson(), dataSetIds);
        }

        final boolean isSystemUser = context.getSession().tryGetPerson() != null && context.getSession().tryGetPerson().isSystemUser();

        for (SampleRelationshipRecord record : records)
        {
            boolean isValid = false;

            if (record.spaceId != null)
            {
                isValid = spaceIds.contains(record.spaceId);
            } else if (record.projectId != null)
            {
                isValid = projectIds.contains(record.projectId);
            } else if (record.experimentId != null)
            {
                isValid = experimentIds.contains(record.experimentId);
            } else if (record.sampleId != null)
            {
                isValid = sampleIds.contains(record.sampleId);
            } else if (record.dataSetId != null)
            {
                isValid = dataSetIds.contains(record.dataSetId);
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

        SampleRelationshipRecord sampleRecord = (SampleRelationshipRecord) record;

        if (sampleRecord.spaceId != null)
        {
            entry.setRelationType(SampleRelationType.SPACE);
            entry.setRelatedObjectId(new SpacePermId(sampleRecord.relatedObjectId));
        } else if (sampleRecord.projectId != null)
        {
            entry.setRelationType(SampleRelationType.PROJECT);
            entry.setRelatedObjectId(new ProjectPermId(sampleRecord.relatedObjectId));
        } else if (sampleRecord.experimentId != null)
        {
            entry.setRelationType(SampleRelationType.EXPERIMENT);
            entry.setRelatedObjectId(new ExperimentPermId(sampleRecord.relatedObjectId));
        } else if (sampleRecord.sampleId != null)
        {
            RelationType relationType = RelationType.valueOf(sampleRecord.relationType);

            switch (relationType)
            {
                case PARENT:
                    entry.setRelationType(SampleRelationType.CHILD);
                    break;
                case CHILD:
                    entry.setRelationType(SampleRelationType.PARENT);
                    break;
                case CONTAINER:
                    entry.setRelationType(SampleRelationType.COMPONENT);
                    break;
                case CONTAINED:
                case COMPONENT:
                    entry.setRelationType(SampleRelationType.CONTAINER);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported relation type: " + relationType);
            }
            entry.setRelatedObjectId(new SamplePermId(sampleRecord.relatedObjectId));
        } else if (sampleRecord.dataSetId != null)
        {
            entry.setRelationType(SampleRelationType.DATA_SET);
            entry.setRelatedObjectId(new DataSetPermId(sampleRecord.relatedObjectId));
        } else
        {
            entry.setRelatedObjectId(new UnknownRelatedObjectId(sampleRecord.relatedObjectId, sampleRecord.relationType));
        }

        return entry;
    }

}
