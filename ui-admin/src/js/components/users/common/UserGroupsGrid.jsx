import React from 'react'
import GridWithOpenbis from '@src/js/components/common/grid/GridWithOpenbis.jsx'
import GridExportOptions from '@src/js/components/common/grid/GridExportOptions.js'
import UserGroupLink from '@src/js/components/common/link/UserGroupLink.jsx'
import UserLink from '@src/js/components/common/link/UserLink.jsx'
import date from '@src/js/common/date.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

export default class GroupsGrid extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'GroupsGrid.render')

    const { id, rows, selectedRowId, onSelectedRowChange, controllerRef } =
      this.props

    return (
      <GridWithOpenbis
        id={id}
        settingsId={id}
        controllerRef={controllerRef}
        header={messages.get(messages.GROUPS)}
        sort='code'
        columns={[
          {
            name: 'code',
            label: messages.get(messages.CODE),
            getValue: ({ row }) => row.code.value,
            renderValue: ({ value }) => {
              if (value) {
                return <UserGroupLink groupCode={value} />
              } else {
                return null
              }
            }
          },
          {
            name: 'description',
            label: messages.get(messages.DESCRIPTION),
            getValue: ({ row }) => row.description.value
          },
          {
            name: 'registrator',
            label: messages.get(messages.REGISTRATOR),
            getValue: ({ row }) => row.registrator.value,
            renderValue: ({ value }) => {
              return <UserLink userId={value} />
            }
          },
          {
            name: 'registrationDate',
            label: messages.get(messages.REGISTRATION_DATE),
            getValue: ({ row }) => date.format(row.registrationDate.value)
          },
          {
            name: 'modificationDate',
            label: messages.get(messages.MODIFICATION_DATE),
            getValue: ({ row }) => date.format(row.modificationDate.value)
          }
        ]}
        rows={rows}
        exportable={{
          fileFormat: GridExportOptions.TSV_FILE_FORMAT,
          filePrefix: 'groups'
        }}
        selectable={true}
        selectedRowId={selectedRowId}
        onSelectedRowChange={onSelectedRowChange}
      />
    )
  }
}
