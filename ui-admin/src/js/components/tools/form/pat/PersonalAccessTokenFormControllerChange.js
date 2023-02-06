import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class PersonalAccessTokenFormControllerChange {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
    this.gridController = controller.gridController
  }

  async execute(params) {
    await this.context.setState(state => {
      const { newCollection } = FormUtil.changeCollectionItemField(
        state.pats,
        params.id,
        params.field,
        params.value
      )
      return {
        pats: newCollection
      }
    })

    await this.controller.changed(true)
  }
}
