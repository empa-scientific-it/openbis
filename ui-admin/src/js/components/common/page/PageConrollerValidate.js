import _ from 'lodash'
import FormValidator from '@src/js/components/common/form/FormValidator.js'

export default class PageControllerValidate {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
    this.object = controller.object
  }

  // eslint-disable-next-line no-unused-vars
  async validate(validator) {
    throw 'Method not implemented'
  }

  // eslint-disable-next-line no-unused-vars
  async select(firstError) {
    throw 'Method not implemented'
  }

  async execute(autofocus) {
    const { validate } = this.context.getState()

    const validator = new FormValidator(validate)
    const newState = await this.validate(validator)
    const errors = validator.getErrors()

    if (!_.isEmpty(errors) && autofocus) {
      const firstError = errors[0]
      await this.select(firstError)
    }

    await this.context.setState(newState)

    return _.isEmpty(errors)
  }

  async setSelection(selection) {
    await this.context.setState({
      selection
    })
  }
}
