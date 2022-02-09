import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import EntityTypeFormParametersType from '@src/js/components/types/form/entitytype/EntityTypeFormParametersType.jsx'
import EntityTypeFormParametersProperty from '@src/js/components/types/form/entitytype/EntityTypeFormParametersProperty.jsx'
import EntityTypeFormParametersSection from '@src/js/components/types/form/entitytype/EntityTypeFormParametersSection.jsx'
import EntityTypeFormParametersTypeWrapper from './EntityTypeFormParametersTypeWrapper.js'
import EntityTypeFormParametersPropertyWrapper from './EntityTypeFormParametersPropertyWrapper.js'
import EntityTypeFormParametersSectionWrapper from './EntityTypeFormParametersSectionWrapper.js'

export default class EntityTypeFormParametersWrapper extends BaseWrapper {
  getType() {
    return new EntityTypeFormParametersTypeWrapper(
      this.findComponent(EntityTypeFormParametersType)
    )
  }

  getProperty() {
    return new EntityTypeFormParametersPropertyWrapper(
      this.findComponent(EntityTypeFormParametersProperty)
    )
  }

  getSection() {
    return new EntityTypeFormParametersSectionWrapper(
      this.findComponent(EntityTypeFormParametersSection)
    )
  }

  toJSON() {
    return {
      type: this.getType().toJSON(),
      property: this.getProperty().toJSON(),
      section: this.getSection().toJSON()
    }
  }
}
