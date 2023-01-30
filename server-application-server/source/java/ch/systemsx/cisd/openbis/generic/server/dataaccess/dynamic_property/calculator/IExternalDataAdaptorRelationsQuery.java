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

/**
 * @author pkupczyk
 */
public interface IExternalDataAdaptorRelationsQuery extends BaseQuery
{

    public int FETCH_SIZE = 1000;

    @Select(sql = "SELECT id, code FROM data_set_types", fetchSize = FETCH_SIZE)
    public List<EntityTypeRecord> getDataSetTypes();

    @Select(sql = "SELECT dsr.data_id_parent FROM data_set_relationships dsr, "
            + " data d, data_set_types dst WHERE "
            + " dsr.data_id_parent = d.id AND d.dsty_id = dst.id AND "
            + " dsr.data_id_child = ?{1} AND dst.id = any(?{2}) AND "
            + " dsr.relationship_id = ?{3}", parameterBindings =
    { TypeMapper.class, LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<Long> getParentIdsOfTypes(Long childId, LongSet parentTypeIds, Long relationshipTypeId);

    @Select(sql = "SELECT dsr.data_id_child FROM data_set_relationships dsr, "
            + " data d, data_set_types dst WHERE "
            + " dsr.data_id_child = d.id AND d.dsty_id = dst.id AND "
            + " dsr.data_id_parent = ?{1} AND dst.id = any(?{2}) AND "
            + " dsr.relationship_id = ?{3}", parameterBindings =
    { TypeMapper.class, LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<Long> getChildIdsOfTypes(Long parentId, LongSet childTypeIds, Long relationshipTypeId);

}
