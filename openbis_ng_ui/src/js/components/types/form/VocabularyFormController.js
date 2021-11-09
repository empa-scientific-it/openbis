import PageController from '@src/js/components/common/page/PageController.js'
import VocabularyFormControllerLoad from '@src/js/components/types/form/VocabularyFormControllerLoad.js'
import VocabularyFormControllerAdd from '@src/js/components/types/form/VocabularyFormControllerAdd.js'
import VocabularyFormControllerRemove from '@src/js/components/types/form/VocabularyFormControllerRemove.js'
import VocabularyFormControllerValidate from '@src/js/components/types/form/VocabularyFormControllerValidate.js'
import VocabularyFormControllerChange from '@src/js/components/types/form/VocabularyFormControllerChange.js'
import VocabularyFormControllerSelectionChange from '@src/js/components/types/form/VocabularyFormControllerSelectionChange.js'
import VocabularyFormControllerSave from '@src/js/components/types/form/VocabularyFormControllerSave.js'
import pages from '@src/js/common/consts/pages.js'
import objectTypes from '@src/js/common/consts/objectType.js'

export default class VocabularyFormController extends PageController {
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
    return new VocabularyFormControllerLoad(this).execute()
  }

  validate(autofocus) {
    return new VocabularyFormControllerValidate(this).execute(autofocus)
  }

  handleAdd() {
    return new VocabularyFormControllerAdd(this).execute()
  }

  handleRemove() {
    return new VocabularyFormControllerRemove(this).execute()
  }

  handleChange(type, params) {
    return new VocabularyFormControllerChange(this).execute(type, params)
  }

  handleSelectionChange(type, params) {
    return new VocabularyFormControllerSelectionChange(this).execute(
      type,
      params
    )
  }

  handleSave() {
    return new VocabularyFormControllerSave(this).execute()
  }
}
