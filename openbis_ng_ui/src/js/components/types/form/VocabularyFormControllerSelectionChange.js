import PageControllerSelectionChange from '@src/js/components/common/page/PageControllerSelectionChange.js'

export default class VocabularyFormControllerSelectionChange extends PageControllerSelectionChange {
  constructor(controller) {
    super(controller)
    this.context = controller.context
    this.gridController = controller.gridController
  }

  async execute(type, params) {
    super.execute(type, params)

    if (this.gridController) {
      await this.gridController.selectRow(params ? params.id : null)
      await this.context.setState({
        selectedRow: this.gridController.getSelectedRow()
      })
    }
  }
}
