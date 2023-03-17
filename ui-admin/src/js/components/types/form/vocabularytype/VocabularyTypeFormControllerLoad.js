import _ from 'lodash'
import AppController from '@src/js/components/AppController.js'
import PageControllerLoad from '@src/js/components/common/page/PageControllerLoad.js'
import VocabularyTypeFormSelectionType from '@src/js/components/types/form/vocabularytype/VocabularyTypeFormSelectionType.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import users from '@src/js/common/consts/users.js'

export default class VocabularyTypeFormControllerLoad extends PageControllerLoad {
  async load(object, isNew) {
    let loadedVocabulary = null

    if (!isNew) {
      loadedVocabulary = await this.facade.loadVocabulary(object.id)
      if (!loadedVocabulary) {
        return
      }
    }

    const vocabulary = this._createVocabulary(loadedVocabulary)

    let termsCounter = 0
    let terms = []

    if (loadedVocabulary && loadedVocabulary.terms) {
      terms = loadedVocabulary.terms.map(loadedTerm =>
        this._createTerm('term-' + termsCounter++, loadedVocabulary, loadedTerm)
      )
    }

    const selection = this._createSelection(terms)

    return this.context.setState({
      vocabulary,
      terms,
      termsCounter,
      selection,
      original: {
        vocabulary: vocabulary.original,
        terms: terms.map(term => term.original)
      }
    })
  }

  _createVocabulary(loadedVocabulary) {
    const registrator = _.get(loadedVocabulary, 'registrator.userId', null)
    const internal = _.get(loadedVocabulary, 'managedInternally', false)

    const vocabulary = {
      id: _.get(loadedVocabulary, 'code', null),
      code: FormUtil.createField({
        value: _.get(loadedVocabulary, 'code', null),
        enabled: loadedVocabulary === null
      }),
      description: FormUtil.createField({
        value: _.get(loadedVocabulary, 'description', null),
        enabled: !internal || AppController.getInstance().isSystemUser()
      }),
      urlTemplate: FormUtil.createField({
        value: _.get(loadedVocabulary, 'urlTemplate', null),
        enabled: !internal || AppController.getInstance().isSystemUser()
      }),
      internal: FormUtil.createField({
        value: internal,
        visible: AppController.getInstance().isSystemUser(),
        enabled:
          loadedVocabulary === null &&
          AppController.getInstance().isSystemUser()
      }),
      registrator: FormUtil.createField({
        value: registrator,
        visible: false,
        enabled: false
      })
    }
    if (loadedVocabulary) {
      vocabulary.original = _.cloneDeep(vocabulary)
    }
    return vocabulary
  }

  _createTerm(id, loadedVocabulary, loadedTerm) {
    const official = _.get(loadedTerm, 'official', false)
    const registrator = _.get(loadedTerm, 'registrator.userId', null)
    const internalVocabulary = _.get(
      loadedVocabulary,
      'managedInternally',
      false
    )
    const internalTerm = internalVocabulary && registrator === users.SYSTEM

    const term = {
      id: id,
      code: FormUtil.createField({
        value: _.get(loadedTerm, 'code', null),
        enabled: false
      }),
      label: FormUtil.createField({
        value: _.get(loadedTerm, 'label', null),
        enabled: !internalTerm || AppController.getInstance().isSystemUser()
      }),
      description: FormUtil.createField({
        value: _.get(loadedTerm, 'description', null),
        enabled: !internalTerm || AppController.getInstance().isSystemUser()
      }),
      official: FormUtil.createField({
        value: official,
        enabled:
          !official &&
          (!internalTerm || AppController.getInstance().isSystemUser())
      }),
      registrator: FormUtil.createField({
        value: registrator,
        visible: false,
        enabled: false
      }),
      registrationDate: FormUtil.createField({
        value: _.get(loadedTerm, 'registrationDate', null),
        visible: false,
        enabled: false
      })
    }
    term.original = _.cloneDeep(term)
    return term
  }

  _createSelection(newTerms) {
    const { selection: oldSelection, terms: oldTerms } = this.context.getState()

    if (!oldSelection) {
      return null
    } else if (oldSelection.type === VocabularyTypeFormSelectionType.TERM) {
      const oldTerm = _.find(
        oldTerms,
        oldTerm => oldTerm.id === oldSelection.params.id
      )
      const newTerm = _.find(newTerms, newTerm => {
        const newValue = newTerm.code.value
          ? newTerm.code.value.toLowerCase()
          : null
        const oldValue = oldTerm.code.value
          ? oldTerm.code.value.toLowerCase()
          : null
        return newValue === oldValue
      })

      if (newTerm) {
        return {
          type: VocabularyTypeFormSelectionType.TERM,
          params: {
            id: newTerm.id
          }
        }
      }
    } else {
      return null
    }
  }
}
