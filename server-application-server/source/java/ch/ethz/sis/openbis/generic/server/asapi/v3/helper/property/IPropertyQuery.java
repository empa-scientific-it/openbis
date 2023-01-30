/*
 * Copyright 2020 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.property;

import java.util.List;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectQuery;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.PropertyRecord;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.Update;

/**
 * @author Franz-Josef Elmer
 */
public interface IPropertyQuery extends ObjectQuery
{
    public static final String SELECT = "select p.id as objectId, p.value as propertyValue, "
            + "s.perm_id as sample_perm_id, cvt.code as vocabularyPropertyValue, "
            + "m.code as materialPropertyValueCode, mt.code as materialPropertyValueTypeCode ";

    public static final String JOIN_WHERE = "left join samples s on p.samp_prop_id = s.id "
            + "left join materials m on p.mate_prop_id = m.id "
            + "left join material_types mt on m.maty_id = mt.id "
            + "left join controlled_vocabulary_terms cvt on p.cvte_id = cvt.id "
            + "join property_types pt on etpt.prty_id = pt.id "
            + "where pt.code = ?{1}";

    public static final String SET_WHERE = "set value = ?{1.propertyValue}, cvte_id = null, "
            + "samp_prop_id = null, mate_prop_id = null where id = ?{1.objectId}";

    @Select(sql = SELECT
            + "from experiment_properties p "
            + "join experiment_type_property_types etpt on p.etpt_id = etpt.id "
            + JOIN_WHERE, fetchSize = FETCH_SIZE)
    public List<PropertyRecord> listExperimentProperties(String propertyTypeCode);

    @Update(sql = "update experiment_properties " + SET_WHERE, batchUpdate = true)
    public void updateExperimentProperties(List<PropertyRecord> convertedProperties);

    @Select(sql = SELECT
            + "from sample_properties p "
            + "join sample_type_property_types etpt on p.stpt_id = etpt.id "
            + JOIN_WHERE, fetchSize = FETCH_SIZE)
    public List<PropertyRecord> listSampleProperties(String propertyTypeCode);

    @Update(sql = "update sample_properties " + SET_WHERE, batchUpdate = true)
    public void updateSampleProperties(List<PropertyRecord> convertedProperties);

    @Select(sql = SELECT
            + "from data_set_properties p "
            + "join data_set_type_property_types etpt on p.dstpt_id = etpt.id "
            + JOIN_WHERE, fetchSize = FETCH_SIZE)
    public List<PropertyRecord> listDataSetProperties(String propertyTypeCode);

    @Update(sql = "update data_set_properties " + SET_WHERE, batchUpdate = true)
    public void updateDataSetProperties(List<PropertyRecord> convertedProperties);

}
