import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import GridWrapper from '@srcTest/js/components/common/grid/wrapper/GridWrapper.js'
import UserGroupFormGridUsers from '@src/js/components/users/form/usergroup/UserGroupFormGridUsers.jsx'
import UserGroupFormGridRoles from '@src/js/components/users/form/usergroup/UserGroupFormGridRoles.jsx'
import UserGroupFormParametersWrapper from '@srcTest/js/components/users/form/usergroup/wrapper/UserGroupFormParametersWrapper.js'
import UserGroupFormButtons from '@src/js/components/users/form/usergroup/UserGroupFormButtons.jsx'
import UserGroupFormButtonsWrapper from '@srcTest/js/components/users/form/usergroup/wrapper/UserGroupFormButtonsWrapper.js'

export default class UserGroupFormWrapper extends BaseWrapper {
  getUsersGrid() {
    return new GridWrapper(
      this.findComponent(Grid, this.findComponent(UserGroupFormGridUsers))
    )
  }

  getRolesGrid() {
    return new GridWrapper(
      this.findComponent(Grid, this.findComponent(UserGroupFormGridRoles))
    )
  }

  getParameters() {
    return new UserGroupFormParametersWrapper(this.wrapper)
  }

  getButtons() {
    return new UserGroupFormButtonsWrapper(
      this.findComponent(UserGroupFormButtons)
    )
  }

  toJSON() {
    return {
      usersGrid: this.getUsersGrid().toJSON(),
      rolesGrid: this.getRolesGrid().toJSON(),
      parameters: this.getParameters().toJSON(),
      buttons: this.getButtons().toJSON()
    }
  }
}
