import PageControllerValidate from '@src/js/components/common/page/PageConrollerValidate.js'
import messages from '@src/js/common/messages.js'

export default class ImportAllFormControllerValidate extends PageControllerValidate {
  validate(validator) {
    const { fields } = this.context.getState()

    validator.validateNotEmpty(
      fields,
      'file',
      messages.get(messages.IMPORT_FILE)
    )
    validator.validateNotEmpty(
      fields,
      'updateMode',
      messages.get(messages.UPDATE_MODE)
    )

    return {
      fields: validator.withErrors(fields)
    }
  }

  async select(firstError) {
    await this.setSelection({
      field: firstError.name
    })
  }
}
