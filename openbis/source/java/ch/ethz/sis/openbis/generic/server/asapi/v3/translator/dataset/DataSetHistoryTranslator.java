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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.history.DataSetRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.ContentCopyHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.fetchoptions.HistoryEntryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.id.UnknownRelatedObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.experiment.IExperimentAuthorizationValidator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample.ISampleAuthorizationValidator;
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
public class DataSetHistoryTranslator extends HistoryTranslator implements IDataSetHistoryTranslator
{

    @Autowired
    private IExperimentAuthorizationValidator experimentValidator;

    @Autowired
    private ISampleAuthorizationValidator sampleValidator;

    @Autowired
    private IDataSetAuthorizationValidator dataSetValidator;

    @Override
    protected List<HistoryPropertyRecord> loadPropertyHistory(Collection<Long> entityIds)
    {
        DataSetQuery query = QueryTool.getManagedQuery(DataSetQuery.class);
        return query.getPropertiesHistory(new LongOpenHashSet(entityIds));
    }

    @Override
    protected List<? extends HistoryRelationshipRecord> loadRelationshipHistory(TranslationContext context, Collection<Long> entityIds)
    {
        DataSetQuery query = QueryTool.getManagedQuery(DataSetQuery.class);

        List<DataSetRelationshipRecord> records = query.getRelationshipsHistory(new LongOpenHashSet(entityIds));
        List<DataSetRelationshipRecord> validRecords = new ArrayList<DataSetRelationshipRecord>();

        Set<Long> experimentIds = new HashSet<Long>();
        Set<Long> sampleIds = new HashSet<Long>();
        Set<Long> dataSetIds = new HashSet<Long>();

        for (DataSetRelationshipRecord record : records)
        {
            if (record.experimentId != null)
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

        for (DataSetRelationshipRecord record : records)
        {
            boolean isValid = false;

            if (record.experimentId != null)
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

        DataSetRelationshipRecord dataSetRecord = (DataSetRelationshipRecord) record;

        if (dataSetRecord.experimentId != null)
        {
            entry.setRelationType(DataSetRelationType.EXPERIMENT);
            entry.setRelatedObjectId(new ExperimentPermId(dataSetRecord.relatedObjectId));
        } else if (dataSetRecord.sampleId != null)
        {
            entry.setRelationType(DataSetRelationType.SAMPLE);
            entry.setRelatedObjectId(new SamplePermId(dataSetRecord.relatedObjectId));
        } else if (dataSetRecord.dataSetId != null)
        {
            RelationType relationType = RelationType.valueOf(dataSetRecord.relationType);

            switch (relationType)
            {
                case PARENT:
                    entry.setRelationType(DataSetRelationType.CHILD);
                    break;
                case CHILD:
                    entry.setRelationType(DataSetRelationType.PARENT);
                    break;
                case CONTAINER:
                    entry.setRelationType(DataSetRelationType.COMPONENT);
                    break;
                case CONTAINED:
                case COMPONENT:
                    entry.setRelationType(DataSetRelationType.CONTAINER);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported relation type: " + relationType);
            }
            entry.setRelatedObjectId(new DataSetPermId(dataSetRecord.relatedObjectId));
        } else
        {
            entry.setRelatedObjectId(new UnknownRelatedObjectId(dataSetRecord.relatedObjectId, dataSetRecord.relationType));
        }

        return entry;
    }

    @Override
    protected List<? extends HistoryRecord> loadAbritraryHistory(TranslationContext context, Collection<Long> entityIds)
    {
        DataSetQuery query = QueryTool.getManagedQuery(DataSetQuery.class);
        return query.getContentCopyHistory(new LongOpenHashSet(entityIds));
    }

    @Override
    protected void createArbitraryEntries(Map<Long, List<HistoryEntry>> entriesMap, List<? extends HistoryRecord> records,
            Map<Long, Person> authorMap, HistoryEntryFetchOptions fetchOptions)
    {
        for (HistoryRecord record : records)
        {
            HistoryContentCopyRecord contentCopyRecord = (HistoryContentCopyRecord) record;
            List<HistoryEntry> entries = entriesMap.get(contentCopyRecord.dataSetId);

            if (entries == null)
            {
                entries = new LinkedList<HistoryEntry>();
                entriesMap.put(contentCopyRecord.dataSetId, entries);
            }

            entries.add(createContentCopyEntry(record, authorMap, fetchOptions));
        }
    }

    private HistoryEntry createContentCopyEntry(HistoryRecord record, Map<Long, Person> authorMap, HistoryEntryFetchOptions fetchOptions)
    {
        HistoryContentCopyRecord contentCopyRecord = (HistoryContentCopyRecord) record;
        ContentCopyHistoryEntry entry = new ContentCopyHistoryEntry();
        entry.setFetchOptions(new HistoryEntryFetchOptions());
        entry.setExternalCode(contentCopyRecord.externalCode);
        entry.setPath(contentCopyRecord.path);
        entry.setGitCommitHash(contentCopyRecord.gitCommitHash);
        entry.setGitRepositoryId(contentCopyRecord.gitRepositoryId);
        entry.setExternalDmsId(contentCopyRecord.externalDmsId);
        entry.setExternalDmsCode(contentCopyRecord.externalDmsCode);
        entry.setExternalDmsLabel(contentCopyRecord.externalDmsLabel);
        entry.setExternalDmsAddress(contentCopyRecord.externalDmsAddress);
        entry.setValidFrom(contentCopyRecord.validFrom);
        entry.setValidTo(contentCopyRecord.validTo);
        if (fetchOptions.hasAuthor())
        {
            entry.setAuthor(authorMap.get(record.authorId));
            entry.getFetchOptions().withAuthorUsing(fetchOptions.withAuthor());
        }
        return entry;
    }

}
