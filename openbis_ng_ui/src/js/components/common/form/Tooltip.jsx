import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import MaterialTooltip from '@material-ui/core/Tooltip'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  title: {
    fontSize: theme.typography.body2.fontSize
  }
})

class Tooltip extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'Tooltip.render')

    const { children, classes, title, delay = 1000 } = this.props

    return (
      <MaterialTooltip
        enterDelay={delay}
        title={<span className={classes.title}>{title}</span>}
      >
        {children}
      </MaterialTooltip>
    )
  }
}

export default withStyles(styles)(Tooltip)
