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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.experiment;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.history.ExperimentRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.fetchoptions.HistoryEntryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.id.UnknownRelatedObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.IDataSetAuthorizationValidator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryPropertyRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryRelationshipRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.project.IProjectAuthorizationValidator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample.ISampleAuthorizationValidator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author pkupczyk
 */
@Component
public class ExperimentHistoryTranslator extends HistoryTranslator implements IExperimentHistoryTranslator
{

    @Autowired
    private IProjectAuthorizationValidator projectValidator;

    @Autowired
    private ISampleAuthorizationValidator sampleValidator;

    @Autowired
    private IDataSetAuthorizationValidator dataSetValidator;

    @Override
    protected List<HistoryPropertyRecord> loadPropertyHistory(Collection<Long> entityIds)
    {
        ExperimentQuery query = QueryTool.getManagedQuery(ExperimentQuery.class);
        return query.getPropertiesHistory(new LongOpenHashSet(entityIds));
    }

    @Override
    protected List<? extends HistoryRelationshipRecord> loadRelationshipHistory(TranslationContext context, Collection<Long> entityIds)
    {
        ExperimentQuery query = QueryTool.getManagedQuery(ExperimentQuery.class);

        List<ExperimentRelationshipRecord> records = query.getRelationshipsHistory(new LongOpenHashSet(entityIds));
        List<ExperimentRelationshipRecord> validRecords = new ArrayList<ExperimentRelationshipRecord>();

        Set<Long> projectIds = new HashSet<Long>();
        Set<Long> sampleIds = new HashSet<Long>();
        Set<Long> dataSetIds = new HashSet<Long>();

        for (ExperimentRelationshipRecord record : records)
        {
            if (record.projectId != null)
            {
                projectIds.add(record.projectId);
            } else if (record.sampleId != null)
            {
                sampleIds.add(record.sampleId);
            } else if (record.dataSetId != null)
            {
                dataSetIds.add(record.dataSetId);
            }
        }

        if (false == projectIds.isEmpty())
        {
            projectIds = projectValidator.validate(context.getSession().tryGetPerson(), projectIds);
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

        for (ExperimentRelationshipRecord record : records)
        {
            boolean isValid = false;

            if (record.projectId != null)
            {
                isValid = projectIds.contains(record.projectId);
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

        ExperimentRelationshipRecord experimentRecord = (ExperimentRelationshipRecord) record;

        if (experimentRecord.projectId != null)
        {
            entry.setRelationType(ExperimentRelationType.PROJECT);
            entry.setRelatedObjectId(new ProjectPermId(experimentRecord.relatedObjectId));
        } else if (experimentRecord.sampleId != null)
        {
            entry.setRelationType(ExperimentRelationType.SAMPLE);
            entry.setRelatedObjectId(new SamplePermId(experimentRecord.relatedObjectId));
        } else if (experimentRecord.dataSetId != null)
        {
            entry.setRelationType(ExperimentRelationType.DATA_SET);
            entry.setRelatedObjectId(new DataSetPermId(experimentRecord.relatedObjectId));
        } else
        {
            entry.setRelatedObjectId(new UnknownRelatedObjectId(experimentRecord.relatedObjectId, experimentRecord.relationType));
        }

        return entry;
    }

}
