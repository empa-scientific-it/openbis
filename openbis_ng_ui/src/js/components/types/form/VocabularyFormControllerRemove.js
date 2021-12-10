import VocabularyFormSelectionType from '@src/js/components/types/form/VocabularyFormSelectionType.js'

export default class VocabularyFormControllerRemove {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
    this.gridController = controller.gridController
  }

  execute() {
    const { selection } = this.context.getState()
    if (selection.type === VocabularyFormSelectionType.TERM) {
      this._handleRemoveTerm(selection.params.id)
    }
  }

  async _handleRemoveTerm(termId) {
    const { terms } = this.context.getState()

    const termIndex = terms.findIndex(term => term.id === termId)

    const newTerms = Array.from(terms)
    newTerms.splice(termIndex, 1)

    await this.context.setState(state => ({
      ...state,
      terms: newTerms,
      selection: null
    }))

    if (this.gridController) {
      await this.gridController.selectRow(null)
      await this.gridController.load()
    }

    await this.controller.changed(true)
  }
}
