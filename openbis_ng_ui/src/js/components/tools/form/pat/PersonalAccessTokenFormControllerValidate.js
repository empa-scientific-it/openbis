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
      await this.controller.handleSelectionChange({
        id: firstError.object.id,
        part: firstError.name
      })

      if (this.controller.gridController) {
        await this.controller.gridController.load()
        await this.controller.gridController.showRow(firstError.object.id)
      }
    }
  }

  _validatePats(validator, pats) {
    pats.forEach(pat => {
      if (!pat.original) {
        this._validatePat(validator, pat)
      }
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
    validator.validateDateNotEmpty(
      pat,
      'validFromDate',
      messages.get(messages.VALID_FROM)
    )
    validator.validateDateNotEmpty(
      pat,
      'validToDate',
      messages.get(messages.VALID_TO)
    )

    if (
      pat.validFromDate.value &&
      pat.validFromDate.value.dateObject &&
      pat.validToDate.value &&
      pat.validToDate.value.dateObject
    ) {
      const validFromMillis = pat.validFromDate.value.dateObject.getTime()
      const validToMillis = pat.validToDate.value.dateObject.getTime()

      if (validToMillis < validFromMillis) {
        validator.addError(
          pat,
          'validToDate',
          messages.get(messages.VALID_TO_CANNOT_BE_BEFORE_VALID_FROM)
        )
      }

      if (this.controller.getMaxValidityPeriod()) {
        const validityPeriod = validToMillis - validFromMillis

        if (validityPeriod > this.controller.getMaxValidityPeriod() * 1000) {
          validator.addError(
            pat,
            'validToDate',
            messages.get(
              messages.PERSONAL_ACCESS_TOKEN_MAX_VALIDITY_PERIOD_EXCEEDED
            )
          )
        }
      }
    }
  }
}
