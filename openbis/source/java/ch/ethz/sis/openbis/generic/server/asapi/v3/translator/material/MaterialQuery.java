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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.material;

import java.util.List;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectQuery;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectRelationRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryPropertyRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.MaterialPropertyRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.PropertyAssignmentRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.PropertyRecord;
import ch.systemsx.cisd.common.db.mapper.LongSetMapper;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.Select;

/**
 * @author pkupczyk
 */
public interface MaterialQuery extends ObjectQuery
{

    @Select(sql =
            "select m.id, m.code, mt.code as typeCode, m.pers_id_registerer as registererId, m.registration_timestamp as registrationDate, m.modification_timestamp as modificationDate "
                    + "from materials m, material_types mt "
                    + "where m.maty_id = mt.id and m.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<MaterialBaseRecord> getMaterials(LongSet materialIds);

    @Select(sql = "select m.id as objectId, m.maty_id as relatedId from materials m where m.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getTypeIds(LongSet materialIds);

    @Select(sql = "select mt.id, mt.code, mt.description, mt.modification_timestamp as modificationDate from material_types mt where mt.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<MaterialTypeBaseRecord> getTypes(LongSet materialTypeIds);

    // PropertyQueryGenerator was used to generate this query
    @Select(sql =
            "select p.mate_id as objectId, p.pers_id_author AS authorId, p.modification_timestamp AS modificationTimestamp, case pt.is_managed_internally when FALSE then pt.code else '$' || pt.code end as propertyCode, p.value as propertyValue, m.code as materialPropertyValueCode, mt.code as materialPropertyValueTypeCode, cvt.code as vocabularyPropertyValue, cv.code as vocabularyPropertyValueTypeCode "
                    + "from material_properties p "
                    + "left join materials m on p.mate_prop_id = m.id "
                    + "left join controlled_vocabulary_terms cvt on p.cvte_id = cvt.id "
                    + "left join controlled_vocabularies cv on cvt.covo_id = cv.id "
                    + "left join material_types mt on m.maty_id = mt.id "
                    + "join material_type_property_types etpt on p.mtpt_id = etpt.id "
                    + "join property_types pt on etpt.prty_id = pt.id "
                    + "where p.mate_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<PropertyRecord> getProperties(LongSet materialIds);

    // PropertyQueryGenerator was used to generate this query
    @Select(sql =
            "select p.mate_id as objectId, case pt.is_managed_internally when FALSE then pt.code else '$' || pt.code end as propertyCode, p.mate_prop_id as propertyValue "
                    + "from material_properties p "
                    + "join material_type_property_types etpt on p.mtpt_id = etpt.id "
                    + "join property_types pt on etpt.prty_id = pt.id "
                    + "where p.mate_prop_id is not null and p.mate_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<MaterialPropertyRecord> getMaterialProperties(LongOpenHashSet materialIds);

    // PropertyQueryGenerator was used to generate this query
    @Select(sql =
            "select ph.mate_id as objectId, ph.pers_id_author as authorId, case pt.is_managed_internally when FALSE then pt.code else '$' || pt.code end as propertyCode, ph.value as propertyValue, ph.material as materialPropertyValue, ph.vocabulary_term as vocabularyPropertyValue, ph.valid_from_timestamp as validFrom, ph.valid_until_timestamp as validTo "
                    + "from material_properties_history ph "
                    + "join material_type_property_types etpt on ph.mtpt_id = etpt.id "
                    + "join property_types pt on etpt.prty_id = pt.id "
                    + "where ph.mate_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<HistoryPropertyRecord> getPropertiesHistory(LongSet materialIds);

    @Select(sql = "select m.id as objectId, m.pers_id_registerer as relatedId from materials m where m.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getRegistratorIds(LongSet materialIds);

    @Select(sql = "select ma.mate_id as objectId, ma.mepr_id as relatedId from metaproject_assignments ma where ma.mate_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getTagIds(LongSet materialIds);

    @Select(sql = "select maty_id as objectId, id as relatedId from material_type_property_types where maty_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getPropertyAssignmentIds(LongSet materialTypeIds);

    @Select(sql = "select pt.code as prty_code, pt.is_managed_internally as prty_is_managed_internally, 'MATERIAL' as kind_code, mt.id as type_id, mt.code as type_code, mtpt.* from material_type_property_types mtpt, property_types pt, material_types mt where mtpt.id = any(?{1}) and mtpt.prty_id = pt.id and mtpt.maty_id = mt.id", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<PropertyAssignmentRecord> getPropertyAssignments(LongSet materialTypePropertyTypeIds);

    @Select(sql = "select t.id as objectId, t.validation_script_id as relatedId from material_types t where t.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getValidationPluginIds(LongSet materialTypeIds);

}
