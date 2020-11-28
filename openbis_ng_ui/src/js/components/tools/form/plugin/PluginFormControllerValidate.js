import PageControllerValidate from '@src/js/components/common/page/PageConrollerValidate.js'
import PluginFormSelectionType from '@src/js/components/tools/form/plugin/PluginFormSelectionType.js'

export default class PluginFormControllerValidate extends PageControllerValidate {
  validate(validator) {
    const { plugin } = this.context.getState()

    const newPlugin = this._validatePlugin(validator, plugin)

    return {
      plugin: newPlugin
    }
  }

  async select(firstError) {
    const { plugin } = this.context.getState()

    if (firstError.object === plugin) {
      await this.setSelection({
        type: PluginFormSelectionType.PLUGIN,
        params: {
          part: firstError.name
        }
      })
    }
  }

  _validatePlugin(validator, plugin) {
    validator.validateNotEmpty(plugin, 'name', 'Name')
    validator.validateNotEmpty(plugin, 'script', 'Script')
    return validator.withErrors(plugin)
  }
}
