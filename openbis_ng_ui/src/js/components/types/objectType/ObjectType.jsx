import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import { Resizable } from 're-resizable'
import ObjectTypePreview from './ObjectTypePreview.jsx'
import ObjectTypeParameters from './ObjectTypeParameters.jsx'
import ObjectTypeButtons from './ObjectTypeButtons.jsx'
import logger from '../../../common/logger.js'
import { facade, dto } from '../../../services/openbis.js'

const styles = theme => ({
  container: {
    height: '100%',
    display: 'flex',
    flexDirection: 'row'
  },
  content: {
    display: 'flex',
    flexDirection: 'column',
    height: '100%',
    flex: '1 1 auto'
  },
  preview: {
    height: '100%',
    flex: '1 1 auto',
    overflow: 'auto'
  },
  buttons: {
    flex: '0 0 auto'
  },
  parameters: {
    backgroundColor: theme.palette.action.selected,
    height: '100%',
    overflow: 'auto',
    flex: '0 0 auto'
  }
})

class ObjectType extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {
      loaded: false
    }
    this.handleOrderChange = this.handleOrderChange.bind(this)
    this.handleSelectionChange = this.handleSelectionChange.bind(this)
    this.handleChange = this.handleChange.bind(this)
    this.handleAddSection = this.handleAddSection.bind(this)
    this.handleAddProperty = this.handleAddProperty.bind(this)
    this.handleRemove = this.handleRemove.bind(this)
    this.handleSave = this.handleSave.bind(this)
  }

  componentDidMount() {
    this.load()
  }

  load() {
    this.setState({
      loaded: false
    })

    this.loadType(this.props.objectId).then(loadedType => {
      const type = {
        code: loadedType.code,
        autoGeneratedCode: loadedType.autoGeneratedCode,
        generatedCodePrefix: loadedType.generatedCodePrefix,
        subcodeUnique: loadedType.subcodeUnique
      }

      const sections = []
      const properties = []
      let currentSection = null
      let currentProperty = null
      let sectionsCounter = 0
      let propertiesCounter = 0

      loadedType.propertyAssignments.forEach(assignment => {
        currentProperty = {
          id: 'property-' + propertiesCounter++,
          code: assignment.propertyType.code,
          label: assignment.propertyType.label,
          description: assignment.propertyType.description,
          dataType: assignment.propertyType.dataType,
          vocabulary: assignment.propertyType.vocabulary
            ? assignment.propertyType.vocabulary.code
            : null,
          materialType: assignment.propertyType.materialType
            ? assignment.propertyType.materialType.code
            : null,
          visible: assignment.showInEditView,
          mandatory: assignment.mandatory
        }

        if (currentSection && currentSection.name === assignment.section) {
          currentSection.properties.push(currentProperty.id)
        } else {
          currentSection = {
            id: 'section-' + sectionsCounter++,
            name: assignment.section,
            properties: [currentProperty.id]
          }
          sections.push(currentSection)
        }
        currentProperty.section = currentSection.id

        properties.push(currentProperty)
      })

      this.setState(() => ({
        loaded: true,
        type,
        properties,
        propertiesCounter,
        sections,
        sectionsCounter,
        selection: null
      }))
    })
  }

  loadType(typeId) {
    let id = new dto.EntityTypePermId(typeId)
    let fo = new dto.SampleTypeFetchOptions()
    fo.withPropertyAssignments()
      .withPropertyType()
      .withMaterialType()
    fo.withPropertyAssignments()
      .withPropertyType()
      .withVocabulary()
    fo.withPropertyAssignments()
      .sortBy()
      .ordinal()

    return facade.getSampleTypes([id], fo).then(map => {
      return map[typeId]
    })
  }

  handleOrderChange(type, params) {
    if (type === 'section') {
      let { fromIndex, toIndex } = params
      this.handleOrderChangeSection(fromIndex, toIndex)
    } else if (type === 'property') {
      let { fromSectionId, toSectionId, fromIndex, toIndex } = params
      this.handleOrderChangeProperty(
        fromSectionId,
        fromIndex,
        toSectionId,
        toIndex
      )
    }
  }

  handleOrderChangeSection(fromIndex, toIndex) {
    let newSections = Array.from(this.state.sections)
    let [section] = newSections.splice(fromIndex, 1)
    newSections.splice(toIndex, 0, section)
    this.setState(state => ({
      ...state,
      sections: newSections
    }))
  }

  handleOrderChangeProperty(fromSectionId, fromIndex, toSectionId, toIndex) {
    if (fromSectionId === toSectionId) {
      let sections = this.state.sections
      let sectionIndex = _.findIndex(sections, ['id', fromSectionId])
      let section = sections[sectionIndex]
      let newProperties = Array.from(section.properties)
      let [property] = newProperties.splice(fromIndex, 1)
      newProperties.splice(toIndex, 0, property)
      let newSection = {
        ...section,
        properties: newProperties
      }
      let newSections = Array.from(sections)
      newSections[sectionIndex] = newSection
      this.setState(state => ({
        ...state,
        sections: newSections
      }))
    } else {
      let sections = this.state.sections
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

      let properties = this.state.properties
      let newProperties = Array.from(properties)

      let propertyIndex = _.findIndex(properties, ['id', property])
      let propertyObj = properties[propertyIndex]
      let newPropertyObj = {
        ...propertyObj,
        section: newToSection.id
      }
      newProperties[propertyIndex] = newPropertyObj

      this.setState(state => ({
        ...state,
        sections: newSections,
        properties: newProperties
      }))
    }
  }

  handleSelectionChange(type, params) {
    let selection = null

    if (type && params) {
      selection = {
        type,
        params
      }
    }

    this.setState(state => ({
      ...state,
      selection
    }))
  }

  handleChange(type, params) {
    if (type === 'type') {
      const { field, value } = params
      this.handleChangeType(field, value)
    } else if (type === 'section') {
      const { id, field, value } = params
      this.handleChangeSection(id, field, value)
    } else if (type === 'property') {
      const { id, field, value } = params
      this.handleChangeProperty(id, field, value)
    }
  }

  handleChangeType(field, value) {
    this.setState(state => ({
      ...state,
      type: {
        ...state.type,
        [field]: value
      }
    }))
  }

  handleChangeSection(id, field, value) {
    let sections = this.state.sections
    let newSections = Array.from(sections)

    let index = sections.findIndex(section => section.id === id)
    let section = sections[index]
    let newSection = {
      ...section,
      [field]: value
    }
    newSections[index] = newSection

    this.setState(state => ({
      ...state,
      sections: newSections
    }))
  }

  handleChangeProperty(id, field, value) {
    let properties = this.state.properties
    let newProperties = Array.from(properties)

    let index = properties.findIndex(property => property.id === id)
    let property = properties[index]
    let newProperty = {
      ...property,
      [field]: value
    }
    newProperties[index] = newProperty

    this.setState(state => ({
      ...state,
      properties: newProperties
    }))
  }

  handleAddSection() {
    let { sections, sectionsCounter, selection } = this.state

    let newSections = Array.from(sections)
    let newSection = {
      id: 'section-' + sectionsCounter++,
      name: null,
      properties: []
    }
    let newSelection = {
      type: 'section',
      params: {
        id: newSection.id
      }
    }

    if (selection) {
      if (selection.type === 'section') {
        let index = sections.findIndex(
          section => section.id === selection.params.id
        )
        newSections.splice(index + 1, 0, newSection)
      } else if (selection.type === 'property') {
        let index = sections.findIndex(
          section => section.properties.indexOf(selection.params.id) !== -1
        )
        newSections.splice(index + 1, 0, newSection)
      } else {
        newSections.push(newSection)
      }
    } else {
      newSections.push(newSection)
    }

    this.setState(state => ({
      ...state,
      sections: newSections,
      sectionsCounter,
      selection: newSelection
    }))
  }

  handleAddProperty() {
    let { sections, properties, propertiesCounter, selection } = this.state

    let sectionIndex = null
    let sectionPropertyIndex = null
    let propertyIndex = null

    if (selection.type === 'section') {
      sectionIndex = sections.findIndex(
        section => section.id === selection.params.id
      )
      sectionPropertyIndex = sections[sectionIndex].properties.length
      propertyIndex = properties.length
    } else if (selection.type === 'property') {
      sections.forEach((section, i) => {
        section.properties.forEach((property, j) => {
          if (property === selection.params.id) {
            sectionIndex = i
            sectionPropertyIndex = j + 1
          }
        })
      })
      propertyIndex =
        properties.findIndex(property => property.id === selection.params.id) +
        1
    }

    let section = sections[sectionIndex]

    let newProperties = Array.from(properties)
    let newProperty = {
      id: 'property-' + propertiesCounter++,
      code: '',
      label: '',
      description: '',
      dataType: 'VARCHAR',
      vocabulary: null,
      materialType: null,
      visible: true,
      mandatory: false,
      section: section.id
    }
    newProperties.splice(propertyIndex, 0, newProperty)

    let newSection = {
      ...section,
      properties: Array.from(section.properties)
    }
    newSection.properties.splice(sectionPropertyIndex, 0, newProperty.id)

    let newSections = Array.from(sections)
    newSections[sectionIndex] = newSection

    let newSelection = {
      type: 'property',
      params: {
        id: newProperty.id
      }
    }

    this.setState(state => ({
      ...state,
      sections: newSections,
      properties: newProperties,
      propertiesCounter,
      selection: newSelection
    }))
  }

  handleRemove() {
    const { selection } = this.state

    if (selection.type === 'section') {
      this.handleRemoveSection(selection.params.id)
    } else if (selection.type === 'property') {
      this.handleRemoveProperty(selection.params.id)
    }
  }

  handleRemoveSection(sectionId) {
    const { sections, properties } = this.state

    const sectionIndex = sections.findIndex(section => section.id === sectionId)
    const section = sections[sectionIndex]

    const newProperties = Array.from(properties)
    _.remove(
      newProperties,
      property => section.properties.indexOf(property.id) !== -1
    )

    const newSections = Array.from(sections)
    newSections.splice(sectionIndex, 1)

    this.setState(state => ({
      ...state,
      sections: newSections,
      properties: newProperties,
      selection: null
    }))
  }

  handleRemoveProperty(propertyId) {
    const { sections, properties } = this.state

    const propertyIndex = properties.findIndex(
      property => property.id === propertyId
    )
    const property = properties[propertyIndex]

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

    this.setState(state => ({
      ...state,
      sections: newSections,
      properties: newProperties,
      selection: null
    }))
  }

  handleSave() {}

  render() {
    logger.log(logger.DEBUG, 'ObjectType.render')

    if (!this.state.loaded) {
      return <div></div>
    }

    let { type, properties, sections, selection } = this.state
    let { classes } = this.props

    return (
      <div className={classes.container}>
        <div className={classes.content}>
          <div className={classes.preview}>
            <ObjectTypePreview
              type={type}
              properties={properties}
              sections={sections}
              selection={selection}
              onOrderChange={this.handleOrderChange}
              onSelectionChange={this.handleSelectionChange}
            />
          </div>
          <div className={classes.buttons}>
            <ObjectTypeButtons
              onAddSection={this.handleAddSection}
              onAddProperty={this.handleAddProperty}
              onRemove={this.handleRemove}
              onSave={this.handleSave}
              addSectionEnabled={true}
              addPropertyEnabled={selection !== null}
              removeEnabled={selection !== null}
              saveEnabled={false}
            />
          </div>
        </div>
        <Resizable
          defaultSize={{
            width: 400,
            height: 'auto'
          }}
          enable={{
            left: true,
            top: false,
            right: false,
            bottom: false,
            topRight: false,
            bottomRight: false,
            bottomLeft: false,
            topLeft: false
          }}
        >
          <div className={classes.parameters}>
            <ObjectTypeParameters
              type={type}
              properties={properties}
              sections={sections}
              selection={selection}
              onChange={this.handleChange}
              onSelectionChange={this.handleSelectionChange}
            />
          </div>
        </Resizable>
      </div>
    )
  }
}

export default _.flow(withStyles(styles))(ObjectType)
