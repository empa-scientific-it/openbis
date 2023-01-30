/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.Select;

/**
 * A simple id resolver for entities.
 * 
 * @author Bernd Rinn
 */
public interface IEntityResolverQuery extends BaseQuery
{
    /**
     * Returns the tech id of a dataset by its code / perm id.
     */
    @Select(sql = "select d.id from data d where d.code = ?{1}")
    public Long tryResolveDatasetIdByCode(String datasetCode);

    /**
     * Returns the tech id of a sample by its perm id.
     */
    @Select(sql = "select s.id from samples s where s.perm_id = ?{1}")
    public Long tryResolveSampleIdByPermId(String permId);

    /**
     * Returns the tech id of a sample by its space and sample code.
     */
    @Select(sql = "select s.id from samples s left join spaces sp on s.space_id = sp.id where sp.code = ?{1} and s.code = ?{2}")
    public Long tryResolveSampleIdByCode(String spaceCodeOrNull, String sampleCode);

    /**
     * Returns the tech id of a material by its code / perm id.
     */
    @Select(sql = "select m.id from materials m left join material_types mt on m.maty_id = mt.id where mt.code = ?{1} and m.code = ?{2}")
    public Long tryResolveMaterialIdByCode(String materialTypeCode, String materialCode);

    /**
     * Returns the tech id of an experiment by its perm id.
     */
    @Select(sql = "select e.id from experiments e where e.perm_id = ?{1}")
    public Long tryResolveExperimentIdByPermId(String permId);

    /**
     * Returns the tech id of an experiment by its space, project and experiment code.
     */
    @Select(sql = "select e.id from experiments e left join projects p on e.proj_id = p.id left join spaces sp on p.space_id = sp.id where sp.code = ?{1} and p.code = ?{2} and e.code = ?{3}")
    public Long tryResolveExperimentIdByCode(String spaceCode, String projectCode,
            String experimentCode);

    /**
     * Returns the tech id of a project by its perm id.
     */
    @Select(sql = "select p.id from projects p where p.perm_id = ?{1}")
    public Long tryResolveProjectIdByPermId(String permId);

    /**
     * Returns the tech id of a project by its space and project code.
     */
    @Select(sql = "select p.id from projects p left join spaces sp on p.space_id = sp.id where sp.code = ?{1} and p.code = ?{2}")
    public Long tryResolveProjectIdByCode(String spaceCode, String projectCode);

    /**
     * Returns the tech id of a space by its code.
     */
    @Select(sql = "select sp.id from spaces sp where sp.code = ?{1}")
    public Long tryResolveSpaceIdByCode(String spaceCode);

}
