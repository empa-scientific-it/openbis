import _ from 'lodash'
import PageMode from '@src/js/components/common/page/PageMode.js'
import FormValidator from '@src/js/components/common/form/FormValidator.js'
import AppController from '@src/js/components/AppController.js'

export default class PageControllerLoad {
  constructor(controller) {
    this.controller = controller
    this.context = controller.getContext()
    this.facade = controller.getFacade()
    this.object = controller.getObject()
  }

  // eslint-disable-next-line no-unused-vars
  async load(object, isNew) {
    throw 'Method not implemented'
  }

  async execute() {
    try {
      await this.context.setState({
        loading: true,
        validate: FormValidator.MODE_BASIC
      })

      let isNew = undefined

      if (
        _.isFunction(this.controller.getNewObjectType) &&
        _.isFunction(this.controller.getExistingObjectType)
      ) {
        isNew = this.object.type === this.controller.getNewObjectType()

        if (isNew) {
          await this.context.setState({
            mode: PageMode.EDIT
          })
        } else {
          await this.context.setState({
            mode: PageMode.VIEW
          })
        }
      }

      await this.load(this.object, isNew)
    } catch (error) {
      AppController.getInstance().errorChange(error)
    } finally {
      if (_.isFunction(this.controller.changed)) {
        this.controller.changed(false)
      }
      this.context.setState({
        loadId: _.uniqueId('load'),
        loaded: true,
        loading: false
      })
    }
  }
}
