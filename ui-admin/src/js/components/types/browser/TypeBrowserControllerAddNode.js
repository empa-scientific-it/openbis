import AppController from '@src/js/components/AppController.js'
import objectType from '@src/js/common/consts/objectType.js'
import pages from '@src/js/common/consts/pages.js'

const NEW_OBJECT_TYPES = {
  [objectType.OBJECT_TYPE]: objectType.NEW_OBJECT_TYPE,
  [objectType.COLLECTION_TYPE]: objectType.NEW_COLLECTION_TYPE,
  [objectType.DATA_SET_TYPE]: objectType.NEW_DATA_SET_TYPE,
  [objectType.MATERIAL_TYPE]: objectType.NEW_MATERIAL_TYPE,
  [objectType.VOCABULARY_TYPE]: objectType.NEW_VOCABULARY_TYPE
}

export default class TypeBrowserControllerAddNode {
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
      pages.TYPES,
      NEW_OBJECT_TYPES[selectedObject.id]
    )
  }
}
