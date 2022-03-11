import _ from 'lodash'
import { put, takeEvery, select } from '@src/js/store/sagas/effects.js'
import selectors from '@src/js/store/selectors/selectors.js'
import actions from '@src/js/store/actions/actions.js'
import routes from '@src/js/common/consts/routes.js'
import objectOperation from '@src/js/common/consts/objectOperation.js'

import AppController from '@src/js/components/AppController.js'

export default function* pageSaga() {
  yield takeEvery(actions.OBJECT_NEW, objectNew)
  yield takeEvery(actions.OBJECT_CREATE, objectCreate)
  yield takeEvery(actions.OBJECT_OPEN, objectOpen)
  yield takeEvery(actions.OBJECT_UPDATE, objectUpdate)
  yield takeEvery(actions.OBJECT_DELETE, objectDelete)
  yield takeEvery(actions.OBJECT_CHANGE, objectChange)
  yield takeEvery(actions.OBJECT_CLOSE, objectClose)
  yield takeEvery(actions.ROUTE_CHANGE, routeChange)
}

function* objectNew(action) {
  const { page, type } = action.payload

  let id = 1

  const openObjects = yield select(selectors.getOpenObjects, page)
  openObjects.forEach(openObject => {
    if (openObject.type === type) {
      id++
    }
  })

  const route = routes.format({ page, type, id })
  yield put(actions.routeChange(route))
}

function* objectCreate(action) {
  const { page, oldType, oldId, newType, newId } = action.payload

  const openTabs = yield select(selectors.getOpenTabs, page)
  const oldTab = _.find(openTabs, { object: { type: oldType, id: oldId } })

  if (oldTab) {
    const newTab = {
      ...oldTab,
      object: { type: newType, id: newId },
      changed: false
    }
    yield put(actions.replaceOpenTab(page, oldTab.id, newTab))

    yield AppController.setLastObjectModification(
      newType,
      objectOperation.CREATE,
      Date.now()
    )

    const route = routes.format({ page, type: newType, id: newId })
    yield put(actions.routeReplace(route))
  }
}

function* objectOpen(action) {
  const { page, type, id } = action.payload
  const route = routes.format({ page, type, id })
  yield put(actions.routeChange(route))
}

function* objectUpdate(action) {
  const { type } = action.payload
  yield AppController.setLastObjectModification(
    type,
    objectOperation.UPDATE,
    Date.now()
  )
}

function* objectDelete(action) {
  const { page, type, id } = action.payload
  yield put(actions.objectClose(page, type, id))
  yield AppController.setLastObjectModification(
    type,
    objectOperation.DELETE,
    Date.now()
  )
}

function* objectChange(action) {
  const { page, type, id, changed } = action.payload

  const openTabs = yield select(selectors.getOpenTabs, page)
  const oldTab = _.find(openTabs, { object: { type, id } })

  if (oldTab) {
    const newTab = { ...oldTab, changed }
    yield put(actions.replaceOpenTab(page, oldTab.id, newTab))
  }
}

function* objectClose(action) {
  const { page, type, id } = action.payload

  const objectToClose = { type, id }
  const openTabs = yield select(selectors.getOpenTabs, page)
  let selectedObject = yield select(selectors.getSelectedObject, page)

  if (selectedObject && _.isEqual(selectedObject, objectToClose)) {
    if (_.size(openTabs) === 1) {
      selectedObject = null
    } else {
      let selectedIndex = _.findIndex(openTabs, { object: selectedObject })
      if (selectedIndex === 0) {
        selectedObject = openTabs[selectedIndex + 1].object
      } else {
        selectedObject = openTabs[selectedIndex - 1].object
      }
    }
  }

  let tabToClose = _.find(openTabs, { object: objectToClose })
  if (tabToClose) {
    yield put(actions.removeOpenTab(page, tabToClose.id))
  }

  if (selectedObject) {
    let route = routes.format({
      page,
      type: selectedObject.type,
      id: selectedObject.id
    })
    yield put(actions.routeChange(route))
  } else {
    let route = routes.format({ page })
    yield put(actions.routeChange(route))
  }
}

function* routeChange(action) {
  const route = routes.parse(action.payload.route)

  if (route.type && route.id) {
    const object = { type: route.type, id: route.id }
    const openTabs = yield select(selectors.getOpenTabs, route.page)

    if (openTabs) {
      let found = false
      let id = 1

      openTabs.forEach(openTab => {
        if (_.isMatch(openTab.object, object)) {
          found = true
        }
        if (openTab.id >= id) {
          id = openTab.id + 1
        }
      })

      if (!found) {
        yield put(actions.addOpenTab(route.page, { id, object }))
      }
    }
  }

  yield put(actions.setCurrentRoute(route.page, route.path))
}
