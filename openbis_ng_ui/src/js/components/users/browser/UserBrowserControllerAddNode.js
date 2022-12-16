import AppController from '@src/js/components/AppController.js'
import objectType from '@src/js/common/consts/objectType.js'
import pages from '@src/js/common/consts/pages.js'

const NEW_OBJECT_TYPES = {
  [objectType.USER]: objectType.NEW_USER,
  [objectType.USER_GROUP]: objectType.NEW_USER_GROUP
}

export default class UserBrowserControllerAddNode {
  canAddNode(selectedObject) {
    return (
      selectedObject &&
      selectedObject.type === objectType.OVERVIEW &&
      NEW_OBJECT_TYPES[selectedObject.id]
    )
  }

  async doAddNode(selectedObject) {
    if (!this.canAddNode(selectedObject)) {
      return
    }
    await AppController.getInstance().objectNew(
      pages.USERS,
      NEW_OBJECT_TYPES[selectedObject.id]
    )
  }
}
