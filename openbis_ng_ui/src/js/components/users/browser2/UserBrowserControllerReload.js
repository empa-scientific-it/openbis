import BrowserControllerReload from '@src/js/components/common/browser2/BrowserControllerReload.js'
import objectType from '@src/js/common/consts/objectType.js'
import objectOperation from '@src/js/common/consts/objectOperation.js'

export default class TypeBrowserControllerReload extends BrowserControllerReload {
  constructor(controller) {
    super(controller)
  }

  doGetObservedModifications() {
    return {
      [objectType.USER]: [objectOperation.CREATE, objectOperation.DELETE],
      [objectType.USER_GROUP]: [objectOperation.CREATE, objectOperation.DELETE]
    }
  }
}
