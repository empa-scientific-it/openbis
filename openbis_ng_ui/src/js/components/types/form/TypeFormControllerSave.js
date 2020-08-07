import _ from 'lodash'
import PageControllerSave from '@src/js/components/common/page/PageControllerSave.js'
import openbis from '@src/js/services/openbis.js'

import TypeFormControllerStrategies from './TypeFormControllerStrategies.js'
import TypeFormUtil from './TypeFormUtil.js'

export default class TypeFormControllerSave extends PageControllerSave {
  async save() {
    const state = this.context.getState()
    const type = this._prepareType(state.type)
    const properties = this._prepareProperties(
      state.type,
      state.properties,
      state.sections
    )
    const operations = []
    const assignments = []

    if (type.original) {
      type.original.properties.forEach(originalProperty => {
        const property = _.find(properties, ['id', originalProperty.id])
        if (!property) {
          operations.push(
            this._deletePropertyAssignmentOperation(type, originalProperty)
          )
          if (originalProperty.assignments === 1) {
            operations.push(this._deletePropertyTypeOperation(originalProperty))
          }
        }
      })
    }

    properties.forEach((property, index) => {
      const original = property.scope.globalPropertyType || property.original

      if (original) {
        if (this._isPropertyTypeUpdateNeeded(property, original)) {
          if (this._isPropertyTypeUpdatePossible(property, original)) {
            operations.push(this._updatePropertyTypeOperation(property))
          } else {
            operations.push(
              this._deletePropertyAssignmentOperation(type, property)
            )
            operations.push(this._deletePropertyTypeOperation(property))
            operations.push(this._createPropertyTypeOperation(property))
          }
        }
        assignments.push(this._propertyAssignmentCreation(property, index))
      } else {
        if (property.scope.value === 'local') {
          property = this._addTypePrefix(type, property)
        }
        operations.push(this._createPropertyTypeOperation(property))
        assignments.push(this._propertyAssignmentCreation(property, index))
      }
    })

    if (type.original) {
      operations.push(this._updateTypeOperation(type, assignments))
    } else {
      operations.push(this._createTypeOperation(type, assignments))
    }

    const options = new openbis.SynchronousOperationExecutionOptions()
    options.setExecuteInOrder(true)
    await this.facade.executeOperations(operations, options)

    return type.code.value
  }

  _prepareValue(field) {
    const trim = str => {
      const trimmed = str.trim()
      return trimmed.length > 0 ? trimmed : null
    }

    if (field) {
      if (_.isString(field)) {
        return trim(field)
      } else if (_.isObject(field) && _.isString(field.value)) {
        return {
          ...field,
          value: trim(field.value)
        }
      }
    }

    return field
  }

  _prepareType(type) {
    let code = type.code.value

    if (code) {
      code = code.toUpperCase()
    }

    const newType = _.mapValues(
      {
        ...type,
        code: {
          value: code
        }
      },
      this._prepareValue
    )

    return newType
  }

  _prepareProperties(type, properties, sections) {
    const propertiesMap = properties.reduce((map, property) => {
      map[property.id] = property
      return map
    }, {})

    const results = []
    sections.forEach(section => {
      section.properties.forEach(propertyId => {
        const property = propertiesMap[propertyId]

        let code = property.code.value
        if (code) {
          code = code.toUpperCase()
        }

        const newProperty = _.mapValues(
          {
            ...property,
            code: {
              value: code
            },
            section: section.name.value
          },
          this._prepareValue
        )

        results.push(newProperty)
      })
    })

    return results
  }

  _addTypePrefix(type, property) {
    return {
      ...property,
      code: {
        ...property.code,
        value: TypeFormUtil.addTypePrefix(type.code.value, property.code.value)
      }
    }
  }

  _hasPropertyChanged(property, original, path) {
    const originalValue = original ? _.get(original, path) : null
    const currentValue = _.get(property, path)
    return originalValue.value !== currentValue.value
  }

  _isPropertyTypeUpdateNeeded(property, original) {
    return (
      this._hasPropertyChanged(property, original, 'dataType') ||
      this._hasPropertyChanged(property, original, 'vocabulary') ||
      this._hasPropertyChanged(property, original, 'materialType') ||
      this._hasPropertyChanged(property, original, 'label') ||
      this._hasPropertyChanged(property, original, 'description') ||
      this._hasPropertyChanged(property, original, 'schema') ||
      this._hasPropertyChanged(property, original, 'transformation')
    )
  }

  _isPropertyTypeUpdatePossible(property, original) {
    if (
      this._hasPropertyChanged(property, original, 'dataType') ||
      this._hasPropertyChanged(property, original, 'vocabulary') ||
      this._hasPropertyChanged(property, original, 'materialType')
    ) {
      return false
    }
    return true
  }

  _deletePropertyAssignmentOperation(type, property) {
    const strategy = this._getStrategy()
    const assignmentId = new openbis.PropertyAssignmentPermId(
      new openbis.EntityTypePermId(type.code.value, strategy.getEntityKind()),
      new openbis.PropertyTypePermId(property.code.value)
    )

    const update = strategy.createTypeUpdate()
    update.setTypeId(
      new openbis.EntityTypePermId(type.code.value, strategy.getEntityKind())
    )
    update.getPropertyAssignments().remove([assignmentId])
    update
      .getPropertyAssignments()
      .setForceRemovingAssignments(property.usagesLocal > 0)

    return strategy.createTypeUpdateOperation([update])
  }

  _createPropertyTypeOperation(property) {
    const creation = new openbis.PropertyTypeCreation()
    creation.setCode(property.code.value)
    creation.setLabel(property.label.value)
    creation.setDescription(property.description.value)
    creation.setDataType(property.dataType.value)
    creation.setSchema(property.schema.value)
    creation.setTransformation(property.transformation.value)

    if (
      property.dataType.value === openbis.DataType.CONTROLLEDVOCABULARY &&
      property.vocabulary.value
    ) {
      creation.setVocabularyId(
        new openbis.VocabularyPermId(property.vocabulary.value)
      )
    }
    if (
      property.dataType.value === openbis.DataType.MATERIAL &&
      property.materialType.value
    ) {
      creation.setMaterialTypeId(
        new openbis.EntityTypePermId(
          property.materialType.value,
          openbis.EntityKind.MATERIAL
        )
      )
    }
    return new openbis.CreatePropertyTypesOperation([creation])
  }

  _updatePropertyTypeOperation(property) {
    const update = new openbis.PropertyTypeUpdate()
    if (property.code.value) {
      update.setTypeId(new openbis.PropertyTypePermId(property.code.value))
    }
    update.setLabel(property.label.value)
    update.setDescription(property.description.value)
    update.setSchema(property.schema.value)
    update.setTransformation(property.transformation.value)
    return new openbis.UpdatePropertyTypesOperation([update])
  }

  _deletePropertyTypeOperation(property) {
    const id = new openbis.PropertyTypePermId(property.code.value)
    const options = new openbis.PropertyTypeDeletionOptions()
    options.setReason('deleted via ng_ui')
    return new openbis.DeletePropertyTypesOperation([id], options)
  }

  _propertyAssignmentCreation(property, index) {
    let creation = new openbis.PropertyAssignmentCreation()
    creation.setOrdinal(index + 1)
    creation.setMandatory(property.mandatory.value)
    creation.setInitialValueForExistingEntities(
      property.initialValueForExistingEntities.value
    )
    creation.setShowInEditView(property.showInEditView.value)
    creation.setShowRawValueInForms(property.showRawValueInForms.value)
    creation.setSection(property.section)

    if (property.code.value) {
      creation.setPropertyTypeId(
        new openbis.PropertyTypePermId(property.code.value)
      )
    }

    if (property.plugin.value) {
      creation.setPluginId(new openbis.PluginPermId(property.plugin.value))
    }

    return creation
  }

  _createTypeOperation(type, assignments) {
    const strategy = this._getStrategy()
    const creation = strategy.createTypeCreation()
    creation.setCode(type.code.value)
    creation.setDescription(type.description.value)
    creation.setValidationPluginId(
      type.validationPlugin.value
        ? new openbis.PluginPermId(type.validationPlugin.value)
        : null
    )
    creation.setPropertyAssignments(assignments.reverse())
    strategy.setTypeAttributes(creation, type)
    return strategy.createTypeCreateOperation([creation])
  }

  _updateTypeOperation(type, assignments) {
    const strategy = this._getStrategy()
    const update = strategy.createTypeUpdate()
    update.setTypeId(
      new openbis.EntityTypePermId(type.code.value, strategy.getEntityKind())
    )
    update.setDescription(type.description.value)
    update.setValidationPluginId(
      type.validationPlugin.value
        ? new openbis.PluginPermId(type.validationPlugin.value)
        : null
    )
    update.getPropertyAssignments().set(assignments.reverse())
    strategy.setTypeAttributes(update, type)
    return strategy.createTypeUpdateOperation([update])
  }

  _getStrategy() {
    const strategies = new TypeFormControllerStrategies()
    strategies.extendObjectTypeStrategy(new ObjectTypeStrategy())
    strategies.extendCollectionTypeStrategy(new CollectionTypeStrategy())
    strategies.extendDataSetTypeStrategy(new DataSetTypeStrategy())
    strategies.extendMaterialTypeStrategy(new MaterialTypeStrategy())
    return strategies.getStrategy(this.object.type)
  }
}

class ObjectTypeStrategy {
  createTypeCreation() {
    return new openbis.SampleTypeCreation()
  }

  createTypeCreateOperation(creations) {
    return new openbis.CreateSampleTypesOperation(creations)
  }

  createTypeUpdate() {
    return new openbis.SampleTypeUpdate()
  }

  createTypeUpdateOperation(updates) {
    return new openbis.UpdateSampleTypesOperation(updates)
  }

  setTypeAttributes(object, type) {
    object.setListable(type.listable.value)
    object.setShowContainer(type.showContainer.value)
    object.setShowParents(type.showParents.value)
    object.setShowParentMetadata(type.showParentMetadata.value)
    object.setAutoGeneratedCode(type.autoGeneratedCode.value)
    object.setGeneratedCodePrefix(type.generatedCodePrefix.value)
    object.setSubcodeUnique(type.subcodeUnique.value)
  }
}

class CollectionTypeStrategy {
  createTypeCreation() {
    return new openbis.ExperimentTypeCreation()
  }

  createTypeCreateOperation(creations) {
    return new openbis.CreateExperimentTypesOperation(creations)
  }

  createTypeUpdate() {
    return new openbis.ExperimentTypeUpdate()
  }

  createTypeUpdateOperation(updates) {
    return new openbis.UpdateExperimentTypesOperation(updates)
  }

  setTypeAttributes() {}
}

class DataSetTypeStrategy {
  createTypeCreation() {
    return new openbis.DataSetTypeCreation()
  }

  createTypeCreateOperation(creations) {
    return new openbis.CreateDataSetTypesOperation(creations)
  }

  createTypeUpdate() {
    return new openbis.DataSetTypeUpdate()
  }

  createTypeUpdateOperation(updates) {
    return new openbis.UpdateDataSetTypesOperation(updates)
  }

  setTypeAttributes(object, type) {
    object.setMainDataSetPattern(type.mainDataSetPattern.value)
    object.setMainDataSetPath(type.mainDataSetPath.value)
    object.setDisallowDeletion(type.disallowDeletion.value)
  }
}

class MaterialTypeStrategy {
  createTypeCreation() {
    return new openbis.MaterialTypeCreation()
  }

  createTypeCreateOperation(creations) {
    return new openbis.CreateMaterialTypesOperation(creations)
  }

  createTypeUpdate() {
    return new openbis.MaterialTypeUpdate()
  }

  createTypeUpdateOperation(updates) {
    return new openbis.UpdateMaterialTypesOperation(updates)
  }

  setTypeAttributes() {}
}
