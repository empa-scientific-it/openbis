import AppController from '@src/js/components/AppController.js'
import BrowserController from '@src/js/components/common/browser/BrowserController.js'
import TypeBrowserControllerLoadNodePath from '@src/js/components/types/browser/TypeBrowserControllerLoadNodePath.js'
import TypeBrowserControllerLoadNodes from '@src/js/components/types/browser/TypeBrowserControllerLoadNodes.js'
import TypeBrowserControllerAddNode from '@src/js/components/types/browser/TypeBrowserControllerAddNode.js'
import TypeBrowserControllerRemoveNode from '@src/js/components/types/browser/TypeBrowserControllerRemoveNode.js'
import TypeBrowserControllerReload from '@src/js/components/types/browser/TypeBrowserControllerReload.js'
import pages from '@src/js/common/consts/pages.js'
import ids from '@src/js/common/consts/ids.js'

export default class TypeBrowserController extends BrowserController {
  getId() {
    return ids.TYPE_BROWSER_ID
  }

  async loadNodePath(params) {
    return await new TypeBrowserControllerLoadNodePath().doLoadNodePath(params)
  }

  async loadNodes(params) {
    return await new TypeBrowserControllerLoadNodes().doLoadNodes(params)
  }

  async reload(objectModifications) {
    new TypeBrowserControllerReload(this).reload(objectModifications)
  }

  onSelectedChange({ object }) {
    if (object) {
      AppController.getInstance().objectOpen(
        pages.TYPES,
        object.type,
        object.id
      )
    }
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
