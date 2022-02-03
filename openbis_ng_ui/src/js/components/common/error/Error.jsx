import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import ErrorDialog from '@src/js/components/common/error/ErrorDialog.jsx'
import logger from '@src/js/common/logger.js'

const styles = {
  container: {
    height: '100%'
  }
}

class Error extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'Error.render')

    const { classes } = this.props

    return (
      <div className={classes.container}>
        {this.props.error && (
          <ErrorDialog
            error={this.props.error}
            onClose={this.props.errorClosed}
          />
        )}
        {this.props.children}
      </div>
    )
  }
}

export default withStyles(styles)(Error)
