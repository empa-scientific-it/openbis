export default class PersonalAccessTokenFormControllerSelectionChange {
  constructor(controller) {
    this.context = controller.context
    this.gridController = controller.gridController
  }

  async execute(params) {
    let selection = null

    if (params) {
      selection = {
        params
      }
    }

    this.context.setState(state => ({
      ...state,
      selection
    }))

    if (this.gridController) {
      await this.gridController.selectRow(
        selection ? selection.params.id : null
      )
      await this.context.setState({
        selectedRow: this.gridController.getSelectedRow()
      })
    }
  }
}
