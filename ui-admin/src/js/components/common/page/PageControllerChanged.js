import AppController from '@src/js/components/AppController.js'

export default class PageControllerChanged {
  constructor(controller) {
    this.controller = controller
    this.context = controller.getContext()
  }

  async execute(newChanged) {
    const { changed } = this.context.getState()

    if (newChanged !== changed) {
      await this.context.setState({
        changed: newChanged
      })

      const { id, type } = this.controller.getObject()

      AppController.getInstance().objectChange(
        this.controller.getPage(),
        type,
        id,
        newChanged
      )
    }
  }
}
