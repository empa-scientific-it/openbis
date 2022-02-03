import PageControllerChange from '@src/js/components/common/page/PageControllerChange.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class ImportAllFormControllerChange extends PageControllerChange {
  async execute(params) {
    await this.context.setState(oldState => {
      const { newObject } = FormUtil.changeObjectField(
        oldState.fields,
        params.field,
        params.value
      )
      return {
        ...oldState,
        fields: newObject
      }
    })
  }
}
