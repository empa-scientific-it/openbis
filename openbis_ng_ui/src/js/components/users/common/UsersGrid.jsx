import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import UserLink from '@src/js/components/common/link/UserLink.jsx'
import ids from '@src/js/common/consts/ids.js'
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
      <Grid
        settingsId={{
          webAppId: ids.WEB_APP_ID,
          gridId: id
        }}
        controllerRef={controllerRef}
        header={messages.get(messages.USERS)}
        columns={[
          {
            name: 'userId',
            label: messages.get(messages.USER_ID),
            sort: 'asc',
            getValue: ({ row }) => row.userId.value,
            renderValue: ({ value }) => {
              return <UserLink userId={value} />
            }
          },
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
          }
        ]}
        rows={rows}
        selectedRowId={selectedRowId}
        onSelectedRowChange={onSelectedRowChange}
      />
    )
  }
}

export default _.flow(withStyles(styles))(UsersGrid)
