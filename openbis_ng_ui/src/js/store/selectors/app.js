import routes from '@src/js/common/consts/routes.js'

const getInitialized = state => {
  return state.initialized
}

const getLoading = state => {
  return state.ui.loading
}

const getSearch = state => {
  return state.ui.search
}

const getRoute = state => {
  return state.route
}

const getCurrentPage = state => {
  let route = routes.parse(state.route)
  return route.page
}

const getError = state => {
  return state.ui.error
}

const getSession = state => {
  return state.session
}

export default {
  getInitialized,
  getLoading,
  getSearch,
  getRoute,
  getCurrentPage,
  getError,
  getSession
}
