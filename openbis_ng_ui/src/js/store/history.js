import { createBrowserHistory } from 'history'
import actions from '@src/js/store/actions/actions.js'
import routes from '@src/js/common/consts/routes.js'
import url from '@src/js/common/url.js'

let history = createBrowserHistory({
  basename: url.getApplicationPath() + '#'
})

history.configure = store => {
  history.listen(location => {
    let route = routes.parse(location.pathname)

    if (route.path !== store.getState().route) {
      store.dispatch(actions.routeChange(route.path, location.state))
    }
  })

  let currentRoute = store.getState().route

  store.subscribe(() => {
    let newRoute = store.getState().route

    if (newRoute && newRoute !== currentRoute) {
      currentRoute = newRoute
      if (currentRoute && currentRoute !== history.location.pathname) {
        history.push(currentRoute)
      }
    }
  })
}

export default history
