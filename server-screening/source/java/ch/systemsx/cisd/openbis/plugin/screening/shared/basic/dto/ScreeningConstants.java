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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

/**
 * Constants which are specific for screening. Here some assumptions are made about:
 * <ul>
 * <li>initial database content (metadata) of each screening module</li>
 * <li>mandatory part of datastore server configuration</li>
 * </ul>
 * 
 * @author Tomasz Pylak
 */
public class ScreeningConstants
{
    public static final String TECHNOLOGY_NAME = "screening";

    /** Name of the directory inside the dataset where files in original form are stored. */
    public static final String ORIGINAL_DATA_DIR = "original";

    /**
     * Name of the data source (configured in service.properties) which allows to access imaging db.
     */
    public static final String IMAGING_DATA_SOURCE = "imaging-db";

    // ---- required entity type patterns -----------

    /**
     * All image datasets (both hcs and microscopy) which contain this marker in the dataset type code are considered to be container datasets with
     * original physical dataset inside. Optionally the container can contain thumbnail datasets.
     */
    public static final String IMAGE_CONTAINER_DATASET_TYPE_MARKER = "_CONTAINER";

    /**
     * All image datasets (both hcs and microscopy) which contain this marker in the dataset type code are considered to be thumbnail datasets.
     */
    public static final String IMAGE_THUMBNAIL_DATASET_TYPE_MARKER = "_OVERVIEW";

    // --- HCS dataset types

    /** Prefix of a dataset's type which stores hcs data. */
    public static final String HCS_DATASET_TYPE_PREFIX = "HCS_";

    /** Prefix of a type of the dataset which stores any plate images. */
    public static final String HCS_IMAGE_DATASET_TYPE_PREFIX = "HCS_IMAGE";

    /** Type of the dataset which stores plate image overlays. */
    public static final String HCS_SEGMENTATION_IMAGE_DATASET_TYPE_PATTERN =
            (HCS_IMAGE_DATASET_TYPE_PREFIX + ".*OVERLAY.*") + "|" // legacy
                    + (HCS_IMAGE_DATASET_TYPE_PREFIX + ".*_SEGMENTATION.*");

    /** Type of the dataset which stores raw plate images. */
    public static final String HCS_RAW_IMAGE_DATASET_TYPE_PATTERN = HCS_IMAGE_DATASET_TYPE_PREFIX
            + ".*_RAW.*";

    /** The plain old legacy type for raw image data sets. */
    public static final String HCS_RAW_IMAGE_LEGACY_DATASET_TYPE = HCS_IMAGE_DATASET_TYPE_PREFIX;

    /**
     * Type of the dataset which stores plate images (raw, overvies or overlays). We do not want old analysis data to match to this pattern.
     */
    public static final String ANY_HCS_IMAGE_DATASET_TYPE_PATTERN = HCS_IMAGE_DATASET_TYPE_PREFIX
            + "($|[^_].*|_[^A].*|_A[^N].*|_AN[^A].*)";

    /** Prefix for types of the dataset which stores image analysis data. */
    public static final String HCS_ANALYSIS_DATASET_TYPE_PREFIX = "HCS_ANALYSIS_WELL";

    /** Prefix for types of the dataset which stores image analysis data. */
    public static final String HCS_ANALYSIS_CONTAINER_DATASET_TYPE_PREFIX =
            "HCS_ANALYSIS_CONTAINER_WELL";

    /** Prefix for types of the dataset which stores image analysis data. */
    public static final String HCS_ANALYSIS_ANY_CONTAINER_DATASET_TYPE_PREFIX =
            "HCS_ANALYSIS_CONTAINER";

    /** Type of the dataset which stores image analysis data, there should be at most one. */
    public static final String HCS_IMAGE_ANALYSIS_DATASET_TYPE_PATTERN = "HCS_IMAGE_ANALYSIS_DATA|" // legacy
            + (HCS_ANALYSIS_DATASET_TYPE_PREFIX + ".*")
            + "|"
            + HCS_ANALYSIS_CONTAINER_DATASET_TYPE_PREFIX + ".*";

    // --- HCS sample types

    public static final String HCS_PLATE_SAMPLE_TYPE_PATTERN = "PLATE.*";

    /** the non-control well has to have a type code which contains this string. */
    public static final String NON_CONTROL_WELL_SAMPLE_TYPE_PATTERN =
            ".*WELL.*|.*CHAMBER.*|OLIGO|GENE";

    /** the well is considered to be a control well if its type code contains this string. */
    public static final String CONTROL_WELL_SAMPLE_TYPE_PATTERN = ".*CONTROL.*";

    // --- HCS experiment types

    public static final String HCS_EXPERIMENT_TYPE_PATTERN = ".*HCS.*";

    // --- Microscopy

    // --- Microscopy dataset types

    private static final String MICROSCOPY_IMAGE_TYPE_PATTERN = ".*IMG.*";

    public static final String MICROSCOPY_IMAGE_TYPE_SUBSTRING = "IMG";

    public static final String MICROSCOPY_CONTAINER_TYPE_SUBSTRING =
            MICROSCOPY_IMAGE_TYPE_SUBSTRING + "_CONTAINER";

    public static final String MICROSCOPY_THUMBNAIL_TYPE_SUBSTRING =
            MICROSCOPY_IMAGE_TYPE_SUBSTRING + "_OVERVIEW";

    /** type of the dataset which stores microscopy images. */
    public static final String ANY_MICROSCOPY_IMAGE_DATASET_TYPE_PATTERN = "MICROSCOPY_IMAGE|"// legacy
            + MICROSCOPY_IMAGE_TYPE_PATTERN;

    /** type of the dataset which stores image overlays. */
    public static final String MICROSCOPY_SEGMENTATION_IMAGE_DATASET_TYPE_PATTERN =
            MICROSCOPY_IMAGE_TYPE_PATTERN + ".*OVERLAY.*|" + // legacy
                    MICROSCOPY_IMAGE_TYPE_PATTERN + ".*SEGMENTATION.*";

    // --- Microscopy sample types

    /** The sample is considered to be a microscopy sample if its type code contains this string. */
    public static final String MICROSCOPY_IMAGE_SAMPLE_TYPE_PATTERN = MICROSCOPY_IMAGE_TYPE_PATTERN;

    // --- Default dataset type codes for screening datasets

    /** type of the image container dataset type for raw images */
    public static final String DEFAULT_RAW_IMAGE_CONTAINER_DATASET_TYPE = "HCS_IMAGE_CONTAINER_RAW";

    /** type of the raw image physical dataset */
    public static final String DEFAULT_RAW_IMAGE_DATASET_TYPE = "HCS_IMAGE_RAW";

    /** type of the image segmentation (overlay) physical dataset */
    public static final String DEFAULT_SEGMENTATION_IMAGE_CONTAINER_DATASET_TYPE =
            "HCS_IMAGE_CONTAINER_SEGMENTATION";

    /** type of the image segmentation (overlay) physical dataset */
    public static final String DEFAULT_SEGMENTATION_IMAGE_DATASET_TYPE = "HCS_IMAGE_SEGMENTATION";

    /**
     * type of the overview (aka thumbnail) image physical dataset. Used for both raw and overview datasets.
     */
    public static final String DEFAULT_OVERVIEW_IMAGE_DATASET_TYPE = "HCS_IMAGE_OVERVIEW";

    public static final String HCS_ANALYSIS_PREFIX = "HCS_ANALYSIS";

    /** type of the new analysis dataset */
    public static final String DEFAULT_ANALYSIS_WELL_DATASET_TYPE = "HCS_ANALYSIS_WELL_FEATURES";

    /**
     * The type containing the single feature grouping
     */
    public static final String ANALYSIS_FEATURE_LIST = "HCS_ANALYSIS_FEATURES_LIST";

    /**
     * The name of the directory in HCS_ANALYSIS_FEATURES_LIST dataset. (so that in container it is parallel to 'original' directory)
     */
    public static final String ANALYSIS_FEATURE_LIST_TOP_LEVEL_DIRECTORY_NAME = "feature_lists";

    /**
     * type of the analysis container dataset
     */
    public static final String DEFAULT_ANALYSIS_WELL_CONTAINER_DATASET_TYPE =
            "HCS_ANALYSIS_CONTAINER_WELL_FEATURES";

    /** unknown file format code */
    public static final String UNKNOWN_FILE_FORMAT = "UNKNOWN";

    /** Default file format of thumbnail datasets. */
    public static final String DEFAULT_OVERVIEW_IMAGE_DATASET_FILE_FORMAT = "PNG";

    // ----

    /** Code of plate geometry vocabulary. */
    public static final String PLATE_GEOMETRY = "$PLATE_GEOMETRY";

    /** Used to import Qiagen siRNA libraries. */
    public static final String LIBRARY_PLUGIN_TYPE_CODE = "LIBRARY";

    public static final String ANALYSIS_PROCEDURE = "$ANALYSIS_PROCEDURE";

    public static final String RESOLUTION = "$RESOLUTION";

    public static final String ANALYSIS_PROCEDURE_PROPERTY = "ANALYSIS_PROCEDURE";

    public static final String FEATURE_LISTS_AGGREGATION_SERVICE_KEY =
            "feature-lists-aggregation-service";

    // --- !!!!!! It's discouraged to use this constant, try hard not to do that !!!!!!!! ---

    /** It's discouraged to use this constant, use {@link #HCS_PLATE_SAMPLE_TYPE_PATTERN} instead. */
    public static final String DEFAULT_PLATE_SAMPLE_TYPE_CODE = "PLATE";

    /** It's discouraged to use this constant. Code of the gene material type */
    public static final String GENE_PLUGIN_TYPE_CODE = "GENE";

    /** It's discouraged to use this constant. Code of the siRNA material type. */
    public static final String SIRNA_PLUGIN_TYPE_NAME = "SIRNA";

    public static final String SIRNA_WELL_TYPE_CODE = "SIRNA_WELL";

    /** It's discouraged to use this constant. */
    public static final String GENE_SYMBOLS = "GENE_SYMBOLS";

    /** It's discouraged to use this constant. */
    public static final String MERGED_CHANNELS = "Merged Channels";

    // ---- required DSS servlets

    /** path to the datastore screening servlet able to display images in different channels */
    public static final String DATASTORE_SCREENING_SERVLET_URL = "datastore_server_screening";

    public static final String IMAGE_VIEWER_LAUNCH_URL = "image-viewer-launch";

    public static class ImageServletUrlParameters
    {
        // -- optional servlet parameters

        public final static String CHANNEL_PARAM = "channel";

        // allows to merge all channels of the basic dataset without specifying all of them
        public final static String MERGE_CHANNELS_PARAM = "mergeChannels";

        public final static String OVERLAY_CHANNEL_PREFIX_PARAM = "overlayChannel-";

        public final static String CHANNEL_STACK_ID_PARAM = "channelStackId";

        public final static String WELL_ROW_PARAM = "wellRow";

        public final static String WELL_COLUMN_PARAM = "wellCol";

        public final static String SINGLE_CHANNEL_TRANSFORMATION_CODE_PARAM = "transformation";

        // -- mandatory servlet parameters

        public final static String DATASET_CODE_PARAM = "dataset";

        public final static String TILE_ROW_PARAM = "tileRow";

        public final static String TILE_COL_PARAM = "tileCol";
    }

    public static final String USER_DEFINED_RESCALING_CODE = "$USER_DEFINED_RESCALING$";
}
