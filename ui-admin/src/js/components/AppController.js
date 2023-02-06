import _ from 'lodash'
import React from 'react'
import { createHashHistory } from 'history'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'
import objectOperation from '@src/js/common/consts/objectOperation.js'
import routes from '@src/js/common/consts/routes.js'
import users from '@src/js/common/consts/users.js'
import cookie from '@src/js/common/cookie.js'
import ids from '@src/js/common/consts/ids.js'

const AppContext = React.createContext()

export class AppController {
  init(context) {
    context.initState(this.initialState())

    const history = createHashHistory()

    history.listen(location => {
      const route = routes.parse(location.location.pathname)
      this.routeChanged(route.path)
    })

    this.context = context
    this.history = history
  }

  initialState() {
    return {
      loaded: false,
      loading: false,
      session: null,
      search: null,
      pages: [],
      error: null,
      settings: {},
      lastObjectModifications: {}
    }
  }

  async load() {
    const { loaded } = this.context.getState()

    if (!loaded) {
      try {
        await this.context.setState({ loading: true })
        await openbis.init()

        const sessionToken = cookie.read(cookie.OPENBIS_COOKIE)

        if (sessionToken) {
          try {
            openbis.useSession(sessionToken)

            const sessionInformation = await openbis.getSessionInformation()

            if (sessionInformation) {
              const [settings, serverInformation] = await Promise.all([
                this._loadSettings(),
                openbis.getServerInformation()
              ])

              await this.context.setState({
                session: {
                  sessionToken: sessionToken,
                  userName: sessionInformation.userName
                },
                settings,
                serverInformation
              })
              const routeObject = routes.parse(this.getRoute())
              await this.routeChanged(routeObject.path)
            } else {
              openbis.useSession(null)
            }
          } catch (e) {
            openbis.useSession(null)
          }
        }

        await this.context.setState({ loaded: true })
      } catch (e) {
        await this.context.setState({ error: e })
      } finally {
        await this.context.setState({ loading: false })
      }
    }
  }

  async login(username, password) {
    try {
      await this.context.setState({ loading: true })

      const sessionToken = await openbis.login(username, password)

      if (sessionToken !== null) {
        cookie.create(cookie.OPENBIS_COOKIE, sessionToken, 7)

        const [settings, serverInformation] = await Promise.all([
          this._loadSettings(),
          openbis.getServerInformation()
        ])

        await this.context.setState({
          session: {
            sessionToken: sessionToken,
            userName: username
          },
          settings,
          serverInformation
        })

        const routeObject = routes.parse(this.getRoute())
        await this.routeChange(routeObject.path)
      } else {
        throw Error('Session token null')
      }
    } catch (e) {
      await this.context.setState({ error: 'Incorrect user or password' })
    } finally {
      await this.context.setState({ loading: false })
    }
  }

  async logout() {
    try {
      await this.context.setState({ loading: true })
      await openbis.logout()
      this.context.setState(state => ({
        ...this.initialState(),
        loaded: state.loaded
      }))
      cookie.erase(cookie.OPENBIS_COOKIE)
      await this.routeChange('/')
    } catch (e) {
      await this.context.setState({ error: e })
    } finally {
      await this.context.setState({ loading: false })
    }
  }

  async search(page, text) {
    if (text.trim().length > 0) {
      await this.objectOpen(page, objectType.SEARCH, text.trim())
    }
    await this.context.setState({ search: '' })
  }

  async searchChange(text) {
    await this.context.setState({ search: text })
  }

  async pageChange(page) {
    const pageRoute = this.getCurrentRoute(page)
    if (pageRoute) {
      await this.routeChange(pageRoute)
    } else {
      const route = routes.format({ page })
      await this.routeChange(route)
    }
  }

  async loadingChange(loading) {
    await this.context.setState({ loading: loading })
  }

  async errorChange(error) {
    await this.context.setState({ error: error })
  }

  async routeChanged(path) {
    const newRoute = routes.parse(path)
    if (newRoute.type && newRoute.id) {
      const object = { type: newRoute.type, id: newRoute.id }
      const openTabs = this.getOpenTabs(newRoute.page)

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
          await this.addOpenTab(newRoute.page, id, { id, object })
        }
      }
    }
    await this.setCurrentRoute(newRoute.page, newRoute.path)
  }

  async routeChange(path) {
    if (path !== this.history.location.pathname) {
      this.history.push(path)
    }
  }

  async routeReplace(route) {
    this.history.replace(route)
  }

  async objectNew(page, type) {
    let id = 1
    const openObjects = this.getOpenObjects(page)
    openObjects.forEach(openObject => {
      if (openObject.type === type) {
        id++
      }
    })

    const route = routes.format({ page, type, id })
    await this.routeChange(route)
  }

  async objectCreate(page, oldType, oldId, newType, newId) {
    const openTabs = this.getOpenTabs(page)
    const oldTab = _.find(openTabs, { object: { type: oldType, id: oldId } })

    if (oldTab) {
      const newTab = {
        ...oldTab,
        object: { type: newType, id: newId },
        changed: false
      }
      await this.replaceOpenTab(page, oldTab.id, newTab)
      await this.setLastObjectModification(
        newType,
        objectOperation.CREATE,
        Date.now()
      )

      const route = routes.format({ page, type: newType, id: newId })
      await this.routeReplace(route)
    }
  }

  async objectOpen(page, type, id) {
    const route = routes.format({ page, type, id })
    await this.routeChange(route)
  }

  async objectUpdate(type) {
    await this.setLastObjectModification(
      type,
      objectOperation.UPDATE,
      Date.now()
    )
  }

  async objectDelete(page, type, id) {
    await this.objectClose(page, type, id)
    await this.setLastObjectModification(
      type,
      objectOperation.DELETE,
      Date.now()
    )
  }

  async objectChange(page, type, id, changed) {
    const openTabs = this.getOpenTabs(page)
    const oldTab = _.find(openTabs, { object: { type, id } })

    if (oldTab) {
      const newTab = { ...oldTab, changed }
      await this.replaceOpenTab(page, oldTab.id, newTab)
    }
  }

  async objectClose(page, type, id) {
    const openTabs = this.getOpenTabs(page)
    const objectToClose = { type, id }

    let selectedObject = this.getSelectedObject(page)
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
      await this.removeOpenTab(page, tabToClose.id)
    }

    if (selectedObject) {
      const route = routes.format({
        page,
        type: selectedObject.type,
        id: selectedObject.id
      })
      await this.routeChange(route)
    } else {
      const route = routes.format({ page })
      await this.routeChange(route)
    }
  }

  getLoaded() {
    return this.context.getState().loaded
  }

  getLoading() {
    return this.context.getState().loading
  }

  getServerInformation(key) {
    const serverInformation = this.context.getState().serverInformation || {}
    return serverInformation[key]
  }

  getSession() {
    return this.context.getState().session
  }

  getSessionToken() {
    const session = this.getSession()
    return session ? session.sessionToken : null
  }

  getUser() {
    const session = this.getSession()
    return session ? session.userName : null
  }

  isSystemUser() {
    return this.getUser() === users.SYSTEM
  }

  getRoute() {
    return routes.parse(this.history.location.pathname).path
  }

  getSearch() {
    return this.context.getState().search
  }

  getError() {
    return this.context.getState().error
  }

  getCurrentPage() {
    const route = this.getRoute()
    return routes.parse(route).page
  }

  getCurrentRoute(page) {
    const { pages } = this.context.getState()
    return pages[page] ? pages[page].currentRoute : null
  }

  async setCurrentRoute(page, route) {
    await this.context.setState(state => ({
      pages: {
        ...state.pages,
        [page]: {
          ...state.pages[page],
          currentRoute: route
        }
      }
    }))
  }

  getSelectedObject(page) {
    const path = this.getCurrentRoute(page)
    if (path) {
      const route = routes.parse(path)
      if (route && route.type && route.id) {
        return { type: route.type, id: route.id }
      }
    }
    return null
  }

  getSelectedTab(page) {
    const selectedObject = this.getSelectedObject(page)
    if (selectedObject) {
      const openTabs = this.getOpenTabs(page)
      return _.find(openTabs, { object: selectedObject })
    } else {
      return null
    }
  }

  getOpenObjects(page) {
    const openTabs = this.getOpenTabs(page)
    return openTabs.map(openTab => {
      return openTab.object
    })
  }

  getOpenTabs(page) {
    const { pages } = this.context.getState()
    return (pages[page] && pages[page].openTabs) || []
  }

  async setOpenTabs(page, newOpenTabs) {
    await this.context.setState(state => ({
      pages: {
        ...state.pages,
        [page]: {
          ...state.pages[page],
          openTabs: newOpenTabs
        }
      }
    }))
  }

  async addOpenTab(page, id, tab) {
    const openTabs = this.getOpenTabs(page)
    const index = _.findIndex(openTabs, { id: id }, _.isMatch)
    if (index === -1) {
      const newOpenTabs = Array.from(openTabs)
      newOpenTabs.push(tab)
      await this.setOpenTabs(page, newOpenTabs)
    }
  }

  async removeOpenTab(page, id) {
    const openTabs = this.getOpenTabs(page)
    const index = _.findIndex(openTabs, { id: id }, _.isMatch)
    if (index !== -1) {
      const newOpenTabs = Array.from(openTabs)
      newOpenTabs.splice(index, 1)
      await this.setOpenTabs(page, newOpenTabs)
    }
  }

  async replaceOpenTab(page, id, tab) {
    const openTabs = this.getOpenTabs(page)
    const index = _.findIndex(openTabs, { id: id }, _.isMatch)
    if (index !== -1) {
      const newOpenTabs = Array.from(openTabs)
      newOpenTabs[index] = tab
      await this.setOpenTabs(page, newOpenTabs)
    }
  }

  getLastObjectModifications() {
    return this.context.getState().lastObjectModifications
  }

  async setLastObjectModification(type, operation, timestamp) {
    const { lastObjectModifications } = this.context.getState()

    if (
      !lastObjectModifications[type] ||
      !lastObjectModifications[type][operation] ||
      lastObjectModifications[type][operation] < timestamp
    ) {
      await this.context.setState({
        lastObjectModifications: {
          ...lastObjectModifications,
          [type]: { ...lastObjectModifications[type], [operation]: timestamp }
        }
      })
    }
  }

  getSetting(settingId) {
    const { settings } = this.context.getState()
    return settings[settingId]
  }

  async setSetting(settingId, settingObject) {
    const { settings } = this.context.getState()

    const newSettings = {
      ...settings,
      [settingId]: settingObject
    }

    await this.context.setState({ settings: newSettings })

    try {
      await this._saveSettings(settingId, settingObject)
    } catch (error) {
      this.errorChange(error)
    }
  }

  async _loadSettings() {
    const id = new openbis.Me()
    const fo = new openbis.PersonFetchOptions()
    fo.withWebAppSettings(ids.WEB_APP_ID).withAllSettings()

    const map = await openbis.getPersons([id], fo)
    const person = map[id]

    const webAppSettings = person.webAppSettings[ids.WEB_APP_ID]
    if (webAppSettings && webAppSettings.settings) {
      const map = {}
      Object.values(webAppSettings.settings).forEach(setting => {
        if (setting.value !== null && setting.value !== undefined) {
          map[setting.name] = JSON.parse(setting.value)
        }
      })
      return map
    } else {
      return {}
    }
  }

  async _saveSettings(settingId, settingObject) {
    const settings = new openbis.WebAppSettingCreation()
    settings.setName(settingId)
    settings.setValue(JSON.stringify(settingObject))

    const update = new openbis.PersonUpdate()
    update.setUserId(new openbis.Me())
    update.getWebAppSettings(ids.WEB_APP_ID).add(settings)

    await openbis.updatePersons([update])
  }

  withState(additionalPropertiesFn) {
    const WithContext = Component => {
      const WithConsumer = props => {
        return React.createElement(AppContext.Consumer, {}, () => {
          const additionalProperties = additionalPropertiesFn
            ? additionalPropertiesFn(props)
            : {}
          return React.createElement(Component, {
            ...props,
            ...additionalProperties
          })
        })
      }
      WithConsumer.displayName = 'WithConsumer'
      return WithConsumer
    }
    WithContext.displayName = 'WithContext'
    return WithContext
  }
}

let INSTANCE = new AppController()

export default {
  AppContext,
  AppController,
  getInstance() {
    return INSTANCE
  },
  setInstance(instance) {
    INSTANCE = instance
  }
}
