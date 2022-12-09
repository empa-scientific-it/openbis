import BrowserController from '@src/js/components/common/browser2/BrowserController.js'
import UserBrowserControllerLoadNodePath from '@src/js/components/users/browser2/UserBrowserControllerLoadNodePath.js'
import UserBrowserControllerLoadNodes from '@src/js/components/users/browser2/UserBrowserControllerLoadNodes.js'
import UserBrowserControllerAddNode from '@src/js/components/users/browser2/UserBrowserControllerAddNode.js'
import UserBrowserControllerRemoveNode from '@src/js/components/users/browser2/UserBrowserControllerRemoveNode.js'
import UserBrowserControllerReload from '@src/js/components/users/browser2/UserBrowserControllerReload.js'

export default class UserBrowserController extends BrowserController {
  async doLoadNodePath(params) {
    return await new UserBrowserControllerLoadNodePath().doLoadNodePath(params)
  }

  async doLoadNodes(params) {
    return await new UserBrowserControllerLoadNodes().doLoadNodes(params)
  }

  async reload(objectModifications) {
    new UserBrowserControllerReload(this).reload(objectModifications)
  }

  canAddNode() {
    return new UserBrowserControllerAddNode().canAddNode(
      this.getSelectedObject()
    )
  }

  async addNode() {
    await new UserBrowserControllerAddNode().doAddNode(this.getSelectedObject())
  }

  canRemoveNode() {
    return new UserBrowserControllerRemoveNode().canRemoveNode(
      this.getSelectedObject()
    )
  }

  async removeNode() {
    await new UserBrowserControllerRemoveNode().doRemoveNode(
      this.getSelectedObject()
    )
  }
}
