import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TableCell from '@material-ui/core/TableCell'
import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  multiselect: {
    backgroundColor: 'inherit',
    paddingTop: theme.spacing(1),
    paddingBottom: theme.spacing(1),
    paddingLeft: theme.spacing(2),
    paddingRight: 0,
    position: 'sticky',
    left: 0,
    zIndex: 100
  },
  checkbox: {
    display: 'inline-block'
  }
})

class GridMultiselectCell extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'GridMultiselectCell.render')

    const { value, onClick, classes } = this.props

    return (
      <TableCell classes={{ root: classes.multiselect }}>
        <div className={classes.checkbox}>
          <CheckboxField value={value} onClick={onClick} />
        </div>
      </TableCell>
    )
  }
}

export default withStyles(styles)(GridMultiselectCell)
