import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@src/js/services/openbis.js'

export default class EntityTypeFormControllerStrategies {
  constructor() {
    this.objectTypeStrategy = {
      getEntityKind: function () {
        return openbis.EntityKind.SAMPLE
      },
      getNewObjectType: function () {
        return objectTypes.NEW_OBJECT_TYPE
      },
      getExistingObjectType: function () {
        return objectTypes.OBJECT_TYPE
      }
    }
    this.collectionTypeStrategy = {
      getEntityKind: function () {
        return openbis.EntityKind.EXPERIMENT
      },
      getNewObjectType: function () {
        return objectTypes.NEW_COLLECTION_TYPE
      },
      getExistingObjectType: function () {
        return objectTypes.COLLECTION_TYPE
      }
    }
    this.dataSetTypeStrategy = {
      getEntityKind: function () {
        return openbis.EntityKind.DATA_SET
      },
      getNewObjectType: function () {
        return objectTypes.NEW_DATA_SET_TYPE
      },
      getExistingObjectType: function () {
        return objectTypes.DATA_SET_TYPE
      }
    }
    this.materialTypeStrategy = {
      getEntityKind: function () {
        return openbis.EntityKind.MATERIAL
      },
      getNewObjectType: function () {
        return objectTypes.NEW_MATERIAL_TYPE
      },
      getExistingObjectType: function () {
        return objectTypes.MATERIAL_TYPE
      }
    }
  }

  extendObjectTypeStrategy(strategy) {
    this.objectTypeStrategy = Object.assign(strategy, this.objectTypeStrategy)
  }

  extendCollectionTypeStrategy(strategy) {
    this.collectionTypeStrategy = Object.assign(
      strategy,
      this.collectionTypeStrategy
    )
  }

  extendDataSetTypeStrategy(strategy) {
    this.dataSetTypeStrategy = Object.assign(strategy, this.dataSetTypeStrategy)
  }

  extendMaterialTypeStrategy(strategy) {
    this.materialTypeStrategy = Object.assign(
      strategy,
      this.materialTypeStrategy
    )
  }

  getStrategy(type) {
    const strategies = [
      this.objectTypeStrategy,
      this.collectionTypeStrategy,
      this.dataSetTypeStrategy,
      this.materialTypeStrategy
    ]

    const strategy = strategies.find(
      strategy =>
        type === strategy.getNewObjectType() ||
        type === strategy.getExistingObjectType()
    )

    if (strategy) {
      return strategy
    } else {
      throw 'Unsupported type: ' + type
    }
  }
}
