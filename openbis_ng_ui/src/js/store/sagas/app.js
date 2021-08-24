import {
  call,
  put,
  putAndWait,
  takeEvery,
  select
} from '@src/js/store/sagas/effects.js'
import openbis from '@src/js/services/openbis.js'
import selectors from '@src/js/store/selectors/selectors.js'
import actions from '@src/js/store/actions/actions.js'
import objectType from '@src/js/common/consts/objectType.js'
import routes from '@src/js/common/consts/routes.js'
import cookie from '@src/js/common/cookie.js'
import history from '@src/js/store/history.js'

export default function* appSaga() {
  yield takeEvery(actions.INIT, init)
  yield takeEvery(actions.LOGIN, login)
  yield takeEvery(actions.LOGOUT, logout)
  yield takeEvery(actions.SEARCH, search)
  yield takeEvery(actions.CURRENT_PAGE_CHANGE, currentPageChange)
  yield takeEvery(actions.SEARCH_CHANGE, searchChange)
  yield takeEvery(actions.ERROR_CHANGE, errorChange)
  yield takeEvery(actions.ROUTE_CHANGE, routeChange)
  yield takeEvery(actions.ROUTE_REPLACE, routeReplace)
}

function* init() {
  let initialized = yield select(selectors.getInitialized)

  if (!initialized) {
    try {
      yield put(actions.setLoading(true))
      yield call([openbis, openbis.init])

      let sessionToken = cookie.read(cookie.OPENBIS_COOKIE)

      if (sessionToken) {
        try {
          openbis.useSession(sessionToken)
          let sessionInformation = yield call([
            openbis,
            openbis.getSessionInformation
          ])
          if (sessionInformation) {
            yield put(
              actions.setSession({
                sessionToken: sessionToken,
                userName: sessionInformation.userName
              })
            )
          } else {
            openbis.useSession(null)
          }
        } catch (e) {
          openbis.useSession(null)
        }
      }

      yield put(actions.setInitialized(true))
    } catch (e) {
      yield put(actions.setError(e))
    } finally {
      yield put(actions.setLoading(false))
    }
  }
}

function* login(action) {
  try {
    yield put(actions.setLoading(true))

    let { username, password } = action.payload
    let loginResponse = yield putAndWait(
      actions.apiRequest({ method: 'login', params: [username, password] })
    )

    if (loginResponse.payload.result) {
      let sessionToken = loginResponse.payload.result

      yield put(
        actions.setSession({
          sessionToken: sessionToken,
          userName: username
        })
      )
      cookie.create(cookie.OPENBIS_COOKIE, sessionToken, 7)

      let path = yield select(selectors.getRoute)
      let route = routes.parse(path)
      yield put(actions.routeChange(route.path))
    } else {
      throw { message: 'Incorrect user or password' }
    }
  } catch (e) {
    yield put(actions.setError(e))
  } finally {
    yield put(actions.setLoading(false))
  }
}

function* logout() {
  try {
    yield put(actions.setLoading(true))
    yield putAndWait(actions.apiRequest({ method: 'logout' }))
    yield put(actions.clear())
    cookie.erase(cookie.OPENBIS_COOKIE)
    yield put(actions.routeChange('/'))
  } catch (e) {
    yield put(actions.setError(e))
  } finally {
    yield put(actions.setLoading(false))
  }
}

function* search(action) {
  const { page, text } = action.payload
  if (text.trim().length > 0) {
    yield put(actions.objectOpen(page, objectType.SEARCH, text.trim()))
  }
  yield put(actions.setSearch(''))
}

function* currentPageChange(action) {
  let page = action.payload.currentPage
  let route = yield select(selectors.getCurrentRoute, page)

  if (route) {
    yield put(actions.routeChange(route))
  } else {
    route = routes.format({ page })
    yield put(actions.routeChange(route))
  }
}

function* searchChange(action) {
  yield put(actions.setSearch(action.payload.search))
}

function* errorChange(action) {
  yield put(actions.setError(action.payload.error))
}

function* routeChange(action) {
  const route = action.payload.route
  yield put(actions.setRoute(route))
}

function routeReplace(action) {
  const { route, state } = action.payload
  history.replace(route, state)
}
