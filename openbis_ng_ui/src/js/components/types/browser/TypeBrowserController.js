import BrowserController from '@src/js/components/common/browser2/BrowserController.js'
import TypeBrowserControllerLoadNodePath from '@src/js/components/types/browser/TypeBrowserControllerLoadNodePath.js'
import TypeBrowserControllerLoadNodes from '@src/js/components/types/browser/TypeBrowserControllerLoadNodes.js'
import TypeBrowserControllerAddNode from '@src/js/components/types/browser/TypeBrowserControllerAddNode.js'
import TypeBrowserControllerRemoveNode from '@src/js/components/types/browser/TypeBrowserControllerRemoveNode.js'
import TypeBrowserControllerReload from '@src/js/components/types/browser/TypeBrowserControllerReload.js'

export default class TypeBrowserController extends BrowserController {
  async doLoadNodePath(params) {
    return await new TypeBrowserControllerLoadNodePath().doLoadNodePath(params)
  }

  async doLoadNodes(params) {
    return await new TypeBrowserControllerLoadNodes().doLoadNodes(params)
  }

  async reload(objectModifications) {
    new TypeBrowserControllerReload(this).reload(objectModifications)
  }

  canAddNode() {
    return new TypeBrowserControllerAddNode().canAddNode(
      this.getSelectedObject()
    )
  }

  async addNode() {
    await new TypeBrowserControllerAddNode().doAddNode(this.getSelectedObject())
  }

  canRemoveNode() {
    return new TypeBrowserControllerRemoveNode().canRemoveNode(
      this.getSelectedObject()
    )
  }

  async removeNode() {
    await new TypeBrowserControllerRemoveNode().doRemoveNode(
      this.getSelectedObject()
    )
  }
}
