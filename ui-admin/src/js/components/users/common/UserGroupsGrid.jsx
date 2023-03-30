import React from 'react'
import GridWithOpenbis from '@src/js/components/common/grid/GridWithOpenbis.jsx'
import GridExportOptions from '@src/js/components/common/grid/GridExportOptions.js'
import GridUtil from '@src/js/components/common/grid/GridUtil.js'
import UserGroupLink from '@src/js/components/common/link/UserGroupLink.jsx'
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
          GridUtil.registratorColumn({ path: 'registrator.value' }),
          GridUtil.registrationDateColumn({ path: 'registrationDate.value' }),
          GridUtil.modificationDateColumn({ path: 'modificationDate.value' })
        ]}
        rows={rows}
        exportable={{
          fileFormat: GridExportOptions.FILE_FORMAT.TSV,
          filePrefix: 'groups'
        }}
        selectable={true}
        selectedRowId={selectedRowId}
        onSelectedRowChange={onSelectedRowChange}
      />
    )
  }
}
