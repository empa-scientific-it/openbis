import _ from 'lodash'
import openbis from '@src/js/services/openbis.js'
import AppController from '@src/js/components/AppController.js'
import PageControllerLoad from '@src/js/components/common/page/PageControllerLoad.js'
import EntityTypeFormControllerStrategies from '@src/js/components/types/form/entitytype/EntityTypeFormControllerStrategies.js'
import EntityTypeFormSelectionType from '@src/js/components/types/form/entitytype/EntityTypeFormSelectionType.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import users from '@src/js/common/consts/users.js'
import util from '@src/js/common/util.js'

export default class EntityTypeFormControllerLoad extends PageControllerLoad {
  async load(object, isNew) {
    return Promise.all([
      this._loadDictionaries(object),
      this._loadType(object, isNew)
    ])
  }

  async _loadDictionaries(object) {
    const [
      validationPlugins,
      dynamicPlugins,
      vocabularies,
      materialTypes,
      sampleTypes,
      propertyTypes
    ] = await Promise.all([
      this.facade.loadValidationPlugins(object.type),
      this.facade.loadDynamicPlugins(object.type),
      this.facade.loadVocabularies(),
      this.facade.loadMaterialTypes(),
      this.facade.loadSampleTypes(),
      this.facade.loadPropertyTypes()
    ])

    await this.context.setState(() => ({
      dictionaries: {
        validationPlugins,
        dynamicPlugins,
        vocabularies,
        materialTypes,
        sampleTypes,
        propertyTypes
      }
    }))
  }

  async _loadType(object, isNew) {
    let loadedType = null

    if (!isNew) {
      loadedType = await this.facade.loadType(object)
      if (!loadedType) {
        return
      }
    }

    const loadedAssignments = await this.facade.loadAssignments(object)

    const sections = []
    const properties = []
    let section = null
    let property = null
    let sectionsCounter = 0
    let propertiesCounter = 0

    if (loadedType && loadedType.propertyAssignments) {
      loadedType.propertyAssignments.forEach(loadedAssignment => {
        property = this._createProperty(
          'property-' + propertiesCounter++,
          loadedAssignment,
          loadedAssignments
        )
        properties.push(property)

        if (
          !section ||
          section.name.value !== util.trim(loadedAssignment.section)
        ) {
          section = this._createSection(
            'section-' + sectionsCounter++,
            loadedAssignment
          )
          sections.push(section)
        }

        section.properties.push(property.id)
        property.section = section.id
        property.original = _.cloneDeep(property)
      })
    }

    const type = this._createType(loadedType)

    if (loadedType) {
      type.original = _.cloneDeep({
        ...type,
        properties
      })
    }

    const selection = this._createSelection(sections)

    return this.context.setState(() => ({
      type,
      properties,
      propertiesCounter,
      sections,
      sectionsCounter,
      preview: {},
      selection: selection,
      assignments: loadedAssignments,
      removeSectionDialogOpen: false,
      removePropertyDialogOpen: false
    }))
  }

  _createType(loadedType) {
    const strategy = this._getStrategy()
    const type = {
      code: FormUtil.createField({
        value: _.get(loadedType, 'code', null),
        enabled: loadedType === null
      }),
      objectType: FormUtil.createField({
        value: this.object.type
      }),
      description: FormUtil.createField({
        value: _.get(loadedType, 'description', null)
      }),
      validationPlugin: FormUtil.createField({
        value: _.get(loadedType, 'validationPlugin.name', null)
      }),
      errors: 0
    }
    strategy.setTypeAttributes(type, loadedType)
    return type
  }

  _createSection(id, loadedAssignment) {
    return {
      id: id,
      name: FormUtil.createField({
        value: util.trim(loadedAssignment.section)
      }),
      properties: []
    }
  }

  _createProperty(id, loadedAssignment, loadedAssignments) {
    const propertyType = loadedAssignment.propertyType

    const code = _.get(propertyType, 'code', null)
    const dataType = _.get(propertyType, 'dataType', null)
    const plugin = _.get(loadedAssignment, 'plugin.name', null)

    const assignments =
      (loadedAssignments && loadedAssignments[propertyType.code]) || 0

    const assignmentRegistrator = _.get(
      loadedAssignment,
      'registrator.userId',
      null
    )

    const propertyTypeInternal = _.get(propertyType, 'managedInternally', false)

    const assignmentInternal =
      propertyTypeInternal && assignmentRegistrator === users.SYSTEM

    return {
      id: id,
      code: FormUtil.createField({
        value: code,
        enabled: false
      }),
      internal: FormUtil.createField({
        value: propertyTypeInternal,
        visible: AppController.getInstance().isSystemUser(),
        enabled: false
      }),
      assignmentInternal: FormUtil.createField({
        value: assignmentInternal,
        visible: false,
        enabled: false
      }),
      label: FormUtil.createField({
        value: _.get(propertyType, 'label', null),
        enabled:
          !propertyTypeInternal || AppController.getInstance().isSystemUser()
      }),
      description: FormUtil.createField({
        value: _.get(propertyType, 'description', null),
        enabled:
          !propertyTypeInternal || AppController.getInstance().isSystemUser()
      }),
      dataType: FormUtil.createField({
        value: dataType,
        enabled:
          !propertyTypeInternal || AppController.getInstance().isSystemUser()
      }),
      schema: FormUtil.createField({
        value: _.get(propertyType, 'schema', null),
        visible: dataType === openbis.DataType.XML,
        enabled:
          !propertyTypeInternal || AppController.getInstance().isSystemUser()
      }),
      transformation: FormUtil.createField({
        value: _.get(propertyType, 'transformation', null),
        visible: dataType === openbis.DataType.XML,
        enabled:
          !propertyTypeInternal || AppController.getInstance().isSystemUser()
      }),
      vocabulary: FormUtil.createField({
        value: _.get(propertyType, 'vocabulary.code', null),
        visible: dataType === openbis.DataType.CONTROLLEDVOCABULARY,
        enabled: false
      }),
      materialType: FormUtil.createField({
        value: _.get(propertyType, 'materialType.code', null),
        visible: dataType === openbis.DataType.MATERIAL,
        enabled: false
      }),
      sampleType: FormUtil.createField({
        value: _.get(propertyType, 'sampleType.code', null),
        visible: dataType === openbis.DataType.SAMPLE,
        enabled: false
      }),
      plugin: FormUtil.createField({
        value: plugin,
        enabled:
          plugin &&
          (!assignmentInternal || AppController.getInstance().isSystemUser())
      }),
      mandatory: FormUtil.createField({
        value: _.get(loadedAssignment, 'mandatory', false),
        enabled:
          !assignmentInternal || AppController.getInstance().isSystemUser()
      }),
      isMultiValue: FormUtil.createField({
        value: _.get(propertyType, 'multiValue', false),
        enabled: false
      }),
      showInEditView: FormUtil.createField({
        value: _.get(loadedAssignment, 'showInEditView', true),
        enabled:
          !assignmentInternal || AppController.getInstance().isSystemUser()
      }),
      unique: FormUtil.createField({
        value: _.get(loadedAssignment, 'unique', false),
        enabled: false
      }),
      showRawValueInForms: FormUtil.createField({
        value: _.get(loadedAssignment, 'showRawValueInForms', false),
        enabled:
          !assignmentInternal || AppController.getInstance().isSystemUser()
      }),
      initialValueForExistingEntities: FormUtil.createField({
        visible: false,
        enabled:
          !assignmentInternal || AppController.getInstance().isSystemUser()
      }),
      assignments,
      errors: 0
    }
  }

  _createSelection(newSections) {
    const { selection: oldSelection, sections: oldSections } =
      this.context.getState()

    if (!oldSelection) {
      return null
    } else if (oldSelection.type === EntityTypeFormSelectionType.SECTION) {
      let sectionIndex = -1

      oldSections.forEach((oldSection, i) => {
        if (oldSection.id === oldSelection.params.id) {
          sectionIndex = i
        }
      })

      if (sectionIndex >= 0 && sectionIndex < newSections.length) {
        const newSection = newSections[sectionIndex]
        return {
          type: EntityTypeFormSelectionType.SECTION,
          params: {
            id: newSection.id,
            part: oldSelection.params.part
          }
        }
      }
    } else if (oldSelection.type === EntityTypeFormSelectionType.PROPERTY) {
      let sectionIndex = -1
      let propertyIndex = -1

      oldSections.forEach((oldSection, i) => {
        oldSection.properties.forEach((oldProperty, j) => {
          if (oldProperty === oldSelection.params.id) {
            sectionIndex = i
            propertyIndex = j
          }
        })
      })

      if (sectionIndex >= 0 && sectionIndex < newSections.length) {
        const newSection = newSections[sectionIndex]
        if (
          propertyIndex >= 0 &&
          propertyIndex < newSection.properties.length
        ) {
          const newProperty = newSection.properties[propertyIndex]
          return {
            type: EntityTypeFormSelectionType.PROPERTY,
            params: {
              id: newProperty,
              part: oldSelection.params.part
            }
          }
        }
      }
    } else {
      return null
    }
  }

  _getStrategy() {
    const strategies = new EntityTypeFormControllerStrategies()
    strategies.extendObjectTypeStrategy(new ObjectTypeStrategy())
    strategies.extendCollectionTypeStrategy(new CollectionTypeStrategy())
    strategies.extendDataSetTypeStrategy(new DataSetTypeStrategy())
    strategies.extendMaterialTypeStrategy(new MaterialTypeStrategy())
    return strategies.getStrategy(this.object.type)
  }
}

class ObjectTypeStrategy {
  setTypeAttributes(object, loadedType) {
    Object.assign(object, {
      listable: FormUtil.createField({
        value: _.get(loadedType, 'listable', true)
      }),
      showContainer: FormUtil.createField({
        value: _.get(loadedType, 'showContainer', true)
      }),
      showParents: FormUtil.createField({
        value: _.get(loadedType, 'showParents', true)
      }),
      showParentMetadata: FormUtil.createField({
        value: _.get(loadedType, 'showParentMetadata', true)
      }),
      autoGeneratedCode: FormUtil.createField({
        value: _.get(loadedType, 'autoGeneratedCode', true)
      }),
      generatedCodePrefix: FormUtil.createField({
        value: _.get(loadedType, 'generatedCodePrefix', null)
      }),
      subcodeUnique: FormUtil.createField({
        value: _.get(loadedType, 'subcodeUnique', false)
      })
    })
  }
}

class CollectionTypeStrategy {
  setTypeAttributes() {}
}

class DataSetTypeStrategy {
  setTypeAttributes(object, loadedType) {
    Object.assign(object, {
      mainDataSetPattern: FormUtil.createField({
        value: _.get(loadedType, 'mainDataSetPattern', null)
      }),
      mainDataSetPath: FormUtil.createField({
        value: _.get(loadedType, 'mainDataSetPath', null)
      }),
      disallowDeletion: FormUtil.createField({
        value: _.get(loadedType, 'disallowDeletion', false)
      })
    })
  }
}

class MaterialTypeStrategy {
  setTypeAttributes() {}
}
