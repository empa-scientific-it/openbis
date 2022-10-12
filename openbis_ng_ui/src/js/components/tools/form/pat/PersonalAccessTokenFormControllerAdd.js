import _ from 'lodash'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import AppController from '@src/js/components/AppController.js'

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
      hash: FormUtil.createField({ visible: false }),
      sessionName: FormUtil.createField({}),
      validFromDate: FormUtil.createField({}),
      validToDate: FormUtil.createField({}),
      owner: FormUtil.createField({
        value: AppController.getInstance().getUser()
      }),
      registrator: FormUtil.createField({ visible: false }),
      registrationDate: FormUtil.createField({ visible: false }),
      accessDate: FormUtil.createField({ visible: false })
    }

    const newPats = Array.from(pats)
    newPats.push(newPat)

    await this.context.setState(state => ({
      ...state,
      pats: newPats,
      selection: {
        params: {
          id: newPat.id,
          part: 'sessionName'
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
