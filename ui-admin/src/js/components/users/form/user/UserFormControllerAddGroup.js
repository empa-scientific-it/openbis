import _ from 'lodash'
import UserFormSelectionType from '@src/js/components/users/form/user/UserFormSelectionType.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class UserFormControllerAddGroup {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
    this.groupsGridController = controller.groupsGridController
  }

  async execute() {
    let { groups } = this.context.getState()

    const newGroup = {
      id: _.uniqueId('group-'),
      code: FormUtil.createField({}),
      description: FormUtil.createField({}),
      registrator: FormUtil.createField({}),
      registrationDate: FormUtil.createField({}),
      modificationDate: FormUtil.createField({}),
      original: null
    }

    const newGroups = Array.from(groups)
    newGroups.push(newGroup)

    await this.context.setState(state => ({
      ...state,
      groups: newGroups,
      selection: {
        type: UserFormSelectionType.GROUP,
        params: {
          id: newGroup.id,
          part: 'code'
        }
      }
    }))

    await this.controller.changed(true)

    if (this.groupsGridController) {
      await this.groupsGridController.load()
      await this.groupsGridController.selectRow(newGroup.id)
      await this.groupsGridController.showRow(newGroup.id)
    }
  }
}
