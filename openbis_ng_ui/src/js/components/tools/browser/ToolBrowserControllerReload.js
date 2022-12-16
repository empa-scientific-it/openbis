import BrowserControllerReload from '@src/js/components/common/browser/BrowserControllerReload.js'
import objectType from '@src/js/common/consts/objectType.js'
import objectOperation from '@src/js/common/consts/objectOperation.js'

export default class ToolBrowserControllerReload extends BrowserControllerReload {
  constructor(controller) {
    super(controller)
  }

  doGetObservedModifications() {
    return {
      [objectType.DYNAMIC_PROPERTY_PLUGIN]: [
        objectOperation.CREATE,
        objectOperation.DELETE
      ],
      [objectType.ENTITY_VALIDATION_PLUGIN]: [
        objectOperation.CREATE,
        objectOperation.DELETE
      ],
      [objectType.QUERY]: [objectOperation.CREATE, objectOperation.DELETE]
    }
  }
}
