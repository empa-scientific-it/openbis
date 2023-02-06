export default class PersonalAccessTokenFormControllerRemove {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
    this.gridController = controller.gridController
  }

  async execute() {
    const { selection, pats } = this.context.getState()

    const patIndex = pats.findIndex(pat => pat.id === selection.params.id)

    const newPats = Array.from(pats)
    newPats.splice(patIndex, 1)

    await this.context.setState(state => ({
      ...state,
      pats: newPats,
      selection: null
    }))

    if (this.gridController) {
      await this.gridController.selectRow(null)
      await this.gridController.load()
    }

    await this.controller.changed(true)
  }
}
