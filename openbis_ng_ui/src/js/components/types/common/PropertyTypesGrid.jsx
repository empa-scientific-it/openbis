import React from 'react'
import GridWithSettings from '@src/js/components/common/grid/GridWithSettings.jsx'
import PropertyTypesGridUsagesCell from '@src/js/components/types/common/PropertyTypesGridUsagesCell.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

class PropertyTypesGrid extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'PropertyTypesGrid.render')

    const { id, rows, selectedRowId, onSelectedRowChange, controllerRef } =
      this.props

    return (
      <GridWithSettings
        id={id}
        controllerRef={controllerRef}
        header={messages.get(messages.PROPERTY_TYPES)}
        columns={[
          {
            name: 'code',
            label: messages.get(messages.CODE),
            getValue: ({ row }) => row.code
          },
          {
            name: 'label',
            label: messages.get(messages.LABEL),
            getValue: ({ row }) => row.label
          },
          {
            name: 'description',
            label: messages.get(messages.DESCRIPTION),
            getValue: ({ row }) => row.description
          },
          {
            name: 'dataType',
            label: messages.get(messages.DATA_TYPE),
            getValue: ({ row }) => row.dataType
          },
          {
            name: 'vocabulary',
            label: messages.get(messages.VOCABULARY_TYPE),
            getValue: ({ row }) => row.vocabulary
          },
          {
            name: 'materialType',
            label: messages.get(messages.MATERIAL_TYPE),
            getValue: ({ row }) => row.materialType
          },
          {
            name: 'sampleType',
            label: messages.get(messages.OBJECT_TYPE),
            getValue: ({ row }) => row.sampleType
          },
          {
            name: 'schema',
            label: messages.get(messages.XML_SCHEMA),
            getValue: ({ row }) => row.schema
          },
          {
            name: 'transformation',
            label: messages.get(messages.XSLT_SCRIPT),
            getValue: ({ row }) => row.schema
          },
          {
            name: 'usages',
            label: messages.get(messages.USAGES),
            getValue: ({ row }) =>
              row.usages ? JSON.stringify(row.usages) : null,
            compareValue: ({ row1, row2, defaultCompare }) => {
              const value1 = row1.usages ? row1.usages.count : 0
              const value2 = row2.usages ? row2.usages.count : 0
              return defaultCompare(value1, value2)
            },
            renderValue: ({ row }) => {
              return <PropertyTypesGridUsagesCell value={row.usages} />
            }
          }
        ]}
        rows={rows}
        sort='code'
        selectable={true}
        selectedRowId={selectedRowId}
        onSelectedRowChange={onSelectedRowChange}
      />
    )
  }
}

export default PropertyTypesGrid
