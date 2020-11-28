import PageControllerChange from '@src/js/components/common/page/PageControllerChange.js'
import PluginFormSelectionType from '@src/js/components/tools/form/plugin/PluginFormSelectionType.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class PluginFormControllerChange extends PageControllerChange {
  async execute(type, params) {
    if (type === PluginFormSelectionType.PLUGIN) {
      await this._handleChangePlugin(params)
    }
  }

  async _handleChangePlugin(params) {
    await this.context.setState(state => {
      const { newObject } = FormUtil.changeObjectField(
        state.plugin,
        params.field,
        params.value
      )
      return {
        plugin: newObject
      }
    })
    await this.controller.changed(true)
  }
}
