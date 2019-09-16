export const INIT = 'INIT'
export const LOGIN = 'LOGIN'
export const LOGOUT = 'LOGOUT'
export const SEARCH = 'SEARCH'
export const CURRENT_PAGE_CHANGE = 'CURRENT_PAGE_CHANGE'
export const SEARCH_CHANGE = 'SEARCH_CHANGE'
export const ERROR_CHANGE = 'ERROR_CHANGE'
export const ROUTE_CHANGE = 'ROUTE_CHANGE'

export const SET_LOADING = 'SET_LOADING'
export const SET_SEARCH = 'SET_SEARCH'
export const SET_SESSION = 'SET_SESSION'
export const SET_ERROR = 'SET_ERROR'
export const SET_ROUTE = 'SET_ROUTE'

export const init = () => ({
  type: INIT
})

export const login = (username, password) => ({
  type: LOGIN,
  payload: {
    username,
    password
  }
})

export const logout = () => ({
  type: LOGOUT
})

export const search = (page, text) => ({
  type: SEARCH,
  payload: {
    page,
    text
  }
})

export const currentPageChange = currentPage => ({
  type: CURRENT_PAGE_CHANGE,
  payload: {
    currentPage
  }
})

export const searchChange = search => ({
  type: SEARCH_CHANGE,
  payload: {
    search
  }
})

export const errorChange = error => ({
  type: ERROR_CHANGE,
  payload: {
    error
  }
})

export const routeChange = route => ({
  type: ROUTE_CHANGE,
  payload: {
    route
  }
})

export const setLoading = loading => ({
  type: SET_LOADING,
  payload: {
    loading
  }
})

export const setSearch = search => ({
  type: SET_SEARCH,
  payload: {
    search
  }
})

export const setSession = session => ({
  type: SET_SESSION,
  payload: {
    session
  }
})

export const setError = error => ({
  type: SET_ERROR,
  payload: {
    error
  }
})

export const setRoute = route => ({
  type: SET_ROUTE,
  payload: {
    route
  }
})
