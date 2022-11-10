import React from 'react'
import GridWithOpenbis from '@src/js/components/common/grid/GridWithOpenbis.jsx'
import GridExportOptions from '@src/js/components/common/grid/GridExportOptions.js'
import VocabularyTypeLink from '@src/js/components/common/link/VocabularyTypeLink.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

class VocabularyTypesGrid extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'VocabularyTypesGrid.render')

    const { id, rows, selectedRowId, onSelectedRowChange, controllerRef } =
      this.props

    return (
      <GridWithOpenbis
        id={id}
        settingsId={id}
        controllerRef={controllerRef}
        header={messages.get(messages.VOCABULARY_TYPES)}
        columns={[
          {
            name: 'code',
            label: messages.get(messages.CODE),
            getValue: ({ row }) => row.code,
            renderValue: ({ row }) => {
              return <VocabularyTypeLink vocabularyCode={row.code} />
            }
          },
          {
            name: 'description',
            label: messages.get(messages.DESCRIPTION),
            getValue: ({ row }) => row.description
          },
          {
            name: 'urlTemplate',
            label: messages.get(messages.URL_TEMPLATE),
            getValue: ({ row }) => row.urlTemplate
          }
        ]}
        rows={rows}
        sort='code'
        exportable={{
          fileFormat: GridExportOptions.XLS_FILE_FORMAT,
          filePrefix: 'vocabulary-types'
        }}
        selectable={true}
        selectedRowId={selectedRowId}
        onSelectedRowChange={onSelectedRowChange}
      />
    )
  }
}

export default VocabularyTypesGrid
