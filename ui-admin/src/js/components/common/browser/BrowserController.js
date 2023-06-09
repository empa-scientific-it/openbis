import _ from 'lodash'
import autoBind from 'auto-bind'
import ComponentController from '@src/js/components/common/ComponentController.js'
import BrowserTreeController from '@src/js/components/common/browser/BrowserTreeController.js'
import util from '@src/js/common/util.js'

export default class BrowserController extends ComponentController {
  async loadNodePath(params) {
    throw 'Method not implemented'
  }

  async loadNodes(params) {
    throw 'Method not implemented'
  }

  async loadSettings() {
    throw 'Method not implemented'
  }

  async onSettingsChange(settings) {
    throw 'Method not implemented'
  }

  onSelectedChange(params) {
    throw 'Method not implemented'
  }

  onError(error) {
    throw 'Method not implemented'
  }

  async _doLoadNodePath(params) {
    try {
      return await this.loadNodePath(params)
    } catch (error) {
      this._onError(error)
    }
  }

  async _doLoadNodes(params) {
    try {
      return await this.loadNodes(params)
    } catch (error) {
      this._onError(error)
    }
  }

  constructor() {
    super()
    autoBind(this)

    const controller = this

    class FullTreeController extends BrowserTreeController {
      getState() {
        return controller.getState('fullTree')
      }
      async setState(state) {
        return controller.setState(state, 'fullTree')
      }
      async loadNodePath(params) {
        return controller._doLoadNodePath(params)
      }
      async loadNodes(params) {
        const loadResult = await controller._doLoadNodes({
          ...params,
          filter: null
        })

        function verifyNodes(loadResult) {
          if (!_.isEmpty(loadResult) && !_.isEmpty(loadResult.nodes)) {
            const nodesWithChildren = loadResult.nodes.filter(
              node => !_.isNil(node.children)
            )
            if (!_.isEmpty(nodesWithChildren)) {
              console.error(
                'Unfiltered tree nodes cannot be loaded together with their children. They must be loaded level by level, otherwise the custom sorting will not work properly. Incorrect nodes: ' +
                  JSON.stringify(nodesWithChildren)
              )
              loadResult.nodes = []
              loadResult.totalCount = 0
            }
          }
        }

        verifyNodes(loadResult)

        return loadResult
      }
      async loadSettings() {
        return controller.settings.fullTree
      }
      onSettingsChange(settings) {
        controller.settings.fullTree = settings
        controller._saveSettings()
      }
      onSelectedChange(params) {
        if (controller._getTreeController() === this) {
          controller.onSelectedChange(params)
        }
      }
    }

    class FilteredTreeController extends BrowserTreeController {
      getState() {
        return controller.getState('filteredTree')
      }
      async setState(state) {
        return controller.setState(state, 'filteredTree')
      }
      async loadNodePath(params) {
        return controller._doLoadNodePath(params)
      }
      async loadNodes(params) {
        const { filter } = controller.getState()

        const loadResult = await controller._doLoadNodes({
          ...params,
          filter: util.trim(filter)
        })

        function modifyNodes(loadResult) {
          if (!_.isEmpty(loadResult) && !_.isEmpty(loadResult.nodes)) {
            loadResult.nodes.forEach(node => {
              delete node.sortings
              delete node.sortingId
              node.draggable = false
              node.rootable = false
              if (!_.isEmpty(node.children)) {
                modifyNodes(node.children)
              }
            })
          }
        }

        modifyNodes(loadResult)

        return loadResult
      }
      async loadSettings() {
        // do nothing
      }
      onSettingsChange(settings) {
        // do nothing
      }
      onSelectedChange(params) {
        if (controller._getTreeController() === this) {
          controller.onSelectedChange(params)
        }
      }
    }

    this.setState({
      loaded: false,
      loading: false,
      nodeSetAsRoot: null,
      filter: null,
      autoShowSelectedObject: true
    })

    this.settings = {}
    this.fullTreeController = new FullTreeController()
    this.filteredTreeController = new FilteredTreeController()
    this.lastFilterTimeoutId = null
  }

  async load() {
    await this.setState({
      loading: true
    })

    this.settings = await this._loadSettings()

    if (!_.isEmpty(this.settings)) {
      this.setState(state => {
        const newState = { ...state }
        _.merge(newState, this.settings.common)
        return newState
      })
    }

    let { nodeSetAsRoot } = this.getState()

    if (nodeSetAsRoot) {
      const nodeSetAsRootPath = await this._doLoadNodePath({
        object: nodeSetAsRoot.object
      })
      if (_.isEmpty(nodeSetAsRootPath)) {
        nodeSetAsRoot = null
      }
    }

    await this._getTreeController().load(nodeSetAsRoot)
    await this._getTreeController().expandNode(
      this._getTreeController().getRoot().id
    )

    await this.showSelectedObject()

    await this.setState({
      loaded: true,
      loading: false,
      nodeSetAsRoot
    })

    this._saveSettings()
  }

  async loadNode(nodeId, offset, limit, append) {
    await this._getTreeController().loadNode(nodeId, offset, limit, append)
  }

  async reloadNode(nodeId) {
    await this._getTreeController().reloadNode(nodeId)
  }

  async filterChange(newFilter) {
    await this._setFilter(newFilter, 500)
  }

  async filterClear() {
    await this._setFilter(null, 0)
  }

  async _setFilter(newFilter, silentPeriod) {
    await this.setState({
      filter: newFilter,
      loading: true
    })

    if (this.lastFilterTimeoutId) {
      clearTimeout(this.lastFilterTimeoutId)
      this.lastFilterTimeoutId = null
    }

    if (util.trim(newFilter) === null) {
      await this.load()
    } else {
      return new Promise((resolve, reject) => {
        this.lastFilterTimeoutId = setTimeout(async () => {
          try {
            await this.load()
            resolve()
          } catch (e) {
            reject(e)
          }
        }, silentPeriod)
      })
    }
  }

  async loadMoreNodes(nodeId) {
    await this._getTreeController().loadMoreNodes(nodeId)
  }

  async expandNode(nodeId) {
    await this._getTreeController().expandNode(nodeId)
  }

  async collapseNode(nodeId) {
    await this._getTreeController().collapseNode(nodeId)
  }

  async collapseAllNodes(nodeId) {
    await this._getTreeController().collapseAllNodes(nodeId)
  }

  async undoCollapseAllNodes(nodeId) {
    await this._getTreeController().undoCollapseAllNodes(nodeId)
  }

  canUndoCollapseAllNodes(nodeId) {
    return this._getTreeController().canUndoCollapseAllNodes(nodeId)
  }

  async setNodeAsRoot(nodeId) {
    let nodeSetAsRoot = null

    if (!_.isNil(nodeId)) {
      const currentRoot = this.getNodeSetAsRoot()

      if (!_.isNil(currentRoot)) {
        const pathIndex = currentRoot.path.findIndex(pathNode =>
          _.isEqual(pathNode.id, nodeId)
        )

        if (pathIndex !== -1) {
          // new root selected from the existing root path

          nodeSetAsRoot = {
            ...currentRoot.path[pathIndex],
            path: currentRoot.path.slice(0, pathIndex + 1)
          }
        }
      }

      if (_.isNil(nodeSetAsRoot)) {
        let root = this._getTreeController().getRoot()
        let node = this._getTreeController().getNode(nodeId)
        let path = []

        if (node) {
          while (!_.isNil(node) && node !== root) {
            path.unshift({
              ...node
            })
            node = this._getTreeController().getNode(node.parentId)
          }

          if (!_.isEmpty(path)) {
            // new root selected in the browser with an existing root path prepended

            if (!_.isNil(currentRoot)) {
              path.unshift(...currentRoot.path)
            }

            nodeSetAsRoot = {
              ...path[path.length - 1],
              path
            }
          }
        }
      }
    }

    await this.setState({
      nodeSetAsRoot,
      loaded: false
    })

    await this._saveSettings()
    await this.load()
  }

  async selectObject(nodeObject, event) {
    await this.fullTreeController.selectObject(nodeObject, event)
    await this.filteredTreeController.selectObject(nodeObject, event)
    await this.showSelectedObject()
  }

  async changeAutoShowSelectedObject() {
    let { autoShowSelectedObject } = this.getState()

    autoShowSelectedObject = !autoShowSelectedObject
    await this.setState({
      autoShowSelectedObject
    })

    await this.showSelectedObject()

    this._saveSettings()
  }

  async showSelectedObject() {
    const { autoShowSelectedObject } = this.getState()
    if (autoShowSelectedObject) {
      await this._getTreeController().showSelectedObject(
        this.isFullTreeVisible()
      )
    }
  }

  async changeSorting(nodeId, sortingId) {
    await this._getTreeController().changeSorting(nodeId, sortingId)
  }

  async changeCustomSorting(nodeId, oldIndex, newIndex) {
    await this._getTreeController().changeCustomSorting(
      nodeId,
      oldIndex,
      newIndex
    )
  }

  async clearCustomSorting(nodeId) {
    await this._getTreeController().clearCustomSorting(nodeId)
  }

  isLoaded() {
    const { loaded } = this.getState()
    return loaded
  }

  isLoading() {
    const { loading } = this.getState()
    return loading
  }

  isTreeLoading() {
    return this._getTreeController().isLoading()
  }

  getRoot() {
    return this._getTreeController().getRoot()
  }

  getNodeSetAsRoot() {
    const { nodeSetAsRoot } = this.getState()
    return nodeSetAsRoot
  }

  getNode(nodeId) {
    return this._getTreeController().getNode(nodeId)
  }

  getNodes() {
    return this._getTreeController().getNodes()
  }

  getSelectedObject() {
    return this._getTreeController().getSelectedObject()
  }

  isAutoShowSelectedObject() {
    const { autoShowSelectedObject } = this.getState()
    return autoShowSelectedObject
  }

  getTree() {
    return this._getTreeController().getTree()
  }

  getFullTree() {
    return this.fullTreeController.getTree()
  }

  isFullTreeVisible() {
    return this._getTreeController() === this.fullTreeController
  }

  getFilteredTree() {
    return this.filteredTreeController.getTree()
  }

  isFilteredTreeVisible() {
    return this._getTreeController() === this.filteredTreeController
  }

  getFilter() {
    const { filter } = this.getState()
    return filter
  }

  _getTreeController() {
    const { filter } = this.getState()
    if (util.trim(filter)) {
      return this.filteredTreeController
    } else {
      return this.fullTreeController
    }
  }

  async _loadSettings() {
    const loaded = await this.loadSettings()

    if (!loaded || !_.isObject(loaded)) {
      return {}
    }

    const settings = {
      common: {},
      fullTree: {},
      filteredTree: {}
    }

    if (_.isObject(loaded.common)) {
      const common = {}

      if (_.isBoolean(loaded.common.autoShowSelectedObject)) {
        common.autoShowSelectedObject = loaded.common.autoShowSelectedObject
      }

      settings.common = common
    }

    if (_.isObject(loaded.fullTree)) {
      settings.fullTree = loaded.fullTree
    }

    if (_.isObject(loaded.filteredTree)) {
      settings.filteredTree = loaded.filteredTree
    }

    return settings
  }

  async _saveSettings() {
    const state = this.getState()

    const settings = {
      common: {
        autoShowSelectedObject: state.autoShowSelectedObject
      },
      fullTree: this.settings.fullTree || {},
      filteredTree: this.settings.filteredTree || {}
    }

    await this.onSettingsChange(settings)
  }

  _onError(error) {
    this.onError(error)
    throw error
  }
}
