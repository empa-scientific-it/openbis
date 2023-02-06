import React from 'react'
import ImportType from '@src/js/components/tools/form/import/ImportType.js'
import ImportAllForm from '@src/js/components/tools/form/import/all/ImportAllForm.jsx'
import logger from '@src/js/common/logger.js'

class ImportForm extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'ImportForm.render')

    const { object } = this.props

    if (object.id === ImportType.ALL) {
      return <ImportAllForm />
    } else {
      throw new Error('Unsupported import type: ' + object.type)
    }
  }
}

export default ImportForm
