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

import java.util.List;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectQuery;
import net.lemnik.eodsql.Select;

public interface IDeletedEntityQuery extends ObjectQuery
{
    public static final String SELECT =
            "select e.perm_id as permId, d.id, d.registration_timestamp as deletionTimestamp, d.reason as reason ";

    public static final String SELECT_DS =
            "select e.code as permId, d.id, d.registration_timestamp as deletionTimestamp, d.reason as reason ";

    @Select(sql = SELECT + "from samples_all e join spaces s on e.space_id=s.id join deletions d on e.del_id=d.id "
            + "where s.code = ?{1}", fetchSize = FETCH_SIZE)
    public List<DeletedEntityRecord> listDeletedSamplesOfSpace(String spaceCode);

    @Select(sql = SELECT + "from samples_all e join projects p on e.proj_id=p.id join deletions d on e.del_id=d.id "
            + "where p.perm_id = ?{1}", fetchSize = FETCH_SIZE)
    public List<DeletedEntityRecord> listDeletedSamplesOfProject(String projectPermId);

    @Select(sql = SELECT + "from experiments_all e join projects p on e.proj_id=p.id join deletions d on e.del_id=d.id "
            + "where p.perm_id = ?{1}", fetchSize = FETCH_SIZE)
    public List<DeletedEntityRecord> listDeletedExperimentsOfProject(String projectPermId);

    @Select(sql = SELECT + "from samples_all e join experiments ex on e.expe_id=ex.id join deletions d on e.del_id=d.id "
            + "where ex.perm_id = ?{1}", fetchSize = FETCH_SIZE)
    public List<DeletedEntityRecord> listDeletedSamplesOfExperiment(String experimentPermId);

    @Select(sql = SELECT_DS + "from data_all e join experiments ex on e.expe_id=ex.id join deletions d on e.del_id=d.id "
            + "where ex.perm_id = ?{1}", fetchSize = FETCH_SIZE)
    public List<DeletedEntityRecord> listDeletedDataSetsOfExperiment(String experimentPermId);

    @Select(sql = SELECT_DS + "from data_all e join data_set_relationships_all r on r.data_id_child=e.id "
            + "join data_all p on r.data_id_parent=p.id join relationship_types t on r.relationship_id=t.id "
            + "join deletions d on e.del_id=d.id "
            + "where t.code = 'PARENT_CHILD' and p.code = ?{1}", fetchSize = FETCH_SIZE)
    public List<DeletedEntityRecord> listDeletedChildrenOfDataSet(String dataSetCode);

    @Select(sql = SELECT_DS + "from data_all e join data_set_relationships_all r on r.data_id_parent=e.id "
            + "join data_all c on r.data_id_child=c.id join relationship_types t on r.relationship_id=t.id "
            + "join deletions d on e.del_id=d.id "
            + "where t.code = 'PARENT_CHILD' and c.code = ?{1}", fetchSize = FETCH_SIZE)
    public List<DeletedEntityRecord> listDeletedParentsOfDataSet(String dataSetCode);

    @Select(sql = SELECT_DS + "from data_all e join data_set_relationships_all r on r.data_id_child=e.id "
            + "join data_all p on r.data_id_parent=p.id join relationship_types t on r.relationship_id=t.id "
            + "join deletions d on e.del_id=d.id "
            + "where t.code = 'CONTAINER_COMPONENT' and p.code = ?{1}", fetchSize = FETCH_SIZE)
    public List<DeletedEntityRecord> listDeletedComponentsOfDataSet(String permId);

    @Select(sql = SELECT + "from samples_all e join sample_relationships_all r on r.sample_id_child=e.id "
            + "join samples_all p on r.sample_id_parent=p.id join relationship_types t on r.relationship_id=t.id "
            + "join deletions d on e.del_id=d.id "
            + "where t.code = 'PARENT_CHILD' and p.perm_id = ?{1}", fetchSize = FETCH_SIZE)
    public List<DeletedEntityRecord> listDeletedChildrenOfSample(String samplePermId);

    @Select(sql = SELECT + "from samples_all e join sample_relationships_all r on r.sample_id_parent=e.id "
            + "join samples_all c on r.sample_id_child=c.id join relationship_types t on r.relationship_id=t.id "
            + "join deletions d on e.del_id=d.id "
            + "where t.code = 'PARENT_CHILD' and c.perm_id = ?{1}", fetchSize = FETCH_SIZE)
    public List<DeletedEntityRecord> listDeletedParentsOfSample(String samplePermId);

    @Select(sql = SELECT + "from samples_all e join samples_all c on e.samp_id_part_of=c.id "
            + "join deletions d on e.del_id=d.id "
            + "where c.perm_id = ?{1}", fetchSize = FETCH_SIZE)
    public List<DeletedEntityRecord> listDeletedComponentsOfSample(String samplePermId);

    @Select(sql = SELECT_DS + "from data_all e join samples s on e.samp_id=s.id join deletions d on e.del_id=d.id "
            + "where s.perm_id = ?{1}", fetchSize = FETCH_SIZE)
    public List<DeletedEntityRecord> listDeletedDataSetsOfSample(String samplePermId);

}