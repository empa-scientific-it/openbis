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

package ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess;

import java.util.List;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.TypeMapper;

import ch.systemsx.cisd.common.db.mapper.LongArrayMapper;
import ch.systemsx.cisd.common.db.mapper.StringArrayMapper;

/**
 * Screening specific queries on openbis database.
 * 
 * @author Tomasz Pylak
 */
public interface IScreeningQuery extends BaseQuery
{

    public static final int FETCH_SIZE = 1000;

    final String WELLS_FOR_MATERIAL_ID_SELECT =
            "select "
                    + "      well.id as well_id,"
                    + "      well.perm_id as well_perm_id,"
                    + "      well.code as well_code,"
                    + "      well_type.code as well_type_code,"
                    + "      pl.id as plate_id,"
                    + "      pl.perm_id as plate_perm_id,"
                    + "      pl.code as plate_code,"
                    + "      pl_type.code as plate_type_code,"
                    + "      exp.id as exp_id,"
                    + "      exp.code as exp_code,"
                    + "      exp.perm_id as exp_perm_id,"
                    + "      exp_type.code as exp_type_code,"
                    + "      projects.code as proj_code,"
                    + "      spaces.code as space_code,"
                    + "      well_material.id as material_content_id"
                    + " from materials well_material "
                    + "      join sample_properties well_props on well_props.mate_prop_id = well_material.id"
                    + "      join samples well on  well_props.samp_id = well.id"
                    + "      join samples pl on well.samp_id_part_of = pl.id"
                    + "      join experiments exp on pl.expe_id = exp.id  "
                    + "      join experiment_types exp_type on exp.exty_id = exp_type.id  "
                    + "      join projects on exp.proj_id = projects.id     "
                    + "      join spaces on projects.space_id = spaces.id  "
                    + "      join sample_types pl_type on pl.saty_id = pl_type.id   "
                    + "      join sample_types well_type on well.saty_id = well_type.id  "
                    + "      join material_types well_material_type on well_material.maty_id = well_material_type.id";

    /**
     * @return well locations which belong to a parent plate connected to a specified experiment. Each well will have a material property (e.g. gene)
     *         with the specified id.
     */
    @Select(sql = WELLS_FOR_MATERIAL_ID_SELECT + " where well_material.id = ?{1} and exp.id = ?{2}")
    public DataIterator<WellContentQueryResult> getPlateLocationsForMaterialId(long materialId,
            long experimentId);

    /**
     * @return well locations which are connected to a given material (e.g. gene) and belong to the experiments within a given project.
     */
    @Select(sql = WELLS_FOR_MATERIAL_ID_SELECT
            + " where well_material.id = ?{1} and spaces.code = ?{2} and projects.code = ?{3}")
    public DataIterator<WellContentQueryResult> getPlateLocationsForMaterialId(long materialId,
            String spaceCode, String projectCode);

    /**
     * @return well locations which are connected to a given material (e.g. gene) with the specified id.
     */
    @Select(sql = WELLS_FOR_MATERIAL_ID_SELECT + " where well_material.id = ?{1}")
    public DataIterator<WellContentQueryResult> getPlateLocationsForMaterialId(long materialId);

    /**
     * @return well locations which are connected to a given material (e.g. gene) and belong to the experiments within a given project.
     */
    @Select(sql = WELLS_FOR_MATERIAL_ID_SELECT
            + " where well_material.id = ?{1} and projects.id = ?{2}")
    public DataIterator<WellContentQueryResult> getPlateLocationsForMaterialAndProjectIds(
            long materialId, long projectId);

    /**
     * @return well locations which belong to a parent plate connected to any experiment in the specified project. Each well will have a material
     *         property (e.g. gene) with one of the specified codes. The connected material will have one of the specified types.
     */
    @Select(sql = WELLS_FOR_MATERIAL_ID_SELECT + " where well_material.id = any(?{1}) and "
            + "well_material_type.code = any(?{2}) and spaces.code = ?{3} and projects.code = ?{4}", parameterBindings =
    { LongArrayMapper.class, StringArrayMapper.class, TypeMapper.class /* default mapper */,
            TypeMapper.class })
    public DataIterator<WellContentQueryResult> getPlateLocationsForMaterialCodesInProject(
            long[] materialIds, String[] materialTypeCodes, String spaceCode, String projectCode);

    /**
     * @return well locations which belong to a parent plate connected to a specified experiment. Each well will have a material property (e.g. gene)
     *         with one of the specified codes. The connected material will have one of the specified types.
     */
    @Select(sql = WELLS_FOR_MATERIAL_ID_SELECT + " where well_material.id = any(?{1}) and "
            + "well_material_type.code = any(?{2}) and exp.id = ?{3}", parameterBindings =
    { LongArrayMapper.class, StringArrayMapper.class, TypeMapper.class /* default mapper */})
    public DataIterator<WellContentQueryResult> getPlateLocationsForMaterialCodes(
            long[] materialIds, String[] materialTypeCodes, long experimentId);

    /**
     * @return well locations with the specified materials, from any experiment. Each well will have a material property (e.g. gene) with one of the
     *         specified codes. The connected material will have one of the specified types.
     */
    @Select(sql = WELLS_FOR_MATERIAL_ID_SELECT + " where well_material.id = any(?{1}) and "
            + "well_material_type.code = any(?{2})", parameterBindings =
    { LongArrayMapper.class, StringArrayMapper.class })
    public DataIterator<WellContentQueryResult> getPlateLocationsForMaterialCodes(
            long[] materialIds, String[] materialTypeCodes);

    /**
     * @return the material to well plate mapping for the given <var>platePermId</var>. Only consider materials of <var>materialTypeCode</var>. Only
     *         fills <var>well_code</var> and <var>material_content_code</var>. Note that this may return more than one row per well.
     */
    @Select(sql = "select "
            + "      well.code as well_code,"
            + "      well_material.code as material_content_code"
            + " from samples well"
            + "   join samples pl on pl.id = well.samp_id_part_of"
            + "   join sample_properties well_props on well.id = well_props.samp_id"
            + "   join materials well_material on well_props.mate_prop_id = well_material.id"
            + "   join material_types well_material_type on well_material.maty_id = well_material_type.id"
            + " where well_material_type.code=?{2} and pl.perm_id=?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<WellContentQueryResult> getPlateMappingForMaterialType(String platePermId,
            String materialTypeCode);

    /**
     * @return the material to well plate mapping for the given <var>platePermId</var>. Consider all material types. Only fills <var>well_code</var>,
     *         <var>material_content_code</var> and <var>material_content_code</var>. Note that this may return more than one row per well.
     */
    @Select(sql = "select "
            + "      well.code as well_code,"
            + "      well_material_type.code as material_content_type_code,"
            + "      well_material.code as material_content_code"
            + " from samples well"
            + "   join samples pl on pl.id = well.samp_id_part_of"
            + "   join sample_properties well_props on well.id = well_props.samp_id"
            + "   join materials well_material on well_props.mate_prop_id = well_material.id"
            + "   join material_types well_material_type on well_material.maty_id = well_material_type.id"
            + " where pl.perm_id=?{1} order by material_content_type_code", fetchSize = FETCH_SIZE)
    public DataIterator<WellContentQueryResult> getPlateMapping(String platePermId);

    /**
     * @return the material to well plate mapping for the given <var>spaceCode</var> and <var>plateCode</var>. Only consider materials of
     *         <var>materialTypeCode</var>. Only fills <var>well_code</var> and <var>material_content_code</var>. Note that this may return more than
     *         one row per well.
     */
    @Select(sql = "select "
            + "      well.code as well_code,"
            + "      well_material.code as material_content_code"
            + " from samples well"
            + "   join samples pl on pl.id = well.samp_id_part_of"
            + "   join spaces sp on pl.space_id = sp.id"
            + "   join sample_properties well_props on well.id = well_props.samp_id"
            + "   join materials well_material on well_props.mate_prop_id = well_material.id"
            + "   join material_types well_material_type on well_material.maty_id = well_material_type.id"
            + " where well_material_type.code = ?{3} and pl.code = ?{2} and sp.code = ?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<WellContentQueryResult> getPlateMappingForMaterialType(String spaceCode,
            String plateCode, String materialTypeCode);

    final String MINIMAL_WELLS_FOR_MATERIAL_ID_QUERY =
            "select "
                    + "     well.code as well_code, "
                    + "     pl.perm_id as plate_perm_id, "
                    + "     exp.perm_id as exp_perm_id, "
                    + "     well_props.mate_prop_id as material_content_id "
                    + "from experiments exp "
                    + "     join samples pl on pl.expe_id = exp.id "
                    + "     join samples well on well.samp_id_part_of = pl.id "
                    + "     join sample_properties well_props on well_props.samp_id = well.id "
                    + "     join materials well_material on well_props.mate_prop_id = well_material.id "
                    + "     join material_types well_material_type on well_material.maty_id = well_material_type.id "
                    + "where pl.samp_id_part_of is null ";

    /**
     * @param materialTypesPattern only materials with a type matching to this pattern will be considered
     * @return the material to well plate mapping for all the wells of the specified experiment.
     */
    @Select(sql = MINIMAL_WELLS_FOR_MATERIAL_ID_QUERY + " and exp.id = any(?{1}) "
            + "      and well_material_type.code similar to ?{2}", parameterBindings =
    { LongArrayMapper.class, TypeMapper.class /* default mapper */}, fetchSize = FETCH_SIZE)
    public List<BasicWellContentQueryResult> getPlateLocationsForExperiment(long[] experimentId,
            String materialTypesPattern);

    /**
     * @return the material to well plate mapping for all the wells of the specified experiment which contain specified material.
     */
    @Select(sql = MINIMAL_WELLS_FOR_MATERIAL_ID_QUERY + " and exp.id = any(?{1}) "
            + "      and well_material.id = ?{2}", parameterBindings =
    { LongArrayMapper.class, TypeMapper.class /* default mapper */}, fetchSize = FETCH_SIZE)
    public List<BasicWellContentQueryResult> getPlateLocationsForExperiment(long[] experimentId,
            long materialId);

    /**
     * @return the material to well plate mapping for the given <var>spaceCode</var> and <var>plateCode</var>. Consider all material types. Only fills
     *         <var>well_code</var>, <var>material_content_code</var> and <var>material_content_code</var>. Note that this may return more than one
     *         row per well.
     */
    @Select(sql = "select "
            + "      well.code as well_code,"
            + "      well_material_type.code as material_content_type_code,"
            + "      well_material.code as material_content_code"
            + " from samples well"
            + "   join samples pl on pl.id = well.samp_id_part_of"
            + "   join spaces sp on pl.space_id = sp.id"
            + "   join sample_properties well_props on well.id = well_props.samp_id"
            + "   join materials well_material on well_props.mate_prop_id = well_material.id"
            + "   join material_types well_material_type on well_material.maty_id = well_material_type.id"
            + " where sp.code = ?{1} and pl.code = ?{2} order by material_content_type_code", fetchSize = FETCH_SIZE)
    public DataIterator<WellContentQueryResult> getPlateMapping(String spaceCode, String plateCode);

    /**
     * Returns the plate geometry string for the plate with given <var>platePermId</var>, or <code>null</code>, if no plate with that perm id can be
     * found.
     */
    @Select(sql = "select space.code as space_code, pl.code as plate_code, cvte.code as plate_geometry "
            + "      from sample_properties sp "
            + "         join samples pl on pl.id = sp.samp_id "
            + "         join controlled_vocabulary_terms cvte on cvte.id = sp.cvte_id "
            + "         join sample_type_property_types stpt on stpt.id = sp.stpt_id "
            + "         join property_types pt on pt.id = stpt.prty_id "
            + "         join spaces space on pl.space_id = space.id"
            + "      where pt.code = 'PLATE_GEOMETRY' "
            + "         and pt.is_managed_internally = true and pl.perm_id = ?{1}")
    public PlateGeometryContainer tryGetPlateGeometry(String platePermId);

    /**
     * Returns the plate geometry string for the plate with given <var>spaceCode</var> and <var>plateCode</var>, or <code>null</code>, if no plate
     * with that code can be found.
     */
    @Select(sql = "select pl.perm_id, cvte.code as plate_geometry "
            + "      from sample_properties sp "
            + "         join samples pl on pl.id = sp.samp_id "
            + "         join controlled_vocabulary_terms cvte on cvte.id = sp.cvte_id "
            + "         join sample_type_property_types stpt on stpt.id = sp.stpt_id "
            + "         join property_types pt on pt.id = stpt.prty_id "
            + "         join spaces space on pl.space_id = space.id"
            + "      where pt.code = 'PLATE_GEOMETRY' "
            + "         and pt.is_managed_internally = true and space.code = ?{1} and pl.code = ?{2}")
    public PlateGeometryContainer tryGetPlateGeometry(String spaceCode, String plateCode);

    /**
     * Returns the ids of materials of specified type and used in specified experiment.
     */
    @Select(sql = "SELECT distinct well_material.id            "
            + "      FROM samples pl                           "
            + "      JOIN samples well ON well.samp_id_part_of = pl.id"
            + "      JOIN sample_properties well_props ON well_props.samp_id = well.id"
            + "      JOIN materials well_material ON well_material.id = well_props.mate_prop_id"
            + "     WHERE pl.expe_id = ?{1}                   "
            + "       AND well_material.maty_id = ?{2}", fetchSize = FETCH_SIZE)
    public DataIterator<Long> getMaterialsForExperimentWells(long experimentId, long materialTypeId);

    static final String EXPERIMENTS_WITH_MATERIAL_SELECT =
            "         select distinct                 "
                    + "      exp.id as exp_id,                        "
                    + "      exp.code as exp_code,                    "
                    + "      exp.perm_id as exp_perm_id,              "
                    + "      exp_type.code as exp_type_code,          "
                    + "      projects.code as proj_code,              "
                    + "      spaces.code as space_code               "
                    + " from materials well_material"
                    + "      join sample_properties well_props on well_props.mate_prop_id = well_material.id "
                    + "      join samples well on well_props.samp_id = well.id"
                    + "      join samples plate on well.samp_id_part_of = plate.id"
                    + "      join experiments exp on plate.expe_id = exp.id"
                    + "      join experiment_types exp_type on exp.exty_id = exp_type.id "
                    + "      join projects on exp.proj_id = projects.id     "
                    + "      join spaces on projects.space_id = spaces.id  ";

    /** @returns experiments in which a given material has been screened */
    @Select(sql = EXPERIMENTS_WITH_MATERIAL_SELECT + " where well_material.id = ?{1}")
    public List<ExperimentReferenceQueryResult> getExperimentsWithMaterial(long materialId);

    /**
     * @returns experiments restricted to the specified project in which a given material has been screened
     */
    @Select(sql = EXPERIMENTS_WITH_MATERIAL_SELECT
            + " where well_material.id = ?{1} and exp.proj_id = ?{2}")
    public List<ExperimentReferenceQueryResult> getExperimentsWithMaterial(long materialId,
            long projectId);

    final static String ANALYSIS_PROCEDURE_SELECT =
            "select distinct "
                    + "       ds_props.value as analysisProcedure, ds_type.code as datasetTypeCode"
                    + "  from experiments exp "
                    + "       join data ds on ds.expe_id = exp.id "
                    + "       join samples plate on plate.id = ds.samp_id "
                    + "       join samples well on well.samp_id_part_of = plate.id "
                    + "       join data_set_types ds_type on ds.dsty_id = ds_type.id "
                    + "       join data_set_type_property_types  dst_pt on dst_pt.dsty_id = ds_type.id "
                    + "       left outer join data_set_properties ds_props on ds_props.ds_id = ds.id and ds_props.dstpt_id = dst_pt.id"
                    + "  where "
                    + "       dst_pt.prty_id = (select id from property_types where code='ANALYSIS_PROCEDURE' and is_managed_internally=true)";

    @Select(sql = ANALYSIS_PROCEDURE_SELECT)
    public List<AnalysisProcedureResult> listAllAnalysisProcedures();

    @Select(sql = ANALYSIS_PROCEDURE_SELECT + " and exp.id = ?{1}")
    public List<AnalysisProcedureResult> listAnalysisProceduresForExperiment(long experimentId);

    final static String SELECT_DSS_CODES_FOR_EXPERIMENT = "  select distinct datastore.code "
            + "      from experiments exp "
            + "           join data dataset on dataset.expe_id = exp.id "
            + "           join data_stores datastore on dataset.dast_id = datastore.id "
            + "      where exp.id = ?{1}";

    @Select(sql = SELECT_DSS_CODES_FOR_EXPERIMENT)
    public List<String> listDataStoreCodesForExperiment(long experimentId);

}
