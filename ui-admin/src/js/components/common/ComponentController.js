import _ from 'lodash'

export default class ComponentController {
  constructor() {
    this.state = {}
    this.component = null
  }

  attach(component) {
    this.component = component
    component.state = this.state
  }

  getState(namespace) {
    let fullState = null

    if (this.component) {
      fullState = this.component.state
    } else {
      fullState = this.state
    }

    if (namespace) {
      return fullState[namespace] || {}
    } else {
      return fullState || {}
    }
  }

  setState(stateOrFunction, namespace) {
    if (this.component) {
      if (namespace) {
        return new Promise(resolve => {
          this.component.setState(fullState => {
            var namespaceState = fullState[namespace] || {}
            if (_.isFunction(stateOrFunction)) {
              return {
                [namespace]: {
                  ...namespaceState,
                  ...stateOrFunction(namespaceState)
                }
              }
            } else if (_.isObject(stateOrFunction)) {
              return {
                [namespace]: {
                  ...namespaceState,
                  ...stateOrFunction
                }
              }
            }
          }, resolve)
        })
      } else {
        return new Promise(resolve => {
          this.component.setState(stateOrFunction, resolve)
        })
      }
    } else {
      if (namespace) {
        var namespaceState = this.state[namespace] || {}
        if (_.isFunction(stateOrFunction)) {
          this.state = {
            ...this.state,
            [namespace]: {
              ...namespaceState,
              ...stateOrFunction(namespaceState)
            }
          }
        } else if (_.isObject(stateOrFunction)) {
          this.state = {
            ...this.state,
            [namespace]: {
              ...namespaceState,
              ...stateOrFunction
            }
          }
        }
      } else {
        if (_.isFunction(stateOrFunction)) {
          this.state = {
            ...this.state,
            ...stateOrFunction(this.state)
          }
        } else if (_.isObject(stateOrFunction)) {
          this.state = {
            ...this.state,
            ...stateOrFunction
          }
        }
      }
      return Promise.resolve()
    }
  }
}
