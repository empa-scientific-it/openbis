import PageController from '@src/js/components/common/page/PageController.js'
import QueryFormControllerLoad from '@src/js/components/tools/form/query/QueryFormControllerLoad.js'
import QueryFormControllerValidate from '@src/js/components/tools/form/query/QueryFormControllerValidate.js'
import QueryFormControllerChange from '@src/js/components/tools/form/query/QueryFormControllerChange.js'
import QueryFormControllerExecute from '@src/js/components/tools/form/query/QueryFormControllerExecute.js'
import QueryFormControllerSave from '@src/js/components/tools/form/query/QueryFormControllerSave.js'
import pages from '@src/js/common/consts/pages.js'
import objectTypes from '@src/js/common/consts/objectType.js'

export default class QueryFormController extends PageController {
  constructor(facade) {
    super(facade)
  }

  getPage() {
    return pages.TOOLS
  }

  getNewObjectType() {
    return objectTypes.NEW_QUERY
  }

  getExistingObjectType() {
    return objectTypes.QUERY
  }

  load() {
    return new QueryFormControllerLoad(this).execute()
  }

  validate(autofocus) {
    return new QueryFormControllerValidate(this).execute(autofocus)
  }

  handleChange(type, params) {
    return new QueryFormControllerChange(this).execute(type, params)
  }

  handleExecute() {
    return new QueryFormControllerExecute(this).execute()
  }

  handleSave() {
    return new QueryFormControllerSave(this).execute()
  }
}
