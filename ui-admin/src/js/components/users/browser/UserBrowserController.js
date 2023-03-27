import AppController from '@src/js/components/AppController.js'
import BrowserController from '@src/js/components/common/browser/BrowserController.js'
import UserBrowserControllerLoadNodePath from '@src/js/components/users/browser/UserBrowserControllerLoadNodePath.js'
import UserBrowserControllerLoadNodes from '@src/js/components/users/browser/UserBrowserControllerLoadNodes.js'
import UserBrowserControllerAddNode from '@src/js/components/users/browser/UserBrowserControllerAddNode.js'
import UserBrowserControllerRemoveNode from '@src/js/components/users/browser/UserBrowserControllerRemoveNode.js'
import UserBrowserControllerReload from '@src/js/components/users/browser/UserBrowserControllerReload.js'
import pages from '@src/js/common/consts/pages.js'
import ids from '@src/js/common/consts/ids.js'

export default class UserBrowserController extends BrowserController {
  getId() {
    return ids.USER_BROWSER_ID
  }

  async loadNodePath(params) {
    return await new UserBrowserControllerLoadNodePath().doLoadNodePath(params)
  }

  async loadNodes(params) {
    return await new UserBrowserControllerLoadNodes().doLoadNodes(params)
  }

  onSelectedChange({ object }) {
    if (object) {
      AppController.getInstance().objectOpen(
        pages.USERS,
        object.type,
        object.id
      )
    }
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
