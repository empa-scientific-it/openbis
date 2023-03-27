import AppController from '@src/js/components/AppController.js'
import TestAppController from '@srcTest/js/components/AppController.js'
import UserBrowserController from '@src/js/components/users/browser/UserBrowserController.js'
import ComponentContext from '@srcTest/js/components/common/ComponentContext.js'

export default class UserBrowserControllerTest {
  static SUITE = 'UserBrowserController'

  beforeEach() {
    jest.resetAllMocks()

    const appController = new TestAppController()
    AppController.setInstance(appController)

    this.context = new ComponentContext()
    this.controller = new UserBrowserController()
    this.controller.loadSettings = function () {
      return {}
    }
    this.controller.onSettingsChange = function () {}
  }
}
