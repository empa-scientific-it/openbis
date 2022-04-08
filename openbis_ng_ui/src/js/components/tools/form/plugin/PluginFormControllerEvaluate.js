import PageMode from '@src/js/components/common/page/PageMode.js'
import AppController from '@src/js/components/AppController.js'
import openbis from '@src/js/services/openbis.js'

export default class PluginFormControllerEvaluate {
  constructor(controller) {
    this.controller = controller
    this.context = controller.getContext()
    this.facade = controller.getFacade()
  }

  async execute() {
    try {
      this.context.setState(state => ({
        ...state,
        evaluateResults: {
          ...state.evaluateResults,
          loading: true
        }
      }))

      const result = await this.evaluatePlugin()

      this.context.setState(state => ({
        ...state,
        evaluateResults: {
          ...state.evaluateResults,
          loaded: true,
          result,
          timestamp: Date.now()
        }
      }))
    } catch (error) {
      AppController.getInstance().errorChange(error)
    } finally {
      this.context.setState(state => ({
        ...state,
        evaluateResults: {
          ...state.evaluateResults,
          loading: false
        }
      }))
    }
  }

  async evaluatePlugin() {
    const { mode, plugin, evaluateParameters } = this.context.getState()

    const pluginType = plugin.pluginType
    const pluginName = plugin.name.value
    const pluginScript = plugin.script.value

    const entity = evaluateParameters.entity.value
    const entityIsNew = evaluateParameters.entityIsNew.value

    let options = null

    if (pluginType === openbis.PluginType.DYNAMIC_PROPERTY) {
      options = new openbis.DynamicPropertyPluginEvaluationOptions()
    } else if (pluginType === openbis.PluginType.ENTITY_VALIDATION) {
      options = new openbis.EntityValidationPluginEvaluationOptions()
      options.setNew(entityIsNew)
    }

    if (entity) {
      let objectId = null

      if (entity.entityKind === openbis.EntityKind.EXPERIMENT) {
        objectId = new openbis.ExperimentIdentifier(entity.entityId)
      } else if (entity.entityKind === openbis.EntityKind.SAMPLE) {
        objectId = new openbis.SampleIdentifier(entity.entityId)
      } else if (entity.entityKind === openbis.EntityKind.DATA_SET) {
        objectId = new openbis.DataSetPermId(entity.entityId)
      } else if (entity.entityKind === openbis.EntityKind.MATERIAL) {
        objectId = new openbis.MaterialPermId(
          entity.entityId.code,
          entity.entityId.typeCode
        )
      }

      options.setObjectId(objectId)
    }

    if (mode === PageMode.VIEW) {
      options.setPluginId(new openbis.PluginPermId(pluginName))
    } else if (mode === PageMode.EDIT) {
      options.setPluginScript(pluginScript)
    } else {
      throw new Error('Unsupported mode: ' + mode)
    }

    return await this.facade.evaluatePlugin(options)
  }
}
