import PageControllerValidate from '@src/js/components/common/page/PageConrollerValidate.js'
import messages from '@src/js/common/messages.js'

export default class PersonalAccessTokenFormControllerValidate extends PageControllerValidate {
  validate(validator) {
    const { pats } = this.context.getState()

    const newPats = this._validatePats(validator, pats)

    return {
      pats: newPats
    }
  }

  async select(firstError) {
    const { pats } = this.context.getState()

    if (pats.includes(firstError.object)) {
      await this.setSelection({
        params: {
          id: firstError.object.id,
          part: firstError.name
        }
      })

      if (this.controller.gridController) {
        await this.controller.gridController.load()
        await this.controller.gridController.showRow(firstError.object.id)
      }
    }
  }

  _validatePats(validator, pats) {
    pats.forEach(pat => {
      this._validatePat(validator, pat)
    })
    return validator.withErrors(pats)
  }

  _validatePat(validator, pat) {
    validator.validateNotEmpty(pat, 'owner', messages.get(messages.OWNER))
    validator.validateNotEmpty(
      pat,
      'sessionName',
      messages.get(messages.SESSION_NAME)
    )
    validator.validateNotEmpty(
      pat,
      'validFromDate',
      messages.get(messages.VALID_FROM)
    )
    validator.validateNotEmpty(
      pat,
      'validToDate',
      messages.get(messages.VALID_TO)
    )
  }
}
