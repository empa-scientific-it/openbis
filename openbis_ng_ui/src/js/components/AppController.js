import React from 'react'

class AppController {
  constructor() {
    this.LastObjectModificationsContext = React.createContext()
  }

  init(context) {
    this.context = context

    const initialState = {
      lastObjectModifications: 'abc'
    }

    context.initState(initialState)
  }

  getLastObjectModifications() {
    return this.context.getState().lastObjectModifications
  }

  setLastObjectModifications(lastObjectModifications) {
    this.context.setState({
      lastObjectModifications: lastObjectModifications
    })
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

export default new AppController()
