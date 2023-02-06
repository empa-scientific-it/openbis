export default class PageControllerSelectionChange {
  constructor(controller) {
    this.context = controller.getContext()
  }

  execute(type, params) {
    let selection = null

    if (type && params) {
      selection = {
        type,
        params
      }
    }

    this.context.setState(state => ({
      ...state,
      selection
    }))
  }
}
