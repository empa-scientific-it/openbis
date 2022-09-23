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

  init(context) {
    context.initState({
      filter: null
    })

    this.treeNodesController.init(
      new ComponentContextNamespaced(context, 'treeNodes')
    )
    this.filteredNodesController.init(
      new ComponentContextNamespaced(context, 'filteredNodes')
    )
    this.context = context
  }

  async load() {
    await this.treeNodesController.load()
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
    await this.getSubController().nodeLoadMore(nodeId)
  }

  async nodeExpand(nodeId) {
    await this.getSubController().nodeExpand(nodeId)
  }

  async nodeCollapse(nodeId) {
    await this.getSubController().nodeCollapse(nodeId)
  }

  async nodeSelect(nodeId) {
    await this.getSubController().nodeSelect(nodeId)
  }

  async objectSelect(object) {
    await this.getSubController().objectSelect(object)
  }

  isRoot(node) {
    return BrowserSubController.isRoot(node)
  }

  getRoot() {
    return this.getSubController().getRoot()
  }

  getTreeRoot() {
    return this.treeNodesController.getRoot()
  }

  getTreeNodes() {
    return this.treeNodesController.getNodes()
  }

  getFilteredRoot() {
    return this.filteredNodesController.getRoot()
  }

  getFilteredNodes() {
    return this.filteredNodesController.getNodes()
  }

  getFilter() {
    const { filter } = this.context.getState()
    return filter
  }

  getSubController() {
    const { filter } = this.context.getState()
    if (util.trim(filter)) {
      return this.filteredNodesController
    } else {
      return this.treeNodesController
    }
  }
}
