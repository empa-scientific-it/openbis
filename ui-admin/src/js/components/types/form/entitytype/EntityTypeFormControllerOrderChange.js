import _ from 'lodash'
import EntityTypeFormSelectionType from '@src/js/components/types/form/entitytype/EntityTypeFormSelectionType.js'

export default class EntityTypeFormControllerOrderChange {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  execute(type, params) {
    if (type === EntityTypeFormSelectionType.SECTION) {
      let { fromIndex, toIndex } = params
      this._handleOrderChangeSection(fromIndex, toIndex)
    } else if (type === EntityTypeFormSelectionType.PROPERTY) {
      let { fromSectionId, toSectionId, fromIndex, toIndex } = params
      this._handleOrderChangeProperty(
        fromSectionId,
        fromIndex,
        toSectionId,
        toIndex
      )
    }
  }

  _handleOrderChangeSection(fromIndex, toIndex) {
    let { sections } = this.context.getState()
    let newSections = Array.from(sections)
    let [section] = newSections.splice(fromIndex, 1)
    newSections.splice(toIndex, 0, section)

    this.context.setState(state => ({
      ...state,
      sections: newSections
    }))

    this.controller.changed(true)
  }

  _handleOrderChangeProperty(fromSectionId, fromIndex, toSectionId, toIndex) {
    if (fromSectionId === toSectionId) {
      let { sections } = this.context.getState()
      let sectionIndex = _.findIndex(sections, ['id', fromSectionId])
      let section = sections[sectionIndex]
      let newSection = {
        ...section,
        properties: Array.from(section.properties)
      }

      let [property] = newSection.properties.splice(fromIndex, 1)
      newSection.properties.splice(toIndex, 0, property)

      let newSections = Array.from(sections)
      newSections[sectionIndex] = newSection

      this.context.setState(state => ({
        ...state,
        sections: newSections
      }))

      this.controller.changed(true)
    } else {
      let { sections } = this.context.getState()
      let newSections = Array.from(sections)

      let fromSectionIndex = _.findIndex(sections, ['id', fromSectionId])
      let toSectionIndex = _.findIndex(sections, ['id', toSectionId])
      let fromSection = sections[fromSectionIndex]
      let toSection = sections[toSectionIndex]

      let newFromSection = {
        ...fromSection,
        properties: Array.from(fromSection.properties)
      }
      let newToSection = {
        ...toSection,
        properties: Array.from(toSection.properties)
      }

      let [property] = newFromSection.properties.splice(fromIndex, 1)
      newToSection.properties.splice(toIndex, 0, property)

      newSections[fromSectionIndex] = newFromSection
      newSections[toSectionIndex] = newToSection

      let { properties } = this.context.getState()
      let newProperties = Array.from(properties)

      let propertyIndex = _.findIndex(properties, ['id', property])
      let propertyObj = properties[propertyIndex]
      let newPropertyObj = {
        ...propertyObj,
        section: newToSection.id
      }
      newProperties[propertyIndex] = newPropertyObj

      this.context.setState(state => ({
        ...state,
        sections: newSections,
        properties: newProperties
      }))

      this.controller.changed(true)
    }
  }
}
