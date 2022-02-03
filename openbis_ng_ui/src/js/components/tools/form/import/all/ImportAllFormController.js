import autoBind from 'auto-bind'
import ImportAllFormControllerLoad from '@src/js/components/tools/form/import/all/ImportAllFormControllerLoad.js'
import ImportAllFormControllerValidate from '@src/js/components/tools/form/import/all/ImportAllFormControllerValidate.js'
import ImportAllFormControllerChange from '@src/js/components/tools/form/import/all/ImportAllFormControllerChange.js'
import ImportAllFormControllerImport from '@src/js/components/tools/form/import/all/ImportAllFormControllerImport.js'

export default class ImportAllFormController {
  constructor(facade) {
    autoBind(this)
    this.facade = facade
  }

  init(context) {
    this.context = context
    this.object = context.getProps().object
  }

  load() {
    return new ImportAllFormControllerLoad(this).execute()
  }

  handleChange(params) {
    return new ImportAllFormControllerChange(this).execute(params)
  }

  handleBlur() {
    return this.validate()
  }

  validate(autofocus) {
    return new ImportAllFormControllerValidate(this).execute(autofocus)
  }

  import() {
    return new ImportAllFormControllerImport(this).execute()
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
}
