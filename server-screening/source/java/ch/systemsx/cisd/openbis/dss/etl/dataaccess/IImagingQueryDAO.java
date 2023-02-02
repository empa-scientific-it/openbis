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

package ch.systemsx.cisd.openbis.dss.etl.dataaccess;

import java.util.Collection;
import java.util.List;

import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.TransactionQuery;
import net.lemnik.eodsql.Update;

import ch.systemsx.cisd.common.db.mapper.ByteArrayMapper;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgAcquiredImageDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgAnalysisDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelStackDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureValuesDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureVocabularyTermDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageTransformationDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageZoomLevelDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgSpotDTO;

/**
 * Operations on imaging database which are non-read-only.
 * 
 * @author Tomasz Pylak
 */
public interface IImagingQueryDAO extends TransactionQuery, IImagingReadonlyQueryDAO
{
    // generate ids

    @Select("select nextval('images_id_seq')")
    public long createImageId();

    @Select("select nextval('channel_stacks_id_seq')")
    public long createChannelStackId();

    // batch updates

    @Update(sql = "insert into CHANNEL_STACKS (ID, X, Y, Z_in_M, T_in_SEC, SERIES_NUMBER, IS_REPRESENTATIVE, DS_ID, SPOT_ID) values "
            + "(?{1.id}, ?{1.column}, ?{1.row}, ?{1.z}, ?{1.t}, ?{1.seriesNumber}, ?{1.isRepresentative}, ?{1.datasetId}, ?{1.spotId})", batchUpdate = true)
    public void addChannelStacks(Collection<ImgChannelStackDTO> channelStacks);

    @Update(sql = "insert into IMAGES (ID, PATH, IMAGE_ID, COLOR) values "
            + "(?{1.id}, ?{1.filePath}, ?{1.imageID}, ?{1.colorComponentAsString})", batchUpdate = true)
    public void addImages(List<ImgImageDTO> images);

    @Update(sql = "insert into ACQUIRED_IMAGES (IMG_ID, THUMBNAIL_ID, CHANNEL_STACK_ID, CHANNEL_ID, IMAGE_TRANSFORMER_FACTORY) values "
            + "(?{1.imageId}, ?{1.thumbnailId}, ?{1.channelStackId}, ?{1.channelId}, ?{1.serializedImageTransformerFactory})", batchUpdate = true)
    public void addAcquiredImages(List<ImgAcquiredImageDTO> acquiredImages);

    @Update(sql = "update ACQUIRED_IMAGES set THUMBNAIL_ID = ?{1.thumbnailId} where ID = ?{1.id}", batchUpdate = true)
    public void updateAcquiredImagesThumbnails(List<ImgAcquiredImageDTO> acquiredImages);

    // inserts

    @Select("insert into EXPERIMENTS (PERM_ID) values (?{1}) returning ID")
    public long addExperiment(String experimentPermId);

    @Select("insert into CHANNELS (CODE, LABEL, DESCRIPTION, WAVELENGTH, DS_ID, EXP_ID, "
            + "RED_CC, GREEN_CC, BLUE_CC) values "
            + "(?{1.code}, ?{1.label}, ?{1.description}, ?{1.wavelength}, ?{1.datasetId}, ?{1.experimentId}, "
            + "?{1.redColorComponent}, ?{1.greenColorComponent}, ?{1.blueColorComponent}) returning ID")
    public long addChannel(ImgChannelDTO channel);

    @Select("insert into CONTAINERS (PERM_ID, SPOTS_WIDTH, SPOTS_HEIGHT, EXPE_ID) values "
            + "(?{1.permId}, ?{1.numberOfColumns}, ?{1.numberOfRows}, ?{1.experimentId}) returning ID")
    public long addContainer(ImgContainerDTO container);

    @Select("insert into IMAGE_DATA_SETS (PERM_ID, FIELDS_WIDTH, FIELDS_HEIGHT, "
            + "CONT_ID, IS_MULTIDIMENSIONAL, "
            + "IMAGE_LIBRARY_NAME, IMAGE_LIBRARY_READER_NAME) "
            + "values (?{1.permId}, ?{1.fieldNumberOfColumns}, ?{1.fieldNumberOfRows}, "
            + "?{1.containerId}, ?{1.isMultidimensional}, "
            + "?{1.imageLibraryName}, ?{1.imageReaderName}) returning ID")
    public long addImageDataset(ImgImageDatasetDTO dataset);

    @Select("insert into image_zoom_levels (physical_dataset_perm_id, is_original, container_dataset_id, path, width, height, color_depth, file_type)  "
            + "values (?{1.physicalDatasetPermId}, ?{1.isOriginal}, ?{1.containerDatasetId}, ?{1.rootPath}, ?{1.width}, ?{1.height}, ?{colorDepth}, ?{1.fileType}) returning ID")
    public long addImageZoomLevel(ImgImageZoomLevelDTO dataset);

    @Select("insert into image_zoom_level_transformations (zoom_level_id, channel_id, image_transformation_id) "
            + "values (?{1}, ?{2}, (select id from image_transformations where code = ?{3} and channel_id = ?{2})) "
            + "returning ID")
    public long addImageZoomLevelTransformation(long zoomLevelId, long channelId,
            String transformationCode);

    @Select("insert into ANALYSIS_DATA_SETS (PERM_ID, CONT_ID)                     "
            + "values (?{1.permId}, ?{1.containerId}) returning ID")
    public long addAnalysisDataset(ImgAnalysisDatasetDTO dataset);

    @Update(sql = "insert into IMAGE_TRANSFORMATIONS(CODE, LABEL, DESCRIPTION, IS_DEFAULT, IMAGE_TRANSFORMER_FACTORY, IS_EDITABLE, CHANNEL_ID) values "
            + "(?{1.code}, ?{1.label}, ?{1.description}, ?{1.isDefault}, ?{1.serializedImageTransformerFactory}, ?{1.isEditable}, ?{1.channelId})", batchUpdate = true)
    public void addImageTransformations(List<ImgImageTransformationDTO> imageTransformations);

    @Update(sql = "delete from IMAGE_TRANSFORMATIONS where CODE = ?{1} and CHANNEL_ID = ?{2}")
    public void removeImageTransformation(String transformationCode, long channelId);

    @Select("insert into SPOTS (X, Y, CONT_ID) values "
            + "(?{1.column}, ?{1.row}, ?{1.containerId}) returning ID")
    public long addSpot(ImgSpotDTO spot);

    @Select("insert into FEATURE_DEFS (LABEL, CODE, DESCRIPTION, DS_ID) values "
            + "(?{1.label}, ?{1.code}, ?{1.description}, ?{1.dataSetId}) RETURNING ID")
    public long addFeatureDef(ImgFeatureDefDTO featureDef);

    @Update(sql = "insert into FEATURE_VOCABULARY_TERMS (CODE, SEQUENCE_NUMBER, FD_ID) values "
            + "(?{1.code}, ?{1.sequenceNumber}, ?{1.featureDefId})", batchUpdate = true)
    public void addFeatureVocabularyTerms(List<ImgFeatureVocabularyTermDTO> terms);

    @Select(sql = "insert into FEATURE_VALUES (VALUES, Z_in_M, T_in_SEC, FD_ID) values "
            + "(?{1.byteArray}, ?{1.z}, ?{1.t}, ?{1.featureDefId}) RETURNING ID", parameterBindings =
    { ByteArrayMapper.class })
    public long addFeatureValues(ImgFeatureValuesDTO featureValues);

    // updates

    @Update("update CHANNELS "
            + "set DESCRIPTION = ?{1.description}, WAVELENGTH = ?{1.wavelength}, "
            + "RED_CC = ?{1.redColorComponent}, GREEN_CC = ?{1.greenColorComponent}, BLUE_CC = ?{1.blueColorComponent} "
            + "where ID = ?{1.id}")
    public void updateChannel(ImgChannelDTO channel);
}
