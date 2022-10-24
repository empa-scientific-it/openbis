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

    await this.fullTreeController.load()
    await this.fullTreeController.expandNode(
      this.fullTreeController.getRoot().id
    )

    const { autoShowSelectedObject } = this.context.getState()
    if (autoShowSelectedObject) {
      await this.fullTreeController.showSelectedObject()
    }

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
      const { autoShowSelectedObject } = this.context.getState()
      if (autoShowSelectedObject) {
        await this.fullTreeController.showSelectedObject()
      }
      await this.filteredTreeController.collapseNode(
        this.filteredTreeController.getRoot().id
      )
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
      await this.fullTreeController.showSelectedObject()
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
    return loading || this._getTreeController().isLoading()
  }

  getRoot() {
    return this._getTreeController().getRoot()
  }

  getSelectedObject() {
    return this._getTreeController().getSelectedObject()
  }

  isAutoShowSelectedObject() {
    const { autoShowSelectedObject } = this.context.getState()
    return autoShowSelectedObject
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
        common: { autoShowSelectedObject: state.autoShowSelectedObject },
        fullTree: this.settings.fullTree || {},
        filteredTree: this.settings.filteredTree || {}
      }

      onSettingsChange(settings)
    }
  }
}
