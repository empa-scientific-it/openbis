import PageControllerValidate from '@src/js/components/common/page/PageConrollerValidate.js'
import messages from '@src/js/common/messages.js'

export default class PropertyTypeFormControllerValidate extends PageControllerValidate {
  validate(validator) {
    const { propertyType } = this.context.getState()

    const newPropertyType = this._validatePropertyType(validator, propertyType)

    return {
      propertyType: newPropertyType
    }
  }

  async select(firstError) {
    await this.setSelection({
      params: {
        part: firstError.name
      }
    })
  }

  _validatePropertyType(validator, propertyType) {
    validator.validateNotEmpty(
      propertyType,
      'code',
      messages.get(messages.CODE)
    )

    if (propertyType.internal.value) {
      validator.validateInternalCode(
        propertyType,
        'code',
        messages.get(messages.CODE)
      )
    } else {
      validator.validateCode(propertyType, 'code', messages.get(messages.CODE))
    }

    validator.validateNotEmpty(
      propertyType,
      'label',
      messages.get(messages.LABEL)
    )
    validator.validateNotEmpty(
      propertyType,
      'description',
      messages.get(messages.DESCRIPTION)
    )
    validator.validateNotEmpty(
      propertyType,
      'dataType',
      messages.get(messages.DATA_TYPE)
    )

    if (propertyType.vocabulary.visible) {
      validator.validateNotEmpty(
        propertyType,
        'vocabulary',
        messages.get(messages.VOCABULARY_TYPE)
      )
    }

    return validator.withErrors(propertyType)
  }
}
