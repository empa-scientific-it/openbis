import React from 'react'
import ErrorDialog from '@src/js/components/common/error/ErrorDialog.jsx'
import logger from '@src/js/common/logger.js'

class GridExportError extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'GridExportError.render')
    const { open, error, onClose } = this.props
    return <ErrorDialog open={open} error={error} onClose={onClose} />
  }
}

export default GridExportError
