import { combineReducers } from 'redux'
import actions from '@src/js/store/actions/actions.js'
import ui from '@src/js/store/reducers/ui/ui.js'
import session from '@src/js/store/reducers/session/session.js'
import route from '@src/js/store/reducers/route/route.js'

export default function root(state = {}, action) {
  if (action.type === actions.CLEAR) {
    state = {
      initialized: state.initialized
    }
  }
  return combineReducers({
    initialized,
    ui,
    session,
    route
  })(state, action)
}

function initialized(state = false, action) {
  switch (action.type) {
    case actions.SET_INITIALIZED: {
      return action.payload.initialized
    }
    default: {
      return state
    }
  }
}
