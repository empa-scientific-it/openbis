import _ from 'lodash'
import PageMode from '@src/js/components/common/page/PageMode.js'
import FormValidator from '@src/js/components/common/form/FormValidator.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import AppController from '@src/js/components/AppController.js'

export default class PersonalAccessTokenFormControllerLoad {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
    this.facade = controller.facade
  }

  async execute() {
    return Promise.all([this._loadDictionaries(), this._loadPats()])
  }

  async _loadDictionaries() {
    const [users] = await Promise.all([this.facade.loadUsers()])
    await this.context.setState(() => ({
      dictionaries: {
        users
      }
    }))
  }

  async _loadPats() {
    await this.context.setState({
      loading: true,
      mode: PageMode.VIEW,
      validate: FormValidator.MODE_BASIC
    })

    try {
      const loadedPats = await this.facade.loadPats()
      const pats = loadedPats.map(loadedPat => this._createPat(loadedPat))
      const selection = this._createSelection(pats)

      return this.context.setState({
        pats,
        selection,
        original: {
          pats: pats.map(pat => pat.original)
        }
      })
    } catch (error) {
      AppController.getInstance().errorChange(error)
    } finally {
      this.controller.changed(false)
      this.context.setState({
        loadId: _.uniqueId('load'),
        loaded: true,
        loading: false
      })
    }
  }

  _createPat(loadedPat) {
    const validFromDate = _.get(loadedPat, 'validFromDate', null)
    const validToDate = _.get(loadedPat, 'validToDate', null)
    const registrationDate = _.get(loadedPat, 'registrationDate', null)
    const accessDate = _.get(loadedPat, 'accessDate', null)

    const pat = {
      id: _.get(loadedPat, 'hash'),
      hash: FormUtil.createField({
        value: _.get(loadedPat, 'hash', null),
        enabled: false
      }),
      sessionName: FormUtil.createField({
        value: _.get(loadedPat, 'sessionName', null),
        enabled: false
      }),
      validFromDate: FormUtil.createField({
        value: validFromDate
          ? {
              dateObject: new Date(validFromDate)
            }
          : null,
        enabled: false
      }),
      validToDate: FormUtil.createField({
        value: validToDate
          ? {
              dateObject: new Date(validToDate)
            }
          : null,
        enabled: false
      }),
      owner: FormUtil.createField({
        value: loadedPat.owner ? loadedPat.owner.userId : null,
        enabled: false
      }),
      registrator: FormUtil.createField({
        value: loadedPat.registrator ? loadedPat.registrator.userId : null,
        enabled: false
      }),
      registrationDate: FormUtil.createField({
        value: registrationDate
          ? {
              dateObject: new Date(registrationDate)
            }
          : null,
        enabled: false
      }),
      accessDate: FormUtil.createField({
        value: accessDate
          ? {
              dateObject: new Date(accessDate)
            }
          : null,
        enabled: false
      })
    }
    pat.original = _.cloneDeep(pat)
    return pat
  }

  _createSelection(newPats) {
    const { selection: oldSelection, pats: oldPats } = this.context.getState()

    if (!oldSelection) {
      return null
    } else {
      const oldPat = _.find(
        oldPats,
        oldPat => oldPat.id === oldSelection.params.id
      )
      if (oldPat.hash.value) {
        const newPat = _.find(
          newPats,
          newPat => newPat.hash.value === oldPat.hash.value
        )
        if (newPat) {
          return {
            params: {
              id: newPat.id
            }
          }
        } else {
          return null
        }
      } else {
        const getValue = function (pat) {
          return {
            owner: _.trim(pat.owner.value),
            sessionName: _.trim(pat.sessionName.value),
            validFrom: pat.validFromDate.value
              ? pat.validFromDate.value.dateObject
              : null,
            validTo: pat.validToDate.value
              ? pat.validToDate.value.dateObject
              : null
          }
        }
        const oldValue = getValue(oldPat)
        newPats = _.filter(newPats, newPat => {
          const newValue = getValue(newPat)
          return _.isEqual(oldValue, newValue)
        })

        if (newPats.length === 1) {
          return {
            params: {
              id: newPats[0].id
            }
          }
        } else {
          return null
        }
      }
    }
  }
}
