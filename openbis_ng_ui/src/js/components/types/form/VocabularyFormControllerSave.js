import _ from 'lodash'
import PageControllerSave from '@src/js/components/common/page/PageControllerSave.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import openbis from '@src/js/services/openbis.js'

export default class VocabularyFormControllerSave extends PageControllerSave {
  async save() {
    const state = this.context.getState()
    const vocabulary = this._prepareVocabulary(state.vocabulary)
    const terms = this._prepareTerms(state.terms)
    const operations = []

    if (vocabulary.original) {
      if (this._isVocabularyUpdateNeeded(vocabulary)) {
        operations.push(this._updateVocabularyOperation(vocabulary))
      }
    } else {
      operations.push(this._createVocabularyOperation(vocabulary))
    }

    state.original.terms.forEach(originalTerm => {
      const term = _.find(terms, ['id', originalTerm.id])
      if (!term) {
        operations.push(this._deleteTermOperation(vocabulary, originalTerm))
      }
    })

    terms.forEach(term => {
      if (term.original) {
        if (this._isTermUpdateNeeded(term)) {
          operations.push(this._updateTermOperation(vocabulary, term))
        }
      } else {
        operations.push(this._createTermOperation(vocabulary, term))
      }
    })

    const options = new openbis.SynchronousOperationExecutionOptions()
    options.setExecuteInOrder(true)
    await this.facade.executeOperations(operations, options)

    return vocabulary.code.value
  }

  _prepareVocabulary(vocabulary) {
    const code = vocabulary.code.value
    return FormUtil.trimFields({
      ...vocabulary,
      code: {
        value: code ? code.toUpperCase() : null
      }
    })
  }

  _prepareTerms(terms) {
    return terms.map(term => {
      const code = term.code.value
      return FormUtil.trimFields({
        ...term,
        code: {
          value: code ? code.toUpperCase() : null
        }
      })
    })
  }

  _isVocabularyUpdateNeeded(vocabulary) {
    return FormUtil.haveFieldsChanged(vocabulary, vocabulary.original, [
      'code',
      'description',
      'urlTemplate'
    ])
  }

  _isTermUpdateNeeded(term) {
    return FormUtil.haveFieldsChanged(term, term.original, [
      'code',
      'label',
      'description',
      'official'
    ])
  }

  _createVocabularyOperation(vocabulary) {
    const creation = new openbis.VocabularyCreation()
    creation.setCode(vocabulary.code.value)
    creation.setDescription(vocabulary.description.value)
    creation.setUrlTemplate(vocabulary.urlTemplate.value)
    creation.setManagedInternally(vocabulary.internal.value)
    creation.setChosenFromList(true)
    return new openbis.CreateVocabulariesOperation([creation])
  }

  _updateVocabularyOperation(vocabulary) {
    const update = new openbis.VocabularyUpdate()
    update.setVocabularyId(new openbis.VocabularyPermId(vocabulary.code.value))
    update.setDescription(vocabulary.description.value)
    update.setUrlTemplate(vocabulary.urlTemplate.value)
    return new openbis.UpdateVocabulariesOperation([update])
  }

  _createTermOperation(vocabulary, term) {
    const creation = new openbis.VocabularyTermCreation()
    creation.setVocabularyId(
      new openbis.VocabularyPermId(vocabulary.code.value)
    )
    creation.setCode(term.code.value)
    creation.setLabel(term.label.value)
    creation.setDescription(term.description.value)
    creation.setOfficial(term.official.value)
    return new openbis.CreateVocabularyTermsOperation([creation])
  }

  _updateTermOperation(vocabulary, term) {
    const update = new openbis.VocabularyTermUpdate()
    update.setVocabularyTermId(
      new openbis.VocabularyTermPermId(term.code.value, vocabulary.code.value)
    )
    update.setLabel(term.label.value)
    update.setDescription(term.description.value)
    update.setOfficial(term.official.value)
    return new openbis.UpdateVocabularyTermsOperation([update])
  }

  _deleteTermOperation(vocabulary, term) {
    const termId = new openbis.VocabularyTermPermId(
      term.code.value,
      vocabulary.code.value
    )
    const options = new openbis.VocabularyTermDeletionOptions()
    options.setReason('deleted via ng_ui')
    return new openbis.DeleteVocabularyTermsOperation([termId], options)
  }
}
