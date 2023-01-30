/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister;

import java.util.List;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.db.mapper.LongSetMapper;
import ch.systemsx.cisd.common.db.mapper.StringArrayMapper;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IPropertyListingQuery;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.common.MetaProjectWithEntityId;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.TransactionQuery;

/**
 * A {@link TransactionQuery} interface for obtaining large sets of material-related entities from the database.
 * <p>
 * This interface is intended to be used only in this package. The <code>public</code> modifier is needed for creating a dynamic proxy by the EOD SQL
 * library.
 * 
 * @author Tomasz Pylak
 */
@Private
public interface IMaterialListingQuery extends BaseQuery, IPropertyListingQuery
{
    public static final String SELECT_MATERIALS = "select m.id, m.code, m.maty_id, "
            + "m.registration_timestamp, m.modification_timestamp, m.pers_id_registerer "
            + "from materials m";

    public static final String SELECT_MATERIALS_WHERE = SELECT_MATERIALS + " where";

    /**
     * Returns the technical IDs of all materials which have at least one property of type MATERIAL referring to one of the specified materials.
     */
    @Select(sql = "select mate_id from material_properties where mate_prop_id = any(?{1})", parameterBindings =
            LongSetMapper.class, fetchSize = FETCH_SIZE)
    public DataIterator<Long> getMaterialIdsByMaterialProperties(LongSet materialIds);

    /**
     * Returns the materials for the given <var>materialTypeId</var>
     */
    @Select(sql = SELECT_MATERIALS_WHERE + " m.maty_id=?{1} order by m.code", fetchSize = FETCH_SIZE)
    public DataIterator<MaterialRecord> getMaterialsForMaterialType(long materialTypeId);

    /**
     * Returns the materials for the given <var>materialTypeId</var> and <var>materialIds</var>
     */
    @Select(sql = SELECT_MATERIALS_WHERE + " m.id = any(?{1}) order by m.code", parameterBindings =
    { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<MaterialRecord> getMaterialsForMaterialTypeWithIds(LongSet materialIds);

    /**
     * Returns the materials for the given <var>materialTypeId</var> and <var>materialIds</var>
     */
    @Select(sql = SELECT_MATERIALS_WHERE + " m.code = any(?{1}) order by m.code", parameterBindings =
    { StringArrayMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<MaterialRecord> getMaterialsForMaterialCodes(String[] codes);

    /**
     * Returns the materials for the given <var>metaprojectId</var>
     */
    @Select(sql = SELECT_MATERIALS
            + " JOIN metaproject_assignments ma ON m.id=ma.mate_id WHERE ma.mepr_id=?{1} order by m.code", fetchSize = FETCH_SIZE)
    public DataIterator<MaterialRecord> getMaterialsForMetaprojectId(long metaprojectId);

    //
    // Entity Properties
    //

    /**
     * Returns all generic property values of all materials specified by <var>entityIds</var>.
     * 
     * @param entityIds The set of material ids to get the property values for.
     */
    @Select(sql = "SELECT pr.mate_id as entity_id, etpt.prty_id, etpt.script_id, pr.value, "
            + "           pr.cvte_id, pr.mate_prop_id, sc.script_type "
            + "      FROM material_properties pr"
            + "      JOIN material_type_property_types etpt ON pr.mtpt_id=etpt.id"
            + "      LEFT OUTER JOIN scripts sc ON etpt.script_id = sc.id"
            + "     WHERE pr.mate_id = any(?{1})", parameterBindings =
    { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<GenericEntityPropertyRecord> getEntityPropertyGenericValues(
            LongSet entityIds);

    @Select(sql = "select m.id as id, m.name as name, m.description as description, p.user_id as owner_name, "
            + " m.private as is_private, m.creation_date as creation_date, ma.mate_id as entity_id "
            + " from metaprojects m, metaproject_assignments ma, persons p "
            + " where ma.mate_id = any(?{1}) and m.owner = ?{2} and m.id = ma.mepr_id and m.owner = p.id", parameterBindings =
    { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<MetaProjectWithEntityId> getMetaprojects(LongSet entityIds, Long userId);

}
