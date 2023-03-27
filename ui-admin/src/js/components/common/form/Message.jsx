import React from 'react'
import Typography from '@material-ui/core/Typography'
import InfoIcon from '@material-ui/icons/Info'
import WarningIcon from '@material-ui/icons/Warning'
import LockIcon from '@material-ui/icons/Lock'
import CheckCircleIcon from '@material-ui/icons/CheckCircle'
import { withStyles } from '@material-ui/core/styles'
import util from '@src/js/common/util.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  message: {
    display: 'flex',
    '& svg': {
      marginRight: theme.spacing(1)
    },
    fontSize: theme.typography.body2.fontSize,
    color: theme.palette.text.primary
  },
  lock: {
    '& svg': {
      color: theme.palette.hint.main
    }
  },
  error: {
    '& svg': {
      color: theme.palette.error.main
    }
  },
  warning: {
    '& svg': {
      color: theme.palette.warning.main
    }
  },
  info: {
    '& svg': {
      color: theme.palette.info.main
    }
  },
  success: {
    '& svg': {
      color: theme.palette.success.main
    }
  }
})

class Message extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'Message.render')

    const { classes, styles = {}, children, type } = this.props

    return (
      <Typography
        component='div'
        className={util.classNames(classes.message, classes[type], styles.root)}
      >
        {this.renderIcon(type)}
        {children}
      </Typography>
    )
  }

  renderIcon(type) {
    if (type === 'success') {
      return <CheckCircleIcon fontSize='small' />
    } else if (type === 'info' || type === 'error') {
      return <InfoIcon fontSize='small' />
    } else if (type === 'warning') {
      return <WarningIcon fontSize='small' />
    } else if (type === 'lock') {
      return <LockIcon fontSize='small' />
    } else {
      return null
    }
  }
}

export default withStyles(styles)(Message)
