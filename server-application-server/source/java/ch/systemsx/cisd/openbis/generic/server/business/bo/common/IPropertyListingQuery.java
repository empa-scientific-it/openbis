/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.business.bo.common;

import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.Select;

import ch.systemsx.cisd.common.db.mapper.LongSetMapper;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

import it.unimi.dsi.fastutil.longs.LongSet;

/**
 * Query methods for retrieving property types, material types, and vocabulary URL templates.
 * 
 * @author Bernd Rinn
 */
public interface IPropertyListingQuery
{
    public static final int FETCH_SIZE = 1000;

    /**
     * Returns all property types. Fills only <code>id</code>, <code>code</code>, <code>label</var> and <code>DataType</code>. Note that code and
     * label are already HTML escaped.
     */
    @Select(sql = "select pt.id as pt_id, pt.code as pt_code, dt.code as dt_code,"
            + "      pt.label as pt_label, pt.is_managed_internally, pt.schema, pt.transformation"
            + "    from property_types pt join data_types dt on pt.daty_id=dt.id", resultSetBinding = PropertyTypeDataObjectBinding.class)
    public PropertyType[] getPropertyTypes();

    /**
     * Returns id and url template of all vocabularies.
     */
    @Select("select id, source_uri as code from controlled_vocabularies")
    public CodeRecord[] getVocabularyURLTemplates();

    /**
     * Returns id and code of all material types.
     */
    @Select("select id, code from material_types")
    public CodeRecord[] getMaterialTypes();
    
    @Select("select id, code from sample_types")
    public CodeRecord[] getSampleTypeIdsAndCode();
    
    @Select(sql = "SELECT id, covo_id, code, label, ordinal, description "
            + "FROM controlled_vocabulary_terms WHERE id = any(?{1})", 
            parameterBindings = { LongSetMapper.class}, fetchSize = FETCH_SIZE)
    public DataIterator<VocabularyTermRecord> getVocabularyTerms(LongSet termIds);

    @Select(sql = "SELECT id, code, maty_id FROM materials WHERE id = any(?{1})", 
            parameterBindings = { LongSetMapper.class}, fetchSize = FETCH_SIZE)
    public DataIterator<MaterialEntityPropertyRecord> getMaterials(LongSet materialIds);
    
    @Select(sql = "SELECT id, code, perm_id, saty_id FROM samples WHERE id = any(?{1})", 
            parameterBindings = { LongSetMapper.class}, fetchSize = FETCH_SIZE)
    public DataIterator<SampleEntityPropertyRecord> getBasicSamples(LongSet sampleIds);
    
}
