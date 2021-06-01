import React from 'react'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import UserLink from '@src/js/components/common/link/UserLink.jsx'
import openbis from '@src/js/services/openbis.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

class HistoryGrid extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'HistoryGrid.render')

    const {
      id,
      rows,
      selectedRowId,
      onSelectedRowChange,
      controllerRef
    } = this.props

    return (
      <Grid
        id={id}
        controllerRef={controllerRef}
        header={this.getHeader()}
        columns={[
          {
            name: 'eventType',
            label: messages.get(messages.EVENT_TYPE),
            getValue: ({ row }) => row.eventType.value
          },
          {
            name: 'entityType',
            label: messages.get(messages.ENTITY_TYPE),
            getValue: ({ row }) => row.entityType.value
          },
          {
            name: 'entitySpace',
            label: messages.get(messages.ENTITY_SPACE),
            getValue: ({ row }) => row.entitySpace.value
          },
          {
            name: 'entityProject',
            label: messages.get(messages.ENTITY_PROJECT),
            getValue: ({ row }) => row.entityProject.value
          },
          {
            name: 'entityRegistrator',
            label: messages.get(messages.ENTITY_REGISTRATOR),
            getValue: ({ row }) => row.entityRegistrator.value
          },
          {
            name: 'entityRegistrationDate',
            label: messages.get(messages.ENTITY_REGISTRATION_DATE),
            getValue: ({ row }) => row.entityRegistrationDate.value
          },
          {
            name: 'identifier',
            label: messages.get(messages.IDENTIFIER),
            getValue: ({ row }) => row.identifier.value
          },
          {
            name: 'description',
            label: messages.get(messages.DESCRIPTION),
            getValue: ({ row }) => row.description.value
          },
          {
            name: 'reason',
            label: messages.get(messages.REASON),
            getValue: ({ row }) => row.reason.value
          },
          {
            name: 'content',
            label: messages.get(messages.CONTENT),
            getValue: ({ row }) => row.content.value
          },
          {
            name: 'registrator',
            label: messages.get(messages.USER),
            getValue: ({ row }) => row.registrator.value,
            renderValue: ({ value }) => {
              return <UserLink userId={value} />
            }
          },
          {
            name: 'registrationDate',
            label: messages.get(messages.DATE),
            getValue: ({ row }) => row.registrationDate.value
          }
        ]}
        rows={rows}
        selectedRowId={selectedRowId}
        onSelectedRowChange={onSelectedRowChange}
      />
    )
  }

  getHeader() {
    const { eventType } = this.props

    if (eventType === openbis.EventType.DELETION) {
      return messages.get(messages.DELETIONS)
    } else if (eventType === openbis.EventType.FREEZING) {
      return messages.get(messages.FREEZES)
    }
  }
}

export default HistoryGrid
