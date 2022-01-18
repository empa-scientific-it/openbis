import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TableRow from '@material-ui/core/TableRow'
import TableCell from '@material-ui/core/TableCell'
import Button from '@src/js/components/common/form/Button.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  cell: {
    paddingTop: theme.spacing(1),
    paddingBottom: theme.spacing(1),
    paddingLeft: theme.spacing(2) + 2,
    paddingRight: theme.spacing(2)
  },
  messages: {
    display: 'flex',
    alignItems: 'center'
  },
  message: {
    flex: '0 0 auto',
    paddingRight: theme.spacing(1)
  }
})

class GridMultiselectionRow extends React.PureComponent {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'GridMultiselectionRow.render')

    const {
      columns,
      rows,
      multiselectable,
      multiselectedRows,
      onMultiselectionClear,
      classes
    } = this.props

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
      <TableRow selected={true}>
        <TableCell
          colspan={columns.length + 1}
          classes={{
            root: classes.cell
          }}
        >
          <div className={classes.messages}>
            <div className={classes.message}>
              <Message type='info'>
                {messages.get(
                  messages.NUMBER_OF_SELECTED_ROWS,
                  numberOfSelectedRows
                )}
                :
              </Message>
            </div>
            <Button label='Delete' color='primary' />
            <span>&nbsp;&nbsp;</span>
            <Button label='Move' color='primary' />
            <span>&nbsp;&nbsp;</span>
            <Button
              label='Deselect'
              onClick={onMultiselectionClear}
              color='secondary'
            />
            <span>&nbsp;&nbsp;</span>
            <span>&nbsp;&nbsp;</span>
            <span>&nbsp;&nbsp;</span>
            {numberOfSelectedRowsNotVisible > 0 && (
              <div className={classes.message}>
                <Message type='warning'>
                  {messages.get(
                    messages.SELECTED_ROWS_NOT_VISIBLE_DUE_TO_FILTERING_AND_PAGING
                  )}
                </Message>
              </div>
            )}
          </div>
        </TableCell>
      </TableRow>
    )
  }
}

export default withStyles(styles)(GridMultiselectionRow)
