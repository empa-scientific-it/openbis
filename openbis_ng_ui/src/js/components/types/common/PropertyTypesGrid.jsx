import _ from 'lodash'
import React from 'react'
import GridWithSettings from '@src/js/components/common/grid/GridWithSettings.jsx'
import PropertyTypeLink from '@src/js/components/common/link/PropertyTypeLink.jsx'
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
            getValue: ({ row }) => row.code,
            renderValue: ({ row }) => {
              return <PropertyTypeLink propertyTypeCode={row.code} />
            }
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
            name: 'metaData',
            label: messages.get(messages.META_DATA),
            getValue: ({ row }) =>
              _.isEmpty(row.metaData) ? null : JSON.stringify(row.metaData),
            renderValue: ({ value }) => {
              return <pre>{value}</pre>
            }
          },
          {
            name: 'internal',
            label: messages.get(messages.INTERNAL),
            getValue: ({ row }) => row.internal
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
