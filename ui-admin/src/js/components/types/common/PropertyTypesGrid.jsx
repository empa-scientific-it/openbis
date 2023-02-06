import React from 'react'
import GridWithOpenbis from '@src/js/components/common/grid/GridWithOpenbis.jsx'
import GridExportOptions from '@src/js/components/common/grid/GridExportOptions.js'
import EntityTypeLink from '@src/js/components/common/link/EntityTypeLink.jsx'
import VocabularyTypeLink from '@src/js/components/common/link/VocabularyTypeLink.jsx'
import PropertyTypesGridUsagesCell from '@src/js/components/types/common/PropertyTypesGridUsagesCell.jsx'
import PropertyTypesGridXMLCell from '@src/js/components/types/common/PropertyTypesGridXMLCell.jsx'
import openbis from '@src/js/services/openbis.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

class PropertyTypesGrid extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'PropertyTypesGrid.render')

    const { id, rows, selectedRowId, onSelectedRowChange, controllerRef } =
      this.props

    return (
      <GridWithOpenbis
        id={id}
        settingsId={id}
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
            getValue: ({ row }) => row.vocabulary,
            renderValue: ({ row }) => (
              <VocabularyTypeLink vocabularyCode={row.vocabulary} />
            )
          },
          {
            name: 'materialType',
            label: messages.get(messages.MATERIAL_TYPE),
            getValue: ({ row }) => row.materialType,
            renderValue: ({ row }) => (
              <EntityTypeLink
                typeKind={openbis.EntityKind.MATERIAL}
                typeCode={row.materialType}
              />
            )
          },
          {
            name: 'sampleType',
            label: messages.get(messages.OBJECT_TYPE),
            getValue: ({ row }) => row.sampleType,
            renderValue: ({ row }) => (
              <EntityTypeLink
                typeKind={openbis.EntityKind.SAMPLE}
                typeCode={row.sampleType}
              />
            )
          },
          {
            name: 'schema',
            label: messages.get(messages.XML_SCHEMA),
            getValue: ({ row }) => row.schema,
            renderValue: ({ row }) => {
              return <PropertyTypesGridXMLCell value={row.schema} />
            }
          },
          {
            name: 'transformation',
            label: messages.get(messages.XSLT_SCRIPT),
            getValue: ({ row }) => row.transformation,
            renderValue: ({ row }) => {
              return <PropertyTypesGridXMLCell value={row.transformation} />
            }
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
        exportable={{
          fileFormat: GridExportOptions.TSV_FILE_FORMAT,
          filePrefix: 'property-types'
        }}
        selectable={true}
        selectedRowId={selectedRowId}
        onSelectedRowChange={onSelectedRowChange}
      />
    )
  }
}

export default PropertyTypesGrid
