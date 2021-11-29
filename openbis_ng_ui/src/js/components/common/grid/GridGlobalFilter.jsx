import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TableCell from '@material-ui/core/TableCell'
import TextField from '@src/js/components/common/form/TextField.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  cell: {
    paddingTop: theme.spacing(1),
    paddingBottom: theme.spacing(1),
    paddingLeft: theme.spacing(2),
    paddingRight: theme.spacing(2)
  }
})

class GridGlobalFilter extends React.PureComponent {
  constructor(props) {
    super(props)
    this.handleGlobalFilterChange = this.handleGlobalFilterChange.bind(this)
  }

  handleGlobalFilterChange(event) {
    const { onGlobalFilterChange } = this.props
    if (onGlobalFilterChange) {
      onGlobalFilterChange(event.target.value)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'GridGlobalFilter.render')

    const { columns, globalFilter, classes } = this.props

    return (
      <TableCell classes={{ root: classes.cell }} colSpan={columns.length}>
        <TextField
          label={messages.get(messages.FILTER)}
          value={globalFilter}
          onChange={this.handleGlobalFilterChange}
          variant='standard'
        />
      </TableCell>
    )
  }
}

export default withStyles(styles)(GridGlobalFilter)
