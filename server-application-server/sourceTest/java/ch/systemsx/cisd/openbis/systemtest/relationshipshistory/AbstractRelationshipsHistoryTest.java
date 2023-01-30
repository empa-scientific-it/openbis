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

package ch.systemsx.cisd.openbis.systemtest.relationshipshistory;

import java.util.List;

import ch.systemsx.cisd.openbis.systemtest.SystemTestCase;

/**
 * @author Pawel Glyzewski
 */
public class AbstractRelationshipsHistoryTest extends SystemTestCase
{
    protected List<SampleRelationshipsHistory> getSampleRelationshipsHistory(long sampleId)
    {
        List<SampleRelationshipsHistory> list =
                jdbcTemplate
                        .query("select * from sample_relationships_history where main_samp_id = ? order by id asc",
                                new SampleRelationshipsHistoryMapper(), sampleId);
        return list;
    }

    protected List<ExperimentRelationshipsHistory> getExperimentRelationshipsHistory(long expeId)
    {
        List<ExperimentRelationshipsHistory> list =
                jdbcTemplate
                        .query("select * from experiment_relationships_history where main_expe_id = ? order by id asc",
                                new ExperimentRelationshipsHistoryMapper(), expeId);
        return list;
    }

    protected List<DataSetRelationshipsHistory> getDataSetRelationshipsHistory(long dataId)
    {
        List<DataSetRelationshipsHistory> list =
                jdbcTemplate
                        .query("select * from data_set_relationships_history where main_data_id = ? order by id asc",
                                new DataSetRelationshipsHistoryMapper(), dataId);
        return list;
    }
}
