const INIT = 'INIT'
const CLEAR = 'CLEAR'
const LOGIN = 'LOGIN'
const LOGOUT = 'LOGOUT'
const SEARCH = 'SEARCH'
const CURRENT_PAGE_CHANGE = 'CURRENT_PAGE_CHANGE'
const SEARCH_CHANGE = 'SEARCH_CHANGE'
const ERROR_CHANGE = 'ERROR_CHANGE'
const ROUTE_CHANGE = 'ROUTE_CHANGE'
const ROUTE_REPLACE = 'ROUTE_REPLACE'
const SET_INITIALIZED = 'SET_INITIALIZED'
const SET_LOADING = 'SET_LOADING'
const SET_SEARCH = 'SET_SEARCH'
const SET_SESSION = 'SET_SESSION'
const SET_ERROR = 'SET_ERROR'
const SET_ROUTE = 'SET_ROUTE'

const init = () => ({
  type: INIT
})

const clear = () => ({
  type: CLEAR
})

const login = (username, password) => ({
  type: LOGIN,
  payload: {
    username,
    password
  }
})

const logout = () => ({
  type: LOGOUT
})

const search = (page, text) => ({
  type: SEARCH,
  payload: {
    page,
    text
  }
})

const currentPageChange = currentPage => ({
  type: CURRENT_PAGE_CHANGE,
  payload: {
    currentPage
  }
})

const searchChange = search => ({
  type: SEARCH_CHANGE,
  payload: {
    search
  }
})

const errorChange = error => ({
  type: ERROR_CHANGE,
  payload: {
    error
  }
})

const routeChange = (route, state) => ({
  type: ROUTE_CHANGE,
  payload: {
    route,
    state
  }
})

const routeReplace = (route, state) => ({
  type: ROUTE_REPLACE,
  payload: {
    route,
    state
  }
})

const setInitialized = initialized => ({
  type: SET_INITIALIZED,
  payload: {
    initialized
  }
})

const setLoading = loading => ({
  type: SET_LOADING,
  payload: {
    loading
  }
})

const setSearch = search => ({
  type: SET_SEARCH,
  payload: {
    search
  }
})

const setSession = session => ({
  type: SET_SESSION,
  payload: {
    session
  }
})

const setError = error => ({
  type: SET_ERROR,
  payload: {
    error
  }
})

const setRoute = route => ({
  type: SET_ROUTE,
  payload: {
    route
  }
})

export default {
  INIT,
  CLEAR,
  LOGIN,
  LOGOUT,
  SEARCH,
  CURRENT_PAGE_CHANGE,
  SEARCH_CHANGE,
  ERROR_CHANGE,
  ROUTE_CHANGE,
  ROUTE_REPLACE,
  SET_INITIALIZED,
  SET_LOADING,
  SET_SEARCH,
  SET_SESSION,
  SET_ERROR,
  SET_ROUTE,
  init,
  clear,
  login,
  logout,
  search,
  currentPageChange,
  searchChange,
  errorChange,
  routeChange,
  routeReplace,
  setInitialized,
  setLoading,
  setSearch,
  setSession,
  setError,
  setRoute
}
