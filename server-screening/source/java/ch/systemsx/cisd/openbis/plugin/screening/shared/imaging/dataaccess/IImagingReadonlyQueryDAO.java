/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess;

import java.util.List;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.Select;

import ch.systemsx.cisd.common.db.mapper.LongArrayMapper;
import ch.systemsx.cisd.common.db.mapper.StringArrayMapper;
import ch.systemsx.cisd.hcs.Location;

/**
 * Operations on imaging database which are read-only.<br>
 * It is recommended to create one instance of this interface, assign it to a static class field and used everywhere in the application. In this way
 * there will be no problem with closing the database connection.
 * 
 * @author Tomasz Pylak
 */
public interface IImagingReadonlyQueryDAO extends BaseQuery
{
    public static final int FETCH_SIZE = 1000;

    public static final String SQL_HCS_IMAGE =
            "select i.*, ACQUIRED_IMAGES.image_transformer_factory as image_transformer_factory "
                    + "from CHANNEL_STACKS, SPOTS, ACQUIRED_IMAGES, IMAGES as i "
                    + "where                                                                "
                    + "ACQUIRED_IMAGES.CHANNEL_ID = ?{1} and CHANNEL_STACKS.DS_ID = ?{2} and "
                    + "CHANNEL_STACKS.x = ?{3.x} and CHANNEL_STACKS.y = ?{3.y} and "
                    + "SPOTS.x = ?{4.x} and SPOTS.y = ?{4.y} and "
                    // joins
                    + "ACQUIRED_IMAGES.CHANNEL_STACK_ID = CHANNEL_STACKS.ID and "
                    + "CHANNEL_STACKS.SPOT_ID = SPOTS.ID ";

    public static final String SQL_MICROSCOPY_IMAGE =
            "select i.*, ACQUIRED_IMAGES.image_transformer_factory as image_transformer_factory "
                    + "from CHANNEL_STACKS, ACQUIRED_IMAGES, IMAGES as i "
                    + "where                                                                "
                    + "ACQUIRED_IMAGES.CHANNEL_ID = ?{1} and CHANNEL_STACKS.DS_ID = ?{2} and "
                    + "CHANNEL_STACKS.x = ?{3.x} and CHANNEL_STACKS.y = ?{3.y} and "
                    + "CHANNEL_STACKS.spot_id IS NULL and "
                    // joins
                    + "ACQUIRED_IMAGES.CHANNEL_STACK_ID = CHANNEL_STACKS.ID ";

    public static final String SQL_HCS_IMAGE_REPRESENTATIVE =
            "select i.*, ACQUIRED_IMAGES.image_transformer_factory as image_transformer_factory "
                    + "from CHANNEL_STACKS, SPOTS, ACQUIRED_IMAGES, IMAGES as i "
                    + "where                                        "
                    + "CHANNEL_STACKS.is_representative = 'T' and   "
                    + "CHANNEL_STACKS.DS_ID = ?{1} and              "
                    + "ACQUIRED_IMAGES.channel_id = ?{3} and        "
                    + "SPOTS.x = ?{2.x} and SPOTS.y = ?{2.y} and    "
                    // joins
                    + "ACQUIRED_IMAGES.CHANNEL_STACK_ID = CHANNEL_STACKS.ID and "
                    + "CHANNEL_STACKS.SPOT_ID = SPOTS.ID ";

    public static final String SQL_MICROSCOPY_IMAGE_REPRESENTATIVE =
            "select i.*, ACQUIRED_IMAGES.image_transformer_factory as image_transformer_factory "
                    + "from CHANNEL_STACKS, ACQUIRED_IMAGES, IMAGES as i "
                    + "where                                         "
                    + "CHANNEL_STACKS.is_representative = 'T' and    "
                    + "CHANNEL_STACKS.DS_ID = ?{1} and               "
                    + "ACQUIRED_IMAGES.channel_id = ?{2} and         "
                    // joins
                    + "ACQUIRED_IMAGES.CHANNEL_STACK_ID = CHANNEL_STACKS.ID ";

    public static final String SQL_ZOOM_LEVEL_TRANSFORMATIONS_ENRICHED =
            "select izlt.zoom_level_id as zoom_level_id, izlt.channel_id as channel_id, "
                    + "izlt.image_transformation_id as image_transformation_id, ch.code as channel_code, "
                    + "it.code as image_transformation_code, zl.physical_dataset_perm_id as physical_dataset_perm_id "
                    + "from image_zoom_levels zl, image_zoom_level_transformations izlt, image_transformations it, channels ch "
                    + "where                                            "
                    + "izlt.zoom_level_id = zl.id and                   "
                    + "it.id = izlt.image_transformation_id and         "
                    + "ch.id = izlt.channel_id ";

    // TODO 2010-12-10, Tomasz Pylak: uncomment when we are able to show a representative image
    public static final String SQL_NO_MULTIDIMENTIONAL_DATA_COND =
            " order by CHANNEL_STACKS.T_in_SEC, CHANNEL_STACKS.Z_in_M limit 1";

    // " and CHANNEL_STACKS.T_in_SEC IS NULL                        "
    // + " and CHANNEL_STACKS.Z_in_M IS NULL                ";

    public static final String SQL_HCS_WELLS_WITH_IMAGES =
            "select distinct s.*                              "
                    + "from CHANNEL_STACKS ch, SPOTS s, ACQUIRED_IMAGES ai "
                    + "where ch.is_representative = 'T' and ch.DS_ID = ?{1} and "
                    + "      ai.CHANNEL_STACK_ID = ch.ID and ch.SPOT_ID = s.ID";

    // ---------------- HCS ---------------------------------

    /**
     * @return an HCS image for the specified chanel, well and tile. If many images (e.g. for different timepoints or depths) exist, null is returned.
     */
    @Select(SQL_HCS_IMAGE + " and ACQUIRED_IMAGES.IMG_ID = i.ID "
            + SQL_NO_MULTIDIMENTIONAL_DATA_COND)
    public ImgImageDTO tryGetHCSImage(long channelId, long datasetId, Location tileLocation,
            Location wellLocation);

    /**
     * @return a HCS thumbnail for the specified chanel, well and tile. If many images (e.g. for different timepoints or depths) exist, null is
     *         returned.
     */
    @Select(SQL_HCS_IMAGE + " and ACQUIRED_IMAGES.THUMBNAIL_ID = i.ID "
            + SQL_NO_MULTIDIMENTIONAL_DATA_COND)
    public ImgImageDTO tryGetHCSThumbnail(long channelId, long datasetId, Location tileLocation,
            Location wellLocation);

    /**
     * @return an representative HCS image for the specified dataset and well. Returns null if there are no images at all for the specified well.
     */
    @Select(SQL_HCS_IMAGE_REPRESENTATIVE + " and ACQUIRED_IMAGES.IMG_ID = i.ID ")
    public ImgImageDTO tryGetHCSRepresentativeImage(long datasetId, Location wellLocation,
            long channelId);

    /**
     * @return an representative HCS thumbnail for the specified dataset and well. Returns null if there are no images at all for the specified well.
     */
    @Select(SQL_HCS_IMAGE_REPRESENTATIVE + " and ACQUIRED_IMAGES.THUMBNAIL_ID = i.ID ")
    public ImgImageDTO tryGetHCSRepresentativeThumbnail(long datasetId, Location wellLocation,
            long channelId);

    /**
     * @return all images of a given dataset and channel, every image is enriched with the information about the spot to which it belongs.
     */
    @Select("select i.*, ai.image_transformer_factory as image_transformer_factory, "
            + "s.id as spot_id, ai.id as acquired_image_id              "
            + " from image_data_sets d                                       "
            + " join channel_stacks cs on cs.ds_id = d.id              "
            + " join spots s on cs.spot_id = s.id                      "
            + " join acquired_images ai on ai.channel_stack_id = cs.id "
            + " join channels ch on ai.channel_id = ch.id              "
            + " join images i on i.id = ai.img_id                      "
            + " where d.perm_id = ?{1} and ch.code = upper(?{2}) and s.id is NOT NULL")
    public List<ImgImageEnrichedDTO> listHCSImages(String datasetPermId, String channelCode);

    /**
     * @return list of wells for which there are any thumbnail or original images (any tile, any channel).
     */
    @Select(SQL_HCS_WELLS_WITH_IMAGES)
    public List<ImgSpotDTO> listWellsWithAnyImages(long datasetId);

    /** @return list of wells for which there are any thumbnail images (any tile, any channel). */
    @Select(SQL_HCS_WELLS_WITH_IMAGES + " and ai.thumbnail_id is not null")
    public List<ImgSpotDTO> listWellsWithAnyThumbnails(long datasetId);

    /** @return list of wells for which there are any original images (any tile, any channel). */
    @Select(SQL_HCS_WELLS_WITH_IMAGES + " and ai.img_id is not null")
    public List<ImgSpotDTO> listWellsWithAnyOriginalImages(long datasetId);

    // ---------------- Microscopy ---------------------------------

    /**
     * @return an microscopy image for the specified channel and tile. If many images (e.g. for different timepoints or depths) exist, null is
     *         returned.
     */
    @Select(SQL_MICROSCOPY_IMAGE + " and ACQUIRED_IMAGES.IMG_ID = i.ID "
            + SQL_NO_MULTIDIMENTIONAL_DATA_COND)
    public ImgImageDTO tryGetMicroscopyImage(long channelId, long datasetId, Location tileLocation);

    /**
     * @return a microscopy thumbnail for the specified channel and tile. If many images (e.g. for different timepoints or depths) exist, null is
     *         returned.
     */
    @Select(SQL_MICROSCOPY_IMAGE + " and ACQUIRED_IMAGES.THUMBNAIL_ID = i.ID "
            + SQL_NO_MULTIDIMENTIONAL_DATA_COND)
    public ImgImageDTO tryGetMicroscopyThumbnail(long channelId, long datasetId,
            Location tileLocation);

    /**
     * @return an representative microscopy image for the specified dataset.
     */
    @Select(SQL_MICROSCOPY_IMAGE_REPRESENTATIVE + " and ACQUIRED_IMAGES.IMG_ID = i.ID ")
    public ImgImageDTO tryGetMicroscopyRepresentativeImage(long datasetId, long channelId);

    /**
     * @return an representative microscopy thumbnail for the specified dataset. Can be null if thumbnail has not been generated.
     */
    @Select(SQL_MICROSCOPY_IMAGE_REPRESENTATIVE + " and ACQUIRED_IMAGES.THUMBNAIL_ID = i.ID ")
    public ImgImageDTO tryGetMicroscopyRepresentativeThumbnail(long datasetId, long channelId);

    // ---------------- Microscopy - channels ---------------------------------

    @Select("select cs.* from CHANNEL_STACKS cs               "
            + "where cs.ds_id = ?{1} and cs.spot_id is NULL")
    public List<ImgChannelStackDTO> listSpotlessChannelStacks(long datasetId);

    // ---------------- Generic ---------------------------------

    /** @return an image for the specified channel and channel stack or null */
    @Select("select i.*, ACQUIRED_IMAGES.image_transformer_factory as image_transformer_factory "
            + "from IMAGES as i                                      "
            + "join ACQUIRED_IMAGES on ACQUIRED_IMAGES.IMG_ID = i.ID "
            + "join CHANNEL_STACKS on ACQUIRED_IMAGES.CHANNEL_STACK_ID = CHANNEL_STACKS.ID "
            + "where                                                                "
            + "ACQUIRED_IMAGES.CHANNEL_ID = ?{1} and CHANNEL_STACKS.ID = ?{2} and "
            + "CHANNEL_STACKS.DS_ID = ?{3}")
    // The condition on dataset equality is just to ensure that the stack channel belongs the right
    // dataset.
    public ImgImageDTO tryGetImage(long channelId, Long channelStackId, long datasetId);

    /** @return a thumbnail for the specified chanel and channel stack or null */
    @Select("select i.*, ACQUIRED_IMAGES.image_transformer_factory as image_transformer_factory "
            + "from IMAGES as i                                      "
            + "join ACQUIRED_IMAGES on ACQUIRED_IMAGES.THUMBNAIL_ID = i.ID "
            + "join CHANNEL_STACKS on ACQUIRED_IMAGES.CHANNEL_STACK_ID = CHANNEL_STACKS.ID "
            + "where                                                                "
            + "ACQUIRED_IMAGES.CHANNEL_ID = ?{1} and CHANNEL_STACKS.ID = ?{2} and "
            + "CHANNEL_STACKS.DS_ID = ?{3}")
    // The condition on dataset equality is just to ensure that the stack channel belongs the right
    // dataset.
    public ImgImageDTO tryGetThumbnail(long channelId, Long channelStackId, long datasetId);

    @Select("select * from ACQUIRED_IMAGES where CHANNEL_ID in (select ID from CHANNELS where DS_ID = (SELECT ID FROM IMAGE_DATA_SETS WHERE PERM_ID = ?{1}))"
            + "or CHANNEL_STACK_ID in (select id from CHANNEL_STACKS where DS_ID = (SELECT ID FROM IMAGE_DATA_SETS WHERE PERM_ID = ?{1}))")
    // lists all acquired images available for given dataset
    public List<ImgAcquiredImageDTO> listAllAcquiredImagesForDataSet(String permId);

    @Select("select * from ACQUIRED_IMAGES where CHANNEL_ID in (select ID from CHANNELS where DS_ID = ?{1})"
            + " or CHANNEL_STACK_ID in (select id from CHANNEL_STACKS where DS_ID = ?{1})")
    // lists all acquired images available for given dataset
    public List<ImgAcquiredImageDTO> listAllAcquiredImagesForDataSet(long datasetId);

    @Select("select ai.ID as ID, ai.IMAGE_TRANSFORMER_FACTORY as IMAGE_TRANSFORMER_FACTORY, ai.IMG_ID as IMG_ID, ai.CHANNEL_STACK_ID as CHANNEL_STACK_ID, ai.CHANNEL_ID as CHANNEL_ID, "
            + "  s.X as SPOT_X, s.Y as SPOT_Y, i.path as IMAGE_PATH, i.IMAGE_ID as IMAGE_ID, i.color as IMAGE_COLOR, "
            + "  th.path as THUMBNAIL_PATH, th.IMAGE_ID as THUMBNAIL_IMAGE_ID, th.color as THUMBNAIL_COLOR, "
            + "  ch.code as CHANNEL_CODE, cs.X as X, cs.Y as Y, cs.Z_in_M as Z_in_M, cs.T_in_SEC as T_in_SEC, cs.SERIES_NUMBER as SERIES_NUMBER "
            + "from acquired_images ai "
            + "  join images i on ai.img_id = i.id "
            + "  left outer join images th on ai.thumbnail_id = th.id "
            + "  left outer join channels ch on ai.channel_id = ch.id "
            + "  left outer join channel_stacks cs on ai.channel_stack_id = cs.id "
            + "  left outer join spots s on cs.spot_id = s.id "
            + "where ch.ds_id = ?{1} or cs.ds_id = ?{1}")
    public List<ImgAcquiredImageEnrichedDTO> listAllEnrichedAcquiredImagesForDataSet(long datasetId);

    // simple getters

    @Select("select * from IMAGE_DATA_SETS where PERM_ID = ?{1}")
    public ImgImageDatasetDTO tryGetImageDatasetByPermId(String datasetPermId);

    @Select("select * from IMAGE_DATA_SETS where ID = ?{1}")
    public ImgImageDatasetDTO tryGetImageDatasetById(long datasetId);

    @Select("select id from IMAGE_DATA_SETS where ID > ?{1} order by id limit 1")
    public Long tryGetNextDatasetId(long datasetId);

    @Select("select count(*) > 0 from channels ch, image_transformations it where ch.ds_id = ?{1} and it.channel_id = ch.id and it.code = upper(?{2})")
    public boolean hasDatasetDefinedTransformation(long datasetId, String transformationCode);

    @Select("select * from ANALYSIS_DATA_SETS where PERM_ID = ?{1}")
    public ImgAnalysisDatasetDTO tryGetAnalysisDatasetByPermId(String datasetPermId);

    @Select(sql = "select * from IMAGE_DATA_SETS where PERM_ID = any(?{1})", parameterBindings =
    { StringArrayMapper.class }, fetchSize = FETCH_SIZE)
    public List<ImgImageDatasetDTO> listImageDatasetsByPermId(String... datasetPermIds);

    @Select(sql = "select * from IMAGE_ZOOM_LEVELS zoom where zoom.CONTAINER_DATASET_ID = ?{1}", fetchSize = FETCH_SIZE)
    public List<ImgImageZoomLevelDTO> listImageZoomLevels(long datasetId);

    @Select(sql = "select * from IMAGE_ZOOM_LEVELS zoom where zoom.CONTAINER_DATASET_ID = ?{1} "
            + "and ID not in (select ZOOM_LEVEL_ID from IMAGE_ZOOM_LEVEL_TRANSFORMATIONS)", fetchSize = FETCH_SIZE)
    public List<ImgImageZoomLevelDTO> listImageZoomLevelsWithNoTransformations(long datasetId);

    @Select(sql = SQL_ZOOM_LEVEL_TRANSFORMATIONS_ENRICHED + " and zl.container_dataset_id = ?{1}", fetchSize = FETCH_SIZE)
    public List<ImgImageZoomLevelTransformationEnrichedDTO> listImageZoomLevelTransformations(
            long datasetId);

    @Select(sql = SQL_ZOOM_LEVEL_TRANSFORMATIONS_ENRICHED
            + " and zl.container_dataset_id = ?{1} and izlt.channel_id = ?{2} and it.code = ?{3}", fetchSize = FETCH_SIZE)
    public List<ImgImageZoomLevelTransformationEnrichedDTO> findImageZoomLevelTransformations(
            long datasetId, long channelId, String transformationCode);

    @Select(sql = "  select * from IMAGE_ZOOM_LEVELS zoom where zoom.physical_dataset_perm_id = ?{1} and zoom.is_original "
            + "UNION ALL"
            + "  select zoom.* from IMAGE_ZOOM_LEVELS zoom left join image_data_sets data on data.id = zoom.container_dataset_id"
            + "     where data.perm_id = ?{1} and zoom.is_original")
    public List<ImgImageZoomLevelDTO> listOriginalImageZoomLevelsByPermId(String datasetPermId);

    @Select(sql = "select * from IMAGE_ZOOM_LEVELS zoom where zoom.physical_dataset_perm_id = ?{1} and not zoom.is_original "
            + "UNION ALL"
            + "  select zoom.* from IMAGE_ZOOM_LEVELS zoom left join image_data_sets data on data.id = zoom.container_dataset_id"
            + "     where data.perm_id = ?{1} and not zoom.is_original")
    public List<ImgImageZoomLevelDTO> listThumbImageZoomLevelsByPermId(String datasetPermId);

    @Select(sql = "select * from ANALYSIS_DATA_SETS where PERM_ID = any(?{1})", parameterBindings =
    { StringArrayMapper.class }, fetchSize = FETCH_SIZE)
    public List<ImgAnalysisDatasetDTO> listAnalysisDatasetsByPermId(String... datasetPermIds);

    // ------------ dataset and experiment channels

    @Select("select * from CHANNELS where DS_ID = ?{1} order by ID")
    public List<ImgChannelDTO> getChannelsByDatasetId(long datasetId);

    @Select(sql = "select * from CHANNELS where EXP_ID = ?{1} order by ID", fetchSize = FETCH_SIZE)
    public List<ImgChannelDTO> getChannelsByExperimentId(long experimentId);

    @Select("select * from CHANNELS where (DS_ID = ?{1}) and CODE = upper(?{2})")
    public ImgChannelDTO tryGetChannelForDataset(long datasetId, String channelCode);

    @Select("select * from CHANNELS where (EXP_ID = ?{1}) and CODE = upper(?{2})")
    public ImgChannelDTO tryGetChannelForExperiment(long experimentId, String channelCode);

    @Select("select id from channels where ds_id = ?{1} and code = upper(?{2})")
    public long getDatasetChannelId(long datasetId, String channelCode);

    @Select("select id from channels where exp_id = ?{1} and code = upper(?{2})")
    public long getExperimentChannelId(long experimentId, String channelCode);

    @Select("select count(*) > 0 from CHANNELS ch "
            + "join IMAGE_DATA_SETS d on ch.ds_id = d.id where d.PERM_ID = ?{1}")
    public boolean hasDatasetChannels(String datasetPermId);

    @Select("select * from channels where code = ?{2} and "
            + "exp_id in (select id from experiments where perm_id = ?{1})")
    public ImgChannelDTO tryGetChannelForExperimentPermId(String experimentPermId,
            String channelCode);

    // ----------- image transformations

    @Select("select * from IMAGE_TRANSFORMATIONS tr where tr.channel_id = ?{1} order by tr.ID")
    public List<ImgImageTransformationDTO> listImageTransformations(long channelId);

    @Select("select id from image_transformations where channel_id = ?{1} and code = ?{2}")
    public Long tryGetImageTransformationId(long channelId, String transformationCode);

    @Select("select * from image_transformations where channel_id = ?{1} and code = ?{2}")
    public ImgImageTransformationDTO tryGetImageTransformation(long channelId,
            String transformationCode);

    @Select("select tr.* from IMAGE_TRANSFORMATIONS tr                       "
            + " join channels ch on tr.channel_id = ch.id                    "
            + " where ch.ds_id = ?{1} order by tr.ID                        ")
    public List<ImgImageTransformationDTO> listImageTransformationsByDatasetId(long datasetId);

    @Select("select tr.* from IMAGE_TRANSFORMATIONS tr                       "
            + " join channels ch on tr.channel_id = ch.id                    "
            + " where ch.exp_id = ?{1} order by tr.ID                        ")
    public List<ImgImageTransformationDTO> listImageTransformationsByExperimentId(long experimentId);

    // ---------------- HCS - experiments, containers ---------------------------------

    @Select("select * from EXPERIMENTS where PERM_ID = ?{1}")
    public ImgExperimentDTO tryGetExperimentByPermId(String experimentPermId);

    @Select("select * from EXPERIMENTS where ID = ?{1}")
    public ImgExperimentDTO tryGetExperimentById(long experimentId);

    @Select("select ID from CONTAINERS where PERM_ID = ?{1}")
    public Long tryGetContainerIdPermId(String containerPermId);

    @Select("select * from CONTAINERS where ID = ?{1}")
    public ImgContainerDTO getContainerById(long containerId);

    @Select(sql = "select * from CONTAINERS where ID = any(?{1})", parameterBindings =
    { LongArrayMapper.class }, fetchSize = FETCH_SIZE)
    public List<ImgContainerDTO> listContainersByIds(long... containerIds);

    // join with container is needed to use spots index
    @Select("select cs.* from CHANNEL_STACKS cs               "
            + "join SPOTS s on s.id = cs.spot_id              "
            + "join CONTAINERS c on c.id = s.cont_id          "
            + "where cs.ds_id = ?{1} and s.x = ?{2} and s.y = ?{3}")
    public List<ImgChannelStackDTO> listChannelStacks(long datasetId, int spotX, int spotY);

    @Select("select cs.* from CHANNEL_STACKS cs               "
            + "join SPOTS s on s.id = cs.spot_id              "
            + "join CONTAINERS c on c.id = s.cont_id          "
            + "where cs.ds_id = ?{1} and s.id = ?{2}")
    public List<ImgChannelStackDTO> listChannelStacks(long datasetId, long spotId);

    @Select("select cs.* from CHANNEL_STACKS cs               "
            + "join SPOTS s on s.id = cs.spot_id              "
            + "join CONTAINERS c on c.id = s.cont_id          "
            + "where cs.ds_id = ?{1} and s.id IS NULL")
    public List<ImgChannelStackDTO> listChannelStacks(long datasetId);

    @Select("select * from SPOTS where cont_id = ?{1}")
    public List<ImgSpotDTO> listSpots(long contId);

    // ---------------- HCS - feature vectors ---------------------------------

    @Select(sql = "select * from FEATURE_DEFS where DS_ID = any(?{1}) ORDER BY ds_id ASC, id ASC", parameterBindings =
    { LongArrayMapper.class }, fetchSize = FETCH_SIZE)
    public List<ImgFeatureDefDTO> listFeatureDefsByDataSetIds(long... dataSetIds);

    @Select(sql = "select t.*, fd.ds_id as DS_ID from FEATURE_VOCABULARY_TERMS t      "
            + "join FEATURE_DEFS fd on fd.id = t.fd_id                                "
            + "where fd.DS_ID = any(?{1}) ORDER BY fd.ds_id ASC, fd.id ASC            ", parameterBindings =
    { LongArrayMapper.class }, fetchSize = FETCH_SIZE)
    public List<ImgFeatureVocabularyTermDTO> listFeatureVocabularyTermsByDataSetId(
            long... dataSetIds);

    @Select(sql = "select * from FEATURE_VALUES where FD_ID = any(?{1}) order by T_in_SEC, Z_in_M", parameterBindings =
    { LongArrayMapper.class }, fetchSize = FETCH_SIZE, resultSetBinding = FeatureVectorDataObjectBinding.class)
    public List<ImgFeatureValuesDTO> getFeatureValues(long... featureDefIds);

    // ------------------ Experiment metadata queries ------------------------

    final static String SELECT_PLATE_GEOMETRIES_FOR_EXPERIMENT =
            " select distinct container.spots_width as width, container.spots_height as height "
                    + "      from experiments exp "
                    + "           join containers container on container.expe_id = exp.id "
                    + "      where exp.id = ?{1}";

    @Select(sql = SELECT_PLATE_GEOMETRIES_FOR_EXPERIMENT)
    public List<WidthAndHeightDTO> listPlateGeometriesForExperiment(long experimentId);

    final static String SELECT_TILE_GEOMETRIES_FOR_EXPERIMENT =
            " select distinct dataset.fields_width as width, dataset.fields_height as height "
                    + "      from experiments exp "
                    + "           join containers container on container.expe_id = exp.id "
                    + "           join image_data_sets dataset on dataset.cont_id = container.id "
                    + "      where exp.id = ?{1}";

    @Select(sql = SELECT_TILE_GEOMETRIES_FOR_EXPERIMENT)
    public List<WidthAndHeightDTO> listTileGeometriesForExperiment(long experimentId);

    final static String SELECT_IMAGE_SIZES_FOR_EXPERIMENT =
            " select dataset.perm_id, level.width, level.height "
                    + "      from experiments exp "
                    + "           join containers container on container.expe_id = exp.id "
                    + "           join image_data_sets dataset on dataset.cont_id = container.id "
                    + "           join image_zoom_levels level on level.container_dataset_id = dataset.id "
                    + "      where exp.id = ?{1} and level.is_original = ?{2}";

    @Select(sql = SELECT_IMAGE_SIZES_FOR_EXPERIMENT)
    public List<WidthAndHeightAndPermIdDTO> listImageSizesForExperiment(long experimentId,
            boolean original);

}
