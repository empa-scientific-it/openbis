import BrowserControllerReload from '@src/js/components/common/browser/BrowserControllerReload.js'
import objectType from '@src/js/common/consts/objectType.js'
import objectOperation from '@src/js/common/consts/objectOperation.js'

export default class TypeBrowserControllerReload extends BrowserControllerReload {
  constructor(controller) {
    super(controller)
  }

  doGetObservedModifications() {
    return {
      [objectType.OBJECT_TYPE]: [
        objectOperation.CREATE,
        objectOperation.DELETE
      ],
      [objectType.COLLECTION_TYPE]: [
        objectOperation.CREATE,
        objectOperation.DELETE
      ],
      [objectType.DATA_SET_TYPE]: [
        objectOperation.CREATE,
        objectOperation.DELETE
      ],
      [objectType.MATERIAL_TYPE]: [
        objectOperation.CREATE,
        objectOperation.DELETE
      ],
      [objectType.VOCABULARY_TYPE]: [
        objectOperation.CREATE,
        objectOperation.DELETE
      ]
    }
  }
}
