import { combineReducers } from 'redux'
import login from '@src/js/store/reducers/ui/pages/login/login.js'
import types from '@src/js/store/reducers/ui/pages/types/types.js'
import users from '@src/js/store/reducers/ui/pages/users/users.js'
import tools from '@src/js/store/reducers/ui/pages/tools/tools.js'

export default combineReducers({
  login,
  types,
  users,
  tools
})
