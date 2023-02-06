import PageControllerChange from '@src/js/components/common/page/PageControllerChange.js'
import VocabularyTypeFormSelectionType from '@src/js/components/types/form/vocabularytype/VocabularyTypeFormSelectionType.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class VocabularyTypeFormControllerChange extends PageControllerChange {
  constructor(controller) {
    super(controller)
    this.gridController = controller.gridController
  }

  async execute(type, params) {
    if (type === VocabularyTypeFormSelectionType.VOCABULARY) {
      await this._handleChangeVocabulary(params)
    } else if (type === VocabularyTypeFormSelectionType.TERM) {
      await this._handleChangeTerm(params)
    }
  }

  async _handleChangeVocabulary(params) {
    await this.context.setState(state => {
      const { newObject } = FormUtil.changeObjectField(
        state.vocabulary,
        params.field,
        params.value
      )
      return {
        vocabulary: newObject
      }
    })
    await this.controller.changed(true)
  }

  async _handleChangeTerm(params) {
    await this.context.setState(state => {
      const { newCollection } = FormUtil.changeCollectionItemField(
        state.terms,
        params.id,
        params.field,
        params.value
      )
      return {
        terms: newCollection
      }
    })

    if (this.gridController) {
      await this.gridController.load()
      await this.gridController.showRow(params.id)
    }

    await this.controller.changed(true)
  }
}
