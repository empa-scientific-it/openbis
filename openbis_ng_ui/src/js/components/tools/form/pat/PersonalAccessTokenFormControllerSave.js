import _ from 'lodash'
import FormValidator from '@src/js/components/common/form/FormValidator.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import AppController from '@src/js/components/AppController.js'
import openbis from '@src/js/services/openbis.js'

export default class PersonalAccessTokenFormControllerSave {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
    this.facade = controller.facade
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

      const state = this.context.getState()
      const pats = this._preparePats(state.pats)
      const operations = []

      state.original.pats.forEach(originalPat => {
        const pat = _.find(pats, ['id', originalPat.id])
        if (!pat) {
          operations.push(this._deletePatOperation(originalPat))
        }
      })

      pats.forEach(pat => {
        if (!pat.original) {
          operations.push(this._createPatOperation(pat))
        }
      })

      const options = new openbis.SynchronousOperationExecutionOptions()
      options.setExecuteInOrder(true)
      await this.facade.executeOperations(operations, options)
    } catch (error) {
      AppController.getInstance().errorChange(error)
    } finally {
      this.context.setState({
        loading: false
      })
    }
  }

  _preparePats(pats) {
    return pats.map(pat => FormUtil.trimFields(pat))
  }

  _createPatOperation(pat) {
    const creation = new openbis.PersonalAccessTokenCreation()
    creation.setSessionName(pat.sessionName.value)
    return new openbis.CreatePersonalAccessTokensOperation([creation])
  }

  _deletePatOperation(pat) {
    const patId = new openbis.PersonalAccessTokenPermId(pat.hash.value)
    const options = new openbis.PersonalAccessTokenDeletionOptions()
    options.setReason('deleted via ng_ui')
    return new openbis.DeletePersonalAccessTokensOperation([patId], options)
  }
}
