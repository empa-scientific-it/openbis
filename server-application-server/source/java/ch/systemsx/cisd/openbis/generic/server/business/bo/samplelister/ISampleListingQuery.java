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

package ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.db.mapper.LongSetMapper;
import ch.systemsx.cisd.common.db.mapper.StringArrayMapper;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IPropertyListingQuery;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.ExperimentProjectSpaceCodeRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.common.MetaProjectWithEntityId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.EoDException;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.TransactionQuery;
import net.lemnik.eodsql.TypeMapper;
import net.lemnik.eodsql.spi.util.NonUpdateCapableDataObjectBinding;

/**
 * A {@link TransactionQuery} interface for obtaining large sets of sample-related entities from the database.
 * <p>
 * This interface is intended to be used only in this package. The <code>public</code> modifier is needed for creating a dynamic proxy by the EOD SQL
 * library.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses =
{ ExperimentProjectSpaceCodeRecord.class })
@Private
public interface ISampleListingQuery extends BaseQuery, IPropertyListingQuery
{
    static final String SELECT_FROM_SAMPLES_S =
            "           SELECT s.id, s.perm_id, s.code, s.expe_id, s.proj_id, s.space_id, s.saty_id, "
                    + "   s.registration_timestamp, s.modification_timestamp, s.pers_id_registerer, "
                    + "   s.pers_id_modifier, s.del_id, s.samp_id_part_of, s.version                                              "
                    + " FROM samples s";

    /**
     * Returns the total number of all samples in the database.
     */
    @Select(sql = "select count(*) from samples s")
    public long getSampleCount();

    // relationships

    /**
     * Returns the relationship type with given <var>code</var> and namespace.
     */
    @Select(sql = "select * from relationship_types where code=?{1} and is_managed_internally=?{2}")
    public long getRelationshipTypeId(String code, boolean managedInternally);

    /**
     * Returns the children sample ids of the specified samples in specified relationship.
     */
    @Select(sql = "SELECT sample_id_child FROM sample_relationships "
            + "    WHERE relationship_id=?{1} AND sample_id_parent = any(?{2})", parameterBindings =
    { TypeMapper.class/* default */, LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<Long> getChildrenIds(long relationshipId, LongSet parentSampleIds);

    /**
     * Returns the parent sample ids of the specified samples in specified relationship.
     */
    @Select(sql = "SELECT sample_id_parent FROM sample_relationships "
            + "    WHERE relationship_id=?{1} AND sample_id_child = any(?{2})", parameterBindings =
    { TypeMapper.class/* default */, LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<Long> getParentIds(long relationshipId, LongSet childrenSampleIds);

    /**
     * Returns the parent sample ids of the specified children sample ids in specified relationship.
     */
    @Select(sql = "SELECT * FROM sample_relationships "
            + "    WHERE relationship_id=?{1} AND sample_id_child = any(?{2})", parameterBindings =
    { TypeMapper.class/* default */, LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<SampleRelationRecord> getParentRelations(long relationshipId,
            LongSet childrenSampleIds);

    /**
     * Returns the child sample ids of the specified parent sample ids in specified relationship.
     */
    @Select(sql = "SELECT * FROM sample_relationships "
            + "    WHERE relationship_id=?{1} AND sample_id_parent = any(?{2})", parameterBindings =
    { TypeMapper.class/* default */, LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<SampleRelationRecord> getChildrenRelations(long relationshipId,
            LongSet parentSampleIds);

    @Select(sql = "select id, saty_id, space_id, expe_id from samples", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSampleSkeletons();

    @Select(sql = "select * from sample_relationships", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRelationRecord> getSampleRelationshipSkeletons();

    /**
     * Returns the technical ids of all samples (trashed ones excluded) which have at least one property of type MATERIAL referring to one of the
     * specified materials.
     */
    @Select(sql = "select p.samp_id from sample_properties as p join samples_all as s on p.samp_id = s.id " +
            "where s.del_id is null and p.mate_prop_id = any(?{1})", parameterBindings =
            LongSetMapper.class, fetchSize = FETCH_SIZE)
    public DataIterator<Long> getSampleIdsByMaterialProperties(LongSet materialIds);

    //

    /**
     * Returns the sample for the given <var>sampleId</var>.
     */
    @Select(SELECT_FROM_SAMPLES_S + " where s.id=?{1}")
    public SampleRecord getSample(long sampleId);

    /**
     * Returns all samples in the database.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S, fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSamples();

    //
    // Samples for group
    //

    /**
     * Returns the samples for the given <var>groupCode</var>.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " join sample_types st on s.saty_id=st.id"
            + " join spaces g on s.space_id=g.id "
            + " where st.is_listable and g.code=?{1} order by s.code", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getListableSpaceSamples(String groupCode);

    /**
     * Returns the samples for the given <var>groupCode</var> that are assigned to an experiment.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " join spaces g on s.space_id=g.id "
            + " where s.expe_id is not null and g.code=?{1} "
            + " order by s.code", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSpaceSamplesWithExperiment(String groupCode);

    /**
     * Returns the samples for the given <var>groupCode</var> and <var>sampleTypeId</var>
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " join spaces g on s.space_id=g.id  "
            + " where g.code=?{1} and s.saty_id=?{2}       "
            + " order by s.code", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSpaceSamplesForSampleType(
            String groupCode, long sampleTypeId);

    /**
     * Returns the samples for the given <var>groupCode</var> and <var>sampleTypeId</var> that are assigned to an experiment.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " join spaces g on s.space_id=g.id "
            + " where s.expe_id is not null and g.code=?{1} and s.saty_id=?{2} "
            + " order by s.code", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSpaceSamplesForSampleTypeWithExperiment(
            String groupCode, long sampleTypeId);

    /**
     * Returns the samples for all spaces.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " join sample_types st on s.saty_id=st.id"
            + " join spaces g on s.space_id=g.id "
            + " where st.is_listable order by s.code", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getAllListableSpaceSamples();

    /**
     * Returns the samples for all spaces that are assigned to an experiment.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " join spaces g on s.space_id=g.id "
            + " where s.expe_id is not null "
            + " order by s.code", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getAllSpaceSamplesWithExperiment();

    /**
     * Returns the samples for all spaces and <var>sampleTypeId</var>
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " join spaces g on s.space_id=g.id "
            + " where s.saty_id=?{1}                        "
            + " order by s.code", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getAllSpaceSamplesForSampleType(long sampleTypeId);

    /**
     * Returns the samples for all spaces and <var>sampleTypeId</var> that are assigned to an experiment.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " join spaces g on s.space_id=g.id "
            + " where s.expe_id is not null and s.saty_id=?{1} "
            + " order by s.code", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getAllSpaceSamplesForSampleTypeWithExperiment(long sampleTypeId);

    //
    // Samples for experiment
    //

    /**
     * Returns the samples for the given <var>experimentId</var>.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " JOIN sample_types st ON s.saty_id=st.id"
            + " WHERE st.is_listable AND s.expe_id=?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getListableSamplesForExperiment(long experimentId);

    @Select(sql = "with recursive connected_samples as ("
            + "select s.id, s.perm_id, s.code, s.expe_id, s.proj_id, s.space_id, s.saty_id, "
            + "   s.registration_timestamp, s.modification_timestamp, s.pers_id_registerer, "
            + "   s.del_id, s.samp_id_part_of "
            + "from samples as s join sample_types as st on s.saty_id = st.id "
            + "where st.is_listable and s.expe_id = ?{1} "
            + "union select s2.id, s2.perm_id, s2.code, s2.expe_id, s2.proj_id, s2.space_id, s2.saty_id, "
            + "    s2.registration_timestamp, s2.modification_timestamp, s2.pers_id_registerer, "
            + "    s2.del_id, s2.samp_id_part_of "
            + "from connected_samples as s left join sample_relationships as sr on sr.sample_id_parent = s.id "
            + "left join samples as s2 on s2.id = sr.sample_id_child "
            + "join sample_types as st on s2.saty_id = st.id where st.is_listable) "
            + "select * from connected_samples")
    public DataIterator<SampleRecord> getListableSamplesAndDescendentsForExperiment(
            long experimentId);

    //
    // Samples for container
    //

    /**
     * Returns the samples for the given <var>sampleContainerIds</var>.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " where s.samp_id_part_of=any(?{1})", parameterBindings =
    { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSamplesForContainer(LongSet sampleContainerIds);

    //
    // Samples for parent/child
    //

    /**
     * Returns the children samples for the given ids of relationship and parent sample.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " WHERE s.id IN "
            + "     (SELECT sample_id_child FROM sample_relationships "
            + "      WHERE relationship_id=?{1} AND sample_id_parent=?{2})", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getChildrenSamplesForParent(long relationshipId,
            long sampleParentId);

    /**
     * Returns the parent samples for the given ids of relationship and child sample.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " WHERE s.id IN "
            + "     (SELECT sample_id_parent FROM sample_relationships "
            + "      WHERE relationship_id=?{1} AND sample_id_child = any(?{2}))", parameterBindings =
    { TypeMapper.class, LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getParentSamplesForChildren(long relationshipId,
            LongSet sampleChildIds);

    /**
     * Returns the samples for the given <var>metaprojectId</var>.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " JOIN metaproject_assignments ma ON s.id=ma.samp_id"
            + " WHERE ma.mepr_id=?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSamplesForMetaproject(long metaprojectId);

    //
    // New samples of type
    //

    /**
     * Returns all samples with a <var>propertyType</var> attached having specified <var>popertyValue</var>. Additionally id of the sample SHOULDN'T
     * be in the specified set of ids.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " where s.saty_id=?{1} and s.id in ("
            + "       select samp_id from sample_properties sp where sp.stpt_id in ("
            + "              select id from sample_type_property_types stpt where stpt.prty_id = "
            + "                     (select id from property_types where code=?{2})"
            + "              ) and value=?{3}                                      "
            + "       ) and not id = any(?{4})", parameterBindings =
    { TypeMapper.class, TypeMapper.class, TypeMapper.class, LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSamplesWithPropertyValue(long sampleTypeId,
            String propertyTypeCode, String propertyValue, LongSet sampleIds);

    /**
     * Returns all samples with a <var>propertyType</var> and s specific <var>propertyValue</var> The <var>propertyValue</var> is a term of a
     * controlled vocabulary in this case. Additionally <var>sampleTypeId</var> should not be in the the list of <var>sampleIds</var>
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " where s.saty_id=?{1} and s.id in ("
            + "       select samp_id from sample_properties sp where sp.stpt_id in ("
            + "              select id from sample_type_property_types stpt where stpt.prty_id = "
            + "                     (select id from property_types where code=?{2})"
            + "              )  and cvte_id in ( "
            + "                        select id from controlled_vocabulary_terms where code=?{3}"
            + "                 )"
            + "       ) and not id = any(?{4})", parameterBindings =
    { TypeMapper.class, TypeMapper.class, TypeMapper.class, LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSamplesWithControlledVocabularyWithPropertyValue(long sampleTypeId,
            String propertyTypeCode, String propertyValue, LongSet sampleIds);

    //
    // Shared samples
    //

    /**
     * Returns the shared samples for the given <var>dbInstanceId</var>.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " join sample_types st on s.saty_id=st.id "
            + "   where st.is_listable and s.space_id is null order by s.code", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getListableSharedSamples();

    /**
     * Returns the shared samples for the given <var>dbInstanceId</var> and <var>sampleTypeId</var>.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " where s.saty_id=?{1} and s.space_id is null"
            + " order by s.code", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSharedSamplesForSampleType(long sampleTypeId);

    //
    // Types
    //

    /**
     * A binding for the {@link ISampleListingQuery#getPropertyTypes()} query.
     */
    static class SampleTypeDataObjectBinding extends NonUpdateCapableDataObjectBinding<SampleType>
    {
        @Override
        public void unmarshall(ResultSet row, SampleType into) throws SQLException, EoDException
        {
            into.setId(row.getLong("id"));
            into.setCode(row.getString("code"));
            into.setGeneratedFromHierarchyDepth(row.getInt("generated_from_depth"));
            into.setShowContainer(row.getInt("part_of_depth") > 0);
        }
    }

    /**
     * Returns the sample type for the given <code>sampleCode</code>. Note that the code of the result is already HTML escaped.
     */
    @Select(sql = "select id, code, generated_from_depth, part_of_depth from sample_types"
            + "      where code=?{1}", resultSetBinding = SampleTypeDataObjectBinding.class)
    public SampleType getSampleType(String sampleCode);

    /**
     * Returns all sample types.
     */
    @Select(sql = "select id, code, generated_from_depth, part_of_depth from sample_types", resultSetBinding = SampleTypeDataObjectBinding.class)
    public SampleType[] getSampleTypes();

    //
    // Samples
    //

    /**
     * Returns the samples for the given <var>sampleIds</var>.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " where s.id = any(?{1})", parameterBindings =
    { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSamples(LongSet sampleIds);

    /**
     * Returns the samples for the given <var>sampleCodes</var>.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " where s.code = any(?{1})", parameterBindings =
    { StringArrayMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSamplesForCodes(String[] sampleCodes);

    /**
     * Returns the non-empty container samples for the given <var>sampleCodes</var>.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S
            + " where s.id in (select samp_id_part_of from samples where samp_id_part_of in (select id from samples where code = any(?{1})))", parameterBindings =
    { StringArrayMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getContainerSamplesForCodes(String[] sampleCodes);

    /**
     * Returns the samples for the given <var>permIds</var>.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " where s.perm_id = any(?{1})", parameterBindings =
    { StringArrayMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSamplesForPermIds(String[] permIds);

    /**
     * Returns the non-empty container samples for the given <var>sampleCodes</var>.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S
            + " where s.id in (select samp_id_part_of from samples where samp_id_part_of in (select id from samples where permId = any(?{1})))", parameterBindings =
    { StringArrayMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getContainerSamplesForPermIds(String[] permIds);

    //
    // Sample Properties
    //

    /**
     * Returns all generic property values of all samples specified by <var>sampleIds</var>.
     * 
     * @param sampleIds The set of sample ids to get the property values for.
     */
    @Select(sql = "SELECT sp.samp_id as entity_id, stpt.prty_id, stpt.script_id, stpt.ordinal, "
            + "           sp.value, sp.cvte_id, sp.mate_prop_id, sp.samp_prop_id, sc.script_type "
            + "      FROM sample_properties sp"
            + "      JOIN sample_type_property_types stpt ON sp.stpt_id=stpt.id"
            + "      LEFT OUTER JOIN scripts sc ON stpt.script_id = sc.id"
            + "     WHERE sp.samp_id = any(?{1})", parameterBindings =
    { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<GenericEntityPropertyRecord> getEntityPropertyGenericValues(
            LongSet sampleIds);

    @Select(sql = "select m.id as id, m.name as name, m.description as description, p.user_id as owner_name, "
            + " m.private as is_private, m.creation_date as creation_date, ma.samp_id as entity_id "
            + " from metaprojects m, metaproject_assignments ma, persons p "
            + " where ma.samp_id = any(?{1}) and m.owner = ?{2} and m.id = ma.mepr_id and m.owner = p.id", parameterBindings =
    { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<MetaProjectWithEntityId> getMetaprojects(LongSet entityIds, Long userId);

    /**
     * Returns the samples with codes like that match a pattern like TEMP.D977F1F4-B2F5-49AB-8DB5-BE47277286B9.265743 Results are ordered by the last
     * part (after .) so that we preserve the order of sample creation.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S
            + " where s.code similar to 'TEMP\\.[a-zA-Z0-9\\-]+\\.[0-9]+' "
            + "order by  (split_part(s.code, '.', 3)::int) asc", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSamplesWithTemporaryCodes();

}
