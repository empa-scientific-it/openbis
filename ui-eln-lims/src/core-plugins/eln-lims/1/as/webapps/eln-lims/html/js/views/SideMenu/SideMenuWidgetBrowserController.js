class SideMenuWidgetBrowserController extends window.NgComponents.default.BrowserController {
    LOAD_TOTAL_LIMIT = 500
    LOAD_LIMIT = 50
    LOAD_REPEAT_LIMIT_FOR_FULL_BATCH = 10

    TYPE_LAB_NOTEBOOK = "LAB_NOTEBOOK"
    TYPE_LAB_NOTEBOOK_OTHERS = "LAB_NOTEBOOK_OTHERS"
    TYPE_LAB_NOTEBOOK_OTHERS_DISABLED = "LAB_NOTEBOOK_OTHERS_DISABLED"
    TYPE_INVENTORY = "INVENTORY"
    TYPE_STOCK = "STOCK"
    TYPE_UTILITIES = "UTILITIES"
    TYPE_EXPORTS = "EXPORTS"

    TYPE_SPACE = "SPACE"
    TYPE_PROJECT = "PROJECT"
    TYPE_EXPERIMENT = "EXPERIMENT"
    TYPE_EXPERIMENT_SAMPLES = "EXPERIMENT_SAMPLES"
    TYPE_EXPERIMENT_DATASETS = "EXPERIMENT_DATA_SETS"
    TYPE_SAMPLE = "SAMPLE"
    TYPE_SAMPLE_CHILDREN = "SAMPLE_CHILDREN"
    TYPE_SAMPLE_DATASETS = "SAMPLE_DATA_SETS"
    TYPE_DATASET = "DATASET"

    TYPE_JUPYTER_WORKSPACE = "JUPYTER_WORKSPACE"
    TYPE_NEW_JUPYTER_NOTEBOOK = "NEW_JUPYTER_NOTEBOOK"
    TYPE_USER_PROFILE = "USER_PROFILE"
    TYPE_GENERATE_BARCODES = "GENERATE_BARCODES"
    TYPE_DRAWING_BOARD = "DRAWING_BOARD"
    TYPE_SAMPLE_BROWSER = "SAMPLE_BROWSER"
    TYPE_VOCABULARY_BROWSER = "VOCABULARY_BROWSER"
    TYPE_ADVANCED_SEARCH = "ADVANCED_SEARCH"
    TYPE_DROPBOX_MONITOR = "DROPBOX_MONITOR"
    TYPE_ARCHIVING_HELPER = "ARCHIVING_HELPER"
    TYPE_UNARCHIVING_HELPER = "UNARCHIVING_HELPER"
    TYPE_CUSTOM_IMPORT = "CUSTOM_IMPORT"
    TYPE_STORAGE_MANAGER = "STORAGE_MANAGER"
    TYPE_USER_MANAGER = "USER_MANAGER"
    TYPE_USER_MANAGEMENT_CONFIG = "USER_MANAGEMENT_CONFIG"
    TYPE_TRASHCAN = "TRASHCAN"
    TYPE_SETTINGS = "SETTINGS"
    TYPE_OTHER_TOOLS = "OTHERTOOLS"
    TYPE_EXTRA_PLUGIN_UTILITY = "EXTRA_PLUGIN_UTILITY"
    TYPE_EXPORT_TO_ZIP = "EXPORT_TO_ZIP"
    TYPE_EXPORT_TO_RESEARCH_COLLECTION = "EXPORT_TO_RESEARCH_COLLECTION"
    TYPE_EXPORT_TO_ZENODO = "EXPORT_TO_ZENODO"
    TYPE_ABOUT = "ABOUT"

    SORTINGS_BY_NAME = [
        {
            id: "name_asc",
            label: "Name Ascending",
            server: [
                {
                    type: "Property",
                    name: "$NAME",
                    direction: "asc",
                },
                {
                    type: "Attribute",
                    name: "code",
                    direction: "asc",
                },
            ],
            default: true,
        },
        {
            id: "name_desc",
            label: "Name Descending",
            server: [
                {
                    type: "Property",
                    name: "$NAME",
                    direction: "desc",
                },
                {
                    type: "Attribute",
                    name: "code",
                    direction: "desc",
                },
            ],
        },
    ]

    SORTINGS_BY_CODE = [
        {
            id: "code_asc",
            label: "Name Ascending",
            server: [
                {
                    type: "Attribute",
                    name: "code",
                    direction: "asc",
                },
            ],
            default: true,
        },
        {
            id: "code_desc",
            label: "Name Descending",
            server: [
                {
                    type: "Attribute",
                    name: "code",
                    direction: "desc",
                },
            ],
        },
    ]

    SORTINGS_BY_REGISTRATION_DATE = [
        {
            id: "registration_date_asc",
            label: "Registration Date Ascending",
            server: [
                {
                    type: "Attribute",
                    name: "registrationDate",
                    direction: "asc",
                },
            ],
        },
        {
            id: "registration_date_desc",
            label: "Registration Date Descending",
            server: [
                {
                    type: "Attribute",
                    name: "registrationDate",
                    direction: "desc",
                },
            ],
        },
    ]

    SORTINGS_BY_NAME_AND_REGISTRATION_DATE = [].concat(this.SORTINGS_BY_NAME).concat(this.SORTINGS_BY_REGISTRATION_DATE)
    SORTINGS_BY_CODE_AND_REGISTRATION_DATE = [].concat(this.SORTINGS_BY_CODE).concat(this.SORTINGS_BY_REGISTRATION_DATE)

    async _loadHomeSpace() {
        return new Promise((resolve) => {
            if (this._cachedHomeSpace) {
                resolve(this._cachedHomeSpace)
            } else {
                profile.getHomeSpace((HOME_SPACE) => {
                    this._cachedHomeSpace = HOME_SPACE
                    resolve(HOME_SPACE)
                })
            }
        })
    }

    async _loadSpacesDisabledStatus() {
        return new Promise((resolve) => {
            if (this._cachedSpacesDisabledStatus) {
                resolve(this._cachedSpacesDisabledStatus)
            } else {
                var spaceCriteria = {
                    entityKind: "SPACE",
                    logicalOperator: "AND",
                    rules: {},
                }
                var spaceFetchOptions = {
                    only: true,
                }

                mainController.serverFacade.searchForSpacesAdvanced(
                    spaceCriteria,
                    spaceFetchOptions,
                    (searchResult) => {
                        var spaceCodes = searchResult.getObjects().map((space) => {
                            return space.getCode()
                        })
                        mainController.serverFacade.customELNASAPI(
                            {
                                method: "doSpacesBelongToDisabledUsers",
                                spaceCodes: spaceCodes,
                            },
                            (disabledSpaceCodes) => {
                                var result = {}

                                spaceCodes.forEach((spaceCode) => {
                                    result[spaceCode] = false
                                })

                                disabledSpaceCodes.forEach((disabledSpaceCode) => {
                                    result[disabledSpaceCode] = true
                                })

                                this._cachedSpacesDisabledStatus = result

                                resolve(result)
                            }
                        )
                    }
                )
            }
        })
    }

    async _loadSpace(spaceCode) {
        return new Promise((resolve) => {
            var spaceCriteria = {
                entityKind: "SPACE",
                logicalOperator: "AND",
                rules: {
                    [Util.guid()]: { type: "Attribute", name: "CODE", value: spaceCode, operator: "thatEquals" },
                },
            }
            var spaceFetchOptions = {
                only: true,
            }

            mainController.serverFacade.searchForSpacesAdvanced(spaceCriteria, spaceFetchOptions, (searchResult) => {
                resolve(searchResult.objects.length > 0 ? searchResult.objects[0] : null)
            })
        })
    }

    async _loadProject(projectPermId) {
        return new Promise((resolve) => {
            var projectCriteria = {
                entityKind: "PROJECT",
                logicalOperator: "AND",
                rules: {
                    [Util.guid()]: { type: "Attribute", name: "PERM_ID", value: projectPermId, operator: "thatEquals" },
                },
            }
            var projectFetchOptions = {
                only: true,
                withSpace: true,
            }

            mainController.serverFacade.searchForProjectsAdvanced(
                projectCriteria,
                projectFetchOptions,
                (searchResult) => {
                    resolve(searchResult.objects.length > 0 ? searchResult.objects[0] : null)
                }
            )
        })
    }

    async _loadExperiment(experimentPermId) {
        return new Promise((resolve) => {
            var experimentCriteria = {
                entityKind: "EXPERIMENT",
                logicalOperator: "AND",
                rules: {
                    [Util.guid()]: {
                        type: "Attribute",
                        name: "PERM_ID",
                        value: experimentPermId,
                        operator: "thatEquals",
                    },
                },
            }
            var experimentFetchOptions = {
                only: true,
                withType: true,
                withProperties: true,
                withProject: true,
            }

            mainController.serverFacade.searchForExperimentsAdvanced(
                experimentCriteria,
                experimentFetchOptions,
                (searchResult) => {
                    resolve(searchResult.objects.length > 0 ? searchResult.objects[0] : null)
                }
            )
        })
    }

    async _loadSample(samplePermId) {
        return new Promise((resolve) => {
            var sampleCriteria = {
                entityKind: "SAMPLE",
                logicalOperator: "AND",
                rules: {
                    [Util.guid()]: {
                        type: "Attribute",
                        name: "PERM_ID",
                        value: samplePermId,
                        operator: "thatEquals",
                    },
                },
            }
            var sampleFetchOptions = {
                only: true,
                withType: true,
                withProperties: true,
                withExperiment: true,
                withExperimentType: true,
                withParents: true,
                withParentsExperiment: true,
            }

            mainController.serverFacade.searchForSamplesAdvanced(sampleCriteria, sampleFetchOptions, (searchResult) => {
                resolve(searchResult.objects.length > 0 ? searchResult.objects[0] : null)
            })
        })
    }

    async _loadDataSet(dataSetCode) {
        return new Promise((resolve) => {
            var dataSetCriteria = {
                entityKind: "DATASET",
                logicalOperator: "AND",
                rules: {
                    [Util.guid()]: {
                        type: "Attribute",
                        name: "CODE",
                        value: dataSetCode,
                        operator: "thatEquals",
                    },
                },
            }
            var dataSetFetchOptions = {
                only: true,
                withType: true,
                withProperties: true,
                withExperiment: true,
                withExperimentType: true,
                withSample: true,
            }

            mainController.serverFacade.searchForDataSetsAdvanced(
                dataSetCriteria,
                dataSetFetchOptions,
                (searchResult) => {
                    resolve(searchResult.objects.length > 0 ? searchResult.objects[0] : null)
                }
            )
        })
    }

    async loadNodePath(params) {
        var { root, object } = params

        var path = []

        if (object.type === this.TYPE_LAB_NOTEBOOK) {
            path.push(this._createLabNotebookNode())
        } else if (object.type === this.TYPE_LAB_NOTEBOOK_OTHERS) {
            path.push(this._createLabNotebookNode())
            path.push(this._createLabNotebookOthersNode())
        } else if (object.type === this.TYPE_LAB_NOTEBOOK_OTHERS_DISABLED) {
            path.push(this._createLabNotebookNode())
            path.push(this._createLabNotebookOthersDisabledNode())
        } else if (object.type === this.TYPE_INVENTORY) {
            path.push(this._createInventoryNode())
        } else if (object.type === this.TYPE_STOCK) {
            path.push(this._createStockNode())
        } else if (object.type === this.TYPE_SPACE) {
            var spacePath = await this._loadNodePathSpace(params)
            path.push(...spacePath)
        } else if (object.type === this.TYPE_PROJECT) {
            var projectPath = await this._loadNodePathProject(params)
            path.push(...projectPath)
        } else if (object.type === this.TYPE_EXPERIMENT) {
            var experimentPath = await this._loadNodePathExperiment(params)
            path.push(...experimentPath)
        } else if (object.type === this.TYPE_SAMPLE) {
            var samplePath = await this._loadNodePathSample(params)
            path.push(...samplePath)
        } else if (object.type === this.TYPE_DATASET) {
            var dataSetPath = await this._loadNodePathDataSet(params)
            path.push(...dataSetPath)
        } else if (object.type === this.TYPE_JUPYTER_WORKSPACE) {
            path.push(this._createUtilitiesNode())
            path.push(this._createJupyterWorkspaceNode())
        } else if (object.type === this.TYPE_NEW_JUPYTER_NOTEBOOK) {
            path.push(this._createUtilitiesNode())
            path.push(this._createNewJupyterNotebookNode())
        } else if (object.type === this.TYPE_USER_PROFILE) {
            path.push(this._createUtilitiesNode())
            path.push(this._createUserProfileNode())
        } else if (object.type === this.TYPE_GENERATE_BARCODES) {
            path.push(this._createUtilitiesNode())
            path.push(this._createBarcodesGeneratorNode())
        } else if (object.type === this.TYPE_DRAWING_BOARD) {
            path.push(this._createUtilitiesNode())
            path.push(this._createDrawingBoardNode())
        } else if (object.type === this.TYPE_SAMPLE_BROWSER) {
            path.push(this._createUtilitiesNode())
            path.push(this._createObjectBrowserNode())
        } else if (object.type === this.TYPE_VOCABULARY_BROWSER) {
            path.push(this._createUtilitiesNode())
            path.push(this._createVocabularyBrowserNode())
        } else if (object.type === this.TYPE_ADVANCED_SEARCH) {
            path.push(this._createUtilitiesNode())
            path.push(this._createAdvancedSearchNode())
        } else if (object.type === this.TYPE_DROPBOX_MONITOR) {
            path.push(this._createUtilitiesNode())
            path.push(this._createDropboxMonitorNode())
        } else if (object.type === this.TYPE_ARCHIVING_HELPER) {
            path.push(this._createUtilitiesNode())
            path.push(this._createArchivingHelperNode())
        } else if (object.type === this.TYPE_UNARCHIVING_HELPER) {
            path.push(this._createUtilitiesNode())
            path.push(this._createUnarchivingHelperNode())
        } else if (object.type === this.TYPE_CUSTOM_IMPORT) {
            path.push(this._createUtilitiesNode())
            path.push(this._createCustomImportNode())
        } else if (object.type === this.TYPE_STORAGE_MANAGER) {
            path.push(this._createUtilitiesNode())
            path.push(this._createStorageManagerNode())
        } else if (object.type === this.TYPE_USER_MANAGER) {
            path.push(this._createUtilitiesNode())
            path.push(this._createUserManagerNode())
        } else if (object.type === this.TYPE_USER_MANAGEMENT_CONFIG) {
            path.push(this._createUtilitiesNode())
            path.push(this._createUserManagementConfigNode())
        } else if (object.type === this.TYPE_TRASHCAN) {
            path.push(this._createUtilitiesNode())
            path.push(this._createTrashcanNode())
        } else if (object.type === this.TYPE_SETTINGS) {
            path.push(this._createUtilitiesNode())
            path.push(this._createSettingsNode())
        } else if (object.type === this.TYPE_OTHER_TOOLS) {
            path.push(this._createUtilitiesNode())
            path.push(this._createOtherToolsNode())
        } else if (object.type === this.TYPE_EXPORT_TO_ZIP) {
            path.push(this._createUtilitiesNode())
            path.push(this._createExportsNode())
            path.push(this._createExportToZipNode())
        } else if (object.type === this.TYPE_EXPORT_TO_RESEARCH_COLLECTION) {
            path.push(this._createUtilitiesNode())
            path.push(this._createExportsNode())
            path.push(this._createExportToResearchCollectionNode())
        } else if (object.type === this.TYPE_EXPORT_TO_ZENODO) {
            path.push(this._createUtilitiesNode())
            path.push(this._createExportsNode())
            path.push(this._createExportToZenodoNode())
        }

        if (path.some((pathItem) => !pathItem)) {
            return []
        }

        var pathFromRoot = [...path]

        if (root) {
            var index = pathFromRoot.findIndex(
                (pathItem) => pathItem.object.type === root.object.type && pathItem.object.id === root.object.id
            )
            if (index !== -1) {
                pathFromRoot = pathFromRoot.slice(index + 1)
            }
        }

        return pathFromRoot
    }

    async _loadNodePathSpace(params) {
        var { object } = params

        var [HOME_SPACE, spacesDisabledStatus, space] = await Promise.all([
            this._loadHomeSpace(),
            this._loadSpacesDisabledStatus(),
            this._loadSpace(object.id),
        ])

        var path = []

        if (space) {
            var rootNode = window.NgComponents.default.BrowserCommon.rootNode()
            path.push(rootNode)
            if (this._isLabNotebookSpace(space)) {
                path.push(this._createLabNotebookNode())

                if (space.code === HOME_SPACE) {
                    path.push(this._createSpaceNode(space, true))
                } else if (spacesDisabledStatus[space.code]) {
                    path.push(this._createLabNotebookOthersDisabledNode())
                    path.push(this._createSpaceNode(space, false))
                } else {
                    path.push(this._createLabNotebookOthersNode())
                    path.push(this._createSpaceNode(space, false))
                }
            } else if (this._isInventorySpace(space)) {
                path.push(this._createInventoryNode())
                path.push(this._createSpaceNode(space, false))
            } else if (this._isStockSpace(space)) {
                path.push(this._createStockNode())
                path.push(this._createSpaceNode(space, false))
            }
        }

        return path
    }

    async _loadNodePathProject(params) {
        var { object } = params

        var project = await this._loadProject(object.id)
        var path = []

        if (project && !this._isQueriesProject(project)) {
            var spacePath = await this.loadNodePath({
                object: {
                    type: this.TYPE_SPACE,
                    id: project.getSpace().getCode(),
                },
            })

            if (spacePath.length > 0) {
                path.push(...spacePath)
                path.push(this._createProjectNode(project))
            }
        }

        return path
    }

    async _loadNodePathExperiment(params) {
        var { object } = params

        var experiment = await this._loadExperiment(object.id)
        var path = []

        if (experiment) {
            var projectPath = await this.loadNodePath({
                object: {
                    type: this.TYPE_PROJECT,
                    id: experiment.getProject().getPermId().getPermId(),
                },
            })

            if (projectPath.length > 0) {
                path.push(...projectPath)
                path.push(this._createExperimentNode(experiment))
            }
        }

        return path
    }

    async _loadNodePathSample(params) {
        var { object } = params

        var sample = await this._loadSample(object.id)
        var path = []

        if (sample) {
            if (this._isExperimentSample(sample)) {
                if (this._isExperimentWithChildren(sample.getExperiment())) {
                    var experimentPath = await this.loadNodePath({
                        object: {
                            type: this.TYPE_EXPERIMENT,
                            id: sample.getExperiment().getPermId().getPermId(),
                        },
                    })

                    if (experimentPath.length > 0) {
                        path.push(...experimentPath)
                        path.push(this._createExperimentSamplesNode())
                        path.push(this._createSampleNode(sample))
                    }
                }
            } else if (this._isChildSample(sample)) {
                var parent = this._getSampleParent(sample)
                if (parent) {
                    var parentPath = await this.loadNodePath({
                        object: {
                            type: this.TYPE_SAMPLE,
                            id: parent.getPermId().getPermId(),
                        },
                    })
                    if (parentPath.length > 0) {
                        path.push(...parentPath)
                        path.push(this._createSampleChildrenNode())
                        path.push(this._createSampleNode(sample))
                    }
                }
            }
        }

        return path
    }

    async _loadNodePathDataSet(params) {
        var { object } = params

        var dataSet = await this._loadDataSet(object.id)
        var path = []

        if (dataSet) {
            if (this._isExperimentDataSet(dataSet)) {
                if (this._isExperimentWithChildren(dataSet.getExperiment())) {
                    var experimentPath = await this.loadNodePath({
                        object: {
                            type: this.TYPE_EXPERIMENT,
                            id: dataSet.getExperiment().getPermId().getPermId(),
                        },
                    })

                    if (experimentPath.length > 0) {
                        path.push(...experimentPath)
                        path.push(this._createExperimentDataSetsNode())
                        path.push(this._createDataSetNode(dataSet))
                    }
                }
            } else if (this._isSampleDataSet(dataSet)) {
                var samplePath = await this.loadNodePath({
                    object: {
                        type: this.TYPE_SAMPLE,
                        id: dataSet.getSample().getPermId().getPermId(),
                    },
                })

                if (samplePath.length > 0) {
                    path.push(...samplePath)
                    path.push(this._createSampleDataSetsNode())
                    path.push(this._createDataSetNode(dataSet))
                }
            }
        }

        return path
    }

    loadSettings() {
        return new Promise((resolve) => {
            mainController.serverFacade.getSetting("eln-main-browser", (settingsStr) => {
                if (settingsStr) {
                    resolve(JSON.parse(settingsStr))
                } else {
                    resolve({})
                }
            })
        })
    }

    onSettingsChange(settings) {
        if (settings) {
            mainController.serverFacade.setSetting("eln-main-browser", JSON.stringify(settings))
        }
    }

    onSelectedChange(params) {
        var event = params.event

        if (event && event.ignore) {
            return
        }

        var node = params.nodes.length > 0 ? params.nodes[0] : null

        function isEmpty(value) {
            return value === null || value === undefined || value === "null" || value === "undefined"
        }

        if (node) {
            var view = node.view
            var viewData = node.viewData
            var lastView = mainController.lastViewChange
            var lastViewData = mainController.lastArg

            if (isEmpty(viewData)) {
                viewData = null
            }
            if (isEmpty(lastViewData)) {
                lastViewData = null
            }

            if (view && (view !== lastView || viewData !== lastViewData)) {
                mainController.changeView(view, viewData)
            }
        }
    }

    onError(error) {
        Util.showError(error)
    }

    async loadNodes(params) {
        if (params.node.internalRoot) {
            return {
                nodes: [window.NgComponents.default.BrowserCommon.rootNode()],
            }
        }

        if (params.filter !== null && params.filter !== undefined && params.filter.trim().length > 0) {
            return this._loadFilteredNodes(params)
        } else {
            return this._loadNodes(params)
        }
    }

    async _loadFilteredNodes(params) {
        var { node } = params

        var [spaces, projects, experiments, samples, dataSets] = await Promise.all([
            this._loadFilteredSpaces(params),
            this._loadFilteredProjects(params),
            this._loadFilteredExperiments(params),
            this._loadFilteredSamples(params),
            this._loadFilteredDataSets(params),
        ])

        var totalCount =
            spaces.totalCount + projects.totalCount + experiments.totalCount + samples.totalCount + dataSets.totalCount

        if (totalCount > this.LOAD_TOTAL_LIMIT) {
            return {
                nodes: [window.NgComponents.default.BrowserCommon.tooManyResultsFound()],
                totalCount: 1,
            }
        }

        var entities = {
            spaces: {},
            projects: {},
            experiments: {},
            samples: {},
            dataSets: {},
        }

        spaces.objects.forEach((space) => {
            this._addFilteredSpace(entities, space)
        })

        projects.objects.forEach((project) => {
            this._addFilteredProject(entities, project)
        })

        experiments.objects.forEach((experiment) => {
            this._addFilteredExperiment(entities, experiment)
        })

        samples.objects.forEach((sample) => {
            this._addFilteredSample(entities, sample)
        })

        dataSets.objects.forEach((dataSet) => {
            this._addFilteredDataSet(entities, dataSet)
        })

        var results = {
            nodes: [],
            totalCount: totalCount,
        }

        if (node.root) {
            var [labNotebookNode, inventoryNode, stockNode] = await Promise.all([
                this._createFilteredLabNotebookNode(entities),
                this._createFilteredInventoryNode(entities),
                this._createFilteredStockNode(entities),
            ])

            if (labNotebookNode) {
                results.nodes.push(labNotebookNode)
            }

            if (inventoryNode) {
                results.nodes.push(inventoryNode)
            }

            if (stockNode) {
                results.nodes.push(stockNode)
            }
        } else if (node.object.type === this.TYPE_SPACE) {
            var space = entities.spaces[node.object.id]
            if (space) {
                Object.values(space.projects).forEach((project) => {
                    var projectNode = this._createFilteredProjectNode(project)
                    results.nodes.push(projectNode)
                })
                this._sortResultsByText(results)
            }
        } else if (node.object.type === this.TYPE_PROJECT) {
            var project = entities.projects[node.object.id]
            if (project) {
                Object.values(project.experiments).forEach((experiment) => {
                    var experimentNode = this._createFilteredExperimentNode(experiment)
                    results.nodes.push(experimentNode)
                })
                this._sortResultsByText(results)
            }
        } else if (node.object.type === this.TYPE_EXPERIMENT) {
            var experiment = entities.experiments[node.object.id]
            if (experiment) {
                var samplesNode = this._createFilteredExperimentSamplesNode(experiment)
                if (samplesNode) {
                    results.nodes.push(samplesNode)
                }

                var dataSetsNode = this._createFilteredExperimentDataSetsNode(experiment)
                if (dataSetsNode) {
                    results.nodes.push(dataSetsNode)
                }
            }
        }

        var loadedCount =
            spaces.objects.length +
            projects.objects.length +
            experiments.objects.length +
            samples.objects.length +
            dataSets.objects.length

        if (loadedCount < totalCount) {
            var loadMoreNode = window.NgComponents.default.BrowserCommon.loadMoreResults()
            loadMoreNode.onClick = () => {
                this.loadNode(node.id, 0, this.LOAD_TOTAL_LIMIT, false)
            }
            results.nodes.push(loadMoreNode)
        }

        results.totalCount = results.nodes.length

        return results
    }

    async _loadFilteredSpaces(params) {
        var { node, filter } = params

        if (node && !node.root) {
            return {
                objects: [],
                totalCount: 0,
            }
        }

        var codeFilter = filter.toUpperCase().replaceAll(/\s+/g, "_")

        var spaceCriteria = {
            entityKind: "SPACE",
            logicalOperator: "AND",
            rules: {
                [Util.guid()]: { type: "Attribute", name: "CODE", value: codeFilter, operator: "thatStartsWith" },
            },
        }
        var spaceFetchOptions = {
            only: true,
            from: params.offset,
            count: params.limit || this.LOAD_LIMIT,
            sortings: this.SORTINGS_BY_CODE[0].server,
        }

        return new Promise((resolve) => {
            mainController.serverFacade.searchForSpacesAdvanced(spaceCriteria, spaceFetchOptions, resolve)
        })
    }

    async _loadFilteredProjects(params) {
        var { node, filter } = params

        var codeFilter = filter.toUpperCase().replaceAll(/\s+/g, "_")

        var projectCriteria = {
            entityKind: "PROJECT",
            logicalOperator: "AND",
            rules: {
                [Util.guid()]: { type: "Attribute", name: "CODE", value: codeFilter, operator: "thatStartsWith" },
            },
        }

        if (node && !node.root) {
            if (node.object.type === this.TYPE_SPACE) {
                projectCriteria.rules[Util.guid()] = { type: "Attribute", name: "SPACE", value: node.object.id }
            } else {
                return {
                    objects: [],
                    totalCount: 0,
                }
            }
        }

        var projectFetchOptions = {
            only: true,
            withSpace: true,
            from: params.offset,
            count: params.limit || this.LOAD_LIMIT,
            sortings: this.SORTINGS_BY_CODE[0].server,
        }

        return new Promise((resolve) => {
            mainController.serverFacade.searchForProjectsAdvanced(projectCriteria, projectFetchOptions, resolve)
        })
    }

    async _loadFilteredExperiments(params) {
        var { node, filter } = params

        var experimentCriteria = {
            entityKind: "EXPERIMENT",
            logicalOperator: "AND",
            rules: {},
            subCriteria: [
                {
                    logicalOperator: "OR",
                    rules: {
                        [Util.guid()]: {
                            type: "Property",
                            name: "$NAME",
                            value: filter,
                            operator: "thatStartsWithString",
                        },
                        [Util.guid()]: { type: "Attribute", name: "CODE", value: filter, operator: "thatStartsWith" },
                    },
                },
            ],
        }

        if (node && !node.root) {
            if (node.object.type === this.TYPE_SPACE) {
                experimentCriteria.subCriteria.push({
                    logicalOperator: "AND",
                    rules: {
                        [Util.guid()]: { type: "Project", name: "ATTR.SPACE", value: node.object.id },
                    },
                })
            } else if (node.object.type === this.TYPE_PROJECT) {
                experimentCriteria.subCriteria.push({
                    logicalOperator: "AND",
                    rules: {
                        [Util.guid()]: { type: "Project", name: "ATTR.PERM_ID", value: node.object.id },
                    },
                })
            } else {
                return {
                    objects: [],
                    totalCount: 0,
                }
            }
        }

        var experimentFetchOptions = {
            only: true,
            withType: true,
            withProperties: true,
            withProject: true,
            withProjectSpace: true,
            from: params.offset,
            count: params.limit || this.LOAD_LIMIT,
            sortings: [].concat(this.SORTINGS_BY_NAME[0].server).concat(this.SORTINGS_BY_CODE[0].server),
        }

        return new Promise((resolve) => {
            mainController.serverFacade.searchForExperimentsAdvanced(
                experimentCriteria,
                experimentFetchOptions,
                resolve
            )
        })
    }

    async _loadFilteredSamples(params) {
        var { node, filter } = params

        var sampleCriteria = {
            entityKind: "SAMPLE",
            logicalOperator: "AND",
            rules: {},
            subCriteria: [
                {
                    logicalOperator: "OR",
                    rules: {
                        [Util.guid()]: {
                            type: "Property",
                            name: "$NAME",
                            value: filter,
                            operator: "thatStartsWithString",
                        },
                        [Util.guid()]: { type: "Attribute", name: "CODE", value: filter, operator: "thatStartsWith" },
                    },
                },
            ],
        }

        if (node && !node.root) {
            if (node.object.type === this.TYPE_SPACE) {
                sampleCriteria.subCriteria.push({
                    logicalOperator: "AND",
                    rules: {
                        [Util.guid()]: { type: "Experiment", name: "ATTR.PROJECT_SPACE", value: node.object.id },
                    },
                })
            } else if (node.object.type === this.TYPE_PROJECT) {
                sampleCriteria.subCriteria.push({
                    logicalOperator: "AND",
                    rules: {
                        [Util.guid()]: { type: "Experiment", name: "ATTR.PROJECT_PERM_ID", value: node.object.id },
                    },
                })
            } else if (node.object.type === this.TYPE_EXPERIMENT) {
                sampleCriteria.subCriteria.push({
                    logicalOperator: "AND",
                    rules: {
                        [Util.guid()]: { type: "Experiment", name: "ATTR.PERM_ID", value: node.object.id },
                    },
                })
            } else {
                return {
                    objects: [],
                    totalCount: 0,
                }
            }
        } else {
            sampleCriteria.subCriteria.push({
                logicalOperator: "AND",
                rules: {
                    [Util.guid()]: { type: "Experiment", name: "NOT_NULL.NOT_NULL", value: "NOT_NULL" },
                },
            })
        }

        var sampleFetchOptions = {
            only: true,
            withType: true,
            withProperties: true,
            withExperiment: true,
            withExperimentType: true,
            withExperimentProperties: true,
            withExperimentProject: true,
            withExperimentProjectSpace: true,
            withParents: true,
            withParentsType: true,
            withParentsProperties: true,
            withParentsExperiment: true,
            withParentsExperimentType: true,
            withParentsExperimentProperties: true,
            withParentsExperimentProject: true,
            withParentsExperimentProjectSpace: true,
            withParentsParents: true,
            withParentsParentsExperiment: true,
            from: params.offset,
            count: params.limit || this.LOAD_LIMIT,
            sortings: [].concat(this.SORTINGS_BY_NAME[0].server).concat(this.SORTINGS_BY_CODE[0].server),
        }

        return new Promise((resolve) => {
            mainController.serverFacade.searchForSamplesAdvanced(sampleCriteria, sampleFetchOptions, resolve)
        })
    }

    async _loadFilteredDataSets(params) {
        var { node, filter } = params

        var datasetCriteria = {
            entityKind: "DATASET",
            logicalOperator: "AND",
            rules: {},
            subCriteria: [
                {
                    logicalOperator: "OR",
                    rules: {
                        [Util.guid()]: {
                            type: "Property",
                            name: "$NAME",
                            value: filter,
                            operator: "thatStartsWithString",
                        },
                        [Util.guid()]: { type: "Attribute", name: "CODE", value: filter, operator: "thatStartsWith" },
                    },
                },
            ],
        }

        if (node && !node.root) {
            if (node.object.type === this.TYPE_SPACE) {
                datasetCriteria.subCriteria.push({
                    logicalOperator: "AND",
                    rules: {
                        [Util.guid()]: { type: "Experiment", name: "ATTR.PROJECT_SPACE", value: node.object.id },
                    },
                })
            } else if (node.object.type === this.TYPE_PROJECT) {
                datasetCriteria.subCriteria.push({
                    logicalOperator: "AND",
                    rules: {
                        [Util.guid()]: { type: "Experiment", name: "ATTR.PROJECT_PERM_ID", value: node.object.id },
                    },
                })
            } else if (node.object.type === this.TYPE_EXPERIMENT) {
                datasetCriteria.subCriteria.push({
                    logicalOperator: "AND",
                    rules: {
                        [Util.guid()]: { type: "Experiment", name: "ATTR.PERM_ID", value: node.object.id },
                    },
                })
            } else {
                return {
                    objects: [],
                    totalCount: 0,
                }
            }
        } else {
            datasetCriteria.subCriteria.push({
                logicalOperator: "AND",
                rules: {
                    [Util.guid()]: { type: "Experiment", name: "NOT_NULL.NOT_NULL", value: "NOT_NULL" },
                },
            })
        }

        var datasetFetchOptions = {
            only: true,
            withType: true,
            withProperties: true,
            withExperiment: true,
            withExperimentType: true,
            withExperimentProperties: true,
            withExperimentProject: true,
            withExperimentProjectSpace: true,
            withSample: true,
            withSampleType: true,
            withSampleProperties: true,
            withSampleExperiment: true,
            withSampleExperimentType: true,
            withSampleExperimentProperties: true,
            withSampleExperimentProject: true,
            withSampleExperimentProjectSpace: true,
            withSampleParents: true,
            withSampleParentsType: true,
            withSampleParentsProperties: true,
            withSampleParentsExperiment: true,
            withSampleParentsExperimentType: true,
            withSampleParentsExperimentProperties: true,
            withSampleParentsExperimentProject: true,
            withSampleParentsExperimentProjectSpace: true,
            withSampleParentsParents: true,
            withSampleParentsParentsExperiment: true,

            from: params.offset,
            count: params.limit || this.LOAD_LIMIT,
            sortings: [].concat(this.SORTINGS_BY_NAME[0].server).concat(this.SORTINGS_BY_CODE[0].server),
        }

        return new Promise((resolve) => {
            mainController.serverFacade.searchForDataSetsAdvanced(datasetCriteria, datasetFetchOptions, resolve)
        })
    }

    _addFilteredSpace(entities, space) {
        if (!this._isLabNotebookSpace(space) && !this._isInventorySpace(space) && !this._isStockSpace(space)) {
            return null
        }

        var existingSpace = entities.spaces[space.getCode()]

        if (!existingSpace) {
            var newSpace = {
                original: space,
                code: space.getCode(),
                projects: {},
            }
            entities.spaces[space.getCode()] = newSpace
            return newSpace
        } else {
            return existingSpace
        }
    }

    _addFilteredProject(entities, project) {
        if (this._isQueriesProject(project)) {
            return null
        }

        var existingProject = entities.projects[project.getPermId().getPermId()]

        if (!existingProject) {
            var newProject = {
                original: project,
                code: project.getCode(),
                permId: project.getPermId().getPermId(),
                experiments: {},
            }

            var space = this._addFilteredSpace(entities, project.getSpace())

            if (space) {
                space.projects[newProject.permId] = newProject
                entities.projects[newProject.permId] = newProject
                return newProject
            }

            return null
        } else {
            return existingProject
        }
    }

    _addFilteredExperiment(entities, experiment) {
        var existingExperiment = entities.experiments[experiment.getPermId().getPermId()]
        if (!existingExperiment) {
            var newExperiment = {
                original: experiment,
                code: experiment.getCode(),
                permId: experiment.getPermId().getPermId(),
                samples: {},
                dataSets: {},
            }

            var project = this._addFilteredProject(entities, experiment.getProject())

            if (project) {
                project.experiments[newExperiment.permId] = newExperiment
                entities.experiments[newExperiment.permId] = newExperiment
                return newExperiment
            } else {
                return null
            }
        } else {
            return existingExperiment
        }
    }

    _addFilteredSample(entities, sample) {
        var existingSample = entities.samples[sample.getPermId().getPermId()]
        if (!existingSample) {
            var newSample = {
                original: sample,
                code: sample.getCode(),
                permId: sample.getPermId().getPermId(),
                children: {},
                dataSets: {},
            }

            if (this._isExperimentSample(sample)) {
                if (this._isExperimentWithChildren(sample.getExperiment())) {
                    var experiment = this._addFilteredExperiment(entities, sample.getExperiment())
                    if (experiment) {
                        experiment.samples[newSample.permId] = newSample
                        entities.samples[newSample.permId] = newSample
                        return newSample
                    }
                }
            } else {
                // only first level children
                var parent = this._getSampleParent(sample)
                if (parent && this._isExperimentSample(parent)) {
                    var addedParent = this._addFilteredSample(entities, parent)
                    if (addedParent) {
                        addedParent.children[newSample.permId] = newSample
                        entities.samples[newSample.permId] = newSample
                        return newSample
                    }
                }
            }

            return null
        } else {
            return existingSample
        }
    }

    _addFilteredDataSet(entities, dataSet) {
        var existingDataSet = entities.dataSets[dataSet.getPermId().getPermId()]
        if (!existingDataSet) {
            var newDataSet = {
                original: dataSet,
                code: dataSet.getCode(),
                permId: dataSet.getPermId().getPermId(),
            }

            if (this._isExperimentDataSet(dataSet)) {
                if (this._isExperimentWithChildren(dataSet.getExperiment())) {
                    var space = IdentifierUtil.getSpaceCodeFromIdentifier(
                        dataSet.getExperiment().getIdentifier().getIdentifier()
                    )
                    var showDatasets = SettingsManagerUtils.isEnabledForGroup(
                        space,
                        SettingsManagerUtils.ShowSetting.showDatasets
                    )
                    if (showDatasets) {
                        var experiment = this._addFilteredExperiment(entities, dataSet.getExperiment())
                        if (experiment) {
                            if (showDatasets) {
                                experiment.dataSets[newDataSet.permId] = newDataSet
                                entities.dataSets[newDataSet.permId] = newDataSet
                                return newDataSet
                            }
                        }
                    }
                }
            } else if (this._isSampleDataSet(dataSet) && dataSet.getSample().getExperiment()) {
                var space = IdentifierUtil.getSpaceCodeFromIdentifier(
                    dataSet.getSample().getExperiment().getIdentifier().getIdentifier()
                )
                var showDatasets = SettingsManagerUtils.isEnabledForGroup(
                    space,
                    SettingsManagerUtils.ShowSetting.showDatasets
                )
                if (showDatasets) {
                    var sample = this._addFilteredSample(entities, dataSet.getSample())
                    if (sample) {
                        sample.dataSets[newDataSet.permId] = newDataSet
                        entities.dataSets[newDataSet.permId] = newDataSet
                        return newDataSet
                    }
                }
            }

            return null
        } else {
            return existingDataSet
        }
    }

    async _createFilteredLabNotebookNode(entities) {
        var labNotebookSpaces = Object.values(entities.spaces).filter((space) => {
            return this._isLabNotebookSpace(space.original)
        })

        if (labNotebookSpaces.length > 0) {
            var labNotebookNode = this._createLabNotebookNode()

            if (labNotebookNode) {
                labNotebookNode.children = { nodes: [] }
                labNotebookNode.expanded = true

                var othersNode = this._createLabNotebookOthersNode()
                othersNode.children = { nodes: [] }
                othersNode.expanded = true

                var othersDisabledNode = this._createLabNotebookOthersDisabledNode()
                othersDisabledNode.children = { nodes: [] }
                othersDisabledNode.expanded = true

                var [HOME_SPACE, spacesDisabledStatus] = await Promise.all([
                    this._loadHomeSpace(),
                    this._loadSpacesDisabledStatus(),
                ])

                labNotebookSpaces.forEach((space) => {
                    if (space.code === HOME_SPACE) {
                        labNotebookNode.children.nodes.push(this._createFilteredSpaceNode(space, true))
                    } else if (spacesDisabledStatus[space.code]) {
                        othersDisabledNode.children.nodes.push(this._createFilteredSpaceNode(space))
                    } else {
                        othersNode.children.nodes.push(this._createFilteredSpaceNode(space))
                    }
                })

                if (othersNode.children.nodes.length > 0) {
                    this._sortResultsByText(othersNode.children)
                    labNotebookNode.children.nodes.push(othersNode)
                }

                if (othersDisabledNode.children.nodes.length > 0) {
                    this._sortResultsByText(othersDisabledNode.children)
                    labNotebookNode.children.nodes.push(othersDisabledNode)
                }

                return labNotebookNode
            }
        }

        return null
    }

    async _createFilteredInventoryNode(entities) {
        var inventorySpaces = Object.values(entities.spaces).filter((space) => {
            return this._isInventorySpace(space.original)
        })

        if (inventorySpaces.length > 0) {
            var inventoryNode = this._createInventoryNode()
            if (inventoryNode) {
                inventoryNode.children = { nodes: [] }
                inventoryNode.expanded = true

                inventorySpaces.forEach((space) => {
                    inventoryNode.children.nodes.push(this._createFilteredSpaceNode(space))
                })

                this._sortResultsByText(inventoryNode.children)

                return inventoryNode
            }
        }

        return null
    }

    async _createFilteredStockNode(entities) {
        var stockSpaces = Object.values(entities.spaces).filter((space) => {
            return this._isStockSpace(space.original)
        })

        if (stockSpaces.length > 0) {
            var stockNode = this._createStockNode()
            if (stockNode) {
                stockNode.children = { nodes: [] }
                stockNode.expanded = true

                stockSpaces.forEach((space) => {
                    stockNode.children.nodes.push(this._createFilteredSpaceNode(space))
                })

                this._sortResultsByText(stockNode.children)

                return stockNode
            }
        }

        return null
    }

    _createFilteredSpaceNode(space, isHomeSpace) {
        var spaceNode = this._createSpaceNode(space.original, isHomeSpace)
        spaceNode.canHaveChildren = false
        spaceNode.children = { nodes: [] }
        spaceNode.expanded = true

        var projects = Object.values(space.projects)
        if (projects.length > 0) {
            spaceNode.canHaveChildren = true
            projects.forEach((project) => {
                var projectNode = this._createFilteredProjectNode(project)
                spaceNode.children.nodes.push(projectNode)
            })
            this._sortResultsByText(spaceNode.children)
        }

        return spaceNode
    }

    _createFilteredProjectNode(project) {
        var projectNode = this._createProjectNode(project.original)
        projectNode.canHaveChildren = false
        projectNode.children = { nodes: [] }
        projectNode.expanded = true

        var experiments = Object.values(project.experiments)
        if (experiments.length > 0) {
            projectNode.canHaveChildren = true
            experiments.forEach((experiment) => {
                var experimentNode = this._createFilteredExperimentNode(experiment)
                projectNode.children.nodes.push(experimentNode)
            })
            this._sortResultsByText(projectNode.children)
        }

        return projectNode
    }

    _createFilteredExperimentNode(experiment) {
        var experimentNode = this._createExperimentNode(experiment.original)
        experimentNode.canHaveChildren = false
        experimentNode.children = { nodes: [] }
        experimentNode.expanded = true

        var samplesNode = this._createFilteredExperimentSamplesNode(experiment)
        if (samplesNode) {
            experimentNode.canHaveChildren = true
            experimentNode.children.nodes.push(samplesNode)
        }

        var dataSetsNode = this._createFilteredExperimentDataSetsNode(experiment)
        if (dataSetsNode) {
            experimentNode.canHaveChildren = true
            experimentNode.children.nodes.push(dataSetsNode)
        }

        return experimentNode
    }

    _createFilteredExperimentSamplesNode(experiment) {
        var samples = Object.values(experiment.samples)

        if (samples.length > 0) {
            var samplesNode = this._createExperimentSamplesNode()
            samplesNode.children = { nodes: [] }
            samplesNode.expanded = true

            samples.forEach((sample) => {
                var sampleNode = this._createFilteredSampleNode(sample)
                samplesNode.children.nodes.push(sampleNode)
            })

            this._sortResultsByText(samplesNode.children)

            return samplesNode
        }

        return null
    }

    _createFilteredExperimentDataSetsNode(experiment) {
        var dataSets = Object.values(experiment.dataSets)

        if (dataSets.length > 0) {
            var dataSetsNode = this._createExperimentDataSetsNode()
            dataSetsNode.children = { nodes: [] }
            dataSetsNode.expanded = true

            dataSets.forEach((dataSet) => {
                var dataSetNode = this._createFilteredDataSetNode(dataSet)
                dataSetsNode.children.nodes.push(dataSetNode)
            })

            this._sortResultsByText(dataSetsNode.children)

            return dataSetsNode
        }

        return null
    }

    _createFilteredSampleNode(sample) {
        var sampleNode = this._createSampleNode(sample.original)
        sampleNode.canHaveChildren = false
        sampleNode.children = { nodes: [] }
        sampleNode.expanded = true

        var dataSetsNode = this._createFilteredSampleDataSetsNode(sample)
        if (dataSetsNode) {
            sampleNode.canHaveChildren = true
            sampleNode.children.nodes.push(dataSetsNode)
        }

        var childrenNode = this._createFilteredSampleChildrenNode(sample)
        if (childrenNode) {
            sampleNode.canHaveChildren = true
            sampleNode.children.nodes.push(childrenNode)
        }

        return sampleNode
    }

    _createFilteredSampleDataSetsNode(sample) {
        var dataSets = Object.values(sample.dataSets)

        if (dataSets.length > 0) {
            var dataSetsNode = this._createSampleDataSetsNode()
            dataSetsNode.children = { nodes: [] }
            dataSetsNode.expanded = true

            dataSets.forEach((dataSet) => {
                var dataSetNode = this._createFilteredDataSetNode(dataSet)
                dataSetsNode.children.nodes.push(dataSetNode)
            })

            this._sortResultsByText(dataSetsNode.children)

            return dataSetsNode
        }

        return null
    }

    _createFilteredSampleChildrenNode(sample) {
        var children = Object.values(sample.children)

        if (children.length > 0) {
            var childrenNode = this._createSampleChildrenNode()
            childrenNode.children = { nodes: [] }
            childrenNode.expanded = true

            children.forEach((child) => {
                var childNode = this._createFilteredSampleNode(child)
                childrenNode.children.nodes.push(childNode)
            })

            this._sortResultsByText(childrenNode.children)

            return childrenNode
        }

        return null
    }

    _createFilteredDataSetNode(dataSet) {
        var dataSetNode = this._createDataSetNode(dataSet.original)
        dataSetNode.canHaveChildren = false
        return dataSetNode
    }

    async _loadNodes(params) {
        var node = params.node

        if (node.root) {
            return this._loadNodesRoot(params)
        } else if (node.object.type === this.TYPE_LAB_NOTEBOOK) {
            return this._loadNodesLabNotebook(params)
        } else if (node.object.type === this.TYPE_LAB_NOTEBOOK_OTHERS) {
            return this._loadNodesLabNotebookOthers(params, true, false)
        } else if (node.object.type === this.TYPE_LAB_NOTEBOOK_OTHERS_DISABLED) {
            return this._loadNodesLabNotebookOthers(params, false, true)
        } else if (node.object.type === this.TYPE_INVENTORY) {
            return this._loadNodesInventory(params)
        } else if (node.object.type === this.TYPE_STOCK) {
            return this._loadNodesStock(params)
        } else if (node.object.type === this.TYPE_SPACE) {
            return this._loadNodesSpace(params)
        } else if (node.object.type === this.TYPE_PROJECT) {
            return this._loadNodesProject(params)
        } else if (node.object.type === this.TYPE_EXPERIMENT) {
            return this._loadNodesExperiment(params)
        } else if (node.object.type === this.TYPE_EXPERIMENT_SAMPLES) {
            return this._loadNodesExperimentSamples(params)
        } else if (node.object.type === this.TYPE_EXPERIMENT_DATASETS) {
            return this._loadNodesExperimentDataSets(params)
        } else if (node.object.type === this.TYPE_SAMPLE) {
            return this._loadNodesSample(params)
        } else if (node.object.type === this.TYPE_SAMPLE_CHILDREN) {
            return this._loadNodesSampleChildren(params)
        } else if (node.object.type === this.TYPE_SAMPLE_DATASETS) {
            return this._loadNodesSampleDataSets(params)
        } else if (node.object.type === this.TYPE_UTILITIES) {
            return this._loadNodesUtilities(params)
        } else if (node.object.type === this.TYPE_EXPORTS) {
            return this._loadNodesExports(params)
        }
    }

    async _loadNodesRoot(params) {
        var results = { nodes: [] }

        results.nodes.push(this._createLabNotebookNode())
        results.nodes.push(this._createInventoryNode())
        results.nodes.push(this._createStockNode())
        results.nodes.push(this._createUtilitiesNode())
        results.nodes.push(this._createAboutNode())

        results.nodes = results.nodes.filter((node) => !!node)

        return results
    }

    async _loadNodesLabNotebook(params) {
        var HOME_SPACE = await this._loadHomeSpace()

        var spaceCriteria = {
            entityKind: "SPACE",
            logicalOperator: "AND",
            rules: { [Util.guid()]: { type: "Attribute", name: "CODE", value: HOME_SPACE } },
        }

        var spaceFetchOptions = {
            only: true,
        }

        return new Promise((resolve) => {
            mainController.serverFacade.searchForSpacesAdvanced(spaceCriteria, spaceFetchOptions, (searchResult) => {
                var results = { nodes: [] }

                searchResult.objects.forEach((space) => {
                    if (this._isLabNotebookSpace(space) && space.code === HOME_SPACE) {
                        results.nodes.push(this._createSpaceNode(space, true))
                    }
                })

                results.nodes.push(this._createLabNotebookOthersNode())
                results.nodes.push(this._createLabNotebookOthersDisabledNode())

                resolve(results)
            })
        })
    }

    async _loadNodesLabNotebookOthers(params, showEnabled, showDisabled) {
        var spaceCriteria = { entityKind: "SPACE", logicalOperator: "AND", rules: {} }
        var spaceFetchOptions = { only: true, sortings: [] }

        if (params.node.sortings) {
            var spaceSorting = params.node.sortings.find((sorting) => sorting.id === params.node.sortingId)
            if (spaceSorting) {
                spaceFetchOptions.sortings = spaceSorting.server
            }
        }

        var [HOME_SPACE, spacesDisabledStatus] = await Promise.all([
            this._loadHomeSpace(),
            this._loadSpacesDisabledStatus(),
        ])

        return new Promise((resolve) => {
            mainController.serverFacade.searchForSpacesAdvanced(spaceCriteria, spaceFetchOptions, (searchResult) => {
                var results = { nodes: [], totalCount: searchResult.totalCount }

                searchResult.objects.forEach((space) => {
                    if (this._isLabNotebookSpace(space) && space.getCode() !== HOME_SPACE) {
                        var spaceDisabled = spacesDisabledStatus[space.getCode()]
                        if ((showEnabled && !spaceDisabled) || (showDisabled && spaceDisabled)) {
                            results.nodes.push(this._createSpaceNode(space))
                        }
                    }
                })

                this._filterResultsByChildrenInAndNotIn(params, results)

                resolve(results)
            })
        })
    }

    async _loadNodesInventory(params) {
        var spaceCriteria = { entityKind: "SPACE", logicalOperator: "AND", rules: {} }
        var spaceFetchOptions = { only: true, sortings: [] }

        if (params.node.sortings) {
            var spaceSorting = params.node.sortings.find((sorting) => sorting.id === params.node.sortingId)
            if (spaceSorting) {
                spaceFetchOptions.sortings = spaceSorting.server
            }
        }

        return new Promise((resolve) => {
            mainController.serverFacade.searchForSpacesAdvanced(spaceCriteria, spaceFetchOptions, (searchResult) => {
                var results = { nodes: [], totalCount: searchResult.totalCount }

                searchResult.objects.forEach((space) => {
                    if (this._isInventorySpace(space)) {
                        results.nodes.push(this._createSpaceNode(space))
                    }
                })

                this._filterResultsByChildrenInAndNotIn(params, results)

                resolve(results)
            })
        })
    }

    async _loadNodesStock(params) {
        var spaceCriteria = { entityKind: "SPACE", logicalOperator: "AND", rules: {} }
        var spaceFetchOptions = { only: true, sortings: [] }

        if (params.node.sortings) {
            var spaceSorting = params.node.sortings.find((sorting) => sorting.id === params.node.sortingId)
            if (spaceSorting) {
                spaceFetchOptions.sortings = spaceSorting.server
            }
        }

        return new Promise((resolve) => {
            mainController.serverFacade.searchForSpacesAdvanced(spaceCriteria, spaceFetchOptions, (searchResult) => {
                var results = { nodes: [], totalCount: searchResult.totalCount }

                searchResult.objects.forEach((space) => {
                    if (this._isStockSpace(space)) {
                        results.nodes.push(this._createSpaceNode(space))
                    }
                })

                this._filterResultsByChildrenInAndNotIn(params, results)

                resolve(results)
            })
        })
    }

    async _loadNodesSpace(params) {
        var projectRules = { [Util.guid()]: { type: "Attribute", name: "SPACE", value: params.node.object.id } }
        var projectFetchOptions = { only: true, sortings: [] }

        if (params.node.sortings) {
            var projectSorting = params.node.sortings.find((sorting) => sorting.id === params.node.sortingId)
            if (projectSorting) {
                projectFetchOptions.sortings = projectSorting.server
            }
        }

        return new Promise((resolve) => {
            mainController.serverFacade.searchForProjectsAdvanced(
                { entityKind: "PROJECT", logicalOperator: "AND", rules: projectRules },
                projectFetchOptions,
                (searchResult) => {
                    var results = { nodes: [], totalCount: searchResult.totalCount }

                    searchResult.objects.forEach((project) => {
                        if (!this._isQueriesProject(project)) {
                            results.nodes.push(this._createProjectNode(project))
                        }
                    })

                    this._filterResultsByChildrenInAndNotIn(params, results)

                    resolve(results)
                }
            )
        })
    }

    async _loadNodesProject(params) {
        var experimentRules = {
            [Util.guid()]: { type: "Attribute", name: "PROJECT_PERM_ID", value: params.node.object.id },
        }
        var experimentSubcriteria = []

        if (params.childrenIn) {
            experimentSubcriteria.push({
                logicalOperator: "OR",
                rules: params.childrenIn.map((childIn) => {
                    return {
                        type: "Attribute",
                        name: "PERM_ID",
                        value: childIn.object.id,
                    }
                }),
            })
        }

        if (params.childrenNotIn) {
            experimentSubcriteria.push({
                negate: true,
                logicalOperator: "OR",
                rules: params.childrenNotIn.map((childNotIn) => {
                    return {
                        type: "Attribute",
                        name: "PERM_ID",
                        value: childNotIn.object.id,
                    }
                }),
            })
        }

        var experimentFetchOptions = {
            only: true,
            withType: true,
            withProperties: true,
            from: params.offset,
            count: params.limit,
            sortings: [],
        }

        if (params.node.sortings) {
            var experimentSorting = params.node.sortings.find((sorting) => sorting.id === params.node.sortingId)
            if (experimentSorting) {
                experimentFetchOptions.sortings = experimentSorting.server
            }
        }

        return new Promise((resolve) => {
            mainController.serverFacade.searchForExperimentsAdvanced(
                {
                    entityKind: "EXPERIMENT",
                    logicalOperator: "AND",
                    rules: experimentRules,
                    subCriteria: experimentSubcriteria,
                },
                experimentFetchOptions,
                (searchResult) => {
                    var results = { nodes: [], totalCount: searchResult.totalCount }

                    searchResult.objects.forEach((experiment) => {
                        results.nodes.push(this._createExperimentNode(experiment))
                    })

                    resolve(results)
                }
            )
        })
    }

    async _loadNodesExperiment(params) {
        var samplesFolderNode = this._createExperimentSamplesNode()
        samplesFolderNode.experimentPermId = params.node.object.id

        var loadSamplesPromise = this._loadNodesExperimentSamples({
            node: samplesFolderNode,
            offset: 0,
            limit: this.LOAD_LIMIT,
        })

        var dataSetsFolderNode = this._createExperimentDataSetsNode()
        dataSetsFolderNode.experimentPermId = params.node.object.id

        var showDatasets = SettingsManagerUtils.isEnabledForGroup(
            params.node.space,
            SettingsManagerUtils.ShowSetting.showDatasets
        )

        var loadDataSetsPromise = null

        if (showDatasets) {
            loadDataSetsPromise = this._loadNodesExperimentDataSets({
                node: dataSetsFolderNode,
                offset: 0,
                limit: this.LOAD_LIMIT,
            })
        } else {
            loadDataSetsPromise = Promise.resolve({
                totalCount: 0,
            })
        }

        var [samplesResults, dataSetsResults] = await Promise.all([loadSamplesPromise, loadDataSetsPromise])

        var results = {
            nodes: [],
            totalCount: 0,
        }

        if (samplesResults.totalCount > 0) {
            results.nodes.push(samplesFolderNode)
            results.totalCount++
        }

        if (dataSetsResults.totalCount > 0) {
            results.nodes.push(dataSetsFolderNode)
            results.totalCount++
        }

        return results
    }

    async _loadNodesExperimentSamples(params) {
        var sampleRules = {
            [Util.guid()]: { type: "Experiment", name: "ATTR.PERM_ID", value: params.node.experimentPermId },
        }
        var sampleSubcriteria = []

        if (params.childrenIn) {
            sampleSubcriteria.push({
                logicalOperator: "OR",
                rules: params.childrenIn.map((childIn) => {
                    return {
                        type: "Attribute",
                        name: "PERM_ID",
                        value: childIn.object.id,
                    }
                }),
            })
        }

        if (params.childrenNotIn) {
            sampleSubcriteria.push({
                negate: true,
                logicalOperator: "OR",
                rules: params.childrenNotIn.map((childNotIn) => {
                    return {
                        type: "Attribute",
                        name: "PERM_ID",
                        value: childNotIn.object.id,
                    }
                }),
            })
        }

        var sampleFetchOptions = {
            only: true,
            withProperties: true,
            withType: true,
            withExperiment: true,
            withParents: true,
            withParentsExperiment: true,
            from: params.offset,
            count: params.limit,
            sortings: [],
        }

        if (params.node.sortings) {
            var sampleSorting = params.node.sortings.find((sorting) => sorting.id === params.node.sortingId)
            if (sampleSorting) {
                sampleFetchOptions.sortings = sampleSorting.server
            }
        }

        return new Promise((resolve) => {
            mainController.serverFacade.searchForSamplesAdvanced(
                {
                    entityKind: "SAMPLE",
                    logicalOperator: "AND",
                    rules: sampleRules,
                    subCriteria: sampleSubcriteria,
                },
                sampleFetchOptions,
                (searchResult) => {
                    var results = this._filterResultsByFunction(params, searchResult, (sample) => {
                        if (this._isExperimentSample(sample)) {
                            return this._createSampleNode(sample)
                        } else {
                            return null
                        }
                    })

                    resolve(results)
                }
            )
        })
    }

    async _loadNodesExperimentDataSets(params) {
        var datasetRules = {
            [Util.guid()]: { type: "Experiment", name: "ATTR.PERM_ID", value: params.node.experimentPermId },
            [Util.guid()]: { type: "Sample", name: "NULL.NULL", value: "NULL" },
        }
        var datasetSubcriteria = []

        if (params.childrenIn) {
            datasetSubcriteria.push({
                logicalOperator: "OR",
                rules: params.childrenIn.map((childIn) => {
                    return {
                        type: "Attribute",
                        name: "PERM_ID",
                        value: childIn.object.id,
                    }
                }),
            })
        }

        if (params.childrenNotIn) {
            datasetSubcriteria.push({
                negate: true,
                logicalOperator: "OR",
                rules: params.childrenNotIn.map((childNotIn) => {
                    return {
                        type: "Attribute",
                        name: "PERM_ID",
                        value: childNotIn.object.id,
                    }
                }),
            })
        }

        var datasetFetchOptions = {
            only: true,
            withType: true,
            withExperiment: true,
            withSample: true,
            withProperties: true,
            from: params.offset,
            count: params.limit,
            sortings: [],
        }

        if (params.node.sortings) {
            var datasetSorting = params.node.sortings.find((sorting) => sorting.id === params.node.sortingId)
            if (datasetSorting) {
                datasetFetchOptions.sortings = datasetSorting.server
            }
        }

        return new Promise((resolve) => {
            mainController.serverFacade.searchForDataSetsAdvanced(
                {
                    entityKind: "DATASET",
                    logicalOperator: "AND",
                    rules: datasetRules,
                    subCriteria: datasetSubcriteria,
                },
                datasetFetchOptions,
                (searchResult) => {
                    var results = this._filterResultsByFunction(params, searchResult, (dataSet) => {
                        if (this._isExperimentDataSet(dataSet)) {
                            return this._createDataSetNode(dataSet)
                        } else {
                            return null
                        }
                    })

                    resolve(results)
                }
            )
        })
    }

    async _loadNodesSample(params) {
        var childrenFolderNode = this._createSampleChildrenNode()
        childrenFolderNode.samplePermId = params.node.object.id

        var loadChildrenPromise = this._loadNodesSampleChildren({
            node: childrenFolderNode,
            offset: 0,
            limit: this.LOAD_LIMIT,
        })

        var dataSetsFolderNode = this._createSampleDataSetsNode()
        dataSetsFolderNode.samplePermId = params.node.object.id

        var showDatasets = SettingsManagerUtils.isEnabledForGroup(
            params.node.space,
            SettingsManagerUtils.ShowSetting.showDatasets
        )

        var loadDataSetsPromise = null

        if (showDatasets) {
            loadDataSetsPromise = this._loadNodesSampleDataSets({
                node: dataSetsFolderNode,
                offset: 0,
                limit: this.LOAD_LIMIT,
            })
        } else {
            loadDataSetsPromise = Promise.resolve({
                totalCount: 0,
            })
        }

        var [childrenResults, dataSetsResults] = await Promise.all([loadChildrenPromise, loadDataSetsPromise])

        var results = {
            nodes: [],
            totalCount: 0,
        }

        if (childrenResults.totalCount > 0) {
            results.nodes.push(childrenFolderNode)
            results.totalCount++
        }

        if (dataSetsResults.totalCount > 0) {
            results.nodes.push(dataSetsFolderNode)
            results.totalCount++
        }

        return results
    }

    async _loadNodesSampleChildren(params) {
        var parentCriteria = {
            entityKind: "SAMPLE",
            logicalOperator: "AND",
            rules: { [Util.guid()]: { type: "Attribute", name: "PERM_ID", value: params.node.samplePermId } },
        }

        var parentFetchOptions = { only: true, withExperiment: true }

        var parent = await new Promise((resolve) => {
            mainController.serverFacade.searchForSamplesAdvanced(
                parentCriteria,
                parentFetchOptions,
                (parentSearchResult) => {
                    if (parentSearchResult.objects.length > 0) {
                        resolve(parentSearchResult.objects[0])
                    } else {
                        resolve(null)
                    }
                }
            )
        })

        if (!parent) {
            return {
                nodes: [],
                totalCount: 0,
            }
        }

        var sampleRules = {}
        sampleRules[Util.guid()] = { type: "Parent", name: "ATTR.PERM_ID", value: parent.permId.permId }
        sampleRules[Util.guid()] = {
            type: "Experiment",
            name: "ATTR.PERM_ID",
            value: parent.experiment.permId.permId,
        }

        var sampleSubcriteria = []

        if (params.childrenIn) {
            sampleSubcriteria.push({
                logicalOperator: "OR",
                rules: params.childrenIn.map((childIn) => {
                    return {
                        type: "Attribute",
                        name: "PERM_ID",
                        value: childIn.object.id,
                    }
                }),
            })
        }

        if (params.childrenNotIn) {
            sampleSubcriteria.push({
                negate: true,
                logicalOperator: "OR",
                rules: params.childrenNotIn.map((childNotIn) => {
                    return {
                        type: "Attribute",
                        name: "PERM_ID",
                        value: childNotIn.object.id,
                    }
                }),
            })
        }

        var sampleFetchOptions = {
            only: true,
            withProperties: true,
            withType: true,
            withExperiment: true,
            withParents: true,
            withParentsExperiment: true,
            from: params.offset,
            count: params.limit,
            sortings: [],
        }

        if (params.node.sortings) {
            var sampleSorting = params.node.sortings.find((sorting) => sorting.id === params.node.sortingId)
            if (sampleSorting) {
                sampleFetchOptions.sortings = sampleSorting.server
            }
        }

        return new Promise((resolve) => {
            mainController.serverFacade.searchForSamplesAdvanced(
                {
                    entityKind: "SAMPLE",
                    logicalOperator: "AND",
                    rules: sampleRules,
                    subCriteria: sampleSubcriteria,
                },
                sampleFetchOptions,
                (searchResult) => {
                    var results = this._filterResultsByFunction(params, searchResult, (sample) => {
                        if (this._isChildSample(sample)) {
                            return this._createSampleNode(sample)
                        } else {
                            return null
                        }
                    })

                    resolve(results)
                }
            )
        })
    }

    async _loadNodesSampleDataSets(params) {
        var datasetRules = { [Util.guid()]: { type: "Sample", name: "ATTR.PERM_ID", value: params.node.samplePermId } }
        var datasetSubcriteria = []

        if (params.childrenIn) {
            datasetSubcriteria.push({
                logicalOperator: "OR",
                rules: params.childrenIn.map((childIn) => {
                    return {
                        type: "Attribute",
                        name: "PERM_ID",
                        value: childIn.object.id,
                    }
                }),
            })
        }

        if (params.childrenNotIn) {
            datasetSubcriteria.push({
                negate: true,
                logicalOperator: "OR",
                rules: params.childrenNotIn.map((childNotIn) => {
                    return {
                        type: "Attribute",
                        name: "PERM_ID",
                        value: childNotIn.object.id,
                    }
                }),
            })
        }

        var datasetFetchOptions = {
            only: true,
            withType: true,
            withSample: true,
            withProperties: true,
            from: params.offset,
            count: params.limit,
            sortings: [],
        }

        if (params.node.sortings) {
            var datasetSorting = params.node.sortings.find((sorting) => sorting.id === params.node.sortingId)
            if (datasetSorting) {
                datasetFetchOptions.sortings = datasetSorting.server
            }
        }

        return new Promise((resolve) => {
            mainController.serverFacade.searchForDataSetsAdvanced(
                {
                    entityKind: "DATASET",
                    logicalOperator: "AND",
                    rules: datasetRules,
                    subCriteria: datasetSubcriteria,
                },
                datasetFetchOptions,
                (searchResult) => {
                    var results = this._filterResultsByFunction(params, searchResult, (dataSet) => {
                        if (this._isSampleDataSet(dataSet)) {
                            return this._createDataSetNode(dataSet)
                        } else {
                            return null
                        }
                    })

                    resolve(results)
                }
            )
        })
    }

    async _loadNodesUtilities(params) {
        var results = { nodes: [] }

        results.nodes.push(this._createJupyterWorkspaceNode())
        results.nodes.push(this._createNewJupyterNotebookNode())
        results.nodes.push(this._createUserProfileNode())
        results.nodes.push(this._createBarcodesGeneratorNode())
        results.nodes.push(this._createDrawingBoardNode())
        results.nodes.push(this._createObjectBrowserNode())
        results.nodes.push(this._createVocabularyBrowserNode())
        results.nodes.push(this._createAdvancedSearchNode())
        results.nodes.push(this._createDropboxMonitorNode())
        results.nodes.push(this._createArchivingHelperNode())
        results.nodes.push(this._createUnarchivingHelperNode())
        results.nodes.push(this._createCustomImportNode())
        results.nodes.push(this._createExportsNode())
        results.nodes.push(this._createStorageManagerNode())
        results.nodes.push(this._createUserManagerNode())
        results.nodes.push(this._createUserManagementConfigNode())
        results.nodes.push(this._createTrashcanNode())
        results.nodes.push(this._createSettingsNode())
        results.nodes.push(this._createOtherToolsNode())

        var extraPluginUtilities = profile.getPluginUtilities()
        extraPluginUtilities.forEach((extraPluginUtility) => {
            results.nodes.push(this._createExtraPluginNode(extraPluginUtility))
        })

        results.nodes = results.nodes.filter((node) => !!node)

        return results
    }

    async _loadNodesExports(params) {
        var results = { nodes: [] }

        results.nodes.push(this._createExportToZipNode())
        results.nodes.push(this._createExportToResearchCollectionNode())
        results.nodes.push(this._createExportToZenodoNode())

        results.nodes = results.nodes.filter((node) => !!node)

        return results
    }

    _createLabNotebookNode() {
        if (profile.mainMenu.showLabNotebook) {
            return {
                text: profile.MainMenuNodeNames.Lab_Notebook,
                object: {
                    type: this.TYPE_LAB_NOTEBOOK,
                    id: this.TYPE_LAB_NOTEBOOK,
                },
                expanded: true,
                canHaveChildren: true,
                view: "showLabNotebookPage",
                icon: "glyphicon glyphicon-book",
            }
        } else {
            return null
        }
    }

    _createLabNotebookOthersNode() {
        return {
            text: "Others",
            object: {
                type: this.TYPE_LAB_NOTEBOOK_OTHERS,
                id: this.TYPE_LAB_NOTEBOOK_OTHERS,
            },
            canHaveChildren: true,
            childrenLoadLimit: this.LOAD_LIMIT,
            childrenLoadRepeatLimitForFullBatch: this.LOAD_REPEAT_LIMIT_FOR_FULL_BATCH,
            view: "showLabNotebookPage",
            sortings: this.SORTINGS_BY_CODE_AND_REGISTRATION_DATE,
        }
    }

    _createLabNotebookOthersDisabledNode() {
        return {
            text: "Others (disabled)",
            object: {
                type: this.TYPE_LAB_NOTEBOOK_OTHERS_DISABLED,
                id: this.TYPE_LAB_NOTEBOOK_OTHERS_DISABLED,
            },
            canHaveChildren: true,
            childrenLoadLimit: this.LOAD_LIMIT,
            childrenLoadRepeatLimitForFullBatch: this.LOAD_REPEAT_LIMIT_FOR_FULL_BATCH,
            view: "showLabNotebookPage",
            sortings: this.SORTINGS_BY_CODE_AND_REGISTRATION_DATE,
        }
    }

    _createInventoryNode() {
        if (profile.mainMenu.showInventory) {
            return {
                text: profile.MainMenuNodeNames.Inventory,
                object: {
                    type: this.TYPE_INVENTORY,
                    id: this.TYPE_INVENTORY,
                },
                expanded: true,
                canHaveChildren: true,
                childrenLoadLimit: this.LOAD_LIMIT,
                childrenLoadRepeatLimitForFullBatch: this.LOAD_REPEAT_LIMIT_FOR_FULL_BATCH,
                view: "showInventoryPage",
                icon: "fa fa-cubes",
                sortings: this.SORTINGS_BY_CODE_AND_REGISTRATION_DATE,
            }
        } else {
            return null
        }
    }

    _createStockNode() {
        if (profile.mainMenu.showStock) {
            return {
                text: profile.MainMenuNodeNames.Stock,
                object: {
                    type: this.TYPE_STOCK,
                    id: this.TYPE_STOCK,
                },
                expanded: true,
                canHaveChildren: true,
                childrenLoadLimit: this.LOAD_LIMIT,
                childrenLoadRepeatLimitForFullBatch: this.LOAD_REPEAT_LIMIT_FOR_FULL_BATCH,
                view: "showStockPage",
                icon: "fa fa-shopping-cart",
                sortings: this.SORTINGS_BY_CODE_AND_REGISTRATION_DATE,
            }
        } else {
            return null
        }
    }

    _createUtilitiesNode() {
        return {
            text: profile.MainMenuNodeNames.Utilities,
            object: {
                type: this.TYPE_UTILITIES,
                id: this.TYPE_UTILITIES,
            },
            canHaveChildren: true,
            expanded: true,
            selectable: false,
            view: "showBlancPage",
            icon: "glyphicon glyphicon-wrench",
        }
    }

    _createJupyterWorkspaceNode() {
        if (profile.jupyterEndpoint) {
            return {
                text: "Jupyter Workspace",
                object: {
                    type: this.TYPE_JUPYTER_WORKSPACE,
                    id: this.TYPE_JUPYTER_WORKSPACE,
                },
                view: "showJupyterWorkspace",
            }
        } else {
            return null
        }
    }

    _createNewJupyterNotebookNode() {
        if (profile.jupyterEndpoint) {
            return {
                text: "New Jupyter Notebook",
                object: {
                    type: this.TYPE_NEW_JUPYTER_NOTEBOOK,
                    id: this.TYPE_NEW_JUPYTER_NOTEBOOK,
                },
                view: "showNewJupyterNotebookCreator",
            }
        } else {
            return null
        }
    }

    _createUserProfileNode() {
        if (profile.mainMenu.showUserProfile) {
            return {
                text: "User Profile",
                object: {
                    type: this.TYPE_USER_PROFILE,
                    id: this.TYPE_USER_PROFILE,
                },
                view: "showUserProfilePage",
                icon: "glyphicon glyphicon-user",
            }
        } else {
            return null
        }
    }

    _createBarcodesGeneratorNode() {
        if (profile.mainMenu.showBarcodes) {
            return {
                text: "Barcodes/QR codes Generator",
                object: {
                    type: this.TYPE_GENERATE_BARCODES,
                    id: this.TYPE_GENERATE_BARCODES,
                },
                view: "showBarcodesGeneratorPage",
                icon: "glyphicon glyphicon-barcode",
            }
        } else {
            return null
        }
    }

    _createDrawingBoardNode() {
        if (profile.mainMenu.showDrawingBoard) {
            return {
                text: "Drawing Board",
                object: {
                    type: this.TYPE_DRAWING_BOARD,
                    id: this.TYPE_DRAWING_BOARD,
                },
                view: "showDrawingBoard",
            }
        } else {
            return null
        }
    }

    _createObjectBrowserNode() {
        if (profile.mainMenu.showObjectBrowser) {
            return {
                text: "" + ELNDictionary.Sample + " Browser",
                object: {
                    type: this.TYPE_SAMPLE_BROWSER,
                    id: this.TYPE_SAMPLE_BROWSER,
                },
                view: "showSamplesPage",
                icon: "glyphicon glyphicon-list-alt",
            }
        } else {
            return null
        }
    }

    _createVocabularyBrowserNode() {
        if (profile.mainMenu.showVocabularyViewer) {
            return {
                text: "Vocabulary Browser",
                object: {
                    type: this.TYPE_VOCABULARY_BROWSER,
                    id: this.TYPE_VOCABULARY_BROWSER,
                },
                view: "showVocabularyManagerPage",
                icon: "glyphicon glyphicon-list-alt",
            }
        } else {
            return null
        }
    }

    _createAdvancedSearchNode() {
        if (profile.mainMenu.showAdvancedSearch) {
            return {
                text: "Advanced Search",
                object: {
                    type: this.TYPE_ADVANCED_SEARCH,
                    id: this.TYPE_ADVANCED_SEARCH,
                },
                view: "showAdvancedSearchPage",
                icon: "glyphicon glyphicon-search",
            }
        } else {
            return null
        }
    }

    _createDropboxMonitorNode() {
        if (profile.dropboxMonitorUsageAuthorized) {
            return {
                text: "Dropbox Monitor",
                object: {
                    type: this.TYPE_DROPBOX_MONITOR,
                    id: this.TYPE_DROPBOX_MONITOR,
                },
                view: "showDropboxMonitorPage",
                icon: "glyphicon glyphicon-info-sign",
            }
        } else {
            return null
        }
    }

    _createArchivingHelperNode() {
        if (profile.mainMenu.showArchivingHelper && profile.showDatasetArchivingButton) {
            return {
                text: "Archiving Helper",
                object: {
                    type: this.TYPE_ARCHIVING_HELPER,
                    id: this.TYPE_ARCHIVING_HELPER,
                },
                view: "showArchivingHelperPage",
                icon: "fancytree-icon",
                iconUrl: "./img/archive-not-requested-icon.png",
            }
        } else {
            return null
        }
    }

    _createUnarchivingHelperNode() {
        if (profile.mainMenu.showUnarchivingHelper && profile.showDatasetArchivingButton) {
            return {
                text: "Unarchiving Helper",
                object: {
                    type: this.TYPE_UNARCHIVING_HELPER,
                    id: this.TYPE_UNARCHIVING_HELPER,
                },
                view: "showUnarchivingHelperPage",
                icon: "glyphicon glyphicon-open",
            }
        } else {
            return null
        }
    }

    _createCustomImportNode() {
        if (profile.customImportDefinitions && profile.customImportDefinitions.length > 0) {
            return {
                text: "Custom Import",
                object: {
                    type: this.TYPE_CUSTOM_IMPORT,
                    id: this.TYPE_CUSTOM_IMPORT,
                },
                view: "showCustomImportPage",
                icon: "glyphicon glyphicon-import",
            }
        } else {
            return null
        }
    }

    _createExportsNode() {
        if (
            profile.mainMenu.showExports ||
            options.showResearchCollectionExportBuilder ||
            profile.mainMenu.showZenodoExportBuilder
        ) {
            return {
                text: "Exports",
                object: {
                    type: this.TYPE_EXPORTS,
                    id: this.TYPE_EXPORTS,
                },
                canHaveChildren: true,
                selectable: false,
                view: "showBlancPage",
                icon: "glyphicon glyphicon-export",
            }
        } else {
            return null
        }
    }

    _createStorageManagerNode() {
        if (profile.mainMenu.showStorageManager) {
            return {
                text: "Storage Manager",
                object: {
                    type: this.TYPE_STORAGE_MANAGER,
                    id: this.TYPE_STORAGE_MANAGER,
                },
                view: "showStorageManager",
                icon: "glyphicon glyphicon-file",
            }
        } else {
            return null
        }
    }

    _createUserManagerNode() {
        if (profile.mainMenu.showUserManager && profile.isAdmin && !profile.isMultiGroup()) {
            return {
                text: "User Manager",
                object: {
                    type: this.TYPE_USER_MANAGER,
                    id: this.TYPE_USER_MANAGER,
                },
                view: "showUserManagerPage",
                icon: "fa fa-users",
            }
        } else {
            return null
        }
    }

    _createUserManagementConfigNode() {
        if (
            profile.isAdmin &&
            profile.userManagementMaintenanceTaskConfig != null &&
            profile.showUserManagementConfig
        ) {
            return {
                text: "User Management Config",
                object: {
                    type: this.TYPE_USER_MANAGEMENT_CONFIG,
                    id: this.TYPE_USER_MANAGEMENT_CONFIG,
                },
                view: "showUserManagementConfigPage",
                icon: "fa fa-users",
            }
        } else {
            return null
        }
    }

    _createTrashcanNode() {
        if (profile.mainMenu.showTrashcan) {
            return {
                text: "Trashcan",
                object: {
                    type: this.TYPE_TRASHCAN,
                    id: this.TYPE_TRASHCAN,
                },
                view: "showTrashcanPage",
                icon: "glyphicon glyphicon-trash",
            }
        } else {
            return null
        }
    }

    _createSettingsNode() {
        if (profile.mainMenu.showSettings) {
            return {
                text: "Settings",
                object: {
                    type: this.TYPE_SETTINGS,
                    id: this.TYPE_SETTINGS,
                },
                view: "showSettingsPage",
                icon: "glyphicon glyphicon-cog",
            }
        } else {
            return null
        }
    }

    _createOtherToolsNode() {
        return {
            text: "Other Tools",
            object: {
                type: this.TYPE_OTHER_TOOLS,
                id: this.TYPE_OTHER_TOOLS,
            },
            view: "showOtherToolsPage",
            icon: "glyphicon glyphicon-wrench",
        }
    }

    _createExtraPluginNode(extraPluginUtility) {
        return {
            text: extraPluginUtility.label,
            object: {
                type: this.TYPE_EXTRA_PLUGIN_UTILITY,
                id: extraPluginUtility.uniqueViewName,
            },
            view: "EXTRA_PLUGIN_UTILITY",
            viewData: extraPluginUtility.uniqueViewName,
            icon: extraPluginUtility.icon,
        }
    }

    _createExportToZipNode() {
        if (profile.mainMenu.showExports) {
            return {
                text: "Export to ZIP",
                object: {
                    type: this.TYPE_EXPORT_TO_ZIP,
                    id: this.TYPE_EXPORT_TO_ZIP,
                },
                view: "showExportTreePage",
                icon: "glyphicon glyphicon-export",
            }
        } else {
            return null
        }
    }

    _createExportToResearchCollectionNode() {
        if (options.showResearchCollectionExportBuilder) {
            return {
                text: "Export to Research Collection",
                object: {
                    type: this.TYPE_EXPORT_TO_RESEARCH_COLLECTION,
                    id: this.TYPE_EXPORT_TO_RESEARCH_COLLECTION,
                },
                view: "showResearchCollectionExportPage",
                icon: "fancytree-icon",
                iconUrl: "./img/research-collection-icon.png",
            }
        } else {
            return null
        }
    }

    _createExportToZenodoNode() {
        if (profile.mainMenu.showZenodoExportBuilder) {
            return {
                text: "Export to Zenodo",
                object: {
                    type: this.TYPE_EXPORT_TO_ZENODO,
                    id: this.TYPE_EXPORT_TO_ZENODO,
                },
                view: "showZenodoExportPage",
                icon: "glyphicon glyphicon-export",
            }
        } else {
            return null
        }
    }

    _createAboutNode() {
        return {
            text: "About",
            object: {
                type: this.TYPE_ABOUT,
                id: this.TYPE_ABOUT,
            },
            view: "showAbout",
            icon: "glyphicon glyphicon-info-sign",
        }
    }

    _createSpaceNode(space, isHomeSpace) {
        var normalizedSpaceTitle = Util.getDisplayNameFromCode(space.getCode())
        return {
            text: isHomeSpace ? "My Space (" + normalizedSpaceTitle + ")" : normalizedSpaceTitle,
            object: {
                type: this.TYPE_SPACE,
                id: space.getCode(),
            },
            canHaveChildren: true,
            childrenLoadLimit: this.LOAD_LIMIT,
            childrenLoadRepeatLimitForFullBatch: this.LOAD_REPEAT_LIMIT_FOR_FULL_BATCH,
            rootable: true,
            view: "showSpacePage",
            viewData: space.getCode(),
            sortings: this.SORTINGS_BY_CODE_AND_REGISTRATION_DATE,
        }
    }

    _createProjectNode(project) {
        var normalizedProjectTitle = Util.getDisplayNameFromCode(project.getCode())
        return {
            text: normalizedProjectTitle,
            object: {
                type: this.TYPE_PROJECT,
                id: project.getPermId().getPermId(),
            },
            canHaveChildren: true,
            childrenLoadLimit: this.LOAD_LIMIT,
            childrenLoadRepeatLimitForFullBatch: this.LOAD_REPEAT_LIMIT_FOR_FULL_BATCH,
            rootable: true,
            view: "showProjectPageFromPermId",
            viewData: project.getPermId().getPermId(),
            sortings: this.SORTINGS_BY_NAME_AND_REGISTRATION_DATE,
        }
    }

    _createExperimentNode(experiment) {
        var experimentDisplayName = experiment.getCode()
        if (experiment.getProperties() && experiment.getProperties()[profile.propertyReplacingCode]) {
            experimentDisplayName = experiment.getProperties()[profile.propertyReplacingCode]
        }
        var experimentSpace = IdentifierUtil.getSpaceCodeFromIdentifier(experiment.getIdentifier().getIdentifier())

        var viewToUse = null
        var canHaveChildren = null

        if (this._isExperimentWithChildren(experiment)) {
            viewToUse = "showExperimentPageFromIdentifier"
            canHaveChildren = true
        } else {
            viewToUse = "showSamplesPage"
            canHaveChildren = false
        }

        var experimentResult = {
            text: experimentDisplayName,
            object: {
                type: this.TYPE_EXPERIMENT,
                id: experiment.getPermId().getPermId(),
            },
            space: experimentSpace,
            canHaveChildren: canHaveChildren,
            rootable: true,
            view: viewToUse,
            viewData: encodeURIComponent('["' + experiment.getIdentifier().getIdentifier() + '",false]'),
        }
        if (!this._isExperimentWithChildren(experiment)) {
            experimentResult.icon = "fa fa-table"
        }

        return experimentResult
    }

    _createExperimentSamplesNode() {
        return {
            text: "Objects",
            object: {
                type: this.TYPE_EXPERIMENT_SAMPLES,
                id: this.TYPE_EXPERIMENT_SAMPLES,
            },
            selectable: false,
            expanded: true,
            canHaveChildren: true,
            childrenLoadLimit: this.LOAD_LIMIT,
            childrenLoadRepeatLimitForFullBatch: this.LOAD_REPEAT_LIMIT_FOR_FULL_BATCH,
            sortings: this.SORTINGS_BY_NAME_AND_REGISTRATION_DATE,
        }
    }

    _createExperimentDataSetsNode() {
        return {
            text: "Data Sets",
            object: {
                type: this.TYPE_EXPERIMENT_DATASETS,
                id: this.TYPE_EXPERIMENT_DATASETS,
            },
            selectable: false,
            expanded: true,
            canHaveChildren: true,
            childrenLoadLimit: this.LOAD_LIMIT,
            childrenLoadRepeatLimitForFullBatch: this.LOAD_REPEAT_LIMIT_FOR_FULL_BATCH,
            sortings: this.SORTINGS_BY_NAME_AND_REGISTRATION_DATE,
        }
    }

    _createSampleNode(sample) {
        var sampleIsExperiment = sample.getType().getCode().indexOf("EXPERIMENT") > -1
        var sampleIcon
        if (sampleIsExperiment) {
            sampleIcon = "fa fa-flask"
        } else if (sample.getType().getCode() === "ENTRY") {
            sampleIcon = "fa fa-file-text"
        } else {
            sampleIcon = "fa fa-file"
        }
        var sampleDisplayName = sample.getCode()
        if (sample.getProperties() && sample.getProperties()[profile.propertyReplacingCode]) {
            sampleDisplayName = sample.getProperties()[profile.propertyReplacingCode]
        }

        return {
            text: sampleDisplayName,
            object: {
                type: this.TYPE_SAMPLE,
                id: sample.getPermId().getPermId(),
            },
            space: IdentifierUtil.getSpaceCodeFromIdentifier(sample.getIdentifier().getIdentifier()),
            canHaveChildren: true,
            view: "showViewSamplePageFromPermId",
            viewData: sample.getPermId().getPermId(),
            icon: sampleIcon,
        }
    }

    _createSampleChildrenNode() {
        return {
            text: "Children",
            object: {
                type: this.TYPE_SAMPLE_CHILDREN,
                id: this.TYPE_SAMPLE_CHILDREN,
            },
            selectable: false,
            expanded: true,
            canHaveChildren: true,
            childrenLoadLimit: this.LOAD_LIMIT,
            childrenLoadRepeatLimitForFullBatch: this.LOAD_REPEAT_LIMIT_FOR_FULL_BATCH,
            sortings: this.SORTINGS_BY_NAME_AND_REGISTRATION_DATE,
        }
    }

    _createSampleDataSetsNode() {
        return {
            text: "Data Sets",
            object: {
                type: this.TYPE_SAMPLE_DATASETS,
                id: this.TYPE_SAMPLE_DATASETS,
            },
            selectable: false,
            expanded: true,
            canHaveChildren: true,
            childrenLoadLimit: this.LOAD_LIMIT,
            childrenLoadRepeatLimitForFullBatch: this.LOAD_REPEAT_LIMIT_FOR_FULL_BATCH,
            sortings: this.SORTINGS_BY_NAME_AND_REGISTRATION_DATE,
        }
    }

    _createDataSetNode(dataset) {
        var datasetDisplayName = dataset.getCode()
        if (dataset.getProperties() && dataset.getProperties()[profile.propertyReplacingCode]) {
            datasetDisplayName = dataset.getProperties()[profile.propertyReplacingCode]
        }

        return {
            text: datasetDisplayName,
            object: {
                type: this.TYPE_DATASET,
                id: dataset.getPermId().getPermId(),
            },
            view: "showViewDataSetPageFromPermId",
            viewData: dataset.getPermId().getPermId(),
            icon: "fa fa-database",
        }
    }

    _sortResultsByText(results) {
        const collator = new Intl.Collator(undefined, {
            numeric: true,
            sensitivity: "base",
        })

        results.nodes.sort((node1, node2) => {
            var text1 = node1.text
            var text2 = node2.text
            return collator.compare(text1 || "", text2 || "")
        })
    }

    _filterResultsByChildrenInAndNotIn(params, results) {
        if (params.childrenIn && params.childrenIn.length > 0) {
            const childrenInMap = {}
            params.childrenIn.forEach((child) => {
                childrenInMap[child.object.id] = child
            })
            results.nodes = results.nodes.filter((node) => !!childrenInMap[node.object.id])
            results.totalCount = results.nodes.length
        }

        if (params.childrenNotIn && params.childrenNotIn.length > 0) {
            const childrenNotInMap = {}
            params.childrenNotIn.forEach((child) => {
                childrenNotInMap[child.object.id] = child
            })
            results.nodes = results.nodes.filter((node) => !childrenNotInMap[node.object.id])
            results.totalCount = results.nodes.length
        }

        if (
            params.offset !== undefined &&
            params.offset !== null &&
            params.limit !== undefined &&
            params.limit !== null
        ) {
            results.nodes = results.nodes.slice(params.offset, params.offset + params.limit)
        }
    }

    _filterResultsByFunction(params, results, filterFunction) {
        var filteredResults = { nodes: [] }

        results.objects.forEach((object) => {
            var node = filterFunction(object)

            if (node) {
                filteredResults.nodes.push(node)
            }
        })

        if (params.offset === 0 && params.limit >= results.totalCount) {
            // all available results have been loaded from the server, as the total count let's use the number of results that passed the client-side filtering (that's more accurate)
            filteredResults.totalCount = filteredResults.nodes.length
        } else {
            // otherwise we have to use the total count from the server as we cannot tell how much of them would pass the client-side filtering without loading them all
            filteredResults.totalCount = results.totalCount
        }

        return filteredResults
    }

    _isLabNotebookSpace(space) {
        var showLabNotebook = SettingsManagerUtils.isEnabledForGroup(
            space.getCode(),
            SettingsManagerUtils.ShowSetting.showLabNotebook
        )
        var isLabNotebookSpace = !profile.isInventorySpace(space.getCode())
        var isHiddenSpace = profile.isHiddenSpace(space.getCode())
        return showLabNotebook && isLabNotebookSpace && !isHiddenSpace
    }

    _isInventorySpace(space) {
        var showInventory = SettingsManagerUtils.isEnabledForGroup(
            space.getCode(),
            SettingsManagerUtils.ShowSetting.showInventory
        )
        var isInventorySpace = profile.isInventorySpace(space.getCode())
        var isHiddenSpace = profile.isHiddenSpace(space.getCode())
        return (
            showInventory &&
            isInventorySpace &&
            !isHiddenSpace &&
            !space.getCode().endsWith("STOCK_CATALOG") &&
            !space.getCode().endsWith("STOCK_ORDERS")
        )
    }

    _isStockSpace(space) {
        var showStock = SettingsManagerUtils.isEnabledForGroup(
            space.getCode(),
            SettingsManagerUtils.ShowSetting.showStock
        )
        var isInventorySpace = profile.isInventorySpace(space.getCode())
        return (
            showStock &&
            isInventorySpace &&
            (space.getCode().endsWith("STOCK_CATALOG") || space.getCode().endsWith("STOCK_ORDERS"))
        )
    }

    _isQueriesProject(project) {
        return (
            project.code == "QUERIES" &&
            project.description == ELNDictionary.generatedObjects.searchQueriesProject.description
        )
    }

    _isExperimentWithChildren(experiment) {
        var experimentIdentifier = experiment.getIdentifier().getIdentifier()
        var experimentSpaceCode = IdentifierUtil.getSpaceCodeFromIdentifier(experimentIdentifier)
        var isInventorySpace = profile.isInventorySpace(experimentSpaceCode)

        var isInventoryCollectionExperiment = experiment.getType().getCode() === "COLLECTION" || isInventorySpace
        return !isInventoryCollectionExperiment
    }

    _isExperimentSample(sample) {
        if (!sample.getExperiment()) {
            return false
        }

        var sampleIsExperiment = sample.getType().getCode().indexOf("EXPERIMENT") > -1
        var sampleTypeOnNav = profile.showOnNavForSpace(
            IdentifierUtil.getSpaceCodeFromIdentifier(sample.getIdentifier().getIdentifier()),
            sample.getType().getCode()
        )
        if (
            (sampleIsExperiment || sampleTypeOnNav) &&
            profile.showOnNavForSpace(
                IdentifierUtil.getSpaceCodeFromIdentifier(sample.getIdentifier().getIdentifier()),
                sample.getType().getCode()
            )
        ) {
            return this._getSampleParent(sample) === null
        }

        return false
    }

    _isChildSample(sample) {
        if (!sample.getExperiment()) {
            return false
        }

        var sampleTypeOnNav = profile.showOnNavForSpace(
            IdentifierUtil.getSpaceCodeFromIdentifier(sample.getIdentifier().getIdentifier()),
            sample.getType().getCode()
        )

        if (sampleTypeOnNav) {
            return this._getSampleParent(sample) !== null
        }

        return false
    }

    _getSampleParent(sample) {
        if (sample.getExperiment() && sample.getParents()) {
            var sampleExperimentIdentifier = sample.getExperiment().getIdentifier().getIdentifier()
            var sampleParent = sample.getParents().find((parent) => {
                if (!parent.getExperiment()) {
                    return false
                }
                var parentIdentifier = parent.getIdentifier().getIdentifier()
                var parentExperimentIdentifier = parent.getExperiment().getIdentifier().getIdentifier()
                return (
                    profile.isELNIdentifier(parentIdentifier) &&
                    parentExperimentIdentifier === sampleExperimentIdentifier
                )
            })
            return sampleParent ? sampleParent : null
        }
        return null
    }

    _isExperimentDataSet(dataset) {
        if (dataset.getExperiment() && !dataset.getSample()) {
            var datasetTypeCode = dataset.getType().getCode()
            if (profile.showDatasetOnNav(datasetTypeCode)) {
                return true
            }
        }
        return false
    }

    _isSampleDataSet(dataset) {
        if (dataset.getSample()) {
            var datasetTypeCode = dataset.getType().getCode()
            if (profile.showDatasetOnNav(datasetTypeCode)) {
                return true
            }
        }
        return false
    }
}
