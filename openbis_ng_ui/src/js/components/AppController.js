import React from 'react'

export class AppController {
  constructor() {
    this.LastObjectModificationsContext = React.createContext()
  }

  init(context) {
    this.context = context

    const initialState = {
      lastObjectModifications: {}
    }

    context.initState(initialState)
  }

  getLastObjectModifications() {
    return this.context.getState().lastObjectModifications
  }

  setLastObjectModification(type, operation, timestamp) {
    const { lastObjectModifications } = this.context.getState()

    if (
      !lastObjectModifications[type] ||
      !lastObjectModifications[type][operation] ||
      lastObjectModifications[type][operation] < timestamp
    ) {
      this.context.setState({
        lastObjectModifications: {
          ...lastObjectModifications,
          [type]: { ...lastObjectModifications[type], [operation]: timestamp }
        }
      })
    }
  }

  withLastObjectModifications() {
    return this._withContext(
      this.LastObjectModificationsContext,
      'lastObjectModifications'
    )
  }

  _withContext(Context, propertyName) {
    const WithContext = Component => {
      const WithConsumer = props => {
        return React.createElement(Context.Consumer, {}, value =>
          React.createElement(Component, {
            ...props,
            [propertyName]: value
          })
        )
      }
      WithConsumer.displayName = 'WithConsumer'
      return WithConsumer
    }
    WithContext.displayName = 'WithContext'
    return WithContext
  }
}

let INSTANCE = new AppController()

export function setInstance(instance) {
  INSTANCE = instance
}

export function getInstance() {
  return INSTANCE
}

export default INSTANCE
