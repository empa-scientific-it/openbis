import React from 'react'
import LoadingDialog from '@src/js/components/common/loading/LoadingDialog.jsx'
import logger from '@src/js/common/logger.js'

class GridExportLoading extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'GridExportLoading.render')
    const { loading } = this.props
    return <LoadingDialog loading={loading} />
  }
}

export default GridExportLoading
