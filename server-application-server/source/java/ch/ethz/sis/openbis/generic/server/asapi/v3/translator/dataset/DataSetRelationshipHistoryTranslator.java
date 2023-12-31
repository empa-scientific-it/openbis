/*
 * Copyright ETH 2015 - 2023 Zürich, Scientific IT Services
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.history.DataSetRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.fetchoptions.HistoryEntryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.id.UnknownRelatedObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.experiment.IExperimentAuthorizationValidator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryRelationshipRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.RelationshipHistoryTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample.ISampleAuthorizationValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationType;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;

/**
 * @author pkupczyk
 */
@Component
public abstract class DataSetRelationshipHistoryTranslator extends RelationshipHistoryTranslator
{

    @Autowired
    private IExperimentAuthorizationValidator experimentValidator;

    @Autowired
    private ISampleAuthorizationValidator sampleValidator;

    @Autowired
    private IDataSetAuthorizationValidator dataSetValidator;

    protected abstract String getRelationshipEntityKind();

    protected abstract RelationType getRelationshipRelationType();

    @Override
    protected List<? extends HistoryRelationshipRecord> loadRelationshipHistory(TranslationContext context, Collection<Long> entityIds)
    {
        DataSetQuery query = QueryTool.getManagedQuery(DataSetQuery.class);

        List<DataSetRelationshipRecord> records = null;

        if (getRelationshipEntityKind() != null)
        {
            records =
                    query.getRelationshipsHistory(new LongOpenHashSet(entityIds), getRelationshipEntityKind(), getRelationshipRelationType().name());
        } else
        {
            records = query.getUnknownRelationshipsHistory(new LongOpenHashSet(entityIds));
        }

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

        if (isExperiment(dataSetRecord))
        {
            entry.setRelationType(DataSetRelationType.EXPERIMENT);
            entry.setRelatedObjectId(new ExperimentPermId(dataSetRecord.relatedObjectId));
        } else if (isSample(dataSetRecord))
        {
            entry.setRelationType(DataSetRelationType.SAMPLE);
            entry.setRelatedObjectId(new SamplePermId(dataSetRecord.relatedObjectId));
        } else if (isDataSet(dataSetRecord))
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

}
