import PageControllerValidate from '@src/js/components/common/page/PageConrollerValidate.js'
import VocabularyTypeFormSelectionType from '@src/js/components/types/form/vocabularytype/VocabularyTypeFormSelectionType.js'
import messages from '@src/js/common/messages.js'

const URL_TEMPLATE_PATTERN = /^.*\$\{term\}.*$/

export default class VocabularyTypeFormControllerValidate extends PageControllerValidate {
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
        type: VocabularyTypeFormSelectionType.VOCABULARY,
        params: {
          part: firstError.name
        }
      })
    } else if (terms.includes(firstError.object)) {
      await this.setSelection({
        type: VocabularyTypeFormSelectionType.TERM,
        params: {
          id: firstError.object.id,
          part: firstError.name
        }
      })

      if (this.controller.gridController) {
        await this.controller.gridController.showRow(firstError.object.id)
      }
    }
  }

  _validateVocabulary(validator, vocabulary) {
    validator.validateNotEmpty(vocabulary, 'code', messages.get(messages.CODE))

    if (vocabulary.internal.value) {
      validator.validateInternalCode(
        vocabulary,
        'code',
        messages.get(messages.CODE)
      )
    } else {
      validator.validateCode(vocabulary, 'code', messages.get(messages.CODE))
    }

    validator.validatePattern(vocabulary, 'urlTemplate', messages.get(messages.URL_TEMPLATE_PATTERN), URL_TEMPLATE_PATTERN)

    return validator.withErrors(vocabulary)
  }

  _validateTerms(validator, terms) {
    terms.forEach(term => {
      this._validateTerm(validator, term)
    })
    return validator.withErrors(terms)
  }

  _validateTerm(validator, term) {
    validator.validateNotEmpty(term, 'code', messages.get(messages.CODE))
    validator.validateTermCode(term, 'code', messages.get(messages.CODE))
  }
}
