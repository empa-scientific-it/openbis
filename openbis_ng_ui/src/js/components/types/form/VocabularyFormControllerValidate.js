import PageControllerValidate from '@src/js/components/common/page/PageConrollerValidate.js'

export default class VocabularyFormControllerValidate extends PageControllerValidate {
  validate(validator) {
    const { vocabulary, terms } = this.context.getState()

    const newVocabulary = this._validateVocabulary(validator, vocabulary)
    const newTerms = this._validateTerms(validator, terms)

    return {
      vocabulary: newVocabulary,
      terms: newTerms
    }
  }

  async select(firstError) {
    const { vocabulary, terms } = this.context.getState()

    if (firstError.object === vocabulary) {
      await this.setSelection({
        type: 'vocabulary',
        params: {
          part: firstError.name
        }
      })
    } else if (terms.includes(firstError.object)) {
      await this.setSelection({
        type: 'term',
        params: {
          id: firstError.object.id,
          part: firstError.name
        }
      })

      if (this.controller.gridController) {
        await this.controller.gridController.showSelectedRow()
      }
    }
  }

  _validateVocabulary(validator, vocabulary) {
    validator.validateNotEmpty(vocabulary, 'code', 'Code')
    validator.validateCode(vocabulary, 'code', 'Code')
    return validator.withErrors(vocabulary)
  }

  _validateTerms(validator, terms) {
    terms.forEach(term => {
      this._validateTerm(validator, term)
    })
    return validator.withErrors(terms)
  }

  _validateTerm(validator, term) {
    validator.validateNotEmpty(term, 'code', 'Code')
    validator.validateTermCode(term, 'code', 'Code')
  }
}
