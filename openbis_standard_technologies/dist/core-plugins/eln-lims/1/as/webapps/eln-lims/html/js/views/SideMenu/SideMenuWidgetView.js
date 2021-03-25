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

/**
 * Creates an instance of SideMenuWidgetView.
 *
 * @constructor
 * @this {SideMenuWidgetView}
 */
function SideMenuWidgetView(sideMenuWidgetController, sideMenuWidgetModel) {
    this._sideMenuWidgetController = sideMenuWidgetController;
    this._sideMenuWidgetModel = sideMenuWidgetModel;
    
    var toggleMenuSizeBig = false;
    var MIN_LENGTH = 3;
    var DISPLAY_NAME_LENGTH_SHORT = 15;
    var DISPLAY_NAME_LENGTH_LONG = 300;
    var cutDisplayNameAtLength = DISPLAY_NAME_LENGTH_SHORT; // Fix for long names
    
    this.repaint = function($container) {
        var _this = this;
        this._$container = $container;
        var $widget = $("<div>", { id : "sideMenuTopContainer" });
        //
        // Fix Header
        //
        var $header = $("<div>", {"id": "sideMenuHeader"});
            $header.css("background-color", "rgb(248, 248, 248)");
            $header.css("padding", "10px");
        var searchDomains = profile.getSearchDomains();

        var searchFunction = function() {
            var searchText = $("#search").val();
            if(searchText.length < MIN_LENGTH) {
                Util.showInfo("The minimum length for a global text search is " + MIN_LENGTH + " characters.", function() {}, true);
                return false;
            }

            var domainIndex = $("#search").attr("domain-index");
            var searchDomain = null;
            var searchDomainLabel = null;
            
            if(domainIndex) {
                searchDomain = profile.getSearchDomains()[domainIndex].name;
                searchDomainLabel = profile.getSearchDomains()[domainIndex].label;
            } else {
                searchDomain = profile.getSearchDomains()[0].name;
                searchDomainLabel = profile.getSearchDomains()[0].label;
            }

            var argsMap = {
                "searchText": searchText,
                "searchDomain": searchDomain,
                "searchDomainLabel": searchDomainLabel
            };
            var argsMapStr = JSON.stringify(argsMap);

            mainController.changeView("showSearchPage", argsMapStr);
        };
        
        var dropDownSearch = null;
        if (searchDomains.length > 0) {
            //Prefix function
            var selectedFunction = function(selectedSearchDomain, domainIndex) {
                return function() {
                    var $search = $("#search");
                    $search.attr("placeholder", selectedSearchDomain.label + " Search");
                    $search.attr("domain-index", domainIndex);
                };
            };

            //Dropdown elements
            var dropDownComponents = [];
            for (var i = 0; i < searchDomains.length; i++) {
                dropDownComponents.push({
                    href: selectedFunction(searchDomains[i], i),
                    title: searchDomains[i].label,
                    id: searchDomains[i].name
                });
            }

            dropDownSearch = FormUtil.getDropDownToogleWithSelectedFeedback(null, dropDownComponents, true, searchFunction);
            dropDownSearch.change();
        }


        var searchElement = $("<input>", {"id": "search", "type": "text", "class": "form-control search-query", "placeholder": "Global Search"});
        searchElement.keypress(function (e) {
             var key = e.which;
             var onFocus = searchElement.is(":focus");
             var searchString = searchElement.val();
             if(key == 13 && // the enter key code
                onFocus && // ensure is focused
                searchString.length >= MIN_LENGTH) // min search length of 3 characters
              {
                searchFunction();
                return false;
              } else if(key == 13 &&
                        onFocus &&
                        searchString.length < MIN_LENGTH) {
                Util.showInfo("The minimum length for a global text search is " + MIN_LENGTH + " characters.", function() {}, true);
                return false;
              }
        });
        searchElement.css({"display" : "inline", "width" : "30%"});
        searchElement.css({"padding-top" : "2px"});
        searchElement.css({"margin-left" : "2px"});
        searchElement.css({"margin-right" : "2px"});
        
        var icon = mainController.loggedInAnonymously ? "glyphicon-log-in" : "glyphicon-off";
        var logoutButton = FormUtil.getButtonWithIcon(icon, function() {
            $('body').addClass('bodyLogin');
            sessionStorage.setItem("forceNormalLogin", mainController.loggedInAnonymously);
            mainController.loggedInAnonymously = false;
            mainController.serverFacade.logout();
         }, null, null, "logoutBtn");
        
        var $searchForm = $("<form>", { "onsubmit": "return false;" })
                            .append(logoutButton)
                            .append(searchElement)
                            .append(dropDownSearch);
        $searchForm.css("width", "100%");
        
        $header.append($searchForm);
        
        var $body = $("<div>", {"id": "sideMenuBody"});
        $body.css("overflow-y", "auto");
        
        LayoutManager.addResizeEventHandler(function() {
                if(LayoutManager.FOUND_SIZE === LayoutManager.MOBILE_SIZE) {
                    $body.css("-webkit-overflow-scrolling", "auto");
                } else {
                    $body.css("-webkit-overflow-scrolling", "touch");
                }
        });

        // sorting
        var sortOptions = [
            {
                sortField: "displayName",
                description: "Sort by name",
                icon: "glyphicon-sort-by-alphabet",
            },
            {
                sortField: "registrationDate",
                description: "Sort by date",
                icon: "glyphicon-sort-by-order",
            },
        ];
        var $sortButtonGroup = this._makeSortButtonGroup(sortOptions);
        var $sortBar = $("<span>", { "id": "sideMenuSortBar" });
        $sortBar.append($sortButtonGroup);

        $searchForm.append($("<span>", { class : "vl" }));
        $searchForm.append($sortBar);
        $widget.append($header)
               .append($body);
        $container.empty();
        $container.append($widget);

        //
        // Print Menu
        //
        this._sideMenuWidgetModel.menuDOMBody = $body;
        this.repaintTreeMenuDinamic();
    };

    this._makeSortButtonGroup = function(sortOptions) {
        var buttons = [];
        for (var i=0; i<sortOptions.length; i++) {
            var sortOption = sortOptions[i];
            var $button = this._makeSortButton(sortOption);
            buttons.push($button);
        }
        var $buttonGroup = FormUtil.getButtonGroup(buttons);
        return $buttonGroup;
    }

    this._makeSortButton = function(sortOption) {
        var _this = this;
        var $button = FormUtil.getButtonWithIcon(sortOption.icon, function() {
            _this._sideMenuWidgetController.setSortField(sortOption.sortField);
            _this.repaint(_this._$container);
            _this._sideMenuWidgetController.resize();
        }, null, sortOption.description);
        if (this._sideMenuWidgetModel.sortField == sortOption.sortField) {
            $button.addClass("active");
        }
        return $button;
    }

    this.getLinkForNode = function(displayName, menuId, view, viewData, id) {
        var href = Util.getURLFor(menuId, view, viewData);
        if (id === null) {
            id = menuId;
        }
        displayName = String(displayName).replace(/<(?:.|\n)*?>/gm, ''); //Clean any HTML tags
        var $menuItemLink = $("<a>", {"href": href, "id" : id, "class" : "browser-compatible-javascript-link browser-compatible-javascript-link-tree" }).text(displayName);
        return $menuItemLink[0].outerHTML;
    }


    this.repaintTreeMenuDinamic = function() {
        var _this = this;
        this._sideMenuWidgetModel.menuDOMBody.empty();
        var $tree = $("<div>", { "id" : "tree" });

        var sortItems = function(resultA, resultB) {
            var sortField = _this._sideMenuWidgetModel.sortField;
            // default to displayName
            if (sortField == null || !resultA.hasOwnProperty(sortField) == null || !resultB.hasOwnProperty(sortField)) {
                sortField = "displayName";
            }
            // descending order for registrationDate
            if (sortField === "registrationDate") {
                return naturalSort(resultB[sortField], resultA[sortField]);
            }
            return naturalSort(resultA[sortField], resultB[sortField]);
        }
            
        //
        // Body
        //
        
        var treeModel = [];
        
        if(profile.mainMenu.showLabNotebook) {
            var labNotebookLink = _this.getLinkForNode(profile.MainMenuNodeNames.Lab_Notebook, "LAB_NOTEBOOK", "showLabNotebookPage", null, null);
            treeModel.push({ displayName: profile.MainMenuNodeNames.Lab_Notebook, title : labNotebookLink, entityType: "LAB_NOTEBOOK", key : "LAB_NOTEBOOK", folder : true, lazy : true, view : "showLabNotebookPage", icon : "glyphicon glyphicon-book" });
        }
        
        if(profile.mainMenu.showInventory) {
            var inventoryLink = _this.getLinkForNode(profile.MainMenuNodeNames.Inventory, "INVENTORY", "showInventoryPage", null, null);
            treeModel.push({ displayName: profile.MainMenuNodeNames.Inventory, title : inventoryLink, entityType: "INVENTORY", key : "INVENTORY", folder : true, lazy : true, view : "showInventoryPage", icon : "fa fa-cubes" });
        }
        
        if(profile.mainMenu.showStock) {
            var stockLink = _this.getLinkForNode(profile.MainMenuNodeNames.Stock, "STOCK", "showStockPage", null, null);
            treeModel.push({ displayName: profile.MainMenuNodeNames.Stock, title : stockLink, entityType: "STOCK", key : "STOCK", folder : true, lazy : true, view : "showStockPage", icon: "fa fa-shopping-cart" });
        }
        
        var treeModelUtils = [];
        
        if(profile.jupyterEndpoint) {
            var jupyterLink = _this.getLinkForNode("Jupyter Workspace", "JUPYTER_WORKSPACE", "showJupyterWorkspace", null, null);
            treeModelUtils.push({ title : jupyterLink, entityType: "JUPYTER_WORKSPACE", key : "JUPYTER_WORKSPACE", folder : false, lazy : false, view : "showJupyterWorkspace" });
            
            var jupyterNotebook = _this.getLinkForNode("New Jupyter Notebook", "NEW_JUPYTER_NOTEBOOK", "showNewJupyterWorkspaceCreator", null, null);
            treeModelUtils.push({ title : jupyterNotebook, entityType: "NEW_JUPYTER_NOTEBOOK", key : "NEW_JUPYTER_NOTEBOOK", folder : false, lazy : false, view : "showNewJupyterNotebookCreator" });
        }
        
        if(profile.mainMenu.showUserProfile) {
            // && profile.isFileAuthenticationService && profile.isFileAuthenticationUser
            var userProfileLink = _this.getLinkForNode("User Profile", "USER_PROFILE", "showUserProfilePage", null, null);
            treeModelUtils.push({ title : userProfileLink, entityType: "USER_PROFILE", key : "USER_PROFILE", folder : false, lazy : false, view : "showUserProfilePage", icon : "glyphicon glyphicon-user" });
        }

        if(profile.mainMenu.showBarcodes) {
            var preGenerateBarcodesLink = _this.getLinkForNode("Barcodes Generator", "GENERATE_BARCODES", "showBarcodesGeneratorPage", null, null);
            treeModelUtils.push({ title : preGenerateBarcodesLink, entityType: "GENERATE_BARCODES", key : "GENERATE_BARCODES", folder : false, lazy : false, view : "showBarcodesGeneratorPage", icon : "glyphicon glyphicon-barcode" });
        }

        if(profile.mainMenu.showDrawingBoard) {
            var drawingBoardLink = _this.getLinkForNode("Drawing Board", "DRAWING_BOARD", "showDrawingBoard", null, null);
            treeModelUtils.push({ displayName: "Drawing Board", title : drawingBoardLink, entityType: "DRAWING_BOARD", key : "DRAWING_BOARD", folder : false, lazy : false, view : "showDrawingBoard" });
        }
        
        if(profile.mainMenu.showObjectBrowser) {
            var sampleBrowserLink = _this.getLinkForNode("" + ELNDictionary.Sample + " Browser", "SAMPLE_BROWSER", "showSamplesPage", null, null);
            treeModelUtils.push({ displayName: "" + ELNDictionary.Sample + " Browser", title : sampleBrowserLink, entityType: "SAMPLE_BROWSER", key : "SAMPLE_BROWSER", folder : false, lazy : false, view : "showSamplesPage", icon : "glyphicon glyphicon-list-alt" });
        }
        
        if(profile.mainMenu.showVocabularyViewer) {
            var vocabularyBrowserLink = _this.getLinkForNode("Vocabulary Browser", "VOCABULARY_BROWSER", "showVocabularyManagerPage", null, null);
            treeModelUtils.push({ displayName: "Vocabulary Browser", title : vocabularyBrowserLink, entityType: "VOCABULARY_BROWSER", key : "VOCABULARY_BROWSER", folder : false, lazy : false, view : "showVocabularyManagerPage", icon : "glyphicon glyphicon-list-alt" });
        }
        
        if(profile.mainMenu.showAdvancedSearch) {
            var advancedSearchLink = _this.getLinkForNode("Advanced Search", "ADVANCED_SEARCH", "showAdvancedSearchPage", null, null);
            treeModelUtils.push({ displayName: "Advanced Search", title : advancedSearchLink, entityType: "ADVANCED_SEARCH", key : "ADVANCED_SEARCH", folder : false, lazy : false, view : "showAdvancedSearchPage", icon : "glyphicon glyphicon-search" });
        }

        if(profile.mainMenu.showUnarchivingHelper && profile.showDatasetArchivingButton) {
        	var unarchivingHelperLink = _this.getLinkForNode("Unarchiving Helper", "UNARCHIVING_HELPER", "showUnarchivingHelperPage", null, null);
        	treeModelUtils.push({ displayName: "Unarchiving Helper", title : unarchivingHelperLink, entityType: "UNARCHIVING_HELPER", key : "UNARCHIVING_HELPER", folder : false, lazy : false, view : "showUnarchivingHelperPage", icon : "glyphicon glyphicon-open" });
        }
        
        if (profile.mainMenu.showExports || options.showResearchCollectionExportBuilder || profile.mainMenu.showZenodoExportBuilder) {
            var treeModelExports = [];

            if (profile.mainMenu.showExports) {
                var exportBuilderLink = _this.getLinkForNode("Export to ZIP", "EXPORT_TO_ZIP", "showExportTreePage", null, null);
                treeModelExports.push({
                    displayName: "Export to ZIP", title: exportBuilderLink, entityType: "EXPORT_TO_ZIP", key: "EXPORT_TO_ZIP",
                    folder: false, lazy: false, view: "showExportTreePage", icon: "glyphicon glyphicon-export"
                });
            }

            if (options.showResearchCollectionExportBuilder) {
                var researchCollectionExportBuilderLink = _this.getLinkForNode("Export to Research Collection",
                        "EXPORT_TO_RESEARCH_COLLECTION", "showResearchCollectionExportPage", null, null);
                treeModelExports.push({
                    displayName: "Export to Research Collection", title: researchCollectionExportBuilderLink,
                    entityType: "EXPORT_TO_RESEARCH_COLLECTION", key: "EXPORT_TO_RESEARCH_COLLECTION", folder: false, lazy: false,
                    view: "showResearchCollectionExportPage", icon: "./img/research-collection-icon.png"
                });
            }

            if (profile.mainMenu.showZenodoExportBuilder) {
                var zenodoExportBuilderLink = _this.getLinkForNode("Export to Zenodo", "EXPORT_TO_ZENODO", "showZenodoExportPage", null, null);
                treeModelExports.push({
                    displayName: "Export to Zenodo", title: zenodoExportBuilderLink, entityType: "EXPORT_TO_ZENODO",
                    key: "EXPORT_TO_ZENODO", folder: false, lazy: false, view: "showZenodoExportPage", icon: "glyphicon glyphicon-export"
                });
            }

            treeModelUtils.push({ displayName: "Exports", title: "Exports", entityType: "EXPORTS", key: "EXPORTS", folder: true, lazy: false,
                    expanded: false, children: treeModelExports, view: "showBlancPage", icon: "glyphicon glyphicon-export" });
        }
        
        if(profile.mainMenu.showStorageManager) {
            var storageManagerLink = _this.getLinkForNode("Storage Manager", "STORAGE_MANAGER", "showStorageManager", null, null);
            treeModelUtils.push({ displayName: "Storage Manager", title : storageManagerLink, entityType: "STORAGE_MANAGER", key : "STORAGE_MANAGER", folder : false, lazy : false, view : "showStorageManager" });
        }
        
        if(profile.mainMenu.showUserManager && profile.isAdmin) {
            var userManagerLink = _this.getLinkForNode("User Manager", "USER_MANAGER", "showUserManagerPage", null, null);
            treeModelUtils.push({ displayName: "User Manager", title : userManagerLink, entityType: "USER_MANAGER", key : "USER_MANAGER", folder : false, lazy : false, view : "showUserManagerPage", icon : "fa fa-users" });
        }
        
        if(profile.mainMenu.showTrashcan) {
            var trashCanLink = _this.getLinkForNode("Trashcan", "TRASHCAN", "showTrashcanPage", null, null);
            treeModelUtils.push({ displayName: "Trashcan", title : trashCanLink, entityType: "TRASHCAN", key : "TRASHCAN", folder : false, lazy : false, view : "showTrashcanPage", icon : "glyphicon glyphicon-trash" });
        }
        
        if(profile.mainMenu.showSettings) {
            var settingsLink = _this.getLinkForNode("Settings", "SETTINGS", "showSettingsPage", null, null);
            treeModelUtils.push({ displayName: "Settings", title : settingsLink, entityType: "SETTINGS", key : "SETTINGS", folder : false, lazy : false, view : "showSettingsPage", icon : "glyphicon glyphicon-cog" });
        }

        var extraPluginUtilities = profile.getPluginUtilities();
        for(var ePIdx = 0; ePIdx < extraPluginUtilities.length; ePIdx++) {
            var extraPluginUtility = extraPluginUtilities[ePIdx];
            var extraUtilityLink = _this.getLinkForNode(extraPluginUtility.label, "EXTRA_PLUGIN_UTILITY", "EXTRA_PLUGIN_UTILITY", extraPluginUtility.uniqueViewName, null);
            treeModelUtils.push({   displayName: extraPluginUtility.label,
                                    title : extraUtilityLink,
                                    entityType: "EXTRA_PLUGIN_UTILITY",
                                    key : extraPluginUtility.uniqueViewName,
                                    viewData : extraPluginUtility.uniqueViewName,
                                    folder : false,
                                    lazy : false,
                                    view : "EXTRA_PLUGIN_UTILITY",
                                    icon : extraPluginUtility.icon });
        }

        treeModel.push({ displayName: profile.MainMenuNodeNames.Utilities, title : "Utilities", entityType: "UTILITIES", key : "UTILITIES", folder : true, lazy : false, expanded : true, children : treeModelUtils, view: "showBlancPage", icon : "glyphicon glyphicon-wrench" });
        treeModel.push({ displayName: "About", title : "About", entityType: "ABOUT", key : "ABOUT", folder : false, lazy : false, view : "showAbout", icon : "glyphicon glyphicon-info-sign" });
        
        var glyph_opts = {
            map: {
                doc: "glyphicon glyphicon-file",
                docOpen: "glyphicon glyphicon-file",
                checkbox: "glyphicon glyphicon-unchecked",
                checkboxSelected: "glyphicon glyphicon-check",
                checkboxUnknown: "glyphicon glyphicon-share",
                dragHelper: "glyphicon glyphicon-play",
                dropMarker: "glyphicon glyphicon-arrow-right",
                error: "glyphicon glyphicon-warning-sign",
                expanderClosed: "glyphicon glyphicon-plus-sign",
                expanderLazy: "glyphicon glyphicon-plus-sign",  // glyphicon-expand
                expanderOpen: "glyphicon glyphicon-minus-sign",  // glyphicon-collapse-down
                folder: "glyphicon glyphicon-folder-close",
                folderOpen: "glyphicon glyphicon-folder-open",
                loading: "glyphicon glyphicon-refresh"
            }
        };
        
        var onLazyLoad = function(event, data) {
            var dfd = new $.Deferred();
            data.result = dfd.promise();
            var type = data.node.data.entityType;
            var permId = data.node.key;
            
            var showLabNotebooks = function(dfd, showEnabled, showDisabled) {
                    var spaceRules = { entityKind : "SPACE", logicalOperator : "AND", rules : { } };
                    profile.getHomeSpace(function(HOME_SPACE) {
                        mainController.serverFacade.searchForSpacesAdvanced(spaceRules, null, function(searchResult) {
                        var results = [];
                        var spaces = searchResult.objects;
                    var nonInventoryNonHiddenSpaces = [];
                    var spacesByCode = {}; 
                        for (var i = 0; i < spaces.length; i++) {
                            var space = spaces[i];
                            var isInventorySpace = profile.isInventorySpace(space.code);
                            var isHiddenSpace = profile.isHiddenSpace(space.code);
                            if(!isInventorySpace && (space.code !== HOME_SPACE) && !isHiddenSpace) {
                                    nonInventoryNonHiddenSpaces.push(space.code);
                                    spacesByCode[space.code] = space;
                            }
                        }
                        
                        mainController.serverFacade.customELNASAPI({
                                "method" : "doSpacesBelongToDisabledUsers",
                                "spaceCodes" : nonInventoryNonHiddenSpaces
                        }, function(disabledSpaces) {
                                for(var i = 0; i < nonInventoryNonHiddenSpaces.length; i++) {
                                    var spaceCode = nonInventoryNonHiddenSpaces[i];
                                    var foundDisabled = $.inArray(spaceCode, disabledSpaces) !== -1;
                                    
                                    if(!showDisabled && foundDisabled) {
                                        continue; //Skip disabled spaces
                                    }
                                    
                                    if(!showEnabled && !foundDisabled) {
                                        continue; //Skip enabled spaces
                                    }
                                    
                                    var normalizedSpaceTitle = Util.getDisplayNameFromCode(spaceCode);
                                    var spaceLink = _this.getLinkForNode(normalizedSpaceTitle, spaceCode, "showSpacePage", spaceCode, null);
                                    var spaceNode = {
                                        displayName: normalizedSpaceTitle,
                                        title : spaceLink,
                                        entityType: "SPACE",
                                        key : spaceCode,
                                        folder : true,
                                        lazy : true,
                                        view : "showSpacePage",
                                        viewData: spaceCode,
                                        registrationDate: spacesByCode[spaceCode].registrationDate,
                                    };
                                    results.push(spaceNode);
                                }
                                results.sort(sortItems);
                                dfd.resolve(results);
                        });
                        });
                    });
            };
            
            switch(type) {
                case "LAB_NOTEBOOK":
                    var spaceRules = { entityKind : "SPACE", logicalOperator : "AND", rules : { } };
                    profile.getHomeSpace(function(HOME_SPACE) {
                        mainController.serverFacade.searchForSpacesAdvanced(spaceRules, null, function(searchResult) {
                        var results = [];
                        var spaces = searchResult.objects;
                        for (var i = 0; i < spaces.length; i++) {
                            var space = spaces[i];
                            var isInventorySpace = profile.isInventorySpace(space.code);
                            var isHiddenSpace = profile.isHiddenSpace(space.code);
                            if(!isInventorySpace && (space.code === HOME_SPACE) && !isHiddenSpace) {
                                var normalizedSpaceTitle = Util.getDisplayNameFromCode(space.code);
                                var spaceLink = _this.getLinkForNode("My Space (" + normalizedSpaceTitle + ")", space.getCode(), "showSpacePage", space.getCode(), null);
                                var spaceNode = {
                                    displayName: "My Space (" + normalizedSpaceTitle + ")",
                                    title : spaceLink,
                                    entityType: "SPACE",
                                    key : space.getCode(),
                                    folder : true,
                                    lazy : true,
                                    view : "showSpacePage",
                                    viewData: space.getCode(),
                                    registrationDate: space.registrationDate,
                                };
                                results.push(spaceNode);
                            }
                        }
                        
                    results.push({ displayName: "Others", title : "Others", entityType: "LAB_NOTEBOOK_OTHERS", key : "LAB_NOTEBOOK_OTHERS", folder : true, lazy : true, view : "showLabNotebookPage" });
                    results.push({ displayName: "Others (disabled)", title : "Others (disabled)", entityType: "LAB_NOTEBOOK_OTHERS_DISABLED", key : "LAB_NOTEBOOK_OTHERS_DISABLED", folder : true, lazy : true, view : "showLabNotebookPage" });
                        results.sort(sortItems);
                        dfd.resolve(results);
                        });
                    });
                    break;
                case "LAB_NOTEBOOK_OTHERS":
                showLabNotebooks(dfd, true, false);
                    break;
                case "LAB_NOTEBOOK_OTHERS_DISABLED":
                showLabNotebooks(dfd, false, true);
                    break;
                case "INVENTORY":
                    var spaceRules = { entityKind : "SPACE", logicalOperator : "AND", rules : { } };
                    mainController.serverFacade.searchForSpacesAdvanced(spaceRules, null, function(searchResult) {
                        var results = [];
                        var spaces = searchResult.objects;
                        for (var i = 0; i < spaces.length; i++) {
                            var space = spaces[i];
                            var isInventorySpace = profile.isInventorySpace(space.code);
                            var isHiddenSpace = profile.isHiddenSpace(space.code);
                            if(((type === "LAB_NOTEBOOK" && !isInventorySpace) || (type === "INVENTORY" && isInventorySpace)) && !isHiddenSpace) {
                                var normalizedSpaceTitle = Util.getDisplayNameFromCode(space.code);
                                
                                var spaceLink = _this.getLinkForNode(normalizedSpaceTitle, space.getCode(), "showSpacePage", space.getCode(), null);
                                var spaceNode = {
                                    displayName: normalizedSpaceTitle,
                                    title : spaceLink,
                                    entityType: "SPACE",
                                    key : space.getCode(),
                                    folder : true,
                                    lazy : true,
                                    view : "showSpacePage",
                                    viewData: space.getCode(),
                                    registrationDate: space.registrationDate,
                                };
                                if(!space.getCode().endsWith("STOCK_CATALOG") && !space.getCode().endsWith("STOCK_ORDERS")) {
                                    results.push(spaceNode);
                                }
                            }
                        }
                        results.sort(sortItems);
                        dfd.resolve(results);
                    });
                    break;
                case "STOCK":
                    var spaceRules = { entityKind : "SPACE", logicalOperator : "AND", rules : { } };
                    mainController.serverFacade.searchForSpacesAdvanced(spaceRules, null, function(searchResult) {
                        var results = [];
                        var spaces = searchResult.objects;
                        for (var i = 0; i < spaces.length; i++) {
                            var space = spaces[i];
                            if(space.getCode().endsWith("STOCK_CATALOG") || space.getCode().endsWith("STOCK_ORDERS")) {
                                var normalizedSpaceTitle = Util.getDisplayNameFromCode(space.code);
                                var spaceLink = _this.getLinkForNode(normalizedSpaceTitle, space.getCode(), "showSpacePage", space.getCode(), null);
                                var spaceNode = {
                                    displayName: normalizedSpaceTitle,
                                    title : spaceLink,
                                    entityType: "SPACE",
                                    key : space.getCode(),
                                    folder : true,
                                    lazy : true,
                                    view : "showSpacePage",
                                    viewData: space.getCode(),
                                    registrationDate: space.registrationDate,
                                };
                                results.push(spaceNode);
                            }
                        }
                        results.sort(sortItems);
                        dfd.resolve(results);
                    });
                    break;
                case "SPACE":
                    var projectRules = { "UUIDv4" : { type : "Attribute", name : "SPACE", value : permId } };
                    mainController.serverFacade.searchForProjectsAdvanced({ entityKind : "PROJECT", logicalOperator : "AND", rules : projectRules }, null, function(searchResult) {
                        var results = [];
                        var projects = searchResult.objects;
                      for (var i = 0; i < projects.length; i++) {
                          var project = projects[i];
                          if (project.code == 'QUERIES' && 
                              project.description == ELNDictionary.generatedObjects.searchQueriesProject.description) {
                              continue;
                          }
                          var normalizedProjectTitle = Util.getDisplayNameFromCode(project.code);
                          var projectLink = _this.getLinkForNode(normalizedProjectTitle, project.getPermId().getPermId(), "showProjectPageFromPermId", project.getPermId().getPermId(), project.code);
                          results.push({
                              displayName: normalizedProjectTitle,
                              title : projectLink,
                              entityType: "PROJECT",
                              key : project.getPermId().getPermId(),
                              folder : true,
                              lazy : true,
                              view : "showProjectPageFromPermId",
                              viewData: project.getPermId().getPermId(),
                              registrationDate: project.registrationDate,
                            });
                          
                      }
                      results.sort(sortItems);
                      dfd.resolve(results);
                    });
                    break;
                case "PROJECT":
                    var experimentRules = { "UUIDv4" : { type : "Attribute", name : "PROJECT_PERM_ID", value : permId } };
                    mainController.serverFacade.searchForExperimentsAdvanced({ entityKind : "EXPERIMENT", logicalOperator : "AND", rules : experimentRules }, null, function(searchResult) {
                        var results = [];
                        var experiments = searchResult.objects;
                        for (var i = 0; i < experiments.length; i++) {
                            var experiment = experiments[i];
                            var experimentDisplayName = experiment.code;
                            if(experiment.properties && experiment.properties[profile.propertyReplacingCode]) {
                                experimentDisplayName = experiment.properties[profile.propertyReplacingCode];
                            }
                            var isInventorySpace = profile.isInventorySpace(IdentifierUtil.getSpaceCodeFromIdentifier(experiment.getIdentifier().getIdentifier()));
                            var viewToUse = null;
                            var loadSamples = null;
                            if(isInventorySpace) {
                                viewToUse = "showSamplesPage";
                                loadSamples = false;
                            } else {
                                viewToUse = "showExperimentPageFromIdentifier";
                                loadSamples = true;
                            }
                            
                            var experimentLink = _this.getLinkForNode(experimentDisplayName, experiment.getPermId().getPermId(), viewToUse, experiment.getIdentifier().getIdentifier(), experiment.getIdentifier().getIdentifier().split('/').join('_'));
                            var experimentResult = {
                                    displayName: experimentDisplayName,
                                    title : experimentLink,
                                    entityType: "EXPERIMENT",
                                    key : experiment.getPermId().getPermId(),
                                    folder : true,
                                    lazy : loadSamples,
                                    view : viewToUse,
                                    viewData: experiment.getIdentifier().getIdentifier(),
                                    registrationDate: experiment.registrationDate,
                            };
                            if(experiment.type.code === "COLLECTION" || experiment.type.code === "MATERIALS" || experiment.type.code === "METHODS") {
                                experimentResult.icon = "fa fa-table";
                            }
                            results.push(experimentResult);
                        }
                        results.sort(sortItems);
                        dfd.resolve(results);
                    });
                    break;
                case "EXPERIMENT":
                    var sampleRules = { "UUIDv4" : { type : "Experiment", name : "ATTR.PERM_ID", value : permId } };
                    mainController.serverFacade.searchForSamplesAdvanced({ entityKind : "SAMPLE", logicalOperator : "AND", rules : sampleRules }, 
                    { only : true, withProperties : true, withType : true, withExperiment : true, withParents : true, 
    				 withParentsExperiment: true, withChildren : true, withParentsType : true, withChildrenType : true},
                    function(searchResult) {
                        var samples = searchResult.objects;
                        var samplesToShow = [];
                        for(var sIdx = 0; sIdx < samples.length; sIdx++) {
                            var sample = samples[sIdx];
                            var sampleIsExperiment = sample.type.code.indexOf("EXPERIMENT") > -1;
                            var sampleTypeOnNav = profile.showOnNav(sample.type.code);
    						var sampleExperimentIdentifier = sample.experiment.identifier.identifier;
                            if((sampleIsExperiment || sampleTypeOnNav) &&
                                profile.showOnNav(sample.type.code)) {
                                var parentInELN = false;
                                if(sample.parents) {
                                    for(var pIdx = 0; pIdx < sample.parents.length; pIdx++) {
    									var parent = sample.parents[pIdx];
                                        var parentIdentifier = parent.identifier.identifier;
        								var parentExperimentIdentifier = parent.experiment.identifier.identifier;
                                        parentInELN = profile.isELNIdentifier(parentIdentifier) 
        										&& parentExperimentIdentifier === sampleExperimentIdentifier;
                                        if(parentInELN) {
                                            break;
                                        }
                                    }
                                }
                                if(!parentInELN) {
                                    samplesToShow.push(sample);
                                }
                            }
                        }
                        
                        var getOkResultsFunction = function(dfd, samples) {
                            return function() {
                                var results = [];
                                for (var i = 0; i < samples.length; i++) {
                                    var sample = samples[i];
                                    var sampleIsExperiment = sample.type.code.indexOf("EXPERIMENT") > -1;
                                    var sampleIcon;
                                    if(sampleIsExperiment) {
                                        sampleIcon = "fa fa-flask";
                                    } else if(sample.type.code === "ENTRY") {
                                        sampleIcon = "fa fa-file-text";
                                    } else {
                                        sampleIcon = "fa fa-file";
                                    }
                                    var sampleDisplayName = sample.code;
                                    if(sample.properties && sample.properties[profile.propertyReplacingCode]) {
                                            sampleDisplayName = sample.properties[profile.propertyReplacingCode];
                                    }
                                    
                                    var sampleLink = _this.getLinkForNode(sampleDisplayName, sample.getPermId().getPermId(), "showViewSamplePageFromPermId", sample.getPermId().getPermId(), null);
                                    var sampleNode = {
                                        displayName: sampleDisplayName,
                                        title : sampleLink,
                                        entityType: "SAMPLE",
                                        key : sample.getPermId().getPermId(),
                                        folder : true,
                                        lazy : true,
                                        view : "showViewSamplePageFromPermId",
                                        viewData: sample.getPermId().getPermId(),
                                        icon : sampleIcon,
                                        registrationDate: sample.registrationDate,
                                    };
                                    results.push(sampleNode);
                                }
                                
                                var datasetRules = { "UUIDv4" : { type : "Experiment", name : "ATTR.PERM_ID", value : permId } };
                                mainController.serverFacade.searchForDataSetsAdvanced({ entityKind : "DATASET", logicalOperator : "AND", rules : datasetRules }, null, function(searchResult) {
                                    
                                    var datasets = searchResult.objects;
                                    var experimentDatasets = [];
                                    for (var i = 0; i < datasets.length; i++) {
                                        var dataset = datasets[i];
                                        if(!dataset.sample) {
                                                experimentDatasets.push(dataset);
                                        }
                                    }
                    
                                    if(profile.mainMenu.showDatasets) {
                                        if(experimentDatasets.length > 50) {
                                                Util.showInfo("More than 50 Datasets, please use the dataset viewer on the experiment to navigate them.");
                                        } else {
                                                for (var i = 0; i < experimentDatasets.length; i++) {
                                                var dataset = experimentDatasets[i];
                                                var datasetDisplayName = dataset.code;
                                                if(dataset.properties && dataset.properties[profile.propertyReplacingCode]) {
                                                        datasetDisplayName = dataset.properties[profile.propertyReplacingCode];
                                                }
                            
                                                var datasetLink = _this.getLinkForNode(datasetDisplayName, dataset.getPermId().getPermId(), "showViewDataSetPageFromPermId", dataset.getPermId().getPermId(), null);
                                                results.push({
                                                    displayName: datasetDisplayName,
                                                    title : datasetLink,
                                                    entityType: "DATASET",
                                                    key : dataset.getPermId().getPermId(),
                                                    folder : true,
                                                    lazy : false,
                                                    view : "showViewDataSetPageFromPermId",
                                                    viewData: dataset.getPermId().getPermId(),
                                                    icon : "fa fa-database",
                                                    registrationDate: dataset.registrationDate,
                                                });
                                            }
                                        }
                                    }
                                
                                results.sort(sortItems);
                                    dfd.resolve(results);
                                    Util.unblockUI();
                                });
                                
                            }
                        };
                        
                        var getCancelResultsFunction = function(dfd) {
                            return function() {
                                dfd.resolve([]);
                            }
                        };
                        
                        if(samplesToShow.length > 50) {
                            var toExecute = function() {
                                Util.blockUIConfirm("Do you want to show " + samplesToShow.length + " " + ELNDictionary.Samples + " on the tree?",
                                        getOkResultsFunction(dfd, samplesToShow),
                                        getCancelResultsFunction(dfd));
                            };
                            
                            setTimeout(toExecute, 1000);
                        } else {
                            getOkResultsFunction(dfd, samplesToShow)();
                        }
                    });
                    break;
                case "SAMPLE":
                    var sampleRules = { "UUIDv4" : { type : "Attribute", name : "PERM_ID", value : permId } };
                    mainController.serverFacade.searchForSamplesAdvanced({ entityKind : "SAMPLE", logicalOperator : "AND", rules : sampleRules }, 
                    { only : true, withProperties : true, withType : true, withExperiment : true, withParents : true, 
    				  withChildren : true, withChildrenProperties : true, withChildrenExperiment : true, 
    				  withParentsType : true, withChildrenType : true}
                    , function(searchResult) {
                        var results = [];
                        var samples = searchResult.objects;
                        if(samples && samples[0] && samples[0].children) {
                            if(samples[0].children.length > 50) {
                                Util.showInfo("More than 50 Samples, please use the children table to navigate them.");
                            } else {
                                for(var cIdx = 0; cIdx < samples[0].children.length; cIdx++) {
                                    var sample = samples[0].children[cIdx];
                                    if (sample.experiment.identifier.identifier !== samples[0].experiment.identifier.identifier) {
        								continue;
        							}
                                    var sampleIsExperiment = sample.type.code.indexOf("EXPERIMENT") > -1;
                                    var sampleIcon;
                                    if(sampleIsExperiment) {
                                        sampleIcon = "fa fa-flask";
                                    } else if(sample.type.code === "ENTRY") {
                                        sampleIcon = "fa fa-file-text";
                                    } else {
                                        sampleIcon = "fa fa-file";
                                    }
                                    if(profile.showOnNav(sample.type.code)) {
                                        var parentTypeCode = samples[0].type.code;
                                        var showOnNavForParentTypes = profile.sampleTypeDefinitionsExtension[sample.type.code]["SHOW_ON_NAV_FOR_PARENT_TYPES"];
                                        var showSampleOnNav = false;
                                        if(!showOnNavForParentTypes) {
                                            showSampleOnNav = true;
                                        } else {
                                            for(var ptIdx = 0; ptIdx < showOnNavForParentTypes.length; ptIdx++) {
                                                if(parentTypeCode === showOnNavForParentTypes[ptIdx]) {
                                                    showSampleOnNav = true;
                                                    break;
                                                }
                                            }
                                        }

                                        if(showSampleOnNav) {
                                            var sampleDisplayName = sample.code;
                                            if(sample.properties && sample.properties[profile.propertyReplacingCode]) {
                                                    sampleDisplayName = sample.properties[profile.propertyReplacingCode];
                                            }
                                            var sampleLink = _this.getLinkForNode(sampleDisplayName, sample.getPermId().getPermId(), "showViewSamplePageFromPermId", sample.getPermId().getPermId(), null);
                                            var sampleNode = {
                                                displayName: sampleDisplayName,
                                                title : sampleLink,
                                                entityType: "SAMPLE",
                                                key : sample.getPermId().getPermId(),
                                                folder : true,
                                                lazy : true,
                                                view : "showViewSamplePageFromPermId",
                                                viewData: sample.getPermId().getPermId(),
                                                icon : sampleIcon,
                                                registrationDate: sample.registrationDate
                                            };
                                            results.push(sampleNode);
                                        }
                                    }
                                }
                            }
                        }
                        
                        var datasetRules = { "UUIDv4" : { type : "Sample", name : "ATTR.PERM_ID", value : permId } };
                        mainController.serverFacade.searchForDataSetsAdvanced({ entityKind : "DATASET", logicalOperator : "AND", rules : datasetRules }, null, function(searchResult) {
                            
                            var datasets = searchResult.objects;

                            if(profile.mainMenu.showDatasets) {
                                if(datasets.length > 50) {
                                    Util.showInfo("More than 50 Datasets, please use the dataset viewer on the sample to navigate them.");
                                } else {
                                    for (var i = 0; i < datasets.length; i++) {
                                        var dataset = datasets[i];
                                        var datasetDisplayName = dataset.code;
                                        if(dataset.properties && dataset.properties[profile.propertyReplacingCode]) {
                                            datasetDisplayName = dataset.properties[profile.propertyReplacingCode];
                                        }
                    
                                        var datasetLink = _this.getLinkForNode(datasetDisplayName, dataset.getPermId().getPermId(), "showViewDataSetPageFromPermId", dataset.getPermId().getPermId(), null);
                                        results.push({
                                            displayName: datasetDisplayName,
                                            title : datasetLink,
                                            entityType: "DATASET",
                                            key : dataset.getPermId().getPermId(),
                                            folder : true,
                                            lazy : false,
                                            view : "showViewDataSetPageFromPermId",
                                            viewData: dataset.getPermId().getPermId(),
                                            icon : "fa fa-database",
                                            registrationDate: dataset.registrationDate,
                                        });
                                    }
                                }
                            }
                            
                            results.sort(sortItems);
                            dfd.resolve(results);
                        });
                    });
                    
                    break;
                case "DATASET":
                    break;
            }
        };
        
        var onActivate = function(event, data) {
            data.node.setExpanded(true);
        };
        
        var onClick = function(event, data) {
            if(data.node.data.view) {
                _this._sideMenuWidgetController._showNodeView(data.node);
            }
        };
        
        var onCollapse = function(event, data) {
            if(data.node.lazy) { //Is going to be collapsed
                data.node.removeChildren();
                data.node.resetLazy();
            }
        };
        
        $tree.fancytree({
            extensions: ["dnd", "edit", "glyph"], //, "wide"
            glyph: glyph_opts,
            source: treeModel,
            lazyLoad : onLazyLoad,
            click : onClick,
            activate: onActivate,
            collapse : onCollapse //Yes, for collapsing event we need to use expand 
        });
        
        this._sideMenuWidgetModel.menuDOMBody.append($tree);
        this._sideMenuWidgetModel.tree = $tree;
        
        var labNotebook = $tree.fancytree("getTree").getNodeByKey("LAB_NOTEBOOK");
        if (labNotebook) {
            labNotebook.setExpanded(true);
        }
        var inventoryNode = $tree.fancytree("getTree").getNodeByKey("INVENTORY");
        if (inventoryNode) {
            inventoryNode.setExpanded(true).done(function(){
                inventoryNode.visit(function(node){
                    node.setExpanded(true).done(function(){
                        node.visit(function(node2){
                            node2.setExpanded(true);
                        })
                    });
                })
            });
        }
        var stock = $tree.fancytree("getTree").getNodeByKey("STOCK");
        if (stock) {
            stock.setExpanded(true);
        }
        
        setCustomIcon($tree, "JUPYTER_WORKSPACE", "./img/jupyter-icon.png");
        setCustomIcon($tree, "NEW_JUPYTER_NOTEBOOK", "./img/jupyter-icon.png");
    };
    
    function setCustomIcon($tree, nodeKey, iconImage) {
        var node = $tree.fancytree("getTree").getNodeByKey(nodeKey);
        if(node) {
            var $customIconNode = $("<span>", { class : "fancytree-custom-icon" }).append($("<img>", { "src" : iconImage, 'style' : 'width:16px; height:16px;'}));
            var $nodeSpan = $(node.span);
            var $nodeSpanIcon = $nodeSpan.find(".fancytree-icon");
            $customIconNode.insertAfter($nodeSpanIcon);
            $nodeSpanIcon.remove();
        }
    }
}

