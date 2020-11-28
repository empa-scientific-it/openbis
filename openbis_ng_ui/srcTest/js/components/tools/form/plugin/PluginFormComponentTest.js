import React from 'react'
import ComponentTest from '@srcTest/js/components/common/ComponentTest.js'
import PluginForm from '@src/js/components/tools/form/plugin/PluginForm.jsx'
import PluginFormWrapper from '@srcTest/js/components/tools/form/plugin/wrapper/PluginFormWrapper.js'
import PluginFormController from '@src/js/components/tools/form/plugin/PluginFormController.js'
import PluginFormFacade from '@src/js/components/tools/form/plugin/PluginFormFacade'
import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@srcTest/js/services/openbis.js'

jest.mock('@src/js/components/tools/form/plugin/PluginFormFacade')

export default class PluginFormComponentTest extends ComponentTest {
  static SUITE = 'PluginFormComponent'

  constructor() {
    super(
      object => <PluginForm object={object} controller={this.controller} />,
      wrapper => new PluginFormWrapper(wrapper)
    )
    this.facade = null
    this.controller = null
  }

  async beforeEach() {
    super.beforeEach()

    this.facade = new PluginFormFacade()
    this.controller = new PluginFormController(this.facade)
  }

  async mountNew(pluginType) {
    if (pluginType === openbis.PluginType.DYNAMIC_PROPERTY) {
      return await this.mount({
        type: objectTypes.NEW_DYNAMIC_PROPERTY_PLUGIN
      })
    } else if (pluginType === openbis.PluginType.ENTITY_VALIDATION) {
      return await this.mount({
        type: objectTypes.NEW_ENTITY_VALIDATION_PLUGIN
      })
    } else {
      throw Error('Unsupported plugin type: ' + pluginType)
    }
  }

  async mountExisting(plugin) {
    this.facade.loadPlugin.mockReturnValue(Promise.resolve(plugin))

    if (plugin.pluginType === openbis.PluginType.DYNAMIC_PROPERTY) {
      return await this.mount({
        id: plugin.getName(),
        type: objectTypes.DYNAMIC_PROPERTY_PLUGIN
      })
    } else if (plugin.pluginType === openbis.PluginType.ENTITY_VALIDATION) {
      return await this.mount({
        id: plugin.getName(),
        type: objectTypes.ENTITY_VALIDATION_PLUGIN
      })
    } else {
      throw Error('Unsupported plugin type: ' + plugin.pluginType)
    }
  }
}
