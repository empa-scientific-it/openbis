/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property;

import java.util.List;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectQuery;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectRelationRecord;
import ch.systemsx.cisd.common.db.mapper.LongSetMapper;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.Select;

/**
 * @author Franz-Josef Elmer
 */
public interface PropertyTypeQuery extends ObjectQuery
{
    @Select(sql = "select pt.*, dt.code as data_type from property_types pt, data_types dt where pt.daty_id = dt.id and pt.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<PropertyTypeRecord> getPropertyTypes(LongSet propertyTypeIds);

    @Select(sql = "select id as objectId, covo_id as relatedId from property_types where id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getVocabularyIds(LongSet propertyTypeIds);

    @Select(sql = "select id as objectId, maty_prop_id as relatedId from property_types where id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getMaterialTypeIds(LongSet objectIds);

    @Select(sql = "select id as objectId, saty_prop_id as relatedId from property_types where id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getSampleTypeIds(LongSet objectIds);
    
    @Select(sql = "select id as objectId, pers_id_registerer as relatedId from property_types where id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getRegistratorIds(LongSet objectIds);

    @Select(sql = "select prty_id as objectId, id as relatedId from semantic_annotations where prty_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getSemanticAnnotationIds(LongOpenHashSet propertyTypeIds);

}
