import BrowserController from '@src/js/components/common/browser2/BrowserController.js'
import TypeBrowserControllerLoadNodePath from '@src/js/components/types/browser2/TypeBrowserControllerLoadNodePath.js'
import TypeBrowserControllerLoadNodesFiltered from '@src/js/components/types/browser2/TypeBrowserControllerLoadNodesFiltered.js'
import TypeBrowserControllerLoadNodesUnfiltered from '@src/js/components/types/browser2/TypeBrowserControllerLoadNodesUnfiltered.js'
import TypeBrowserControllerAddNode from '@src/js/components/types/browser2/TypeBrowserControllerAddNode.js'
import TypeBrowserControllerRemoveNode from '@src/js/components/types/browser2/TypeBrowserControllerRemoveNode.js'
import TypeBrowserControllerReload from '@src/js/components/types/browser2/TypeBrowserControllerReload.js'

export default class TypeBrowserController extends BrowserController {
  async doLoadNodePath(params) {
    return await new TypeBrowserControllerLoadNodePath().doLoadNodePath(params)
  }

  async doLoadNodes(params) {
    const { filter } = params

    if (filter) {
      return await new TypeBrowserControllerLoadNodesFiltered().doLoadFilteredNodes(
        params
      )
    } else {
      return await new TypeBrowserControllerLoadNodesUnfiltered().doLoadUnfilteredNodes(
        params
      )
    }
  }

  async reload(objectModifications) {
    new TypeBrowserControllerReload(this).reload(objectModifications)
  }

  canAddNode() {
    return new TypeBrowserControllerAddNode().canAddNode(
      this.getSelectedObject()
    )
  }

  addNode() {
    new TypeBrowserControllerAddNode().doAddNode(this.getSelectedObject())
  }

  canRemoveNode() {
    return new TypeBrowserControllerRemoveNode().canRemoveNode(
      this.getSelectedObject()
    )
  }

  removeNode() {
    new TypeBrowserControllerRemoveNode().doRemoveNode(this.getSelectedObject())
  }
}
