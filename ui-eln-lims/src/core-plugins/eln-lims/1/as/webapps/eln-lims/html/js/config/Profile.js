/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

//When non present it defaults to BOX_POSITION
var ValidationLevel = {
	RACK : 0,
	BOX : 1,
	BOX_POSITION : 2
}
/**
 * Creates an instance of DefaultProfile.
 *
 * @constructor
 * @this {DefaultProfile}
 */
function DefaultProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(DefaultProfile.prototype, {
	init: function(serverFacade) {
	    //
	    // Enable on new openBIS 20.x release
	    //
	    this.isOpenBIS2020 = true;
	    this.enableNewAnnotationsBackend = this.isOpenBIS2020;
	    this.enableLuceneQueryEngine = !this.isOpenBIS2020;

		//
		// Updating title and logo
		//
		this.mainLogo = "./img/openBIS_Logo.png";
		this.mainLogoTitle = "Lab Notebook & Inventory Manager";

		//this.mainLogo = "./img/cross_Logo_alt.png";
		//this.mainLogoTitle = "ETH RDH";

		this.serverFacade = serverFacade;

		//
		// DEFAULTS, TYPICALLY DON'T TOUCH IF YOU DON'T KNOW WHAT YOU DO
		//
		this.showDatasetArchivingButton = false;

		this.mainMenu = {
				showLabNotebook : true,
				showInventory : true,
				showStock : true,
//				showDrawingBoard : false,
				showObjectBrowser : true,
				showExports : true,
				showStorageManager : true,
				showAdvancedSearch : true,
				showArchivingHelper : true,
				showUnarchivingHelper : true,
				showTrashcan : true,
				showSettings : true,
				showVocabularyViewer : true,
				showUserManager : true,
				showUserProfile : true,
				showZenodoExportBuilder : false,
				showBarcodes : false,
				showDatasets : true
		}

		this.MainMenuNodeNames = {
		    Lab_Notebook : "Lab Notebook",
		    Inventory : "Inventory",
		    Stock : "Stock",
		    Utilities : "Utilities"
		}

		this.defaultStartView = {
		    page : "showBlancPage",
            args : null
		};

		this.orderLabInfo = {

		}

		this.orderLanguage = {
				"ENGLISH" : {
					"ORDER_FORM" : "Order Form",
					"ORDER_INFORMATION" : "Order Information",
					"ORDER_DATE" : "Date",
					"ORDER_STATUS" : "Status",
					"ORDER_CODE" : "Code",

					"COSTUMER_INFORMATION" : "Costumer Information",
					"SHIP_TO" : "Ship To",
					"BILL_TO" : "Bill To",
					"SHIP_ADDRESS" : "Address",
					"PHONE" : "Phone",
					"FAX" : "Fax",

					"SUPPLIER_INFORMATION" : "Supplier Information",
					"SUPPLIER" : "Supplier",
					"SUPPLIER_ADDRESS_LINE_1" : "Address",
					"SUPPLIER_ADDRESS_LINE_2" : "       ",
					"SUPPLIER_PHONE" : "Phone",
					"SUPPLIER_FAX" : "Fax",
					"SUPPLIER_EMAIL" : "Email",
					"CUSTOMER_NUMBER" : "Customer No",

					"REQUESTED_PRODUCTS_LABEL" : "Requested Products",
					"PRODUCTS_COLUMN_NAMES_LABEL" : "Quantity\t\tName\t\tCatalog Num\t\tUnit Price",
					"PRICE_TOTALS_LABEL" : "Total Price",
					"ADDITIONAL_INFO_LABEL" : "Additional Information"
				},
				"GERMAN" : {
					"ORDER_FORM" : "Bestellformular",
					"ORDER_INFORMATION" : "Bestellinformation",
					"ORDER_DATE" : "Bestelldatum",
					"ORDER_STATUS" : "Bestellstatus",
					"ORDER_CODE" : "Bestellcode",

					"COSTUMER_INFORMATION" : "Kundeninformation",
					"SHIP_TO" : "Lieferung an",
					"BILL_TO" : "Rechnung an",
					"SHIP_ADDRESS" : "Adresse",
					"PHONE" : "Telefon",
					"FAX" : "Fax",

					"SUPPLIER_INFORMATION" : "Lieferanteninformation",
					"SUPPLIER" : "Lieferant",
					"SUPPLIER_ADDRESS_LINE_1" : "Adresse",
					"SUPPLIER_ADDRESS_LINE_2" : "       ",
					"SUPPLIER_PHONE" : "Lieferant Telefon",
					"SUPPLIER_FAX" : "Lieferant Fax",
					"SUPPLIER_EMAIL" : "Lieferant Email",
					"CUSTOMER_NUMBER" : "Kundennummer",

					"REQUESTED_PRODUCTS_LABEL" : "Angeforderte Produkte",
					"PRODUCTS_COLUMN_NAMES_LABEL" : "Anzahl\t\tName\t\tCatalog Num\t\tPreis pro Einheit",
					"PRICE_TOTALS_LABEL" : "Gesamtpreis",
					"ADDITIONAL_INFO_LABEL" : "Zusätzliche Informationen"
				}
		}

		this.isAdmin = false;
        this.dropboxMonitorUsageAuthorized = false;
        this.userManagementMaintenanceTaskConfig = null;
		this.devEmail = "sis.eln.servicedesk@id.ethz.ch";

//		BigDataLink EDMs config
		this.EDMSs = {
//				"ADMIN-BS-MBPR28.D.ETHZ.CH-E96954A7" : "http://localhost:8080/download"
		}
		this.singleSignOnUrlTemplate = null;
		this.singleSignOnLinkLabel = 'Single Sign On Login';

        this.customWidgetSettings = {};

		this.plugins = [new GenericTechnology()];
		this.experimentFormTop = function($container, model) {
			for(var i = 0; i < this.plugins.length; i++) {
				this.plugins[i].experimentFormTop($container, model);
			}
		}
		this.experimentFormBottom = function($container, model) {
			for(var i = 0; i < this.plugins.length; i++) {
				this.plugins[i].experimentFormBottom($container, model);
			}
		}
		this.sampleFormTop = function($container, model) {
			for(var i = 0; i < this.plugins.length; i++) {
				this.plugins[i].sampleFormTop($container, model);
			}
		}
		this.sampleFormBottom = function($container, model) {
			for(var i = 0; i < this.plugins.length; i++) {
				this.plugins[i].sampleFormBottom($container, model);
			}
		}
		this.dataSetFormTop = function($container, model) {
			for(var i = 0; i < this.plugins.length; i++) {
				this.plugins[i].dataSetFormTop($container, model);
			}
		}
		this.dataSetFormBottom = function($container, model) {
			for(var i = 0; i < this.plugins.length; i++) {
				this.plugins[i].dataSetFormBottom($container, model);
			}
		}
		this.onSampleSave = function(sample, changesToDo, action) {
            var idx = -1;
            var _this = this;

            var failed = function(message) {
                Util.showError(message, null, false, true, false, true);
            }

            var next = null;
                next = function() {
                    idx = idx + 1;
                    if(idx < _this.plugins.length) {
                        _this.plugins[idx].onSampleSave(sample, changesToDo, next, failed);
                    } else {
                        action(sample, null, null, changesToDo);
                    }
                }
            next();
        }
        this.beforeViewPaint = function(viewType, model, $container) {
			for(var i = 0; i < this.plugins.length; i++) {
				this.plugins[i].beforeViewPaint(viewType, model, $container);
			}
		}
        this.afterViewPaint = function(viewType, model, $container) {
			for(var i = 0; i < this.plugins.length; i++) {
				this.plugins[i].afterViewPaint(viewType, model, $container);
			}
		}
		this.getPluginUtilities = function() {
		    var extraUtilities = [];
            for(var eIdx = 0; eIdx < this.plugins.length; eIdx++) {
                var pluginExtraUtilities = this.plugins[eIdx].getExtraUtilities();
                for(var pIdx = 0; pIdx < pluginExtraUtilities.length; pIdx++) {
                    extraUtilities.push(pluginExtraUtilities[pIdx]);
                }
            }
            return extraUtilities;
        }
        this.getPluginUtility = function(uniqueViewName) {
            for(var eIdx = 0; eIdx < this.plugins.length; eIdx++) {
                var pluginExtraUtilities = this.plugins[eIdx].getExtraUtilities();
                for(var pIdx = 0; pIdx < pluginExtraUtilities.length; pIdx++) {
                   if(pluginExtraUtilities[pIdx].uniqueViewName === uniqueViewName) {
                        return pluginExtraUtilities[pIdx];
                   }
                }
            }
            return null;
        }
//		Jupyter integration config
//		this.jupyterIntegrationServerEndpoint = "https://bs-openbis-sis-dev.ethz.ch:8002";
//		this.jupyterEndpoint = "https://bs-openbis-sis-dev.ethz.ch:8000/";

		this.settingsObjects = [];
		this.isMultiGroup = function() {
			return this.settingsObjects.length > 1;
		}

		this.getGroupCodes = function() {
		    var groupCodes = [];
		    for(var sOIdx = 0; sOIdx < this.settingsObjects.length; sOIdx++) {
                var identifier = this.settingsObjects[sOIdx].identifier;
                var spaceCode = identifier.split("/")[1];
                var prefix = spaceCode.substring(0, spaceCode.length - this.settingsSpacesPostFix.length);
                groupCodes.push(prefix);
		    }
		    return groupCodes;
		}

		this.systemProperties = ["$ANNOTATIONS_STATE", "FREEFORM_TABLE_STATE"];
		this.forcedDisableRTF = [];
		this.forceMonospaceFont = [];
		this.imageViewerDataSetCodes = [];
		this.isImageViewerDataSetCode = function(code) {
			return (code && $.inArray(code, this.imageViewerDataSetCodes) !== -1);
		}

		this.isRTF = function(propertytype) {
			var isRTF = (propertytype &&
						propertytype.dataType === "MULTILINE_VARCHAR" &&
						$.inArray(propertytype.code, this.forcedDisableRTF) === -1);
			for(var i = 0; i < this.plugins.length; i++) {
				isRTF = isRTF && (propertytype &&
						propertytype.dataType === "MULTILINE_VARCHAR" &&
						$.inArray(propertytype.code, this.plugins[i].forcedDisableRTF) === -1);
			}
			return isRTF;
		}

		this.isSystemProperty = function(propertytype) {
			return (propertytype && $.inArray(propertytype.code, this.systemProperties) !== -1);
		}

		this.isForcedMonospaceFont = function(propertytype) {
			var isForcedMonospaceFont = (propertytype && $.inArray(propertytype.code, this.forceMonospaceFont) !== -1);
			for(var i = 0; i < this.plugins.length; i++) {
				isForcedMonospaceFont = isForcedMonospaceFont || (propertytype && $.inArray(propertytype.code, this.plugins[i].forceMonospaceFont) !== -1);
			}
			return isForcedMonospaceFont;
		}

		this.isForcedDisableRTF = function(propertytype) {
			return (propertytype && $.inArray(propertytype.code, this.forcedDisableRTF) !== -1);
		}

		this.searchDomains = [ { "@id" : -1, "@type" : "GobalSearch", label : "Global", name : "global"}];

		//Ending in "MATERIALS", "METHODS", "STORAGE", "STOCK_CATALOG"
		this.inventorySpacesPostFixes = ["MATERIALS", "METHODS", "STORAGE", "STOCK_CATALOG"];
		this.inventorySpaces = [];
		//Ending in "ELN_SETTINGS", "STOCK_ORDERS"
		this.inventorySpacesReadOnlyPostFixes = ["ELN_SETTINGS", "STOCK_ORDERS", "PUBLICATIONS"];
		this.inventorySpacesReadOnly = [];
		//Ending in "STORAGE"
		this.storageSpacesPostFixes = ["STORAGE"];
		this.storageSpaces = [];
		//Ending in "ELN_SETTINGS"
		this.settingsSpacesPostFix = "ELN_SETTINGS";
		this.settingsSpaces = [];
		//Ending in "ELN_SETTINGS", "STORAGE"
		this.hideSpacesPostFixes = ["ELN_SETTINGS", "STORAGE"];
		this.hideSpaces = ["NAGIOS"];

        this.isSystemSpace = function(settingsSpaceCode, spaceCode) {
            var prefix = this.getSettingsSpacePrefix(settingsSpaceCode);
            for(var sIdx = 0; sIdx < this.inventorySpacesPostFixes.length; sIdx++) {
                var inventorySpacesPostFix = this.inventorySpacesPostFixes[sIdx];
                if(spaceCode == (prefix + inventorySpacesPostFix)) {
                    return true;
                }
            }
            for(var sIdx = 0; sIdx < this.inventorySpacesReadOnlyPostFixes.length; sIdx++) {
                var inventorySpacesReadOnlyPostFix = this.inventorySpacesReadOnlyPostFixes[sIdx];
                if(spaceCode == (prefix + inventorySpacesReadOnlyPostFix)) {
                    return true;
                }
            }
            return false;
        }

		this.initSpaces = function(callback) {
			var _this = this;
			var spaceRules = { entityKind : "SPACE", logicalOperator : "AND", rules : { } };

    	    		mainController.serverFacade.searchForSpacesAdvanced(spaceRules, null, function(spacesSearchResult) {
    	    			for(var sIdx = 0; sIdx < spacesSearchResult.objects.length; sIdx++) {
    	    				var space = spacesSearchResult.objects[sIdx];
    	    				if(Util.elementEndsWithArrayElement(space.code, _this.inventorySpacesPostFixes)) {
						_this.inventorySpaces.push(space.code);
					}
					if(Util.elementEndsWithArrayElement(space.code, _this.inventorySpacesReadOnlyPostFixes)) {
						_this.inventorySpacesReadOnly.push(space.code);
					}
					if(Util.elementEndsWithArrayElement(space.code, _this.storageSpacesPostFixes)) {
						_this.storageSpaces.push(space.code);
					}
					if(Util.elementEndsWithArrayElement(space.code, [_this.settingsSpacesPostFix])) {
						_this.settingsSpaces.push(space.code);
					}
					if(Util.elementEndsWithArrayElement(space.code, _this.hideSpacesPostFixes)) {
						_this.hideSpaces.push(space.code);
					}
    	    			}
    	    			callback();
    	    		});
		}

		this.getHomeSpace = function(callback) {
			mainController.serverFacade.getPersons([mainController.serverFacade.getUserId()], function(persons) {
				// check if home space is assigned
				var HOME_SPACE = null;
				if(persons !== null) {
					HOME_SPACE = (persons[0].getSpace()?persons[0].getSpace().getCode():null);
				}
				// fallback to space with the same name as the user, if existing
				if(HOME_SPACE === null) {
					var username = mainController.serverFacade.getUserId().toUpperCase();
					mainController.serverFacade.getSpace(username, function(result) {
						if (result && result.hasOwnProperty(username)) {
							HOME_SPACE = username;
						}
						callback(HOME_SPACE);
					})
				} else {
					callback(HOME_SPACE);
				}
			});
		}

        this.initProcessingServiceInfos = function(callback) {
            var _this = this;
            mainController.serverFacade.searchProcessingServices(function(result) {
                _this._processingServices = result.getObjects();
                callback();
            });
        }

        this.getAvailableProcessingServices = function(dataSetTypeCode) {
            var services = [];
            this._processingServices.forEach(function(service) {
                if (service.getDataSetTypeCodes().includes(dataSetTypeCode)) {
                    services.push(service);
                }
            });
            return services;
        }

        this.showDataset = function(datasetTypeCode) {
            return !profile.dataSetTypeDefinitionsExtension[datasetTypeCode] || profile.dataSetTypeDefinitionsExtension[datasetTypeCode]["SHOW"] === true;
        }

        this.showDatasetOnNav = function(datasetTypeCode) {
            return !profile.dataSetTypeDefinitionsExtension[datasetTypeCode] || profile.dataSetTypeDefinitionsExtension[datasetTypeCode]["SHOW_ON_NAV"] === true;
        }

        this.filterDataSetTypesForDropdowns = function(dataSetTypes) {
            var _this = this;
            return dataSetTypes.filter(function(dataSetType) {
                return _this.showDataset(dataSetType.code);
            });
        }

        this.visibleObjectTypesForSpaceCache = {};

		this.showOnNavForSpace = function(spaceCode, sampleTypeCode) {
		    var visibleObjectTypesForSpace = null;
		    if(this.visibleObjectTypesForSpaceCache[spaceCode] === undefined) {
		        this.visibleObjectTypesForSpaceCache[spaceCode] = SettingsManagerUtils.getVisibleObjectTypesForSpace(spaceCode, SettingsManagerUtils.ShowInSpaceSetting.showOnNav);
		    }
            var showOnNavForSpace = $.inArray(sampleTypeCode, this.visibleObjectTypesForSpaceCache[spaceCode]) !== -1;
            return showOnNavForSpace;
		}

        this.getSettingsSpacePrefix = function(spaceCode) {
            var indexOfSettingsCode = spaceCode.indexOf("ELN_SETTINGS");
            return spaceCode.substring(0, indexOfSettingsCode);
        }

		this.getSampleConfigSpacePrefix = function(sample) {
			var prefix = null;
			var spaceCode = sample.spaceCode;
			for(var ssIdx = 0; ssIdx < this.settingsSpaces.length; ssIdx++) {
				var settingsSpaceCode = this.settingsSpaces[ssIdx];
				var spacePrefixIndexOf = settingsSpaceCode.indexOf(this.settingsSpacesPostFix);
				if(spacePrefixIndexOf !== -1) {
					var spacePrefix = settingsSpaceCode.substring(0, spacePrefixIndexOf);
					if(spaceCode.startsWith(spacePrefix) && (prefix === null || (spacePrefix.length > prefix.length))) {
						prefix = spacePrefix;
					}
				}
			}

			return prefix;
		}

		this.getStorageConfigCollectionForConfigSample = function(sample) {
			var prefix = this.getSampleConfigSpacePrefix(sample);
			return IdentifierUtil.getExperimentIdentifier(prefix + "ELN_SETTINGS", prefix + "STORAGES", prefix + "STORAGES_COLLECTION");
		}

		this.getTemplateConfigCollectionForConfigSample = function(sample) {
			var prefix = this.getSampleConfigSpacePrefix(sample);
			return IdentifierUtil.getExperimentIdentifier(prefix + "ELN_SETTINGS", prefix + "TEMPLATES", prefix + "TEMPLATES_COLLECTION");
		}

		this.getStorageSpaceForSample = function(sample) {
			return this.getStorageSpaceForSpace(sample.spaceCode);
		}

		this.getStorageSpaceForSpace = function(spaceCode) {
			var storageSpaceCode = null;
			var prefixIndexOf = spaceCode.indexOf("_"); // This is a euristic that only works if the prefixes can't contain "_"
			if(prefixIndexOf !== -1) {
				var prefix = spaceCode.substring(0, prefixIndexOf);
				for(var ssIdx = 0; ssIdx < this.storageSpaces.length; ssIdx++) {
					if(this.storageSpaces[ssIdx].startsWith(prefix)) {
						storageSpaceCode = this.storageSpaces[ssIdx];
					}
				}
			}
			if(storageSpaceCode === null) { // Look for a default storage
				for(var ssIdx = 0; ssIdx < this.storageSpaces.length; ssIdx++) {
					if(this.storageSpaces[ssIdx] === this.storageSpacesPostFixes[0]) {
						storageSpaceCode = this.storageSpaces[ssIdx];
					}
				}
			}
			return storageSpaceCode;
		}

		this.searchSamplesUsingV3OnDropbox = false;
		this.searchSamplesUsingV3OnDropboxRunCustom = false;

        this.getExperimentTypeToolbarConfiguration = function(experimentTypeCode) {
			var defaultToolbar = { CREATE: true, FREEZE: true, EDIT: true, MOVE: true, DELETE: true, UPLOAD_DATASET: true, UPLOAD_DATASET_HELPER: true, EXPORT_ALL: true, EXPORT_METADATA: true, HISTORY: true };
			if(this.experimentTypeDefinitionsExtension[experimentTypeCode] && this.experimentTypeDefinitionsExtension[experimentTypeCode]["TOOLBAR"]) {
				var toolbarOptions = this.experimentTypeDefinitionsExtension[experimentTypeCode]["TOOLBAR"];
				for(key in toolbarOptions) {
					defaultToolbar[key] = toolbarOptions[key];
				}
			}
			return defaultToolbar;
		}

		this.getDataSetTypeToolbarConfiguration = function(dataSetTypeCode) {
			var defaultToolbar = { EDIT : true, FREEZE : true, MOVE : true, ARCHIVE : true, DELETE : true, HIERARCHY_TABLE : true, EXPORT_ALL : true, EXPORT_METADATA : true, HISTORY: true };
			if(this.dataSetTypeDefinitionsExtension[dataSetTypeCode] && this.dataSetTypeDefinitionsExtension[dataSetTypeCode]["TOOLBAR"]) {
				var toolbarOptions = this.dataSetTypeDefinitionsExtension[dataSetTypeCode]["TOOLBAR"];
				for(key in toolbarOptions) {
					defaultToolbar[key] = toolbarOptions[key];
				}
			}
			return defaultToolbar;
		}

		this.getSampleTypeToolbarConfiguration = function(sampleTypeCode) {
			var defaultToolbar = { CREATE : true, EDIT : true, FREEZE : true, MOVE : true, COPY: true, DELETE : true, PRINT: true, HIERARCHY_GRAPH : true, HIERARCHY_TABLE : true, UPLOAD_DATASET : true, UPLOAD_DATASET_HELPER : true, EXPORT_ALL : true, EXPORT_METADATA : true, TEMPLATES : true, BARCODE : true, HISTORY: true };
			if(this.sampleTypeDefinitionsExtension[sampleTypeCode] && this.sampleTypeDefinitionsExtension[sampleTypeCode]["TOOLBAR"]) {
				var toolbarOptions = this.sampleTypeDefinitionsExtension[sampleTypeCode]["TOOLBAR"];
				for(key in toolbarOptions) {
					defaultToolbar[key] = toolbarOptions[key];
				}
			}
			return defaultToolbar;
		}

		this.isSampleTypeWithStorage = function(sampleTypeCode) {
			return this.sampleTypeDefinitionsExtension[sampleTypeCode] && this.sampleTypeDefinitionsExtension[sampleTypeCode]["ENABLE_STORAGE"];
		}

		this.isELNIdentifier = function(identifier) {
			var space = IdentifierUtil.getSpaceCodeFromIdentifier(identifier);
			return !this.isInventorySpace(space);
		}

		this.isHiddenSpace = function(spaceCode) {
			return $.inArray(spaceCode, this.hideSpaces) !== -1;
		}

        this.isInventorySpace = function(spaceCode) {
            return this.inventorySpaces.includes(spaceCode) || this.inventorySpacesReadOnly.includes(spaceCode)
        }

		this.isFileAuthenticationService = false;
		this.isFileAuthenticationUser = false;
		this.directLinkEnabled = true;
		//To be set during initialization using info retrieved from the DSS configuration by the reporting plugin
		this.sftpFileServer = null;
		this.openbisVersion = "UNKNOWN";

		this.copyPastePlainText = false;
		this.hideCodes = true;
		this.systemTypes = {
				"sampleTypeCodes" : ["GENERAL_ELN_SETTINGS", "STORAGE_POSITION", "STORAGE"],
				"experimentTypeCodes" : []
		}
		this.hideTypes = {
				"sampleTypeCodes" : [],
				"experimentTypeCodes" : ["UNKNOWN"]
		}

		this._deleteSampleConnectionsByTypeIfNotVisited = function(sample, visited) {
			var permId = null;

			if(sample["@type"] === "as.dto.sample.Sample") {
				permId = sample.getPermId().getPermId();
			} else if(sample["@type"] === "Sample") {
				permId = sample.permId;
			}

			if(visited[permId]) {
				return;
			} else {
				visited[permId] = true;
			}

			if(sample.parents) {
				for(var i=0; i < sample.parents.length; i++) {
					var sampleParent = sample.parents[i];
					var sampleTypeCode = null;

					if(sample["@type"] === "as.dto.sample.Sample") {
						sampleTypeCode = sampleParent.getType().getCode();
					} else if(sample["@type"] === "Sample") {
						sampleTypeCode = sampleParent.sampleTypeCode;
					}

					if($.inArray(sampleTypeCode, this.systemTypes["sampleTypeCodes"]) !== -1) {
						sample.parents.splice(i, 1);
						i--;
					} else {
						this._deleteSampleConnectionsByTypeIfNotVisited(sampleParent, visited);
					}
				}
			}
			if(sample.children) {
				for(var i=0; i < sample.children.length; i++) {
					var sampleChild = sample.children[i];
					var sampleTypeCode = null;

					if(sample["@type"] === "as.dto.sample.Sample") {
						sampleTypeCode = sampleChild.getType().getCode();
					} else if(sample["@type"] === "Sample") {
						sampleTypeCode = sampleChild.sampleTypeCode;
					}

					if($.inArray(sampleTypeCode, this.systemTypes["sampleTypeCodes"]) !== -1) {
						sample.children.splice(i, 1);
						i--;
					} else {
						this._deleteSampleConnectionsByTypeIfNotVisited(sampleChild, visited);
					}
				}
			}
		}

		this.deleteSampleConnectionsByType = function(sample) {
			var visited = {};
			this._deleteSampleConnectionsByTypeIfNotVisited(sample, visited);
		}

		this.propertyReplacingCode = "$NAME";

		this.experimentTypeDefinitionsExtension = {

        }

		this.sampleTypeDefinitionsExtension = {

		}

		this.dataSetTypeDefinitionsExtension = {

		}

		this.searchType = {
			"TYPE" : "SEARCH",
			"DISPLAY_NAME" : "Search",
			"SAMPLE_TYPE_ATTRIBUTES" : ["sampleTypeCode", "MATCHED_TEXT", "MATCHED_FIELD", "PROPERTIES_JSON"],
			"SAMPLE_TYPE_ATTRIBUTES_DISPLAY_NAME" : ["Sample Type", "Matched Text", "Matching Field", "Properties"],
		};

		this.allSpaces = [];
		this.allSampleTypes = [];
		this.allExperimentTypes = [];
		this.allDataSetTypes = [];
		this.allVocabularies = [];
		this.allDataStores = [];
		this.allPropertyTypes = [];
		this.allDatasetTypeCodes = [];
		this.displaySettings = {};

		this.typePropertiesForSmallTable = {};

		this.storagesConfiguration = {
			"isEnabled" : false
		};

		this.isDatasetTypeCode = function(datasetTypeCode) {
			return ($.inArray(datasetTypeCode, this.allDatasetTypeCodes) !== -1);
		}

		this.isSampleTypeProtocol = function(sampleTypeCode) {
			return this.sampleTypeDefinitionsExtension[sampleTypeCode] && this.sampleTypeDefinitionsExtension[sampleTypeCode]["USE_AS_PROTOCOL"];
		}

		this.isExperimentTypeHidden = function(experimentTypeCode) {
			return ($.inArray(experimentTypeCode, this.hideTypes["experimentTypeCodes"]) !== -1);
		}

		this.getSearchDomains = function() {
			return this.searchDomains;
		}

		this.getDefaultDataStoreCode = function() {
			var dataStoreCode = null;
			if(this.allDataStores.length > 0) {
				 var dataStoreCode = this.allDataStores[0].code
			}
			return dataStoreCode;
		}

		this.getDefaultDataStoreURL = function() {
			var dataStoreURL = null;
			if(this.allDataStores.length > 0) {
				 var dataStoreURL = this.allDataStores[0].downloadUrl
			}
			return dataStoreURL;
		}

		this.getStoragePropertyGroup = function() {
			if(!this.storagesConfiguration["isEnabled"]) {
				return null;
			}

			propertyGroup = {};
			propertyGroup.groupDisplayName = "Physical Storage";
			propertyGroup.nameProperty = "$STORAGE_POSITION.STORAGE_CODE";
			propertyGroup.rowProperty = "$STORAGE_POSITION.STORAGE_RACK_ROW";
			propertyGroup.columnProperty = "$STORAGE_POSITION.STORAGE_RACK_COLUMN";
			propertyGroup.boxProperty = "$STORAGE_POSITION.STORAGE_BOX_NAME";
			propertyGroup.boxSizeProperty = "$STORAGE_POSITION.STORAGE_BOX_SIZE";
			propertyGroup.positionProperty = "$STORAGE_POSITION.STORAGE_BOX_POSITION";
			propertyGroup.userProperty = "$STORAGE_POSITION.STORAGE_USER";
			return propertyGroup;
		}

		this.getStorageConfigFromSample = function(sample) {
			//
			// !!! IMPORTANT
			// This properties start with $, they are here without it because the V1 API omits the $.
			//
			var propertyReplacingCodeNoDolar = profile.propertyReplacingCode;
			if(propertyReplacingCodeNoDolar.charAt(0) === "$") {
				propertyReplacingCodeNoDolar = propertyReplacingCodeNoDolar.substring(1);
			}
			return {
				spaceCode : sample.spaceCode,
				code : sample.code,
				label : sample.properties[propertyReplacingCodeNoDolar],
				validationLevel : ValidationLevel[sample.properties["STORAGE.STORAGE_VALIDATION_LEVEL"]],
				lowRackSpaceWarning : sample.properties["STORAGE.STORAGE_SPACE_WARNING"],
				lowBoxSpaceWarning : sample.properties["STORAGE.BOX_SPACE_WARNING"],
				rowNum : sample.properties["STORAGE.ROW_NUM"],
				colNum : sample.properties["STORAGE.COLUMN_NUM"],
				boxNum : sample.properties["STORAGE.BOX_NUM"]
			};
		}

		this.getStoragesConfiguation = function(callbackFunction) {
			var _this = this;
			if(!this.storagesConfiguration["isEnabled"]) {
				callbackFunction(null);
			}

			mainController.serverFacade.searchByType("STORAGE", function(results) {
				var configs = [];
				for(var idx = 0; idx < results.length; idx++) {
					configs.push(_this.getStorageConfigFromSample(results[idx]));
				}
				callbackFunction(configs);
			});
		}

		this.getStorageConfiguation = function(storageCode, callbackFunction) {
			var _this = this;
			if(!this.storagesConfiguration["isEnabled"]) {
				callbackFunction(null);
			}

			mainController.serverFacade.searchWithType("STORAGE", storageCode, null, function(results) {
				callbackFunction(_this.getStorageConfigFromSample(results[0]));
			});
		}

		this.getColorForInspectors = function(sampleTypeCode) {
			//Default Color
			var defaultColor = "#ffffc0"

			//Convert to HSL
			var rgb = d3.rgb(defaultColor);
			var hsl =  rgb.hsl();
			//Increase Light
			var newColor = d3.hsl(hsl.h, hsl.s, Math.min(0.90, hsl.l + 0.75));
			return newColor;
		}

		this.searchSorter = function(searchResults) {
			return searchResults;
		}

		//
		// Per Lab Extensions
		//

		/*
		 * Used by DataSet Form
		 */
		//Null: Show checkbox to manually input
		//True: Automatic configuration, will unzip the file before atacching the files
		//False: Will do nothing to the file before attaching
		this.isZipDirectoryUpload = function(dataSetType) {
			return null;
		}

		/*
		 * Used by Inspector
		 */
		this.inspectorContentTransformer = function(sample, propertyCode, propertyContent) {

			if(propertyContent.indexOf("<root ") != -1) {
				return {
					"isSingleColumn" : true,
					"content" : this.getHTMLTableFromManagePropertyXML(propertyContent)
				};
			} else {
				if(propertyContent === "<root/>\n") { //To clean empty XMLs and don't show them.
					propertyContent = "";
				}
				return {
					"isSingleColumn" : false,
					"content" : propertyContent
				};
			}
		}

		this.getHTMLTableFromManagePropertyXML = function(xmlDocument) {
			var table_head = null;
			var table_body = "";
			var dom;

			if (window.DOMParser) {
			  parser = new DOMParser();
			  dom = parser.parseFromString(xmlDocument,"text/xml");
			} else {// Internet Explorer
			  dom = new ActiveXObject("Microsoft.XMLDOM");
			  dom.async = false;
			  dom.loadXML(xmlDocument);
			}

			var html = null;
			var root = dom.childNodes[0];
			var children = root.childNodes;
			for(var i = 0; i < children.length; i++) {
				var child = children[i];
				if (child.localName != null) {
					var keys = child.attributes;
					if (table_head == null) {
						table_head = "<tr>";
						var key = null;
						for (var j = 0; j < keys.length; j++) {
							key = keys[j];
							if(key.localName != "permId") {
								table_head += "<th style='text-align:left; width:" + (100/(keys.length-1)) + "%;'>"+ key.localName + "</th>";
							}
						}
						table_head += "</tr>";
					}
					table_body += "<tr>";
					for (var j = 0; j < keys.length; j++) {
						key = keys[j];
						if(key.localName != "permId") {
							if(key.localName == "date") {
								table_body += "<td style='text-align:left; width:" + (100/(keys.length-1)) + "%;'>" + key.value + "</td>";
							} else {
								table_body += "<td style='text-align:left; width:" + (100/(keys.length-1)) + "%;'>" + key.value + "</td>";
							}
						}
					}
					table_body += "</tr>";
				}
			}
			html = "<table style='font-family:helvetica; font-size:90%; width: 100%;'><thead>" + table_head + "</thead><tbody>"+ table_body + "</tbody></table>";
			return html;
		}

		/*
		 * Modifies sample before submit
		 */
		this.sampleFormOnSubmit = function(sample, action) {
			if(action) {
				action(sample, null);
			}
		}

		/*
		 * Returns a Jquery component that is appended at the end of the form before the data set viewer
		 */
		this.sampleFormContentExtra = function(sampleTypeCode, sample, containerId) {
		}

		/*
		 * Used by DataSet Uploader
		 */
		this.dataSetTypeForFileNameMap = [];

		this.getDataSetTypeForFileName = function(allDatasetFiles, fileName) {
			for (var dataSetTypeForFileName of this.dataSetTypeForFileNameMap) {
				if (fileName && fileName.endsWith(dataSetTypeForFileName.fileNameExtension)) {
					return dataSetTypeForFileName.dataSetType;
				}
			}
			return null;
		}

		//
		// Utility methods used to navigate the configuration easily
		//
		this.isPropertyPressent = function(sampleType, propertyTypeCode) {
			for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
				var propertyTypeGroup = sampleType.propertyTypeGroups[i];
				for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
					var propertyType = propertyTypeGroup.propertyTypes[j];
					if(propertyType.code === propertyTypeCode) {
						return propertyType;
					}
				}
			}
			return null;
		}

		this.isELNExperiment = function(sampleTypeCode) {
			return false;
		}

		this.getPropertyTypes = function() {
			return this.allPropertyTypes;
		}

		this.fixV1PropertyTypeVocabulary = function(propertyType) {
		    var initialisedProperty = this.getPropertyType(propertyType.code);
		    if(initialisedProperty.dataType === "CONTROLLEDVOCABULARY") {
		        propertyType.terms = initialisedProperty.terms;
		        propertyType.vocabulary = initialisedProperty.vocabulary;
		    }
		}

        this.getPropertyTypeFromSampleTypeV3 = function(sampleType, propertyTypeCode) {
            if(!sampleType.propertyAssignments) {
                return null;
            }

            for(var i = 0; i < sampleType.propertyAssignments.length; i++) {
                var propertyType = sampleType.propertyAssignments[i].propertyType;
                if(propertyType.code === propertyTypeCode) {
                    return propertyType;
                }
            }
            return null;
        }

		this.getPropertyTypeFromSampleType = function(sampleType, propertyTypeCode) {
			for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
				var propertyTypeGroup = sampleType.propertyTypeGroups[i];
				for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
					var propertyType = propertyTypeGroup.propertyTypes[j];
					if(propertyType.code === propertyTypeCode) {
						return propertyType;
					}
				}
			}
			return null;
		}

		this.getPropertyType = function(propertyTypeCode) {
			for (var i = 0; i < this.allPropertyTypes.length; i++) {
				if(this.allPropertyTypes[i].code === propertyTypeCode) {
					return this.allPropertyTypes[i];
				}
			}
			return null;
		}

		this.getVocabularyByCode = function(code) {
			for (var i = 0; i < this.allVocabularies.length; i++) {
				var vocabulary = this.allVocabularies[i];
				if (vocabulary.code === code) {
					return vocabulary;
				}
			}
			return null;
		}

		this.getVocabularyTermByCodes = function(vocabularyCode, termCode) {
			var vocabulary = this.getVocabularyByCode(vocabularyCode);
			if(vocabulary) {
				for(var idx = 0; idx < vocabulary.terms.length; idx++) {
					if(vocabulary.terms[idx].code === termCode) {
						return vocabulary.terms[idx];
					}
				}
			}
			return null;
		}

        this.getExperimentTypes = function() {
            var types = [];
			for(var i = 0; i < this.allExperimentTypes.length; i++) {
				if(!this.isExperimentTypeHidden(this.allExperimentTypes[i].code)) {
					types.push(this.allExperimentTypes[i]);
				}
			}
			return types;
		}

		this.getExperimentTypeForExperimentTypeCode = function(typeCode) {
			for(var i = 0; i < this.allExperimentTypes.length; i++) {
				if(this.allExperimentTypes[i].code === typeCode) {
					return this.allExperimentTypes[i];
				}
			}
			return null;
		}

		this.getDataSetTypeForDataSetTypeCode = function(typeCode) {
			for(var i = 0; i < this.allDataSetTypes.length; i++) {
				if(this.allDataSetTypes[i].code === typeCode) {
					return this.allDataSetTypes[i];
				}
			}
			return null;
		}

		this.getSampleTypeForSampleTypeCode = function(typeCode) {
			for(var i = 0; i < this.allSampleTypes.length; i++) {
				if(this.allSampleTypes[i].code === typeCode) {
					return this.allSampleTypes[i];
				}
			}
			return null;
		}

		this.getAllPropertiCodesForExperimentTypeCode = function(typeCode) {
			var allPropertiCodes = new Array();
			var type = this.getExperimentTypeForExperimentTypeCode(typeCode);
			for(var i = 0; i < type.propertyTypeGroups.length; i++) {
				var propertyGroup = type.propertyTypeGroups[i].propertyTypes;
				for(var j = 0; j < propertyGroup.length; j++) {
					var propertyType = propertyGroup[j];
					allPropertiCodes.push(propertyType.code);
				}
			}
			return allPropertiCodes;
		}

		this.getAllPropertiCodesForDataSetTypeCode = function(typeCode) {
			var allPropertiCodes = new Array();
			var type = this.getDataSetTypeForDataSetTypeCode(typeCode);
			for(var i = 0; i < type.propertyTypeGroups.length; i++) {
				var propertyGroup = type.propertyTypeGroups[i].propertyTypes;
				for(var j = 0; j < propertyGroup.length; j++) {
					var propertyType = propertyGroup[j];
					allPropertiCodes.push(propertyType.code);
				}
			}
			return allPropertiCodes;
		}

		this.getPropertiesDisplayNamesForExperimentTypeCode = function(typeCode, propertiesTypeCode) {
			var allPropertiDisplayNames = new Array();
			for(var i = 0; i < propertiesTypeCode.length; i++) {
				var propertyTypeCode = propertiesTypeCode[i];
				var propertyTypeDisplayName = this.getPropertyDisplayNamesForExperimentTypeCode(typeCode, propertyTypeCode);
				allPropertiDisplayNames.push(propertyTypeDisplayName);
			}
			return allPropertiDisplayNames;
		}

		this.getPropertyDisplayNamesForExperimentTypeCode = function(typeCode, propertyTypeCode) {
			var propertyDisplayName = "";
			var type = this.getExperimentTypeForExperimentTypeCode(typeCode);

			for(var i = 0; i < type.propertyTypeGroups.length; i++) {
				var propertyGroup = type.propertyTypeGroups[i].propertyTypes;
				for(var j = 0; j < propertyGroup.length; j++) {
					var propertyType = propertyGroup[j];
					if(propertyType.code === propertyTypeCode) {
						propertyDisplayName = propertyType.label;
					}
				}
			}

			return propertyDisplayName;
		}

		this.getAllPropertiCodesForTypeCode = function(typeCode) {
			var allPropertiCodes = new Array();
			var sampleType = this.getSampleTypeForSampleTypeCode(typeCode);
			for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
				var propertyGroup = sampleType.propertyTypeGroups[i].propertyTypes;
				for(var j = 0; j < propertyGroup.length; j++) {
					var propertyType = propertyGroup[j];
					allPropertiCodes.push(propertyType.code);
				}
			}
			return allPropertiCodes;
		}

		this.getPropertyDisplayNamesForTypeCode = function(sampleTypeCode, propertyTypeCode) {
			var propertyDisplayName = "";
			var sampleType = this.getSampleTypeForSampleTypeCode(sampleTypeCode);

			for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
				var propertyGroup = sampleType.propertyTypeGroups[i].propertyTypes;
				for(var j = 0; j < propertyGroup.length; j++) {
					var propertyType = propertyGroup[j];
					if(propertyType.code === propertyTypeCode) {
						propertyDisplayName = propertyType.label;
					}
				}
			}

			return propertyDisplayName;
		}

		this.getPropertiesDisplayNamesForTypeCode = function(sampleTypeCode, propertiesTypeCode) {
			var allPropertiDisplayNames = new Array();
			var sampleType = this.getSampleTypeForSampleTypeCode(sampleTypeCode);

			for(var i = 0; i < propertiesTypeCode.length; i++) {
				var propertyTypeCode = propertiesTypeCode[i];
				var propertyTypeDisplayName = this.getPropertyDisplayNamesForTypeCode(sampleTypeCode, propertyTypeCode);
				allPropertiDisplayNames.push(propertyTypeDisplayName);
			}

			return allPropertiDisplayNames;
		}

		this.getPropertyTypeFrom = function(sampleType, propertyCode) {
			for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
				var propertyTypeGroup = sampleType.propertyTypeGroups[i];
				for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
					var propertyType = propertyTypeGroup.propertyTypes[j];
					if(propertyType.code === propertyCode) {
						return propertyType;
					}
				}
			}
			return null;
		}

		this.getAllSampleTypes = function() {
			return this.allSampleTypes;
		}

		this.datasetViewerImagePreviewIconSize = 25; // width in px
		this.datasetViewerMaxFilesizeForImagePreview = 50000000; // filesize in bytes

		this.archivingThreshold = "ask your administrator";

		this.initPropertyTypes = function(callback) {
			var _this = this;
			this.serverFacade.listPropertyTypesV3(function(data) {
				_this.allPropertyTypes = data;
				_this.serverFacade.listVocabularies(function(result) {
					//Init Vocabularies, so we don't miss vocabularies missing on sample types used only on annotations, etc...
					_this.allVocabularies = result.result;
					//Fix Property Types
					var intToVocabularyCode = {};

					//1. Obtain mapping from ids to codes
					for(var pIdx = 0; pIdx < _this.allPropertyTypes.length; pIdx++) {
						var propertyType = _this.allPropertyTypes[pIdx];
						if (propertyType.dataType === "CONTROLLEDVOCABULARY") {
							var vocabularyOrNumber = propertyType.vocabulary;
							if (vocabularyOrNumber !== parseInt(vocabularyOrNumber, 10)) { //Is vocabulary
								intToVocabularyCode[propertyType.vocabulary["@id"]] = propertyType.vocabulary.code
							}
						}
					}

					//2. Resolve ids and partial objects from the returned complete vocabularies
					for(var pIdx = 0; pIdx < _this.allPropertyTypes.length; pIdx++) {
						var propertyType = _this.allPropertyTypes[pIdx];
						if (propertyType.dataType === "CONTROLLEDVOCABULARY") {
							var vocabularyOrNumber = propertyType.vocabulary;
							var vocabularyCode = null;
							if (vocabularyOrNumber === parseInt(vocabularyOrNumber, 10)) { //Is number
								vocabularyCode = intToVocabularyCode[vocabularyOrNumber];
							} else {
								vocabularyCode = propertyType.vocabulary.code;
							}

							if(!vocabularyCode) {
								alert("[TO-DELETE] Empty Vocabulary during init, this should never happen, tell the developers.");
							}
							var vocabulary = _this.getVocabularyByCode(vocabularyCode);
							propertyType.vocabulary = vocabulary;
							propertyType.terms = vocabulary.terms;
						}
					}

					//Continue Init Case
					callback();
				});


			});
		}

		this.initVocabulariesForSampleTypes = function(callback) {
			var _this = this;
			var getVocabularyCodeFromId = function(id) {

			}
			this.serverFacade.listVocabularies(function(result) {
				//Init Vocabularies, so we don't miss vocabularies missing on sample types used only on annotations, etc...
				_this.allVocabularies = result.result;
				//Fix Sample Types
				var intToVocabularyCode = {};

				//1. Obtain mapping from ids to codes
				for(var sampleTypeIdx = 0; sampleTypeIdx < _this.allSampleTypes.length; sampleTypeIdx++) {
					var sampleType = _this.allSampleTypes[sampleTypeIdx];
					for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
						var propertyGroup = sampleType.propertyTypeGroups[i].propertyTypes;
						for(var j = 0; j < propertyGroup.length; j++) {
							var propertyType = propertyGroup[j];
							if (propertyType.dataType === "CONTROLLEDVOCABULARY") {
								var vocabularyOrNumber = propertyType.vocabulary;
								if (vocabularyOrNumber !== parseInt(vocabularyOrNumber, 10)) { //Is vocabulary
									intToVocabularyCode[propertyType.vocabulary["@id"]] = propertyType.vocabulary.code
								}
							}
						}
					}
				}

				//2. Resolve ids and partial objects from the returned complete vocabularies
				for(var sampleTypeIdx = 0; sampleTypeIdx < _this.allSampleTypes.length; sampleTypeIdx++) {
					var sampleType = _this.allSampleTypes[sampleTypeIdx];
					for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
						var propertyGroup = sampleType.propertyTypeGroups[i].propertyTypes;
						for(var j = 0; j < propertyGroup.length; j++) {
							var propertyType = propertyGroup[j];
							if (propertyType.dataType === "CONTROLLEDVOCABULARY") {
								var vocabularyOrNumber = propertyType.vocabulary;
								var vocabularyCode = null;
								if (vocabularyOrNumber === parseInt(vocabularyOrNumber, 10)) { //Is number
									vocabularyCode = intToVocabularyCode[vocabularyOrNumber];
								} else {
									vocabularyCode = propertyType.vocabulary.code;
								}

								if(!vocabularyCode) {
									alert("[TO-DELETE] Empty Vocabulary during init, this should never happen, tell the developers.");
								}
								var vocabulary = _this.getVocabularyByCode(vocabularyCode);
								propertyType.vocabulary = vocabulary;
								propertyType.terms = vocabulary.terms;
							}
						}
					}
				}

				//Init Spaces
				_this.serverFacade.listSpaces(function(spaces) {
					if($.inArray("INVENTORY", spaces) === -1) {
						mainController.serverFacade.createReportFromAggregationService(_this.getDefaultDataStoreCode(), {"method" : "init" }, function() {
							_this.serverFacade.listSpaces(function(spaces) {
								_this.allSpaces = spaces;
								callback();
							});
						});
					} else {
						_this.allSpaces = spaces;
						callback();
					}
				})
			});
		}

		this.initSearchDomains = function(callback) {
			var _this = this;
			this.serverFacade.listSearchDomains(function(data) {
				if(data && data.result) {
					for(var i = 0; i < data.result.length; i++) {
						_this.searchDomains.push(data.result[i]);
					}
				}
				callback();
			});
		}

		this.initDirectLinkURL = function(callback) {
			var _this = this;
			this.serverFacade.getDirectLinkURL(function(error, result) {
				if(!error && result.data.sftp) {
					_this.sftpFileServer = result.data.sftp;
				}
				callback();
			});
		}

		this.initAuthorizationInfo = function(callback) {
			var _this = this;
			this.serverFacade.listPersons(function(data) {
				_this.isAdmin = !(data.error);
                _this.serverFacade.isDropboxMonitorUsageAuthorized(function(result) {
                    _this.dropboxMonitorUsageAuthorized = result[0][0].value === "true";
                    callback();
                });
			});
		}

        this.initUserManagementMaintenanceTaskConfig = function(callback) {
            var _this = this;
            this.serverFacade.getUserManagementMaintenanceTaskConfig(function(config) {
                _this.userManagementMaintenanceTaskConfig = config;
                callback();
            });
        }

		this.initDatasetTypeCodes = function(callback) {
			var _this = this;
			this.serverFacade.listDataSetTypes(function(data) {
				var dataSetTypes = data.result;
				for(var i = 0; i < dataSetTypes.length; i++) {
					var datasetType = dataSetTypes[i];
					_this.allDatasetTypeCodes.push(datasetType.code);
				}

				_this.allDatasetTypeCodes.sort(function(a, b){
				    if(a < b) return -1;
				    if(a > b) return 1;
				    return 0;
				});

				callback();
			});
		}

		this.initSettings = function(callback) {
			// sampleTypeDefinitionsExtension and  dataSetTypeDefinitionsExtension gets overwritten with plugins definitions
			for(var i = 0; i < this.plugins.length; i++) {
				for(key in this.plugins[i].experimentTypeDefinitionsExtension) {
                    this.experimentTypeDefinitionsExtension[key] = this.plugins[i].experimentTypeDefinitionsExtension[key];
                }
				for(key in this.plugins[i].sampleTypeDefinitionsExtension) {
					this.sampleTypeDefinitionsExtension[key] = this.plugins[i].sampleTypeDefinitionsExtension[key];
				}
				for(key in this.plugins[i].dataSetTypeDefinitionsExtension) {
					this.dataSetTypeDefinitionsExtension[key] = this.plugins[i].dataSetTypeDefinitionsExtension[key];
				}
			}

			var settingsManager = new SettingsManager(this.serverFacade);
			settingsManager.loadSettingsAndApplyToProfile((function() {
				callback();
			}));
		}

		this.initCustomWidgetSettings = function(callback) {
		    var _this = this;
		    this.serverFacade.getCustomWidgetSettings(function(customWidgetSettings) {
		        for(var cwIdx = 0; cwIdx < customWidgetSettings.length; cwIdx++) {
		            var cw = customWidgetSettings[cwIdx];
		            _this.customWidgetSettings[cw["Property Type"]] = cw["Widget"];
		        }
		        callback();
		    });
		}

		this.initServerInfo = function(callback) {
			var _this = this;
			this.serverFacade.getOpenbisV3(function(openbisV3) {
				openbisV3._private.sessionToken = mainController.serverFacade.getSession();
				openbisV3.getServerInformation().done(function(serverInformation) {
					_this.openbisVersion = serverInformation["openbis-version"];
	                var authSystem = serverInformation["authentication-service"];
	                IdentifierUtil.isProjectSamplesEnabled = (serverInformation["project-samples-enabled"] === "true");
	                IdentifierUtil.createContinuousSampleCodes = (serverInformation["create-continuous-sample-codes"] === "true");
	                if (authSystem && authSystem.indexOf("file") !== -1) {
	                		_this.isFileAuthenticationService = true;
	                }
	                mainController.serverFacade.getCustomImportDefinitions(function(definitions) {
	                    _this.customImportDefinitions = definitions;
	                    mainController.serverFacade.getSessionInformation(callback);
	                });
	            });
			});
		}

		this.isFileAuthUser = function(callback) {
			var _this = this;
			this.serverFacade.isFileAuthUser(function(error, result) {
				_this.isFileAuthenticationUser = result && result.data === 1;
				callback();
			});
		}

		//
		// Initializes
		//
		this.init = function(callbackWhenDone) {
			var _this = this;
			this.initPropertyTypes(function(){
				_this.initVocabulariesForSampleTypes(function() {
					_this.initSearchDomains(function() {
						_this.initDirectLinkURL(function() {
                            _this.initAuthorizationInfo(function() {
                                _this.initUserManagementMaintenanceTaskConfig(function() {
                                    _this.initDatasetTypeCodes(function() {
                                        _this.initServerInfo(function() {
                                            _this.isFileAuthUser(function() {
                                                _this.initSpaces(function() {
                                                    _this.initProcessingServiceInfos(function() {
                                                        _this.initCustomWidgetSettings(function() {
                                                            _this.initSettings(function() {
                                                                //Check if the new storage system can be enabled
                                                                var storageRack = _this.getSampleTypeForSampleTypeCode("STORAGE");
                                                                var storagePositionType = _this.getSampleTypeForSampleTypeCode("STORAGE_POSITION");
                                                                _this.storagesConfiguration = {
                                                                        "isEnabled" : storageRack && storagePositionType
                                                                };
                                                                callbackWhenDone();
                                                            });
                                                        });
                                                    });
                                                });
                                            });
                                        });
                                    });
                                });
							});
						});
					});
				});
			});
		}
	}
});
