import AppController from '@src/js/components/AppController.js'
import objectType from '@src/js/common/consts/objectType.js'
import openbis from '@src/js/services/openbis.js'
import pages from '@src/js/common/consts/pages.js'

const REMOVABLE_OBJECT_TYPES = [
  objectType.OBJECT_TYPE,
  objectType.COLLECTION_TYPE,
  objectType.DATA_SET_TYPE,
  objectType.MATERIAL_TYPE,
  objectType.VOCABULARY_TYPE
]

export default class TypeBrowserControllerRemoveNode {
  canRemoveNode(selectedObject) {
    return (
      selectedObject && REMOVABLE_OBJECT_TYPES.includes(selectedObject.type)
    )
  }

  async doRemoveNode(selectedObject) {
    if (!this.canRemoveNode(selectedObject)) {
      return
    }

    const { type, id } = selectedObject
    const reason = 'deleted via ng_ui'

    try {
      const operations = await this._prepareRemoveOperations(type, id, reason)
      const options = new openbis.SynchronousOperationExecutionOptions()
      options.setExecuteInOrder(true)
      await openbis.executeOperations(operations, options)
      await AppController.getInstance().objectDelete(pages.TYPES, type, id)
    } catch (error) {
      await AppController.getInstance().errorChange(error)
    }
  }

  async _prepareRemoveOperations(type, id, reason) {
    if (
      type === objectType.OBJECT_TYPE ||
      type === objectType.COLLECTION_TYPE ||
      type === objectType.DATA_SET_TYPE ||
      type === objectType.MATERIAL_TYPE
    ) {
      return await this._prepareRemoveEntityTypeOperations(type, id, reason)
    } else if (type === objectType.VOCABULARY_TYPE) {
      return await this._prepareRemoveVocabularyTypeOperations(type, id, reason)
    }
  }

  async _prepareRemoveEntityTypeOperations(type, id, reason) {
    const operations = []

    if (type === objectType.OBJECT_TYPE) {
      const options = new openbis.SampleTypeDeletionOptions()
      options.setReason(reason)
      operations.push(
        new openbis.DeleteSampleTypesOperation(
          [new openbis.EntityTypePermId(id)],
          options
        )
      )
    } else if (type === objectType.COLLECTION_TYPE) {
      const options = new openbis.ExperimentTypeDeletionOptions()
      options.setReason(reason)
      operations.push(
        new openbis.DeleteExperimentTypesOperation(
          [new openbis.EntityTypePermId(id)],
          options
        )
      )
    } else if (type === objectType.DATA_SET_TYPE) {
      const options = new openbis.DataSetTypeDeletionOptions()
      options.setReason(reason)
      operations.push(
        new openbis.DeleteDataSetTypesOperation(
          [new openbis.EntityTypePermId(id)],
          options
        )
      )
    } else if (type === objectType.MATERIAL_TYPE) {
      const options = new openbis.MaterialTypeDeletionOptions()
      options.setReason(reason)
      operations.push(
        new openbis.DeleteMaterialTypesOperation(
          [new openbis.EntityTypePermId(id)],
          options
        )
      )
    }

    const removeUnusuedPropertyTypesOperation =
      await this._prepareRemoveUnusedPropertyTypesOperations(type, id)

    if (removeUnusuedPropertyTypesOperation) {
      operations.push(removeUnusuedPropertyTypesOperation)
    }

    return operations
  }

  async _prepareRemoveUnusedPropertyTypesOperations(type, id) {
    const entityKind = this.getEntityKind(type)

    const propertyAssignmentFetchOptions =
      new openbis.PropertyAssignmentFetchOptions()
    propertyAssignmentFetchOptions.withPropertyType()
    propertyAssignmentFetchOptions.withEntityType()

    const propertyAssignments = await openbis.searchPropertyAssignments(
      new openbis.PropertyAssignmentSearchCriteria(),
      propertyAssignmentFetchOptions
    )

    const potentialPropertyTypesToDelete = []
    const propertyTypeUsages = {}

    propertyAssignments.objects.forEach(propertyAssignment => {
      const propertyTypeCode = propertyAssignment.propertyType.code

      propertyTypeUsages[propertyTypeCode] =
        (propertyTypeUsages[propertyTypeCode] || 0) + 1

      if (
        propertyAssignment.entityType.permId.permId === id &&
        propertyAssignment.entityType.permId.entityKind === entityKind &&
        !propertyAssignment.propertyType.managedInternally
      ) {
        potentialPropertyTypesToDelete.push(propertyTypeCode)
      }
    })

    if (potentialPropertyTypesToDelete.length > 0) {
      const propertyTypesToDelete = []

      potentialPropertyTypesToDelete.forEach(propertyTypeCode => {
        if (propertyTypeUsages[propertyTypeCode] === 1) {
          propertyTypesToDelete.push(
            new openbis.PropertyTypePermId(propertyTypeCode)
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

  async _prepareRemoveVocabularyTypeOperations(type, id, reason) {
    const options = new openbis.VocabularyDeletionOptions()
    options.setReason(reason)
    return new openbis.DeleteVocabulariesOperation(
      [new openbis.VocabularyPermId(id)],
      options
    )
  }

  getEntityKind(type) {
    if (type === objectType.OBJECT_TYPE) {
      return openbis.EntityKind.SAMPLE
    } else if (type === objectType.COLLECTION_TYPE) {
      return openbis.EntityKind.EXPERIMENT
    } else if (type === objectType.DATA_SET_TYPE) {
      return openbis.EntityKind.DATA_SET
    } else if (type === objectType.MATERIAL_TYPE) {
      return openbis.EntityKind.MATERIAL
    }
  }
}
