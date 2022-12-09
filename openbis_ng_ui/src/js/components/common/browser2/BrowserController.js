import _ from 'lodash'
import autoBind from 'auto-bind'
import ComponentContextNamespaced from '@src/js/components/common/ComponentContextNamespaced.js'
import BrowserTreeController from '@src/js/components/common/browser2/BrowserTreeController.js'
import util from '@src/js/common/util.js'

export default class BrowserController {
  async doLoadNodePath() {
    throw 'Method not implemented'
  }

  async doLoadNodes() {
    throw 'Method not implemented'
  }

  constructor() {
    autoBind(this)

    const controller = this

    class FullTreeController extends BrowserTreeController {
      async doLoadNodePath(params) {
        return controller.doLoadNodePath(params)
      }
      async doLoadNodes(params) {
        return await controller.doLoadNodes({
          ...params,
          filter: null
        })
      }
    }

    class FilteredTreeController extends BrowserTreeController {
      async doLoadNodePath(params) {
        return controller.doLoadNodePath(params)
      }
      async doLoadNodes(params) {
        const { filter } = controller.context.getState()
        return await controller.doLoadNodes({
          ...params,
          filter: util.trim(filter)
        })
      }
    }

    this.settings = {}
    this.fullTreeController = new FullTreeController()
    this.filteredTreeController = new FilteredTreeController()
    this.lastFilterTimeoutId = null
  }

  async init(context) {
    context.initState({
      loaded: false,
      loading: false,
      nodeSetAsRoot: null,
      filter: null,
      autoShowSelectedObject: true
    })

    this.fullTreeController.init(
      new ComponentContextNamespaced(context, 'fullTree', originalProps => ({
        loadSettings: () => {
          return this.settings.fullTree
        },
        onSettingsChange: settings => {
          this.settings.fullTree = settings
          this._saveSettings()
        },
        onSelectedChange: originalProps.onSelectedChange
      }))
    )

    this.filteredTreeController.init(
      new ComponentContextNamespaced(context, 'filteredTree', () => ({}))
    )

    this.context = context
  }

  async load() {
    await this.context.setState({
      loading: true
    })

    this.settings = await this._loadSettings()

    if (!_.isEmpty(this.settings)) {
      this.context.setState(state => {
        const newState = { ...state }
        _.merge(newState, this.settings.common)
        return newState
      })
    }

    let { nodeSetAsRoot, autoShowSelectedObject } = this.context.getState()

    if (nodeSetAsRoot) {
      const nodeSetAsRootPath = await this.doLoadNodePath({
        object: nodeSetAsRoot.object
      })
      if (!_.isEmpty(nodeSetAsRootPath)) {
        nodeSetAsRoot.path = nodeSetAsRootPath
      } else {
        nodeSetAsRoot = null
      }
    }

    await this._getTreeController().load(nodeSetAsRoot)
    await this._getTreeController().expandNode(
      this._getTreeController().getRoot().id
    )

    if (autoShowSelectedObject) {
      await this._getTreeController().showSelectedObject()
    }

    await this.context.setState({
      loaded: true,
      loading: false,
      nodeSetAsRoot
    })

    this._saveSettings()
  }

  async filterChange(newFilter) {
    await this._setFilter(newFilter, 500)
  }

  async filterClear() {
    await this._setFilter(null, 0)
  }

  async _setFilter(newFilter, silentPeriod) {
    await this.context.setState({
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

  async setNodeAsRoot(node) {
    let nodeSetAsRoot = null

    if (node) {
      const path = await this.doLoadNodePath({
        object: node.object
      })

      nodeSetAsRoot = {
        ...node,
        path: path,
        canHaveChildren: true,
        children: []
      }
    }

    await this.context.setState({
      nodeSetAsRoot,
      loaded: false
    })
    await this._saveSettings()
    await this.load()
  }

  async selectObject(nodeObject) {
    const { autoShowSelectedObject } = this.context.getState()
    await this.fullTreeController.selectObject(nodeObject)
    await this.filteredTreeController.selectObject(nodeObject)
    if (autoShowSelectedObject) {
      await this._getTreeController().showSelectedObject()
    }
  }

  async changeAutoShowSelectedObject() {
    let { autoShowSelectedObject } = this.context.getState()

    autoShowSelectedObject = !autoShowSelectedObject
    await this.context.setState({
      autoShowSelectedObject
    })

    if (autoShowSelectedObject) {
      await this._getTreeController().showSelectedObject()
    }

    this._saveSettings()
  }

  async changeSorting(nodeId, sortingId) {
    await this._getTreeController().changeSorting(nodeId, sortingId)
  }

  isLoaded() {
    const { loaded } = this.context.getState()
    return loaded
  }

  isLoading() {
    const { loading } = this.context.getState()
    return loading
  }

  isTreeLoading() {
    return this._getTreeController().isLoading()
  }

  getRoot() {
    return this._getTreeController().getRoot()
  }

  getNodeSetAsRoot() {
    const { nodeSetAsRoot } = this.context.getState()
    return nodeSetAsRoot
  }

  getSelectedObject() {
    return this._getTreeController().getSelectedObject()
  }

  isAutoShowSelectedObject() {
    const { autoShowSelectedObject } = this.context.getState()
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
    const { filter } = this.context.getState()
    return filter
  }

  _getTreeController() {
    const { filter } = this.context.getState()
    if (util.trim(filter)) {
      return this.filteredTreeController
    } else {
      return this.fullTreeController
    }
  }

  async _loadSettings() {
    const props = this.context.getProps()

    if (!props.loadSettings) {
      return {}
    }

    const loaded = await props.loadSettings()

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

      if (_.isObject(loaded.common.nodeSetAsRoot)) {
        common.nodeSetAsRoot = loaded.common.nodeSetAsRoot
      }

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
    const { onSettingsChange } = this.context.getProps()

    if (onSettingsChange) {
      const state = this.context.getState()

      const settings = {
        common: {
          nodeSetAsRoot: state.nodeSetAsRoot,
          autoShowSelectedObject: state.autoShowSelectedObject
        },
        fullTree: this.settings.fullTree || {},
        filteredTree: this.settings.filteredTree || {}
      }

      await onSettingsChange(settings)
    }
  }
}
