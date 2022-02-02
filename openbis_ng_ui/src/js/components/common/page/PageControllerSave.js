import _ from 'lodash'
import FormValidator from '@src/js/components/common/form/FormValidator.js'
import actions from '@src/js/store/actions/actions.js'

export default class PageControllerSave {
  constructor(controller) {
    this.controller = controller
    this.context = controller.getContext()
    this.facade = controller.getFacade()
    this.object = controller.getObject()
  }

  async save() {
    throw 'Method not implemented'
  }

  async execute() {
    try {
      await this.context.setState({
        validate: FormValidator.MODE_FULL
      })

      const valid = await this.controller.validate(true)
      if (!valid) {
        return
      }

      await this.context.setState({
        loading: true
      })

      const objectId = await this.save()

      if (
        _.isFunction(this.controller.getNewObjectType) &&
        _.isFunction(this.controller.getExistingObjectType)
      ) {
        const oldObject = this.object
        const newObject = {
          type: this.controller.getExistingObjectType(),
          id: objectId
        }
        this.controller.object = newObject

        await this.controller.load()

        if (oldObject.type === this.controller.getNewObjectType()) {
          this.context.dispatch(
            actions.objectCreate(
              this.controller.getPage(),
              oldObject.type,
              oldObject.id,
              newObject.type,
              newObject.id
            )
          )
        } else if (oldObject.type === this.controller.getExistingObjectType()) {
          this.context.dispatch(
            actions.objectUpdate(
              this.controller.getPage(),
              oldObject.type,
              oldObject.id
            )
          )
        }
      }
    } catch (error) {
      this.context.dispatch(actions.errorChange(error))
    } finally {
      this.context.setState({
        loading: false
      })
    }
  }
}
