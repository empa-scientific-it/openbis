import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import GridWithOpenbis from '@src/js/components/common/grid/GridWithOpenbis.jsx'
import GridExportOptions from '@src/js/components/common/grid/GridExportOptions.js'
import GridUtil from '@src/js/components/common/grid/GridUtil.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class UsersGrid extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
  }

  render() {
    logger.log(logger.DEBUG, 'UsersGrid.render')

    const { id, rows, selectedRowId, onSelectedRowChange, controllerRef } =
      this.props

    return (
      <GridWithOpenbis
        id={id}
        settingsId={id}
        controllerRef={controllerRef}
        header={messages.get(messages.USERS)}
        sort='userId'
        columns={[
          GridUtil.userColumn({
            name: 'userId',
            label: messages.get(messages.USER_ID),
            path: 'userId.value'
          }),
          {
            name: 'firstName',
            label: messages.get(messages.FIRST_NAME),
            getValue: ({ row }) => row.firstName.value
          },
          {
            name: 'lastName',
            label: messages.get(messages.LAST_NAME),
            getValue: ({ row }) => row.lastName.value
          },
          {
            name: 'email',
            label: messages.get(messages.EMAIL),
            nowrap: true,
            getValue: ({ row }) => row.email.value
          },
          {
            name: 'space',
            label: messages.get(messages.HOME_SPACE),
            getValue: ({ row }) => row.space.value
          },
          {
            name: 'active',
            label: messages.get(messages.ACTIVE),
            getValue: ({ row }) => row.active.value
          },
          GridUtil.registratorColumn({ path: 'registrator.value' }),
          GridUtil.registrationDateColumn({ path: 'registrationDate.value' })
        ]}
        rows={rows}
        exportable={{
          fileFormat: GridExportOptions.FILE_FORMAT.TSV,
          filePrefix: 'users'
        }}
        selectable={true}
        selectedRowId={selectedRowId}
        onSelectedRowChange={onSelectedRowChange}
      />
    )
  }
}

export default _.flow(withStyles(styles))(UsersGrid)
