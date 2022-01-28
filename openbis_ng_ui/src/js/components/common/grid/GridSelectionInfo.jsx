import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import GridRowFullWidth from '@src/js/components/common/grid/GridRowFullWidth.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  cell: {},
  content: {
    paddingLeft: theme.spacing(2),
    paddingRight: theme.spacing(2),
    paddingTop: theme.spacing(1),
    paddingBottom: theme.spacing(1),
    display: 'flex',
    alignItems: 'center'
  },
  message: {
    flex: '0 0 auto',
    '&:first-child': {
      marginLeft: '2px',
      marginRight: theme.spacing(1)
    }
  },
  button: {
    marginRight: theme.spacing(1),
    '&:last-child': {
      marginRight: theme.spacing(2)
    }
  }
})

class GridSelectionInfo extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'GridSelectionInfo.render')

    const { columns, rows, multiselectable, multiselectedRows, classes } =
      this.props

    if (columns.length === 0 || !multiselectable) {
      return null
    }

    const numberOfSelectedRows = Object.keys(multiselectedRows).length

    if (numberOfSelectedRows === 0) {
      return null
    }

    const selectedRowsNotVisible = { ...multiselectedRows }
    rows.forEach(row => {
      delete selectedRowsNotVisible[row.id]
    })

    const numberOfSelectedRowsNotVisible = Object.keys(
      selectedRowsNotVisible
    ).length

    return (
      <GridRowFullWidth
        multiselectable={multiselectable}
        columns={columns}
        selected={true}
        styles={{ cell: classes.cell, content: classes.content }}
      >
        {this.renderNumberOfSelectedRows(numberOfSelectedRows)}
        {this.renderButtons()}
        {this.renderNumberOfSelectedRowsNotVisible(
          numberOfSelectedRowsNotVisible
        )}
      </GridRowFullWidth>
    )
  }

  renderNumberOfSelectedRows(numberOfSelectedRows) {
    const { classes } = this.props
    return (
      <div className={classes.message}>
        <Message type='info'>
          {messages.get(messages.NUMBER_OF_SELECTED_ROWS, numberOfSelectedRows)}
          :
        </Message>
      </div>
    )
  }

  renderNumberOfSelectedRowsNotVisible(numberOfSelectedRowsNotVisible) {
    const { classes } = this.props

    if (numberOfSelectedRowsNotVisible === 0) {
      return null
    }

    return (
      <div className={classes.message}>
        <Message type='warning'>
          {messages.get(
            messages.SELECTED_ROWS_NOT_VISIBLE_DUE_TO_FILTERING_AND_PAGING
          )}
        </Message>
      </div>
    )
  }

  renderButtons() {
    const { actions, onExecuteAction, onMultiselectionClear, classes } =
      this.props

    return (
      <div className={classes.buttons}>
        {actions &&
          actions.length > 0 &&
          actions.map(action => (
            <Button
              key={action.label}
              label={action.label}
              color='primary'
              onClick={() => onExecuteAction(action)}
              styles={{ root: classes.button }}
            />
          ))}
        <Button
          label={messages.get(messages.CLEAR_SELECTION)}
          onClick={onMultiselectionClear}
          color='secondary'
          styles={{ root: classes.button }}
        />
      </div>
    )
  }
}

export default withStyles(styles)(GridSelectionInfo)
