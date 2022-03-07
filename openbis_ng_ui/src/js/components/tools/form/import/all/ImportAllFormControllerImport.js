import PageControllerSave from '@src/js/components/common/page/PageControllerSave.js'
import ErrorObject from '@src/js/components/common/error/ErrorObject.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class ImportAllFormControllerImport extends PageControllerSave {
  async save() {
    const { fields } = this.context.getState()

    try {
      await this.context.setState({
        loading: true
      })

      const output = await this.facade.import(
        fields.file.value,
        fields.updateMode.value
      )

      await this.context.setState({
        fields: {
          file: FormUtil.createField({}),
          updateMode: FormUtil.createField({})
        },
        result: {
          success: true,
          output: output,
          visible: false
        }
      })
    } catch (error) {
      this.context.setState({
        result: {
          success: false,
          output: new ErrorObject(error).toString(),
          visible: false
        }
      })
    } finally {
      await this.context.setState({
        loading: false
      })
    }
  }
}
