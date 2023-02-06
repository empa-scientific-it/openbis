import React from 'react'
import LinkToObject from '@src/js/components/common/form/LinkToObject.jsx'
import pages from '@src/js/common/consts/pages.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import logger from '@src/js/common/logger.js'

class PropertyTypeLink extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'PropertyTypeLink.render')

    const { propertyTypeCode } = this.props

    if (propertyTypeCode) {
      return (
        <LinkToObject
          page={pages.TYPES}
          object={{
            type: objectTypes.PROPERTY_TYPE,
            id: propertyTypeCode
          }}
        >
          {propertyTypeCode}
        </LinkToObject>
      )
    } else {
      return null
    }
  }
}

export default PropertyTypeLink
