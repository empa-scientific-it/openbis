import React from 'react'
import logger from '@src/js/common/logger.js'

class ImportForm extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'ImportForm.render')

    return <div>Import Form</div>
  }
}

export default ImportForm
