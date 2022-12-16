import BrowserController from '@src/js/components/common/browser/BrowserController.js'
import DatabaseBrowserControllerLoadNodePath from '@src/js/components/database/browser/DatabaseBrowserControllerLoadNodePath.js'
import DatabaseBrowserControllerLoadNodesFiltered from '@src/js/components/database/browser/DatabaseBrowserControllerLoadNodesFiltered.js'
import DatabaseBrowserControllerLoadNodesUnfiltered from '@src/js/components/database/browser/DatabaseBrowserControllerLoadNodesUnfiltered.js'

export default class DatabaseBrowserController extends BrowserController {
  async doLoadNodePath(params) {
    return await new DatabaseBrowserControllerLoadNodePath().doLoadNodePath(
      params
    )
  }

  async doLoadNodes(params) {
    const { filter } = params

    if (filter) {
      return await new DatabaseBrowserControllerLoadNodesFiltered().doLoadFilteredNodes(
        params
      )
    } else {
      return await new DatabaseBrowserControllerLoadNodesUnfiltered().doLoadUnfilteredNodes(
        params
      )
    }
  }
}
