import _ from 'lodash'
import autoBind from 'auto-bind'

export default class ComponentContextWithNamespace {
  constructor(originalContext, namespace, getPropsFn) {
    autoBind(this)
    this.originalContext = originalContext
    this.namespace = namespace
    this.getPropsFn = getPropsFn
  }

  initState(initialState) {
    const fullState = this.originalContext.getState()
    this.originalContext.initState({
      ...fullState,
      [this.namespace]: initialState
    })
  }

  getProps() {
    return this.getPropsFn(this.originalContext.getProps())
  }

  getState() {
    const fullState = this.originalContext.getState()
    return fullState[this.namespace] || {}
  }

  setState(stateOrFunction) {
    if (_.isFunction(stateOrFunction)) {
      return this.originalContext.setState(fullState => {
        const namespaceState = fullState[this.namespace] || {}
        return {
          [this.namespace]: {
            ...namespaceState,
            ...stateOrFunction(namespaceState)
          }
        }
      })
    } else if (_.isObject(stateOrFunction)) {
      const fullState = this.originalContext.getState()
      const namespaceState = fullState[this.namespace] || {}
      return this.originalContext.setState({
        [this.namespace]: {
          ...namespaceState,
          ...stateOrFunction
        }
      })
    }
  }

  getFacade() {
    return this.originalContext.getFacade()
  }
}
