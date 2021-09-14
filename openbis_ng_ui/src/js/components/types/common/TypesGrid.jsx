import React from 'react'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import TypeLink from '@src/js/components/common/link/TypeLink.jsx'
import PluginLink from '@src/js/components/common/link/PluginLink.jsx'
import openbis from '@src/js/services/openbis.js'
import ids from '@src/js/common/consts/ids.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

class TypesGrid extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'TypesGrid.render')

    const { id, rows, selectedRowId, onSelectedRowChange, controllerRef } =
      this.props

    return (
      <Grid
        settingsId={{
          webAppId: ids.WEB_APP_ID,
          gridId: id
        }}
        controllerRef={controllerRef}
        header={this.getHeader()}
        columns={this.getColumns()}
        rows={rows}
        selectedRowId={selectedRowId}
        onSelectedRowChange={onSelectedRowChange}
      />
    )
  }

  getHeader() {
    const { kind } = this.props

    if (kind === openbis.EntityKind.EXPERIMENT) {
      return messages.get(messages.COLLECTION_TYPES)
    } else if (kind === openbis.EntityKind.SAMPLE) {
      return messages.get(messages.OBJECT_TYPES)
    } else if (kind === openbis.EntityKind.DATA_SET) {
      return messages.get(messages.DATA_SET_TYPES)
    } else if (kind === openbis.EntityKind.MATERIAL) {
      return messages.get(messages.MATERIAL_TYPES)
    }
  }

  getColumns() {
    const { kind } = this.props
    const columns = []

    columns.push({
      name: 'code',
      label: messages.get(messages.CODE),
      sort: 'asc',
      getValue: ({ row }) => row.code,
      renderValue: ({ row }) => {
        return <TypeLink typeCode={row.code} typeKind={kind} />
      }
    })

    columns.push({
      name: 'description',
      label: messages.get(messages.DESCRIPTION),
      getValue: ({ row }) => row.description,
      renderValue: ({ value, classes }) => (
        <span className={classes.wrap}>{value}</span>
      )
    })

    columns.push({
      name: 'validationPlugin',
      label: messages.get(messages.VALIDATION_PLUGIN),
      getValue: ({ row }) => row.validationPlugin,
      renderValue: ({ value }) => {
        return (
          <PluginLink
            pluginName={value}
            pluginType={openbis.PluginType.ENTITY_VALIDATION}
          />
        )
      }
    })

    if (kind === openbis.EntityKind.SAMPLE) {
      columns.push({
        name: 'generatedCodePrefix',
        label: messages.get(messages.GENERATED_CODE_PREFIX),
        getValue: ({ row }) => row.generatedCodePrefix
      })

      columns.push({
        name: 'autoGeneratedCode',
        label: messages.get(messages.GENERATE_CODES),
        getValue: ({ row }) => row.autoGeneratedCode
      })

      columns.push({
        name: 'subcodeUnique',
        label: messages.get(messages.SUBCODES_UNIQUE),
        getValue: ({ row }) => row.subcodeUnique
      })
    }

    if (kind === openbis.EntityKind.DATA_SET) {
      columns.push({
        name: 'mainDataSetPattern',
        label: messages.get(messages.MAIN_DATA_SET_PATTERN),
        getValue: ({ row }) => row.mainDataSetPattern
      })

      columns.push({
        name: 'mainDataSetPath',
        label: messages.get(messages.MAIN_DATA_SET_PATH),
        getValue: ({ row }) => row.mainDataSetPath
      })

      columns.push({
        name: 'disallowDeletion',
        label: messages.get(messages.DISALLOW_DELETION),
        getValue: ({ row }) => row.disallowDeletion
      })
    }

    return columns
  }
}

export default TypesGrid
