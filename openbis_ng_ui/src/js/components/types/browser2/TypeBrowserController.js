import BrowserController from '@src/js/components/common/browser2/BrowserController.js'
import TypeBrowserControllerLoadNodePath from '@src/js/components/types/browser2/TypeBrowserControllerLoadNodePath.js'
import TypeBrowserControllerLoadNodesFiltered from '@src/js/components/types/browser2/TypeBrowserControllerLoadNodesFiltered.js'
import TypeBrowserControllerLoadNodesUnfiltered from '@src/js/components/types/browser2/TypeBrowserControllerLoadNodesUnfiltered.js'

export default class TypeBrowserController extends BrowserController {
  async doLoadNodePath(params) {
    return await new TypeBrowserControllerLoadNodePath().doLoadNodePath(params)
  }

  async doLoadNodes(params) {
    const { filter } = params

    if (filter) {
      return await new TypeBrowserControllerLoadNodesFiltered().doLoadFilteredNodes(
        params
      )
    } else {
      return await new TypeBrowserControllerLoadNodesUnfiltered().doLoadUnfilteredNodes(
        params
      )
    }
  }
}
