import _ from 'lodash'
import autoBind from 'auto-bind'

export default class ComponentContextWithNamespace {
  constructor(originalContext, namespace) {
    autoBind(this)
    this.originalContext = originalContext
    this.namespace = namespace
  }

  initState(initialState) {
    const fullState = this.originalContext.getState()
    this.originalContext.initState({
      ...fullState,
      [this.namespace]: initialState
    })
  }

  getProps() {
    return this.originalContext.getProps()
  }

  getState() {
    const fullState = this.originalContext.getState()
    return fullState[this.namespace] || {}
  }

  setState(stateOrFunction) {
    if (_.isFunction(stateOrFunction)) {
      return this.originalContext.setState(fullState => {
        const state = fullState[this.namespace] || {}
        return {
          [this.namespace]: stateOrFunction(state)
        }
      })
    } else if (_.isObject(stateOrFunction)) {
      return this.originalContext.setState({
        [this.namespace]: stateOrFunction
      })
    }
  }

  getFacade() {
    return this.originalContext.getFacade()
  }
}
