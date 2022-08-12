export default class PersonalAccessTokenFormControllerBlur {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
    this.gridController = controller.gridController
  }

  async execute() {
    const { selection } = this.context.getState()

    this.controller.validate()

    setTimeout(async () => {
      if (this.gridController) {
        await this.gridController.load()
        if (selection) {
          await this.gridController.showRow(selection.params.id)
        }
      }
    }, 1)
  }
}
