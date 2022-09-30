import _ from 'lodash'
import autoBind from 'auto-bind'
import ComponentContextNamespaced from '@src/js/components/common/ComponentContextNamespaced.js'
import BrowserTreeController from '@src/js/components/common/browser2/BrowserTreeController.js'
import util from '@src/js/common/util.js'

export default class BrowserController {
  async doLoadNodes() {
    throw 'Method not implemented'
  }

  constructor() {
    autoBind(this)

    const controller = this

    class FullTreeController extends BrowserTreeController {
      async doLoadNodes(params) {
        return await controller.doLoadNodes({
          ...params,
          filter: null
        })
      }
    }

    class FilteredTreeController extends BrowserTreeController {
      async doLoadNodes(params) {
        const { filter } = controller.context.getState()
        return await controller.doLoadNodes({
          ...params,
          filter: util.trim(filter)
        })
      }
    }

    this.fullTreeController = new FullTreeController()
    this.filteredTreeController = new FilteredTreeController()
    this.lastFilterTimeoutId = null
  }

  async init(context) {
    context.initState({
      loaded: false,
      loading: false,
      filter: null
    })

    this.fullTreeController.init(
      new ComponentContextNamespaced(context, 'fullTree', () => ({
        loadSettings: () => {
          return this.settings.fullTree
        },
        onSettingsChange: settings => {
          this.settings.fullTree = settings
          this._saveSettings()
        }
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
    await this.fullTreeController.load()
    await this.fullTreeController.expandNode(
      this.fullTreeController.getRoot().id
    )
    await this.filteredTreeController.load()

    await this.context.setState({
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
      await this.filteredTreeController.load()
    } else {
      this.lastFilterTimeoutId = setTimeout(async () => {
        await this.filteredTreeController.load()
        await this.filteredTreeController.expandNode(
          this.filteredTreeController.getRoot().id
        )
      }, silentPeriod)
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

  async selectNode(nodeId) {
    await this._getTreeController().selectNode(nodeId)
  }

  async selectObject(object) {
    await this._getTreeController().selectObject(object)
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
    return loading || this._getTreeController().isLoading()
  }

  getRoot() {
    return this._getTreeController().getRoot()
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

    return loaded
  }

  async _saveSettings() {
    const { onSettingsChange } = this.context.getProps()

    if (onSettingsChange) {
      onSettingsChange(this.settings || {})
    }
  }
}
