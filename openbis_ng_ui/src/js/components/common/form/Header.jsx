import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Typography from '@material-ui/core/Typography'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  big: {
    paddingBottom: theme.spacing(2)
  },
  medium: {
    paddingBottom: theme.spacing(1)
  },
  small: {
    paddingBottom: 0
  }
})

class Header extends React.PureComponent {
  static defaultProps = {
    size: 'medium'
  }
  render() {
    logger.log(logger.DEBUG, 'Header.render')

    const { styles, classes, size } = this.props

    let variant = null
    let className = null

    if (size === 'big') {
      variant = 'h5'
      className = classes.big
    } else if (size === 'medium') {
      variant = 'h6'
      className = classes.medium
    } else if (size === 'small') {
      variant = 'subtitle1'
      className = classes.small
    }

    return (
      <Typography variant={variant} className={className} classes={styles}>
        {this.props.children}
      </Typography>
    )
  }
}

export default withStyles(styles)(Header)
