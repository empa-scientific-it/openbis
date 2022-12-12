import BrowserController from '@src/js/components/common/browser2/BrowserController.js'
import ToolBrowserControllerLoadNodePath from '@src/js/components/tools/browser2/ToolBrowserControllerLoadNodePath.js'
import ToolBrowserControllerLoadNodes from '@src/js/components/tools/browser2/ToolBrowserControllerLoadNodes.js'
import ToolBrowserControllerAddNode from '@src/js/components/tools/browser2/ToolBrowserControllerAddNode.js'
import ToolBrowserControllerRemoveNode from '@src/js/components/tools/browser2/ToolBrowserControllerRemoveNode.js'
import ToolBrowserControllerReload from '@src/js/components/tools/browser2/ToolBrowserControllerReload.js'

export default class TypeBrowserController extends BrowserController {
  async doLoadNodePath(params) {
    return await new ToolBrowserControllerLoadNodePath().doLoadNodePath(params)
  }

  async doLoadNodes(params) {
    return await new ToolBrowserControllerLoadNodes().doLoadNodes(params)
  }

  async reload(objectModifications) {
    new ToolBrowserControllerReload(this).reload(objectModifications)
  }

  canAddNode() {
    return new ToolBrowserControllerAddNode().canAddNode(
      this.getSelectedObject()
    )
  }

  async addNode() {
    await new ToolBrowserControllerAddNode().doAddNode(this.getSelectedObject())
  }

  canRemoveNode() {
    return new ToolBrowserControllerRemoveNode().canRemoveNode(
      this.getSelectedObject()
    )
  }

  async removeNode() {
    await new ToolBrowserControllerRemoveNode().doRemoveNode(
      this.getSelectedObject()
    )
  }
}
