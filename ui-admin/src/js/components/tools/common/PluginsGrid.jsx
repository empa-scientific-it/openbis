import React from 'react'
import GridWithOpenbis from '@src/js/components/common/grid/GridWithOpenbis.jsx'
import GridExportOptions from '@src/js/components/common/grid/GridExportOptions.js'
import GridUtil from '@src/js/components/common/grid/GridUtil.js'
import PluginLink from '@src/js/components/common/link/PluginLink.jsx'
import EntityKind from '@src/js/components/common/dto/EntityKind.js'
import openbis from '@src/js/services/openbis.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

class PluginsGrid extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'PluginsGrid.render')

    const {
      id,
      pluginType,
      rows,
      selectedRowId,
      onSelectedRowChange,
      controllerRef
    } = this.props

    return (
      <GridWithOpenbis
        id={id}
        settingsId={id}
        controllerRef={controllerRef}
        header={this.getHeader()}
        sort='name'
        columns={[
          {
            name: 'name',
            label: messages.get(messages.NAME),
            getValue: ({ row }) => row.name.value,
            renderValue: ({ row }) => {
              return (
                <PluginLink
                  pluginName={row.name.value}
                  pluginType={pluginType}
                />
              )
            }
          },
          {
            name: 'description',
            label: messages.get(messages.DESCRIPTION),
            getValue: ({ row }) => row.description.value
          },
          {
            name: 'pluginKind',
            label: messages.get(messages.PLUGIN_KIND),
            getValue: ({ row }) => row.pluginKind.value
          },
          {
            name: 'entityKind',
            label: messages.get(messages.ENTITY_KIND),
            getValue: ({ row }) => {
              return row.entityKind.value
                ? new EntityKind(row.entityKind.value).getLabel()
                : '(' + messages.get(messages.ALL) + ')'
            }
          },
          GridUtil.registratorColumn({ path: 'registrator.value' }),
          GridUtil.registrationDateColumn({ path: 'registrationDate.value' })
        ]}
        rows={rows}
        exportable={this.getExportable()}
        selectable={true}
        selectedRowId={selectedRowId}
        onSelectedRowChange={onSelectedRowChange}
      />
    )
  }

  getHeader() {
    const { pluginType } = this.props

    if (pluginType === openbis.PluginType.DYNAMIC_PROPERTY) {
      return messages.get(messages.DYNAMIC_PROPERTY_PLUGINS)
    } else if (pluginType === openbis.PluginType.ENTITY_VALIDATION) {
      return messages.get(messages.ENTITY_VALIDATION_PLUGINS)
    }
  }

  getExportable() {
    const { pluginType } = this.props

    if (pluginType === openbis.PluginType.DYNAMIC_PROPERTY) {
      return {
        fileFormat: GridExportOptions.FILE_FORMAT.TSV,
        filePrefix: 'dynamic-property-plugins'
      }
    } else if (pluginType === openbis.PluginType.ENTITY_VALIDATION) {
      return {
        fileFormat: GridExportOptions.FILE_FORMAT.TSV,
        filePrefix: 'entity-validation-plugins'
      }
    }
  }
}

export default PluginsGrid
