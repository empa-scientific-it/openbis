import { combineReducers } from 'redux'
import pages from '@src/js/common/consts/pages.js'
import page from '@src/js/store/reducers/ui/pages/common/page.js'

export default function types(state = {}, action) {
  if (page.isPageAction(pages.TOOLS, action)) {
    return combineReducers({
      currentRoute: page.currentRoute,
      openTabs: page.openTabs
    })(state, action)
  } else {
    return state
  }
}
