import _ from 'lodash'
import PageControllerChange from '@src/js/components/common/page/PageControllerChange.js'
import PluginFormSelectionType from '@src/js/components/tools/form/plugin/PluginFormSelectionType.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class PluginFormControllerChange extends PageControllerChange {
  async execute(type, params) {
    if (type === PluginFormSelectionType.PLUGIN) {
      await this._handleChangePlugin(params)
    } else if (type === PluginFormSelectionType.EVALUATE_PARAMETER) {
      await this._handleChangeEvaluateParameter(params)
    }
  }

  async _handleChangePlugin(params) {
    await this.context.setState(oldState => {
      const { newObject } = FormUtil.changeObjectField(
        oldState.plugin,
        params.field,
        params.value
      )

      const newState = {
        ...oldState,
        plugin: newObject
      }

      this._handleChangePluginEntityKind(oldState, newState)

      return newState
    })
    await this.controller.changed(true)
  }

  _handleChangePluginEntityKind(oldState, newState) {
    const oldEntityKind = oldState.plugin.entityKind.value
    const newEntityKind = newState.plugin.entityKind.value

    if (oldEntityKind !== newEntityKind) {
      const newEvaluateEntityKind = newEntityKind
        ? newEntityKind
        : newState.evaluateParameters.entityKind.value

      const newEvaluateEntity =
        newEntityKind &&
        newEntityKind !== newState.evaluateParameters.entityKind.value
          ? null
          : newState.evaluateParameters.entity.value

      _.assign(newState, {
        evaluateParameters: {
          ...newState.evaluateParameters,
          entityKind: {
            ...newState.evaluateParameters.entityKind,
            value: newEvaluateEntityKind,
            enabled: !newEntityKind
          },
          entity: {
            ...newState.evaluateParameters.entity,
            value: newEvaluateEntity,
            enabled: !!newEvaluateEntityKind
          }
        }
      })
    }
  }

  async _handleChangeEvaluateParameter(params) {
    await this.context.setState(state => {
      const { oldObject, newObject } = FormUtil.changeObjectField(
        state.evaluateParameters,
        params.field,
        params.value
      )

      this._handleChangeEvaluateParameterEntityKind(oldObject, newObject)

      return {
        evaluateParameters: newObject
      }
    })
  }

  _handleChangeEvaluateParameterEntityKind(oldParameters, newParameters) {
    const oldEntityKind = oldParameters.entityKind.value
    const newEntityKind = newParameters.entityKind.value

    if (oldEntityKind !== newEntityKind) {
      _.assign(newParameters, {
        entity: {
          ...newParameters.entity,
          value: null,
          enabled: !!newEntityKind
        }
      })
    }
  }
}
