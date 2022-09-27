import _ from 'lodash'
import autoBind from 'auto-bind'
import ComponentContextNamespaced from '@src/js/components/common/ComponentContextNamespaced.js'
import BrowserSubController from '@src/js/components/common/browser2/BrowserSubController.js'
import util from '@src/js/common/util.js'

export default class BrowserController {
  async doLoadNodes() {
    throw 'Method not implemented'
  }

  constructor() {
    autoBind(this)

    const controller = this

    class TreeNodesController extends BrowserSubController {
      async doLoadNodes(params) {
        return await controller.doLoadNodes({
          ...params,
          filter: null
        })
      }
    }

    class FilteredNodesController extends BrowserSubController {
      async doLoadNodes(params) {
        const { filter } = controller.context.getState()
        return await controller.doLoadNodes({
          ...params,
          filter: util.trim(filter)
        })
      }
    }

    this.treeNodesController = new TreeNodesController()
    this.filteredNodesController = new FilteredNodesController()
    this.lastFilterTimeoutId = null
  }

  async init(context) {
    context.initState({
      loaded: false,
      loading: false,
      filter: null
    })

    this.treeNodesController.init(
      new ComponentContextNamespaced(context, 'treeNodes', () => ({
        loadSettings: () => {
          return this.settings.treeNodes
        },
        onSettingsChange: settings => {
          this.settings.treeNodes = settings
          this._saveSettings()
        }
      }))
    )

    this.filteredNodesController.init(
      new ComponentContextNamespaced(context, 'filteredNodes', () => ({}))
    )

    this.context = context
  }

  async load() {
    this.context.setState({
      loading: true
    })

    this.settings = await this._loadSettings()
    await this.treeNodesController.load()

    this.context.setState({
      loaded: true,
      loading: false
    })
  }

  async filterChange(newFilter) {
    this._setFilter(newFilter, 500)
  }

  async filterClear() {
    this._setFilter(null, 0)
  }

  async _setFilter(newFilter, silentPeriod) {
    await this.context.setState({
      filter: newFilter
    })

    if (this.lastFilterTimeoutId) {
      clearTimeout(this.lastFilterTimeoutId)
      this.lastFilterTimeoutId = null
    }

    if (util.trim(newFilter) === null) {
      await this.filteredNodesController.clear()
    } else {
      this.lastFilterTimeoutId = setTimeout(async () => {
        await this.filteredNodesController.load()
      }, silentPeriod)
    }
  }

  async nodeLoadMore(nodeId) {
    await this._getSubController().nodeLoadMore(nodeId)
  }

  async nodeExpand(nodeId) {
    await this._getSubController().nodeExpand(nodeId)
  }

  async nodeCollapse(nodeId) {
    await this._getSubController().nodeCollapse(nodeId)
  }

  async nodeSelect(nodeId) {
    await this._getSubController().nodeSelect(nodeId)
  }

  async objectSelect(object) {
    await this._getSubController().objectSelect(object)
  }

  isLoading() {
    const { loading } = this.context.getState()
    return loading || this._getSubController().isLoading()
  }

  isRoot(node) {
    return BrowserSubController.isRoot(node)
  }

  getRoot() {
    return this._getSubController().getRoot()
  }

  getFullTree() {
    return this.treeNodesController.getTree()
  }

  isFullTreeVisible() {
    return this._getSubController() === this.treeNodesController
  }

  getFilteredTree() {
    return this.filteredNodesController.getTree()
  }

  isFilteredTreeVisible() {
    return this._getSubController() === this.filteredNodesController
  }

  getFilter() {
    const { filter } = this.context.getState()
    return filter
  }

  _getSubController() {
    const { filter } = this.context.getState()
    if (util.trim(filter)) {
      return this.filteredNodesController
    } else {
      return this.treeNodesController
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

    return loaded
  }

  async _saveSettings() {
    const { onSettingsChange } = this.context.getProps()

    if (onSettingsChange) {
      onSettingsChange(this.settings || {})
    }
  }
}
