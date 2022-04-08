import AppController from '@src/js/components/AppController.js'
import TestAppController from '@srcTest/js/components/AppController.js'
import UserBrowserController from '@src/js/components/users/browser/UserBrowserController.js'
import ComponentContext from '@srcTest/js/components/common/ComponentContext.js'
import pages from '@src/js/common/consts/pages.js'
import objectType from '@src/js/common/consts/objectType.js'

export default class UserBrowserControllerTest {
  static SUITE = 'UserBrowserController'

  beforeEach() {
    jest.resetAllMocks()

    const appController = new TestAppController()
    AppController.setInstance(appController)

    this.context = new ComponentContext()
    this.controller = new UserBrowserController()
    this.controller.init(this.context)
  }

  expectOpenUserAction(userId) {
    expect(AppController.getInstance().objectOpen).toHaveBeenCalledWith(
      pages.USERS,
      objectType.USER,
      userId
    )
  }

  expectOpenGroupAction(groupId) {
    expect(AppController.getInstance().objectOpen).toHaveBeenCalledWith(
      pages.USERS,
      objectType.USER_GROUP,
      groupId
    )
  }

  expectOpenUsersOverviewAction() {
    expect(AppController.getInstance().objectOpen).toHaveBeenCalledWith(
      pages.USERS,
      objectType.OVERVIEW,
      objectType.USER
    )
  }
}
