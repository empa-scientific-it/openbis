import React from 'react'
import LinkToObject from '@src/js/components/common/form/LinkToObject.jsx'
import pages from '@src/js/common/consts/pages.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import logger from '@src/js/common/logger.js'

class VocabularyTypeLink extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'VocabularyTypeLink.render')

    const { vocabularyCode } = this.props

    if (vocabularyCode) {
      return (
        <LinkToObject
          page={pages.TYPES}
          object={{
            type: objectTypes.VOCABULARY_TYPE,
            id: vocabularyCode
          }}
        >
          {vocabularyCode}
        </LinkToObject>
      )
    } else {
      return null
    }
  }
}

export default VocabularyTypeLink
