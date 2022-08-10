import autoBind from 'auto-bind'
import PageControllerChanged from '@src/js/components/common/page/PageControllerChanged.js'
import PageControllerEdit from '@src/js/components/common/page/PageControllerEdit.js'
import PageControllerCancel from '@src/js/components/common/page/PageControllerCancel.js'
import PersonalAccessTokenFormControllerLoad from '@src/js/components/tools/form/pat/PersonalAccessTokenFormControllerLoad.js'
import PersonalAccessTokenFormControllerAdd from '@src/js/components/tools/form/pat/PersonalAccessTokenFormControllerAdd.js'
import PersonalAccessTokenFormControllerRemove from '@src/js/components/tools/form/pat/PersonalAccessTokenFormControllerRemove.js'
import PersonalAccessTokenFormControllerValidate from '@src/js/components/tools/form/pat/PersonalAccessTokenFormControllerValidate.js'
import PersonalAccessTokenFormControllerChange from '@src/js/components/tools/form/pat/PersonalAccessTokenFormControllerChange.js'
import PersonalAccessTokenFormControllerSelectionChange from '@src/js/components/tools/form/pat/PersonalAccessTokenFormControllerSelectionChange.js'
import PersonalAccessTokenFormControllerSave from '@src/js/components/tools/form/pat/PersonalAccessTokenFormControllerSave.js'
import pages from '@src/js/common/consts/pages.js'

export default class PersonalAccessTokenFormController {
  constructor(facade) {
    autoBind(this)
    this.facade = facade
  }

  getPage() {
    return pages.TOOLS
  }

  init(context) {
    this.context = context
    this.object = context.getProps().object
  }

  load() {
    return new PersonalAccessTokenFormControllerLoad(this).execute()
  }

  validate(autofocus) {
    return new PersonalAccessTokenFormControllerValidate(this).execute(
      autofocus
    )
  }

  changed(changed) {
    return new PageControllerChanged(this).execute(changed)
  }

  handleEdit() {
    return new PageControllerEdit(this).execute()
  }

  handleCancel() {
    return new PageControllerCancel(this).execute()
  }

  handleAdd() {
    return new PersonalAccessTokenFormControllerAdd(this).execute()
  }

  handleRemove() {
    return new PersonalAccessTokenFormControllerRemove(this).execute()
  }

  handleChange(params) {
    return new PersonalAccessTokenFormControllerChange(this).execute(params)
  }

  handleBlur() {
    return this.validate()
  }

  handleSelectionChange(params) {
    return new PersonalAccessTokenFormControllerSelectionChange(this).execute(
      params
    )
  }

  handleSave() {
    return new PersonalAccessTokenFormControllerSave(this).execute()
  }

  getFacade() {
    return this.facade
  }

  getContext() {
    return this.context
  }

  getObject() {
    return this.object
  }

  getDictionaries() {
    const { dictionaries } = this.context.getState()
    return dictionaries || {}
  }
}
