import _ from 'lodash'
import PageControllerSave from '@src/js/components/common/page/PageControllerSave.js'
import EntityTypeFormControllerStrategies from '@src/js/components/types/form/entitytype/EntityTypeFormControllerStrategies.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import openbis from '@src/js/services/openbis.js'

export default class EntityTypeFormControllerSave extends PageControllerSave {
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
        }
      })
    }

    properties.forEach((property, index) => {
      const original = property.originalGlobal || property.original

      if (original) {
        if (this._isPropertyTypeUpdateNeeded(property, original)) {
          operations.push(this._updatePropertyTypeOperation(property))
        }
        assignments.push(this._propertyAssignmentCreation(property, index))
      } else {
        operations.push(this._createPropertyTypeOperation(property))
        assignments.push(this._propertyAssignmentCreation(property, index))
      }
    })

    if (type.original) {
      operations.push(this._updateTypeOperation(type, assignments))
    } else {
      operations.push(this._createTypeOperation(type, assignments))
    }

    const deleteUnusedPropertyTypesOperation =
      await this._deleteUnusedPropertyTypesOperation(type, properties)

    if (deleteUnusedPropertyTypesOperation) {
      operations.push(deleteUnusedPropertyTypesOperation)
    }

    const options = new openbis.SynchronousOperationExecutionOptions()
    options.setExecuteInOrder(true)
    await this.facade.executeOperations(operations, options)

    return type.code.value
  }

  _prepareType(type) {
    const code = type.code.value
    return FormUtil.trimFields({
      ...type,
      code: {
        value: code ? code.toUpperCase() : null
      }
    })
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
        const code = property.code.value
        const newProperty = FormUtil.trimFields({
          ...property,
          code: {
            value: code ? code.toUpperCase() : null
          },
          section: section.name.value
        })
        results.push(newProperty)
      })
    })

    return results
  }

  _isPropertyTypeUpdateNeeded(property, original) {
    return FormUtil.haveFieldsChanged(property, original, [
      'dataType',
      'label',
      'description',
      'schema',
      'transformation'
    ])
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
    update.getPropertyAssignments().setForceRemovingAssignments(true)

    return strategy.createTypeUpdateOperation([update])
  }

  _createPropertyTypeOperation(property) {
    const creation = new openbis.PropertyTypeCreation()
    creation.setCode(property.code.value)
    creation.setManagedInternally(property.internal.value)
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
    if (
      property.dataType.value === openbis.DataType.SAMPLE &&
      property.sampleType.value
    ) {
      creation.setSampleTypeId(
        new openbis.EntityTypePermId(
          property.sampleType.value,
          openbis.EntityKind.SAMPLE
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
    update.convertToDataType(property.dataType.value)
    return new openbis.UpdatePropertyTypesOperation([update])
  }

  async _deleteUnusedPropertyTypesOperation(type, properties) {
    const potentialPropertyTypesToDelete = []

    if (type.original) {
      type.original.properties.forEach(originalProperty => {
        const property = _.find(properties, [
          'code.value',
          originalProperty.code.value
        ])
        if (!property && !originalProperty.internal.value) {
          potentialPropertyTypesToDelete.push(originalProperty)
        }
      })
    }

    if (potentialPropertyTypesToDelete.length > 0) {
      const propertyTypesToDelete = []

      const assignments = await this.facade.loadAssignments()
      potentialPropertyTypesToDelete.forEach(propertyType => {
        const propertyTypeAssignments = assignments[propertyType.code.value]
        if (propertyTypeAssignments === 1) {
          propertyTypesToDelete.push(
            new openbis.PropertyTypePermId(propertyType.code.value)
          )
        }
      })

      if (propertyTypesToDelete.length > 0) {
        const options = new openbis.PropertyTypeDeletionOptions()
        options.setReason('deleted via ng_ui')
        return new openbis.DeletePropertyTypesOperation(
          propertyTypesToDelete,
          options
        )
      }
    }

    return null
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
    const strategies = new EntityTypeFormControllerStrategies()
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
