import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TableRow from '@material-ui/core/TableRow'
import TableCell from '@material-ui/core/TableCell'
import logger from '@src/js/common/logger.js'

const styles = () => ({
  stickyCell: {
    padding: 0,
    position: 'sticky',
    left: 0
  },
  contentWrapper: {
    width: 0,
    overflow: 'visible'
  },
  content: {
    width: '750px'
  },
  remainingCell: {
    padding: 0
  }
})

class GridRowFullWidth extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'GridRowFullWidth.render')

    const {
      multiselectable,
      columns,
      selected = false,
      children,
      styles = {},
      classes
    } = this.props

    return (
      <TableRow selected={selected}>
        <TableCell
          colSpan={1 + (multiselectable ? 1 : 0)}
          classes={{
            root: `${classes.stickyCell} ${styles.cell}`
          }}
        >
          <div className={classes.contentWrapper}>
            <div className={`${classes.content} ${styles.content}`}>
              {children}
            </div>
          </div>
        </TableCell>
        {columns.length > 1 && (
          <TableCell
            colSpan={columns.length - 1 + (multiselectable ? 1 : 0)}
            classes={{
              root: `${classes.remainingCell} ${styles.cell}`
            }}
          ></TableCell>
        )}
      </TableRow>
    )
  }
}

export default withStyles(styles)(GridRowFullWidth)
