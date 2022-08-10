import _ from 'lodash'
import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class PersonalAccessTokenFormControllerAdd {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
    this.gridController = controller.gridController
  }

  async execute() {
    let { pats } = this.context.getState()

    const newPat = {
      id: _.uniqueId('pat-'),
      hash: FormUtil.createField({}),
      sessionName: FormUtil.createField({}),
      validFromDate: FormUtil.createField({}),
      validToDate: FormUtil.createField({}),
      owner: FormUtil.createField({}),
      registrator: FormUtil.createField({}),
      registrationDate: FormUtil.createField({}),
      accessDate: FormUtil.createField({})
    }

    const newPats = Array.from(pats)
    newPats.push(newPat)

    await this.context.setState(state => ({
      ...state,
      pats: newPats,
      selection: {
        params: {
          id: newPat.id,
          part: 'code'
        }
      }
    }))

    if (this.gridController) {
      await this.gridController.load()
      await this.gridController.selectRow(newPat.id)
      await this.gridController.showRow(newPat.id)
    }

    await this.controller.changed(true)
  }
}
