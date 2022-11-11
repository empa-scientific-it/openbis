import React from 'react'
import Modal from '@material-ui/core/Modal'
import CircularProgress from '@material-ui/core/CircularProgress'
import logger from '@src/js/common/logger.js'

class GridExportLoading extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'GridExportLoading.render')
    const { loading } = this.props
    return (
      <Modal open={loading}>
        <CircularProgress />
      </Modal>
    )
  }
}

export default GridExportLoading
