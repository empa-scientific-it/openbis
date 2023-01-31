/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer;

import java.util.List;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.Update;

/**
 * @author Franz-Josef Elmer
 */
public interface IHarvesterQuery extends BaseQuery
{
    public static final String SETTERS_WITH_REGISTRATOR_ONLY = "set registration_timestamp = ?{1.registrationTimestamp}, "
            + "pers_id_registerer = ?{1.registratorId} ";

    public static final String SETTERS_WITHOUT_MODIFIER = SETTERS_WITH_REGISTRATOR_ONLY
            + ", modification_timestamp = ?{1.modificationTimestamp}";

    public static final String SETTERS_WITH_MODIFIER = SETTERS_WITHOUT_MODIFIER + ", pers_id_modifier = ?{1.modifierId} ";

    @Select("select id,user_id as userId from persons")
    public List<PersonRecord> listAllUsers();

    @Select("select id,code from material_types")
    public List<MaterialTypeRecord> listAllMaterialTypes();

    @Update(sql = "update materials " + SETTERS_WITHOUT_MODIFIER + " where code = ?{1.permId} and maty_id = ?{1.typeId}", batchUpdate = true)
    public void updateMaterialRegistrations(List<RegistrationDTO> registrations);

    @Update(sql = "update spaces " + SETTERS_WITH_REGISTRATOR_ONLY + "where code = ?{1.permId}", batchUpdate = true)
    public void updateSpaceRegistrations(List<RegistrationDTO> registrations);

    @Update(sql = "update projects " + SETTERS_WITH_MODIFIER + "where perm_id = ?{1.permId}", batchUpdate = true)
    public void updateProjectRegistrations(List<RegistrationDTO> registrations);

    @Update(sql = "update experiments_all " + SETTERS_WITH_MODIFIER + "where perm_id = ?{1.permId}", batchUpdate = true)
    public void updateExperimentRegistrations(List<RegistrationDTO> registrations);

    @Update(sql = "update samples_all " + SETTERS_WITH_MODIFIER + "where perm_id = ?{1.permId}", batchUpdate = true)
    public void updateSampleRegistrations(List<RegistrationDTO> registrations);

    @Update(sql = "update data_all " + SETTERS_WITH_MODIFIER + "where code = ?{1.permId}", batchUpdate = true)
    public void updateDataSetRegistrations(List<RegistrationDTO> registrations);

    @Update(sql = "update spaces set frozen = ?{1.frozen}, frozen_for_proj = ?{1.frozenForProjects}, "
            + "frozen_for_samp = ?{1.frozenForSamples} where code = ?{1.permId}", batchUpdate = true)
    public void updateSpaceFrozenFlags(List<FrozenFlags> frozenFlags);

    @Update(sql = "update projects set frozen = ?{1.frozen}, frozen_for_exp = ?{1.frozenForExperiments}, "
            + "frozen_for_samp = ?{1.frozenForSamples} where perm_id = ?{1.permId}", batchUpdate = true)
    public void updateProjectFrozenFlags(List<FrozenFlags> frozenFlags);

    @Update(sql = "update experiments_all set frozen = ?{1.frozen}, frozen_for_data = ?{1.frozenForDataSets}, "
            + "frozen_for_samp = ?{1.frozenForSamples} where perm_id = ?{1.permId}", batchUpdate = true)
    public void updateExperimentFrozenFlags(List<FrozenFlags> frozenFlags);

    @Update(sql = "update samples_all set frozen = ?{1.frozen}, frozen_for_data = ?{1.frozenForDataSets}, "
            + "frozen_for_comp = ?{1.frozenForComponents}, frozen_for_children = ?{1.frozenForChildren}, "
            + "frozen_for_parents = ?{1.frozenForParents} where perm_id = ?{1.permId}", batchUpdate = true)
    public void updateSampleFrozenFlags(List<FrozenFlags> frozenFlags);

    @Update(sql = "update data_all set frozen = ?{1.frozen}, frozen_for_comps = ?{1.frozenForComponents}, "
            + "frozen_for_conts = ?{1.frozenForContainers}, frozen_for_children = ?{1.frozenForChildren}, "
            + "frozen_for_parents = ?{1.frozenForParents} where code = ?{1.permId}", batchUpdate = true)
    public void updateDataSetFrozenFlags(List<FrozenFlags> frozenFlags);
}
