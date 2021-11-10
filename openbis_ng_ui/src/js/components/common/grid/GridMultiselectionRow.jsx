import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TableRow from '@material-ui/core/TableRow'
import TableCell from '@material-ui/core/TableCell'
import Message from '@src/js/components/common/form/Message.jsx'
import Link from '@src/js/components/common/form/Link.jsx'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  cell: {
    paddingTop: theme.spacing(1),
    paddingBottom: theme.spacing(1),
    paddingLeft: theme.spacing(2) + 2,
    paddingRight: theme.spacing(2)
  },
  messages: {
    display: 'flex'
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

    if (!multiselectable) {
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
                {numberOfSelectedRows} selected row(s)
              </Message>
            </div>
            <div className={classes.message}>
              <Link onClick={onMultiselectionClear}>(clear selection)</Link>
            </div>
            {numberOfSelectedRowsNotVisible > 0 && (
              <div className={classes.message}>
                <Message type='warning'>
                  Some selected rows are not visible due to the chosen filtering
                  and paging.
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
