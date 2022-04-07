/*
 * Copyright 2022 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.update;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import net.lemnik.eodsql.QueryTool;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class AssertionOfNoDeletedEntityExecutor implements IAssertionOfNoDeletedEntityExecutor
{
    private IDeletedEntityQuery query;

    public AssertionOfNoDeletedEntityExecutor()
    {
        query = QueryTool.getManagedQuery(IDeletedEntityQuery.class);

    }

    @Override
    public void assertSpaceHasNoDeletedSamples(String spaceCode)
    {
        List<DeletedEntityRecord> entities = query.listDeletedSamplesOfSpace(spaceCode);
        assertEntityHasNoDeletedRelatedEntities("space", spaceCode, "objects", entities);
    }

    @Override
    public void assertProjectHasNoDeletedExperiments(String projectPermId)
    {
        List<DeletedEntityRecord> entities = query.listDeletedExperimentsOfProject(projectPermId);
        assertEntityHasNoDeletedRelatedEntities("project", projectPermId, "experiments", entities);
    }

    @Override
    public void assertProjectHasNoDeletedSamples(String projectPermId)
    {
        List<DeletedEntityRecord> entities = query.listDeletedSamplesOfProject(projectPermId);
        assertEntityHasNoDeletedRelatedEntities("project", projectPermId, "objects", entities);
    }

    @Override
    public void assertExperimentHasNoDeletedSamples(String experimentPermId)
    {
        List<DeletedEntityRecord> entities = query.listDeletedSamplesOfExperiment(experimentPermId);
        assertEntityHasNoDeletedRelatedEntities("experiment", experimentPermId, "objects", entities);
    }

    @Override
    public void assertExperimentHasNoDeletedDataSets(String experimentPermId)
    {
        List<DeletedEntityRecord> entities = query.listDeletedDataSetsOfExperiment(experimentPermId);
        assertEntityHasNoDeletedRelatedEntities("experiment", experimentPermId, "data sets", entities);
    }

    @Override
    public void assertDataSetHasNoDeletedChildren(IDataSetId dataSetId)
    {
        String permId = ((DataSetPermId) dataSetId).getPermId();
        List<DeletedEntityRecord> entities = query.listDeletedChildrenOfDataSet(permId);
        assertEntityHasNoDeletedRelatedEntities("data set", permId, "children", entities);
    }

    @Override
    public void assertDataSetHasNoDeletedParents(IDataSetId dataSetId)
    {
        String permId = ((DataSetPermId) dataSetId).getPermId();
        List<DeletedEntityRecord> entities = query.listDeletedParentsOfDataSet(permId);
        assertEntityHasNoDeletedRelatedEntities("data set", permId, "parents", entities);
    }

    @Override
    public void assertDataSetHasNoDeletedComponents(IDataSetId dataSetId)
    {
        String permId = ((DataSetPermId) dataSetId).getPermId();
        List<DeletedEntityRecord> entities = query.listDeletedComponentsOfDataSet(permId);
        assertEntityHasNoDeletedRelatedEntities("data set", permId, "components", entities);
    }

    @Override
    public void assertSampleHasNoDeletedChildren(String samplePermId)
    {
        List<DeletedEntityRecord> entities = query.listDeletedChildrenOfSample(samplePermId);
        assertEntityHasNoDeletedRelatedEntities("object", samplePermId, "children", entities);
    }

    @Override
    public void assertSampleHasNoDeletedParents(String samplePermId)
    {
        List<DeletedEntityRecord> entities = query.listDeletedParentsOfSample(samplePermId);
        assertEntityHasNoDeletedRelatedEntities("object", samplePermId, "parents", entities);
    }

    @Override
    public void assertSampleHasNoDeletedComponents(String samplePermId)
    {
        List<DeletedEntityRecord> entities = query.listDeletedComponentsOfSample(samplePermId);
        assertEntityHasNoDeletedRelatedEntities("object", samplePermId, "components", entities);
    }

    @Override
    public void assertSampleHasNoDeletedDataSets(String samplePermId)
    {
        List<DeletedEntityRecord> entities = query.listDeletedDataSetsOfSample(samplePermId);
        assertEntityHasNoDeletedRelatedEntities("object", samplePermId, "data sets", entities);
    }

    private void assertEntityHasNoDeletedRelatedEntities(String entityKind, String identifier,
            String entityKindOfDeleted, List<DeletedEntityRecord> entities)
    {
        Map<Long, List<DeletedEntityRecord>> entitiesByDeletion = new TreeMap<>();
        int count = 0;
        for (DeletedEntityRecord record : entities)
        {
            List<DeletedEntityRecord> records = entitiesByDeletion.get(record.id);
            if (records == null)
            {
                records = new ArrayList<DeletedEntityRecord>();
                entitiesByDeletion.put(record.id, records);
            }
            records.add(record);
            count++;
        }
        if (count > 0)
        {
            SimpleDateFormat format = new SimpleDateFormat(BasicConstant.DATE_HOURS_MINUTES_SECONDS_PATTERN);
            String msg = "Can not freeze " + entityKind + " " + identifier + " because it has " 
                    + count + " " + entityKindOfDeleted + " in the trashcan ("
                    + entitiesByDeletion.size() + " deletion sets):\n";
            Collection<List<DeletedEntityRecord>> values = entitiesByDeletion.values();
            for (List<DeletedEntityRecord> records : values)
            {
                Date deletionTimestamp = records.get(0).deletionTimestamp;
                String reason = records.get(0).reason;
                msg += records.size() + " " + entityKindOfDeleted + " (Deletion timestamp: "
                        + format.format(deletionTimestamp) + ", reason: " + reason + ")\n";
            }
            msg += "These deletion sets must first be permanently deleted before " + entityKind 
                    + " " + identifier + " can be frozen.\n";
            throw new UserFailureException(msg);
        }
    }

}
