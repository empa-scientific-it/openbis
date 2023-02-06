import PageControllerSave from '@src/js/components/common/page/PageControllerSave.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import openbis from '@src/js/services/openbis.js'

export default class PluginFormControllerSave extends PageControllerSave {
  async save() {
    const state = this.context.getState()

    const plugin = FormUtil.trimFields({ ...state.plugin })
    const operations = []

    if (plugin.original) {
      if (this._isPluginUpdateNeeded(plugin)) {
        operations.push(this._updatePluginOperation(plugin))
      }
    } else {
      operations.push(this._createPluginOperation(plugin))
    }

    const options = new openbis.SynchronousOperationExecutionOptions()
    options.setExecuteInOrder(true)
    await this.facade.executeOperations(operations, options)

    return plugin.name.value
  }

  _isPluginUpdateNeeded(plugin) {
    return FormUtil.haveFieldsChanged(plugin, plugin.original, [
      'description',
      'script'
    ])
  }

  _createPluginOperation(plugin) {
    const creation = new openbis.PluginCreation()
    creation.setPluginType(plugin.pluginType)
    creation.setEntityKind(plugin.entityKind.value)
    creation.setName(plugin.name.value)
    creation.setDescription(plugin.description.value)
    creation.setScript(plugin.script.value)
    return new openbis.CreatePluginsOperation([creation])
  }

  _updatePluginOperation(plugin) {
    const update = new openbis.PluginUpdate()
    update.setPluginId(new openbis.PluginPermId(plugin.name.value))
    update.setDescription(plugin.description.value)
    update.setScript(plugin.script.value)
    return new openbis.UpdatePluginsOperation([update])
  }
}
