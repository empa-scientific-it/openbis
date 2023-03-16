import React from 'react'
import GridWithOpenbis from '@src/js/components/common/grid/GridWithOpenbis.jsx'
import GridExportOptions from '@src/js/components/common/grid/GridExportOptions.js'
import VocabularyTypeLink from '@src/js/components/common/link/VocabularyTypeLink.jsx'
import UserLink from '@src/js/components/common/link/UserLink.jsx'
import date from '@src/js/common/date.js'
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
          },
          {
            name: 'registrator',
            label: messages.get(messages.REGISTRATOR),
            getValue: ({ row }) => row.registrator,
            renderValue: ({ value }) => {
              return <UserLink userId={value} />
            }
          },
          {
            name: 'registrationDate',
            label: messages.get(messages.REGISTRATION_DATE),
            getValue: ({ row }) => date.format(row.registrationDate)
          },
          {
            name: 'modificationDate',
            label: messages.get(messages.MODIFICATION_DATE),
            getValue: ({ row }) => date.format(row.modificationDate)
          }
        ]}
        rows={rows}
        sort='code'
        exportable={{
          fileFormat: GridExportOptions.XLS_FILE_FORMAT,
          filePrefix: 'vocabulary-types',
          fileContent: GridExportOptions.VOCABULARIES_CONTENT
        }}
        selectable={true}
        selectedRowId={selectedRowId}
        onSelectedRowChange={onSelectedRowChange}
      />
    )
  }
}

export default VocabularyTypesGrid
