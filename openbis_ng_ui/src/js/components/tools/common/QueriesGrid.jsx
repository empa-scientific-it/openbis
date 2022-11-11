import React from 'react'
import GridWithOpenbis from '@src/js/components/common/grid/GridWithOpenbis.jsx'
import GridExportOptions from '@src/js/components/common/grid/GridExportOptions.js'
import QueryLink from '@src/js/components/common/link/QueryLink.jsx'
import UserLink from '@src/js/components/common/link/UserLink.jsx'
import QueryType from '@src/js/components/common/dto/QueryType.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

class QueriesGrid extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'QueriesGrid.render')

    const { id, rows, selectedRowId, onSelectedRowChange, controllerRef } =
      this.props

    return (
      <GridWithOpenbis
        id={id}
        settingsId={id}
        controllerRef={controllerRef}
        header={messages.get(messages.QUERIES)}
        sort='name'
        columns={[
          {
            name: 'name',
            label: messages.get(messages.NAME),
            getValue: ({ row }) => row.name.value,
            renderValue: ({ value }) => {
              return <QueryLink queryName={value} />
            }
          },
          {
            name: 'description',
            label: messages.get(messages.DESCRIPTION),
            getValue: ({ row }) => row.description.value
          },
          {
            name: 'database',
            label: messages.get(messages.DATABASE),
            getValue: ({ row }) => row.database.value
          },
          {
            name: 'queryType',
            label: messages.get(messages.QUERY_TYPE),
            getValue: ({ row }) => new QueryType(row.queryType.value).getLabel()
          },
          {
            name: 'entityTypeCodePattern',
            label: messages.get(messages.ENTITY_TYPE_PATTERN),
            getValue: ({ row }) => row.entityTypeCodePattern.value
          },
          {
            name: 'publicFlag',
            label: messages.get(messages.PUBLIC),
            getValue: ({ row }) => row.publicFlag.value
          },
          {
            name: 'registrator',
            label: messages.get(messages.REGISTRATOR),
            getValue: ({ row }) => row.registrator.value,
            renderValue: ({ value }) => {
              return <UserLink userId={value} />
            }
          }
        ]}
        rows={rows}
        exportable={{
          fileFormat: GridExportOptions.TSV_FILE_FORMAT,
          filePrefix: 'queries'
        }}
        selectable={true}
        selectedRowId={selectedRowId}
        onSelectedRowChange={onSelectedRowChange}
      />
    )
  }
}

export default QueriesGrid
