import _ from 'lodash'
import openbis from '@src/js/services/openbis.js'
import pages from '@src/js/common/consts/pages.js'
import objectType from '@src/js/common/consts/objectType.js'
import objectOperation from '@src/js/common/consts/objectOperation.js'
import BrowserController from '@src/js/components/common/browser/BrowserController.js'
import AppController from '@src/js/components/AppController.js'
import messages from '@src/js/common/messages.js'

export default class TypeBrowserController extends BrowserController {
  doGetPage() {
    return pages.TYPES
  }

  async doLoadNodes() {
    return Promise.all([
      openbis.searchSampleTypes(
        new openbis.SampleTypeSearchCriteria(),
        new openbis.SampleTypeFetchOptions()
      ),
      openbis.searchExperimentTypes(
        new openbis.ExperimentTypeSearchCriteria(),
        new openbis.ExperimentTypeFetchOptions()
      ),
      openbis.searchDataSetTypes(
        new openbis.DataSetTypeSearchCriteria(),
        new openbis.DataSetTypeFetchOptions()
      ),
      openbis.searchMaterialTypes(
        new openbis.MaterialTypeSearchCriteria(),
        new openbis.MaterialTypeFetchOptions()
      ),
      openbis.searchVocabularies(
        new openbis.VocabularySearchCriteria(),
        new openbis.VocabularyFetchOptions()
      )
    ]).then(
      ([
        objectTypes,
        collectionTypes,
        dataSetTypes,
        materialTypes,
        vocabularyTypes
      ]) => {
        const _createNodes = (types, typeName, callback) => {
          return _.map(types, type => {
            const node = {
              id: `${typeName}s/${type.code}`,
              text: type.code,
              object: { type: typeName, id: type.code },
              canMatchFilter: true,
              canRemove: true
            }
            if (callback) {
              callback(type, node)
            }
            return node
          })
        }

        let objectTypeNodes = _createNodes(
          objectTypes.getObjects(),
          objectType.OBJECT_TYPE
        )
        let collectionTypeNodes = _createNodes(
          collectionTypes.getObjects(),
          objectType.COLLECTION_TYPE
        )
        let dataSetTypeNodes = _createNodes(
          dataSetTypes.getObjects(),
          objectType.DATA_SET_TYPE
        )
        let materialTypeNodes = _createNodes(
          materialTypes.getObjects(),
          objectType.MATERIAL_TYPE
        )
        let vocabularyTypeNodes = _createNodes(
          vocabularyTypes.getObjects(),
          objectType.VOCABULARY_TYPE,
          (type, node) => {
            node.canRemove =
              !type.managedInternally ||
              AppController.getInstance().isSystemUser()
          }
        )

        let nodes = [
          {
            id: 'objectTypes',
            text: messages.get(messages.OBJECT_TYPES),
            object: { type: objectType.OVERVIEW, id: objectType.OBJECT_TYPE },
            children: objectTypeNodes,
            childrenType: objectType.NEW_OBJECT_TYPE,
            canAdd: true
          },
          {
            id: 'collectionTypes',
            text: messages.get(messages.COLLECTION_TYPES),
            object: {
              type: objectType.OVERVIEW,
              id: objectType.COLLECTION_TYPE
            },
            children: collectionTypeNodes,
            childrenType: objectType.NEW_COLLECTION_TYPE,
            canAdd: true
          },
          {
            id: 'dataSetTypes',
            text: messages.get(messages.DATA_SET_TYPES),
            object: { type: objectType.OVERVIEW, id: objectType.DATA_SET_TYPE },
            children: dataSetTypeNodes,
            childrenType: objectType.NEW_DATA_SET_TYPE,
            canAdd: true
          },
          {
            id: 'materialTypes',
            text: messages.get(messages.MATERIAL_TYPES),
            object: { type: objectType.OVERVIEW, id: objectType.MATERIAL_TYPE },
            children: materialTypeNodes,
            childrenType: objectType.NEW_MATERIAL_TYPE,
            canAdd: true
          },
          {
            id: 'vocabularyTypes',
            text: messages.get(messages.VOCABULARY_TYPES),
            object: {
              type: objectType.OVERVIEW,
              id: objectType.VOCABULARY_TYPE
            },
            children: vocabularyTypeNodes,
            childrenType: objectType.NEW_VOCABULARY_TYPE,
            canAdd: true
          },
          {
            id: 'propertyTypes',
            text: messages.get(messages.PROPERTY_TYPES),
            object: {
              type: objectType.OVERVIEW,
              id: objectType.PROPERTY_TYPE
            }
          }
        ]

        return nodes
      }
    )
  }

  doNodeAdd(node) {
    if (node && node.childrenType) {
      AppController.getInstance().objectNew(this.getPage(), node.childrenType)
    }
  }

  async doNodeRemove(node) {
    if (!node.object) {
      return Promise.resolve()
    }

    const { type, id } = node.object
    const reason = 'deleted via ng_ui'

    try {
      const operations = await this._prepareRemoveOperations(type, id, reason)
      const options = new openbis.SynchronousOperationExecutionOptions()
      options.setExecuteInOrder(true)
      await openbis.executeOperations(operations, options)
      AppController.getInstance().objectDelete(this.getPage(), type, id)
    } catch (error) {
      AppController.getInstance().errorChange(error)
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

  doGetObservedModifications() {
    return {
      [objectType.OBJECT_TYPE]: [
        objectOperation.CREATE,
        objectOperation.DELETE
      ],
      [objectType.COLLECTION_TYPE]: [
        objectOperation.CREATE,
        objectOperation.DELETE
      ],
      [objectType.DATA_SET_TYPE]: [
        objectOperation.CREATE,
        objectOperation.DELETE
      ],
      [objectType.MATERIAL_TYPE]: [
        objectOperation.CREATE,
        objectOperation.DELETE
      ],
      [objectType.VOCABULARY_TYPE]: [
        objectOperation.CREATE,
        objectOperation.DELETE
      ]
    }
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
