import _ from 'lodash'
import autoBind from 'auto-bind'

export default class ComponentContext {
  constructor() {
    autoBind(this)
    this.props = {}
    this.state = {}
    this.dispatchFn = jest.fn()
    this.facade = {}
  }

  initState(initialState) {
    this.state = initialState
  }

  getProps() {
    return this.props
  }

  setProps(props) {
    this.props = props
  }

  getState() {
    return this.state
  }

  setState(newStateOrFunction) {
    let changes

    if (_.isFunction(newStateOrFunction)) {
      changes = newStateOrFunction(this.state)
    } else {
      changes = newStateOrFunction
    }

    this.state = { ...this.state, ...changes }

    return Promise.resolve()
  }
}
