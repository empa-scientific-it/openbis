import { createBrowserHistory } from 'history'
import AppController from '@src/js/components/AppController.js'
import routes from '@src/js/common/consts/routes.js'
import url from '@src/js/common/url.js'

let history = createBrowserHistory({
  basename: url.getApplicationPath() + '#'
})

history.configure = store => {
  history.listen(location => {
    let route = routes.parse(location.pathname)

    if (route.path !== store.getState().route) {
      AppController.routeChange(route.path, location.state)
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
