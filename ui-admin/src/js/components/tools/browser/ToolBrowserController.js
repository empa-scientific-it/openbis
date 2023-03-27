import AppController from '@src/js/components/AppController.js'
import BrowserController from '@src/js/components/common/browser/BrowserController.js'
import ToolBrowserControllerLoadNodePath from '@src/js/components/tools/browser/ToolBrowserControllerLoadNodePath.js'
import ToolBrowserControllerLoadNodes from '@src/js/components/tools/browser/ToolBrowserControllerLoadNodes.js'
import ToolBrowserControllerAddNode from '@src/js/components/tools/browser/ToolBrowserControllerAddNode.js'
import ToolBrowserControllerRemoveNode from '@src/js/components/tools/browser/ToolBrowserControllerRemoveNode.js'
import ToolBrowserControllerReload from '@src/js/components/tools/browser/ToolBrowserControllerReload.js'
import pages from '@src/js/common/consts/pages.js'
import ids from '@src/js/common/consts/ids.js'

export default class ToolBrowserController extends BrowserController {
  getId() {
    return ids.TOOL_BROWSER_ID
  }

  async loadNodePath(params) {
    return await new ToolBrowserControllerLoadNodePath().doLoadNodePath(params)
  }

  async loadNodes(params) {
    return await new ToolBrowserControllerLoadNodes().doLoadNodes(params)
  }

  onSelectedChange({ object }) {
    if (object) {
      AppController.getInstance().objectOpen(
        pages.TOOLS,
        object.type,
        object.id
      )
    }
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
