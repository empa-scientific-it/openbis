import BrowserController from '@src/js/components/common/browser/BrowserController.js'
import UserBrowserControllerLoadNodePath from '@src/js/components/users/browser/UserBrowserControllerLoadNodePath.js'
import UserBrowserControllerLoadNodes from '@src/js/components/users/browser/UserBrowserControllerLoadNodes.js'
import UserBrowserControllerAddNode from '@src/js/components/users/browser/UserBrowserControllerAddNode.js'
import UserBrowserControllerRemoveNode from '@src/js/components/users/browser/UserBrowserControllerRemoveNode.js'
import UserBrowserControllerReload from '@src/js/components/users/browser/UserBrowserControllerReload.js'

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
