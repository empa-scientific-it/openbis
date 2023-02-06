import React from 'react'
import LinkToObject from '@src/js/components/common/form/LinkToObject.jsx'
import pages from '@src/js/common/consts/pages.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@src/js/services/openbis.js'
import logger from '@src/js/common/logger.js'

class PluginLink extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'PluginLink.render')

    const { pluginName, pluginType } = this.props

    if (pluginName && pluginType) {
      let objectType = null

      if (pluginType === openbis.PluginType.DYNAMIC_PROPERTY) {
        objectType = objectTypes.DYNAMIC_PROPERTY_PLUGIN
      } else if (pluginType === openbis.PluginType.ENTITY_VALIDATION) {
        objectType = objectTypes.ENTITY_VALIDATION_PLUGIN
      } else {
        throw new Error('Unsupported plugin type: ' + pluginType)
      }

      return (
        <LinkToObject
          page={pages.TOOLS}
          object={{
            type: objectType,
            id: pluginName
          }}
        >
          {pluginName}
        </LinkToObject>
      )
    } else {
      return null
    }
  }
}

export default PluginLink
