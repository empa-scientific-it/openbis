/**
 * =======================================================
 * OpenBIS screening facade internal code (DO NOT USE!!!)
 * =======================================================
 */

if(typeof openbis == 'undefined' || typeof _openbisInternal == 'undefined'){
    alert('Loading of openbis-screening.js failed - openbis.js is missing');
}

var _openbisInternalGeneric = _openbisInternal;

var _openbisInternal = function(openbisUrlOrNull){
    this.init(openbisUrlOrNull);
}

$.extend(_openbisInternal.prototype, _openbisInternalGeneric.prototype);

_openbisInternal.prototype.init = function(openbisUrlOrNull){
    _openbisInternalGeneric.prototype.init.call(this, openbisUrlOrNull);
    this.screeningUrl = this.openbisUrl + "/rmi-screening-api-v1.json"
}

_openbisInternal.prototype.getScreeningDataStoreApiUrlForDataStoreUrl = function(dataStoreUrl){
    return dataStoreUrl + "/rmi-datastore-server-screening-api-v1.json"
}

var _openbisGeneric = openbis;

/**
 * =========================
 * OpenBIS screening facade 
 * =========================
 * 
 * The facade provides access to the following services:
 * 
 * - all services that the generic OpenBIS facade provides (see openbis.js file)
 * - ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer
 * - ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening
 * 
 */

var openbis = function(openbisUrlOrNull){
	this._internal = new _openbisInternal(openbisUrlOrNull);
}

$.extend(openbis.prototype, _openbisGeneric.prototype);

/**
 * ====================================================================================
 * ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer methods
 * ====================================================================================
 */

/**
 * @see IScreeningApiServer.listPlates(String)
 * @method
 */
openbis.prototype.listPlates = function(action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "listPlates",
                    "params" : [ this.getSession() ] },
            success: action
    });
}

/**
 * @see IScreeningApiServer.listPlates(String, ExperimentIdentifier)
 * @method
 */
openbis.prototype.listPlatesForExperiment = function(experimentIdentifier, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "listPlates",
                    "params" : [ this.getSession(), experimentIdentifier ] },
            success: action
    });
}

/**
 * @see IScreeningApiServer.getPlateMetadataList(String, List<? extends PlateIdentifier>)
 * @method
 */
openbis.prototype.getPlateMetadataList = function(plateIdentifiers, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "getPlateMetadataList",
                    "params" : [ this.getSession(), plateIdentifiers ] },
            success: action
    });
}

/**
 * @see IScreeningApiServer.listExperiments(String)
 * @method
 */
openbis.prototype.listAllExperiments = function(action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "listExperiments",
                    "params" : [ this.getSession() ] },
            success: action
    });
}

/**
 * @see IScreeningApiServer.listExperiments(String, String)
 * @method
 */
openbis.prototype.listExperimentsVisibleToUser = function(userId, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "listExperiments",
                    "params" : [ this.getSession(), userId ] },
            success: action
    });
}

/**
 * @see IScreeningApiServer.listFeatureVectorDatasets(String, List<? extends PlateIdentifier>)
 * @method
 */
openbis.prototype.listFeatureVectorDatasets = function(plateIdentifiers, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "listFeatureVectorDatasets",
                    "params" : [ this.getSession(), plateIdentifiers ] },
            success: action
    });
}

/**
 * @see IScreeningApiServer.listImageDatasets(String, List<? extends PlateIdentifier>)
 * @method
 */
openbis.prototype.listImageDatasets = function(plateIdentifiers, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "listImageDatasets",
                    "params" : [ this.getSession(), plateIdentifiers ] },
            success: action
    });
}

/**
 * @see IScreeningApiServer.getImageInfo(String, List<String>)
 * @method
 */
openbis.prototype.getImageInfo = function(dataSetCodes, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "getImageInfo",
                    "params" : [ this.getSession(), dataSetCodes ] },
            success: action
    });
}

/**
 * @see IScreeningApiServer.getImageResolutions(String, List<String>)
 * @method
 */
openbis.prototype.getImageResolutions = function(dataSetCodes, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "getImageResolutions",
                    "params" : [ this.getSession(), dataSetCodes ] },
            success: action
    });
}

/**
 * @see IScreeningApiServer.listRawImageDatasets(String, List<? extends PlateIdentifier>)
 * @method
 */
openbis.prototype.listRawImageDatasets = function(plateIdentifiers, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "listRawImageDatasets",
                    "params" : [ this.getSession(), plateIdentifiers ] },
            success: action
    });
}

/**
 * @see IScreeningApiServer.listSegmentationImageDatasets(String, List<? extends PlateIdentifier>)
 * @method
 */
openbis.prototype.listSegmentationImageDatasets = function(plateIdentifiers, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "listSegmentationImageDatasets",
                    "params" : [ this.getSession(), plateIdentifiers ] },
            success: action
    });
}

/**
 * @see IScreeningApiServer.getDatasetIdentifiers(String, List<String>)
 * @method
 */
openbis.prototype.getDatasetIdentifiers = function(datasetCodes, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "getDatasetIdentifiers",
                    "params" : [ this.getSession(), datasetCodes ] },
            success: action
    });
}

/**
 * @see IScreeningApiServer.listPlateWells(String, ExperimentIdentifier, MaterialIdentifier, boolean)
 * @method
 */
openbis.prototype.listPlateWellsForExperimentAndMaterial = function(experimentIdentifer, materialIdentifier, findDatasets, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "listPlateWells",
                    "params" : [ this.getSession(), experimentIdentifer, materialIdentifier, findDatasets ] },
            success: action
    });
}

/**
 * @see IScreeningApiServer.listPlateWells(String, MaterialIdentifier, boolean)
 * @method
 */
openbis.prototype.listPlateWellsForMaterial = function(materialIdentifier, findDatasets, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "listPlateWells",
                    "params" : [ this.getSession(), materialIdentifier, findDatasets ] },
            success: action
    });
}

/**
 * @see IScreeningApiServer.listPlateWells(String, PlateIdentifier)
 * @method
 */
openbis.prototype.listPlateWells = function(plateIdentifier, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "listPlateWells",
                    "params" : [ this.getSession(), plateIdentifier ] },
            success: action
    });
}

/**
 * @see IScreeningApiServer.getWellSample(String, WellIdentifier)
 * @method
 */
openbis.prototype.getWellSample = function(wellIdentifier, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "getWellSample",
                    "params" : [ this.getSession(), wellIdentifier ] },
            success: action
    });
}

/**
 * @see IScreeningApiServer.getPlateSample(String, PlateIdentifier)
 * @method
 */
openbis.prototype.getPlateSample = function(plateIdentifier, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "getPlateSample",
                    "params" : [ this.getSession(), plateIdentifier ] },
            success: action
    });
}

/**
 * @see IScreeningApiServer.listPlateMaterialMapping(String, List<PlateIdentifier>, MaterialTypeIdentifier)
 * @method
 */
openbis.prototype.listPlateMaterialMapping = function(plateIdentifiers, materialTypeIdentifierOrNull, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "listPlateMaterialMapping",
                    "params" : [ this.getSession(), plateIdentifiers, materialTypeIdentifierOrNull ] },
            success: action
    });
}

/**
 * @see IScreeningApiServer.getExperimentImageMetadata(String, ExperimentIdentifier)
 * @method
 */
openbis.prototype.getExperimentImageMetadata = function(experimentIdentifer, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "getExperimentImageMetadata",
                    "params" : [ this.getSession(), experimentIdentifer ] },
            success: action
    });
}

/**
 * @see IScreeningApiServer.listAvailableFeatureCodes(String, List<? extends IFeatureVectorDatasetIdentifier>)
 * @method
 */
openbis.prototype.listAvailableFeatureCodes = function(featureDatasets, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "listAvailableFeatureCodes",
                    "params" : [ this.getSession(), featureDatasets ] },
            success: action
    });
}

/**
 * @see IScreeningApiServer.listAvailableFeatures(String, List<? extends IFeatureVectorDatasetIdentifier>)
 * @method
 */
openbis.prototype.listAvailableFeatures = function(featureDatasets, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "listAvailableFeatures",
                    "params" : [ this.getSession(), featureDatasets ] },
            success: action
    });
}

/**
 * @see IScreeningApiServer.loadFeatures(String, List<FeatureVectorDatasetReference>, List<String>)
 * @method
 */
openbis.prototype.loadFeatures = function(featureDatasets, featureCodes, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "loadFeatures",
                    "params" : [ this.getSession(), featureDatasets, featureCodes ] },
            success: action
    });
}

/**
 * @see IScreeningApiServer.loadFeaturesForDatasetWellReferences(String, List<FeatureVectorDatasetWellReference>, List<String>)
 * @method
 */
openbis.prototype.loadFeaturesForDatasetWellReferences = function(datasetWellReferences, featureCodes, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "loadFeaturesForDatasetWellReferences",
                    "params" : [ this.getSession(), datasetWellReferences, featureCodes ] },
            success: action
    });
}

/**
 * @see IScreeningApiServer.loadImagesBase64(String, List<PlateImageReference>, boolean)
 * @method
 */
openbis.prototype.loadImagesBase64ForImageReferencesAndImageConversion = function(imageReferences, convertToPng, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "loadImagesBase64",
                    "params" : {
                        "sessionToken" : this.getSession(),
                        "imageReferences" : imageReferences,
                        "convertToPng" : convertToPng
                    }
            },
            success: action
    });
}

/**
 * @see IScreeningApiServer.loadThumbnailImagesBase64(String, List<PlateImageReference>)
 * @method
 */
openbis.prototype.loadThumbnailImagesBase64ForImageReferences = function(imageReferences, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "loadThumbnailImagesBase64",
                    "params" : {
                        "sessionToken" : this.getSession(), 
                        "imageReferences" : imageReferences 
                    } 
            },
            success: action
    });
}

/**
 * @see IScreeningApiServer.loadImagesBase64(String, List<PlateImageReference>, ImageSize)
 * @method
 */
openbis.prototype.loadImagesBase64ForImageReferencesAndImageSize = function(imageReferences, size, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "loadImagesBase64",
                    "params" : {
                        "sessionToken" : this.getSession(), 
                        "imageReferences" : imageReferences,
                        "size" : size 
                    } 
            },
            success: action
    });
}

/**
 * @see IScreeningApiServer.loadImagesBase64(String, List<PlateImageReference>)
 * @method
 */
openbis.prototype.loadImagesBase64ForImageReferences = function(imageReferences, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "loadImagesBase64",
                    "params" : { 
                        "sessionToken" : this.getSession(),
                        "imageReferences" : imageReferences 
                    }
            },
            success: action
    });
}

/**
 * @see IScreeningApiServer.loadImagesBase64(String, List<PlateImageReference>, LoadImageConfiguration)
 * @method
 */
openbis.prototype.loadImagesBase64ForImageReferencesAndImageConfiguration = function(imageReferences, configuration, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "loadImagesBase64",
                    "params" : { 
                        "sessionToken" : this.getSession(), 
                        "imageReferences" : imageReferences, 
                        "configuration" : configuration 
                    } 
            },
            success: action
    });
}

/**
 * @see IScreeningApiServer.loadImagesBase64(String, List<PlateImageReference>, ImageRepresentationFormat)
 * @method
 */
openbis.prototype.loadImagesBase64ForImageReferencesAndImageRepresentationFormat = function(imageReferences, format, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "loadImagesBase64",
                    "params" : {
                        "sessionToken" : this.getSession(), 
                        "imageReferences" : imageReferences, 
                        "format" : format 
                    } 
            },
            success: action
    });
}

/**
 * @see IScreeningApiServer.loadImagesBase64(String, List<PlateImageReference>, IImageRepresentationFormatSelectionCriterion...)
 * @method
 */
openbis.prototype.loadImagesBase64ForImageReferencesAndImageRepresentationFormatCriteria = function(imageReferences, criteria, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "loadImagesBase64",
                    "params" : { 
                        "sessionToken" : this.getSession(), 
                        "imageReferences" : imageReferences, 
                        "criteria" : criteria 
                    } 
            },
            success: action
    });
}

/**
 * @see IScreeningApiServer.listImageMetadata(String, List<? extends IImageDatasetIdentifier>)
 * @method
 */
openbis.prototype.listImageMetadata = function(imageDatasets, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "listImageMetadata",
                    "params" : [ this.getSession(), imageDatasets ] },
            success: action
    });
}

/**
 * @see IScreeningApiServer.listAvailableImageRepresentationFormats(String, List<? extends IDatasetIdentifier>)
 * @method
 */
openbis.prototype.listAvailableImageRepresentationFormats = function(imageDatasets, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "listAvailableImageRepresentationFormats",
                    "params" : [ this.getSession(), imageDatasets ] },
            success: action
    });
}

/**
 * @see IScreeningApiServer.loadPhysicalThumbnailsBase64(String, List<PlateImageReference>, ImageRepresentationFormat)
 * @method
 */
openbis.prototype.loadPhysicalThumbnailsBase64ForImageReferencesAndImageRepresentationFormat = function(imageReferences, format, action) {
    this._internal.ajaxRequest({
            url: this._internal.screeningUrl,
            data: { "method" : "loadPhysicalThumbnailsBase64",
                    "params" : [ this.getSession(), imageReferences, format ] },
            success: action
    });
}

/**
 * =====================================================================================
 * ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening methods
 * =====================================================================================
 */

/**
 * @see IDssServiceRpcScreening.loadImagesBase64(String, IDatasetIdentifier, List<WellPosition>, String, ImageSize)
 * @method
 */
openbis.prototype.loadImagesBase64ForDataSetIdentifierAndWellPositionsAndChannelAndImageSize = function(dataSetIdentifier, wellPositions, channel, thumbnailSizeOrNull, action) {
    this._internal.ajaxRequest({
            url: this._internal.getScreeningDataStoreApiUrlForDataStoreUrl(dataSetIdentifier.datastoreServerUrl),
            data: { "method" : "loadImagesBase64",
                    "params" : { 
                        "sessionToken" : this.getSession(), 
                        "dataSetIdentifier" : dataSetIdentifier, 
                        "wellPositions" : wellPositions, 
                        "channel" : channel, 
                        "thumbnailSizeOrNull" : thumbnailSizeOrNull 
                    } 
            },
            success: action
    });
}

/**
 * @see IDssServiceRpcScreening.loadImagesBase64(String, IDatasetIdentifier, String, ImageSize)
 * @method
 */
openbis.prototype.loadImagesBase64ForDataSetIdentifierAndChannelAndImageSize = function(dataSetIdentifier, channel, thumbnailSizeOrNull, action) {
    this._internal.ajaxRequest({
            url: this._internal.getScreeningDataStoreApiUrlForDataStoreUrl(dataSetIdentifier.datastoreServerUrl),
            data: { "method" : "loadImagesBase64",
                    "params" : { 
                        "sessionToken" : this.getSession(), 
                        "dataSetIdentifier" : dataSetIdentifier,
                        "channel" : channel, 
                        "thumbnailSizeOrNull" : thumbnailSizeOrNull 
                    } 
            },
            success: action
    });
}

/**
 * @see IDssServiceRpcScreening.loadThumbnailImagesBase64(String, IDatasetIdentifier, List<String>)
 * @method
 */
openbis.prototype.loadThumbnailImagesBase64ForDataSetIdentifierAndChannels = function(dataSetIdentifier, channels, action) {
    this._internal.ajaxRequest({
            url: this._internal.getScreeningDataStoreApiUrlForDataStoreUrl(dataSetIdentifier.datastoreServerUrl),
            data: { "method" : "loadThumbnailImagesBase64",
                    "params" : { 
                        "sessionToken" : this.getSession(), 
                        "dataSetIdentifier" : dataSetIdentifier, 
                        "channels" : channels 
                    } 
            },
            success: action
    });
}

/**
 * @see IDssServiceRpcScreening.listPlateImageReferences(String, IDatasetIdentifier, List<WellPosition>, String)
 * @method
 */
openbis.prototype.listPlateImageReferencesForDataSetIdentifierAndWellPositionsAndChannel = function(dataSetIdentifier, wellPositions, channel, action) {
    this._internal.ajaxRequest({
            url: this._internal.getScreeningDataStoreApiUrlForDataStoreUrl(dataSetIdentifier.datastoreServerUrl),
            data: { "method" : "listPlateImageReferences",
                    "params" : { 
                        "sessionToken" : this.getSession(), 
                        "dataSetIdentifier" : dataSetIdentifier, 
                        "wellPositions" : wellPositions, 
                        "channel" : channel 
                    } 
            },
            success: action
    });
}

/**
 * @see IDssServiceRpcScreening.listPlateImageReferences(String, IDatasetIdentifier, List<WellPosition>, List<String>)
 * @method
 */
openbis.prototype.listPlateImageReferencesForDataSetIdentifierAndWellPositionsAndChannels = function(dataSetIdentifier, wellPositions, channels, action) {
    this._internal.ajaxRequest({
            url: this._internal.getScreeningDataStoreApiUrlForDataStoreUrl(dataSetIdentifier.datastoreServerUrl),
            data: { "method" : "listPlateImageReferences",
                    "params" : { 
                        "sessionToken" : this.getSession(), 
                        "dataSetIdentifier" : dataSetIdentifier, 
                        "wellPositions" : wellPositions, 
                        "channels" : channels 
                    } 
            },
            success: action
    });
}

/**
 * @see IDssServiceRpcScreening.listImageReferences(String, IDatasetIdentifier, String)
 * @method
 */
openbis.prototype.listImageReferencesForDataSetIdentifierAndChannel = function(dataSetIdentifier, channel, action) {
    this._internal.ajaxRequest({
            url: this._internal.getScreeningDataStoreApiUrlForDataStoreUrl(dataSetIdentifier.datastoreServerUrl),
            data: { "method" : "listImageReferences",
                    "params" : { 
                        "sessionToken" : this.getSession(), 
                        "dataSetIdentifier" : dataSetIdentifier, 
                        "channel" : channel 
                    } 
            },
            success: action
    });
}

/**
 * @see IDssServiceRpcScreening.listImageReferences(String, IDatasetIdentifier, List<String>)
 * @method
 */
openbis.prototype.listImageReferencesForDataSetIdentifierAndChannels = function(dataSetIdentifier, channels, action) {
    this._internal.ajaxRequest({
            url: this._internal.getScreeningDataStoreApiUrlForDataStoreUrl(dataSetIdentifier.datastoreServerUrl),
            data: { "method" : "listImageReferences",
                    "params" : {
                        "sessionToken" : this.getSession(), 
                        "dataSetIdentifier" : dataSetIdentifier, 
                        "channels" : channels 
                    } 
            },
            success: action
    });
}

/**
 * @see IDssServiceRpcScreening.listAvailableFeatureLists(String, IFeatureVectorDatasetIdentifier)
 * @method
 */
openbis.prototype.listAvailableFeatureLists = function(featureVectorDataSet, action) {
	this._internal.ajaxRequest({
	        url: this._internal.getScreeningDataStoreApiUrlForDataStoreUrl(featureVectorDataSet.datastoreServerUrl),
	        data: { "method" : "listAvailableFeatureLists",
 	                params : [this.getSession(), featureVectorDataSet]
	        },
	        success: action
	});
};

/**
 * @see IDssServiceRpcScreening.getFeatureList(String, IFeatureVectorDatasetIdentifier, String)
 * @method
 */
openbis.prototype.getFeatureList = function(featureVectorDataSet, featureListCode, action) {
	this._internal.ajaxRequest({
	        url: this._internal.getScreeningDataStoreApiUrlForDataStoreUrl(featureVectorDataSet.datastoreServerUrl),
	        data: { "method" : "getFeatureList",
 	                params : [this.getSession(), featureVectorDataSet, featureListCode]
	        },
	        success: action
	});
};

