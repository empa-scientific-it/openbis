import React from 'react'
import Grid from '@src/js/components/common/grid/Grid.jsx'
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
        columns={
          [
            /*
          {
            name: 'eventType',
            label: messages.get(messages.NAME),
            getValue: ({ row }) => row.name.value,
            renderValue: ({ row }) => {
              return (
                <PluginLink
                  pluginName={row.name.value}
                  pluginType={pluginType}
                />
              )
            }
          },
          {
            name: 'description',
            label: messages.get(messages.DESCRIPTION),
            getValue: ({ row }) => row.description.value
          },
          {
            name: 'pluginKind',
            label: messages.get(messages.PLUGIN_KIND),
            getValue: ({ row }) => row.pluginKind.value
          },
          {
            name: 'entityKind',
            label: messages.get(messages.ENTITY_KIND),
            getValue: ({ row }) => {
              return row.entityKind.value
                ? new EntityKind(row.entityKind.value).getLabel()
                : '(' + messages.get(messages.ALL) + ')'
            }
          },
          {
            name: 'registrator',
            label: messages.get(messages.REGISTRATOR),
            getValue: ({ row }) => row.registrator.value,
            renderValue: ({ value }) => {
              return <UserLink userId={value} />
            }
          }
          */
          ]
        }
        rows={[]}
        selectedRowId={null}
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
