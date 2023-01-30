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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator;

import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.List;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.TypeMapper;

import ch.systemsx.cisd.common.db.mapper.LongSetMapper;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;

/**
 * @author pkupczyk
 */
public interface ISampleAdaptorRelationsQuery extends BaseQuery
{

    public int FETCH_SIZE = 1000;

    @Select(sql = "SELECT id, code FROM sample_types", fetchSize = FETCH_SIZE)
    public List<EntityTypeRecord> getSampleTypes();

    @Select(sql = "SELECT sr.sample_id_parent FROM sample_relationships sr, "
            + " relationship_types rt, samples s, sample_types st WHERE "
            + " sr.relationship_id = rt.id AND sr.sample_id_parent = s.id AND "
            + " s.saty_id = st.id AND sr.sample_id_child = ?{1} AND " + " rt.code = '"
            + BasicConstant.PARENT_CHILD_DB_RELATIONSHIP + "' AND st.id = any(?{2})", parameterBindings =
    { TypeMapper.class, LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<Long> getParentIdsOfTypes(Long childId, LongSet parentTypeIds);

    @Select(sql = "SELECT sr.sample_id_child FROM sample_relationships sr, "
            + " relationship_types rt, samples s, sample_types st WHERE "
            + " sr.relationship_id = rt.id AND sr.sample_id_child = s.id AND "
            + " s.saty_id = st.id AND sr.sample_id_parent = ?{1} AND " + " rt.code = '"
            + BasicConstant.PARENT_CHILD_DB_RELATIONSHIP + "' AND st.id = any(?{2})", parameterBindings =
    { TypeMapper.class, LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<Long> getChildIdsOfTypes(Long parentId, LongSet childTypeIds);

}
