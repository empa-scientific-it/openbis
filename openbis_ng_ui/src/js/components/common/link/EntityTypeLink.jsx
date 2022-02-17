import React from 'react'
import LinkToObject from '@src/js/components/common/form/LinkToObject.jsx'
import pages from '@src/js/common/consts/pages.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@src/js/services/openbis.js'
import logger from '@src/js/common/logger.js'

class EntityTypeLink extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'EntityTypeLink.render')

    const { typeCode, typeKind } = this.props

    if (typeCode && typeKind) {
      let objectType = null

      if (typeKind === openbis.EntityKind.EXPERIMENT) {
        objectType = objectTypes.COLLECTION_TYPE
      } else if (typeKind === openbis.EntityKind.SAMPLE) {
        objectType = objectTypes.OBJECT_TYPE
      } else if (typeKind === openbis.EntityKind.DATA_SET) {
        objectType = objectTypes.DATA_SET_TYPE
      } else if (typeKind === openbis.EntityKind.MATERIAL) {
        objectType = objectTypes.MATERIAL_TYPE
      } else {
        throw new Error('Unsupported type kind: ' + typeKind)
      }

      return (
        <LinkToObject
          page={pages.TYPES}
          object={{
            type: objectType,
            id: typeCode
          }}
        >
          {typeCode}
        </LinkToObject>
      )
    } else {
      return null
    }
  }
}

export default EntityTypeLink
