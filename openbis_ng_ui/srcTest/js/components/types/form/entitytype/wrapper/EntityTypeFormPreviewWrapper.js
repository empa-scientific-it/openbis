import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import EntityTypeFormPreviewHeader from '@src/js/components/types/form/entitytype/EntityTypeFormPreviewHeader.jsx'
import EntityTypeFormPreviewSection from '@src/js/components/types/form/entitytype/EntityTypeFormPreviewSection.jsx'
import EntityTypeFormPreviewHeaderWrapper from './EntityTypeFormPreviewHeaderWrapper.js'
import EntityTypeFormPreviewSectionWrapper from './EntityTypeFormPreviewSectionWrapper.js'

export default class EntityTypeFormPreviewWrapper extends BaseWrapper {
  getHeader() {
    return new EntityTypeFormPreviewHeaderWrapper(
      this.findComponent(EntityTypeFormPreviewHeader)
    )
  }

  getSections() {
    const sections = []
    this.findComponent(EntityTypeFormPreviewSection).forEach(sectionWrapper => {
      sections.push(new EntityTypeFormPreviewSectionWrapper(sectionWrapper))
    })
    return sections
  }

  toJSON() {
    return {
      header: this.getHeader().toJSON(),
      sections: this.getSections().map(section => section.toJSON())
    }
  }
}
