import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import GridWrapper from '@srcTest/js/components/common/grid/wrapper/GridWrapper.js'
import UserFormGridGroups from '@src/js/components/users/form/user/UserFormGridGroups.jsx'
import UserFormGridRoles from '@src/js/components/users/form/user/UserFormGridRoles.jsx'
import UserFormParametersWrapper from '@srcTest/js/components/users/form/user/wrapper/UserFormParametersWrapper.js'
import UserFormButtons from '@src/js/components/users/form/user/UserFormButtons.jsx'
import UserFormButtonsWrapper from '@srcTest/js/components/users/form/user/wrapper/UserFormButtonsWrapper.js'

export default class UserFormWrapper extends BaseWrapper {
  getGroupsGrid() {
    return new GridWrapper(
      this.findComponent(Grid, this.findComponent(UserFormGridGroups))
    )
  }

  getRolesGrid() {
    return new GridWrapper(
      this.findComponent(Grid, this.findComponent(UserFormGridRoles))
    )
  }

  getParameters() {
    return new UserFormParametersWrapper(this.wrapper)
  }

  getButtons() {
    return new UserFormButtonsWrapper(this.findComponent(UserFormButtons))
  }

  toJSON() {
    return {
      groupsGrid: this.getGroupsGrid().toJSON(),
      rolesGrid: this.getRolesGrid().toJSON(),
      parameters: this.getParameters().toJSON(),
      buttons: this.getButtons().toJSON()
    }
  }
}
