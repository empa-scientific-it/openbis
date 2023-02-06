import Menu from '@src/js/components/common/menu/Menu.jsx'
import Login from '@src/js/components/login/Login.jsx'
import Types from '@src/js/components/types/Types.jsx'
import Users from '@src/js/components/users/Users.jsx'
import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import MenuWrapper from '@srcTest/js/components/common/menu/wrapper/MenuWrapper.js'
import LoginWrapper from '@srcTest/js/components/login/wrapper/LoginWrapper.js'
import TypesWrapper from '@srcTest/js/components/types/wrapper/TypesWrapper.js'
import UsersWrapper from '@srcTest/js/components/users/wrapper/UsersWrapper.js'

export default class AppWrapper extends BaseWrapper {
  getLogin() {
    return new LoginWrapper(this.findComponent(Login))
  }

  getMenu() {
    return new MenuWrapper(this.findComponent(Menu))
  }

  getTypes() {
    return new TypesWrapper(this.findComponent(Types))
  }

  getUsers() {
    return new UsersWrapper(this.findComponent(Users))
  }

  toJSON() {
    return {
      menu: this.getMenu().toJSON(),
      login: this.getLogin().toJSON(),
      types: this.getTypes().toJSON(),
      users: this.getUsers().toJSON()
    }
  }
}
