import AppController from '@src/js/components/AppController.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import VocabularyTypeFormSelectionType from '@src/js/components/types/form/vocabularytype/VocabularyTypeFormSelectionType.js'

export default class VocabularyTypeFormControllerAdd {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
    this.gridController = controller.gridController
  }

  async execute() {
    let { terms, termsCounter } = this.context.getState()

    const newTerm = {
      id: 'term-' + termsCounter++,
      code: FormUtil.createField({}),
      label: FormUtil.createField({}),
      description: FormUtil.createField({}),
      official: FormUtil.createField({
        value: true
      }),
      registrator: FormUtil.createField({
        value: AppController.getInstance().getUser(),
        visible: false,
        enabled: false
      }),
      registrationDate: FormUtil.createField({
        visible: false,
        enabled: false
      }),
      original: null
    }

    const newTerms = Array.from(terms)
    newTerms.push(newTerm)

    await this.context.setState(state => ({
      ...state,
      terms: newTerms,
      termsCounter,
      selection: {
        type: VocabularyTypeFormSelectionType.TERM,
        params: {
          id: newTerm.id,
          part: 'code'
        }
      }
    }))

    if (this.gridController) {
      await this.gridController.load()
      await this.gridController.selectRow(newTerm.id)
      await this.gridController.showRow(newTerm.id)
    }

    await this.controller.changed(true)
  }
}
