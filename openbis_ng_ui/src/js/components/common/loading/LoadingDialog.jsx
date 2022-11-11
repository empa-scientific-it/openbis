import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Modal from '@material-ui/core/Modal'
import CircularProgress from '@material-ui/core/CircularProgress'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  modal: {
    backgroundColor: theme.palette.background.paper + ' !important',
    opacity: 0.6
  },
  icon: {
    position: 'absolute',
    top: '20%',
    left: 'calc(50% - 20px)',
    zIndex: 1001,
    outline: 'none'
  }
})

class LoadingDialog extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'LoadingDialog.render')

    const { loading, classes } = this.props

    return (
      <Modal open={loading} BackdropProps={{ className: classes.modal }}>
        <div className={classes.icon}>
          <CircularProgress />
        </div>
      </Modal>
    )
  }
}

export default withStyles(styles)(LoadingDialog)
