import _ from 'lodash'
import EntityTypeFormSelectionType from '@src/js/components/types/form/entitytype/EntityTypeFormSelectionType.js'

export default class EntityTypeFormControllerRemove {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
    this.facade = controller.facade
    this.object = controller.object
  }

  executeRemove(confirmed = false) {
    const { selection } = this.context.getState()
    if (selection.type === EntityTypeFormSelectionType.SECTION) {
      this._handleRemoveSection(selection.params.id, confirmed)
    } else if (selection.type === EntityTypeFormSelectionType.PROPERTY) {
      this._handleRemoveProperty(selection.params.id, confirmed)
    }
  }

  executeCancel() {
    const { selection } = this.context.getState()
    if (selection.type === EntityTypeFormSelectionType.SECTION) {
      this.context.setState({
        removeSectionDialogOpen: false
      })
    } else if (selection.type === EntityTypeFormSelectionType.PROPERTY) {
      this.context.setState({
        removePropertyDialogOpen: false
      })
    }
  }

  async _handleRemoveSection(sectionId, confirmed) {
    const { sections, properties } = this.context.getState()

    const sectionIndex = sections.findIndex(section => section.id === sectionId)
    const section = sections[sectionIndex]

    if (confirmed) {
      this.context.setState({
        removeSectionDialogOpen: false
      })
    } else {
      const existingProperties = []

      section.properties.map(propertyId => {
        const property = _.find(properties, ['id', propertyId])
        if (property && property.original) {
          existingProperties.push(property.code.value)
        }
      })

      if (existingProperties.length > 0) {
        try {
          this.context.setState({
            loading: true
          })

          const usagesMap = await this.facade.loadPropertyUsages(
            this.object,
            existingProperties
          )

          const totalUsages = _.reduce(
            usagesMap,
            (totalUsages, propertyUsages) => {
              return totalUsages + propertyUsages
            },
            0
          )

          const newSection = {
            ...section,
            usages: totalUsages
          }

          const newSections = Array.from(sections)
          newSections[sectionIndex] = newSection

          this.context.setState({
            removeSectionDialogOpen: true,
            sections: newSections
          })
        } finally {
          this.context.setState({
            loading: false
          })
        }

        return
      }
    }

    const newProperties = Array.from(properties)
    _.remove(
      newProperties,
      property => section.properties.indexOf(property.id) !== -1
    )

    const newSections = Array.from(sections)
    newSections.splice(sectionIndex, 1)

    this.context.setState(state => ({
      ...state,
      sections: newSections,
      properties: newProperties,
      selection: null
    }))

    this.controller.changed(true)
  }

  async _handleRemoveProperty(propertyId, confirmed) {
    const { sections, properties } = this.context.getState()

    const propertyIndex = properties.findIndex(
      property => property.id === propertyId
    )
    const property = properties[propertyIndex]

    if (confirmed) {
      this.context.setState({
        removePropertyDialogOpen: false
      })
    } else if (property.original) {
      try {
        this.context.setState({
          loading: true
        })

        const usagesMap = await this.facade.loadPropertyUsages(this.object, [
          property.code.value
        ])

        const newProperty = {
          ...property,
          usages: usagesMap[property.code.value]
        }

        const newProperties = Array.from(properties)
        newProperties[propertyIndex] = newProperty

        this.context.setState({
          removePropertyDialogOpen: true,
          properties: newProperties
        })
      } finally {
        this.context.setState({
          loading: false
        })
      }

      return
    }

    const newProperties = Array.from(properties)
    newProperties.splice(propertyIndex, 1)

    let sectionIndex = sections.findIndex(
      section => section.id === property.section
    )
    let section = sections[sectionIndex]
    let newSection = {
      ...section,
      properties: Array.from(section.properties)
    }
    _.remove(newSection.properties, property => property === propertyId)

    const newSections = Array.from(sections)
    newSections[sectionIndex] = newSection

    this.context.setState(state => ({
      ...state,
      sections: newSections,
      properties: newProperties,
      selection: null
    }))

    this.controller.changed(true)
  }
}
