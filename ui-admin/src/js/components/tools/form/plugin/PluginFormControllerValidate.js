import PageControllerValidate from '@src/js/components/common/page/PageConrollerValidate.js'
import PluginFormSelectionType from '@src/js/components/tools/form/plugin/PluginFormSelectionType.js'
import messages from '@src/js/common/messages.js'

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
    validator.validateNotEmpty(plugin, 'script', messages.get(messages.SCRIPT))
    validator.validateNotEmpty(plugin, 'name', messages.get(messages.NAME))
    return validator.withErrors(plugin)
  }
}
