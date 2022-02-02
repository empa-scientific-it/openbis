import PageControllerLoad from '@src/js/components/common/page/PageControllerLoad.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class ImportAllFormControllerLoad extends PageControllerLoad {
  async load() {
    return this.context.setState({
      fields: {
        updateMode: FormUtil.createField({})
      }
    })
  }
}
