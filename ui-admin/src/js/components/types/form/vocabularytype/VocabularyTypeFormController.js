import PageController from '@src/js/components/common/page/PageController.js'
import VocabularyTypeFormControllerLoad from '@src/js/components/types/form/vocabularytype/VocabularyTypeFormControllerLoad.js'
import VocabularyTypeFormControllerAdd from '@src/js/components/types/form/vocabularytype/VocabularyTypeFormControllerAdd.js'
import VocabularyTypeFormControllerRemove from '@src/js/components/types/form/vocabularytype/VocabularyTypeFormControllerRemove.js'
import VocabularyTypeFormControllerValidate from '@src/js/components/types/form/vocabularytype/VocabularyTypeFormControllerValidate.js'
import VocabularyTypeFormControllerChange from '@src/js/components/types/form/vocabularytype/VocabularyTypeFormControllerChange.js'
import VocabularyTypeFormControllerSelectionChange from '@src/js/components/types/form/vocabularytype/VocabularyTypeFormControllerSelectionChange.js'
import VocabularyTypeFormControllerSave from '@src/js/components/types/form/vocabularytype/VocabularyTypeFormControllerSave.js'
import pages from '@src/js/common/consts/pages.js'
import objectTypes from '@src/js/common/consts/objectType.js'

export default class VocabularyTypeFormController extends PageController {
  constructor(facade) {
    super(facade)
  }

  getPage() {
    return pages.TYPES
  }

  getNewObjectType() {
    return objectTypes.NEW_VOCABULARY_TYPE
  }

  getExistingObjectType() {
    return objectTypes.VOCABULARY_TYPE
  }

  load() {
    return new VocabularyTypeFormControllerLoad(this).execute()
  }

  validate(autofocus) {
    return new VocabularyTypeFormControllerValidate(this).execute(autofocus)
  }

  handleAdd() {
    return new VocabularyTypeFormControllerAdd(this).execute()
  }

  handleRemove() {
    return new VocabularyTypeFormControllerRemove(this).execute()
  }

  handleChange(type, params) {
    return new VocabularyTypeFormControllerChange(this).execute(type, params)
  }

  handleSelectionChange(type, params) {
    return new VocabularyTypeFormControllerSelectionChange(this).execute(
      type,
      params
    )
  }

  handleSave() {
    return new VocabularyTypeFormControllerSave(this).execute()
  }
}
