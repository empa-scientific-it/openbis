import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import Typography from '@material-ui/core/Typography'
import EntityTypeFormPreviewProperty from '@src/js/components/types/form/entitytype/EntityTypeFormPreviewProperty.jsx'
import EntityTypeFormPreviewPropertyWrapper from './EntityTypeFormPreviewPropertyWrapper.js'

export default class EntityTypeFormPreviewSectionWrapper extends BaseWrapper {
  getName() {
    return this.findComponent(Typography).filter({ 'data-part': 'name' })
  }

  getProperties() {
    const properties = []
    this.findComponent(EntityTypeFormPreviewProperty).forEach(
      propertyWrapper => {
        properties.push(
          new EntityTypeFormPreviewPropertyWrapper(propertyWrapper)
        )
      }
    )
    return properties
  }

  click() {
    this.wrapper.instance().handleClick({
      stopPropagation: () => {}
    })
  }

  toJSON() {
    const name = this.getName().text().trim()
    return {
      name: name.length > 0 ? name : null,
      properties: this.getProperties().map(property => property.toJSON())
    }
  }
}
