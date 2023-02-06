import openbis from '@src/js/services/openbis.js'
import EntityTypeFormControllerStrategies from '@src/js/components/types/form/entitytype/EntityTypeFormControllerStrategies.js'

export default class EntityTypeFormFacade {
  async loadType(object) {
    const strategy = this._getStrategy(object.type)
    const id = new openbis.EntityTypePermId(object.id)
    const fo = strategy.createTypeFetchOptions()
    fo.withValidationPlugin()
    fo.withPropertyAssignments().withPlugin()
    fo.withPropertyAssignments().withRegistrator()
    fo.withPropertyAssignments().withPropertyType().withRegistrator()
    fo.withPropertyAssignments().withPropertyType().withMaterialType()
    fo.withPropertyAssignments().withPropertyType().withSampleType()
    fo.withPropertyAssignments().withPropertyType().withVocabulary()
    fo.withPropertyAssignments().sortBy().ordinal()

    return strategy.getTypes([id], fo).then(map => {
      return map[object.id]
    })
  }

  async loadAssignments() {
    const criteria = new openbis.PropertyAssignmentSearchCriteria()

    const fo = new openbis.PropertyAssignmentFetchOptions()
    fo.withPropertyType()

    return openbis.searchPropertyAssignments(criteria, fo).then(result => {
      const map = {}

      result.getObjects().forEach(assignment => {
        const code = assignment.getPropertyType().getCode()
        map[code] = (map[code] || 0) + 1
      })

      return map
    })
  }

  async loadTypeUsages(object) {
    const strategy = this._getStrategy(object.type)

    if (object.type === strategy.getNewObjectType()) {
      return Promise.resolve(0)
    }

    const criteria = strategy.createEntitySearchCriteria()
    criteria.withType().withCode().thatEquals(object.id)

    const fo = strategy.createEntityFetchOptions()
    fo.from(0)
    fo.count(0)

    const operation = strategy.createEntitySearchOperation(criteria, fo)
    const options = new openbis.SynchronousOperationExecutionOptions()

    return openbis.executeOperations([operation], options).then(result => {
      const operationResult = result.getResults()[0]
      return operationResult.getSearchResult().getTotalCount()
    })
  }

  async loadPropertyUsages(object, propertyTypeCodes) {
    const strategy = this._getStrategy(object.type)

    if (object.type === strategy.getNewObjectType()) {
      return Promise.resolve({})
    }

    function createOperation(propertyTypeCode) {
      const criteria = strategy.createEntitySearchCriteria()
      criteria.withType().withCode().thatEquals(object.id)
      criteria.withProperty(propertyTypeCode)

      const fo = strategy.createEntityFetchOptions()
      fo.from(0)
      fo.count(0)

      return strategy.createEntitySearchOperation(criteria, fo)
    }

    const operations = propertyTypeCodes.map(propertyTypeCode =>
      createOperation(propertyTypeCode)
    )
    const options = new openbis.SynchronousOperationExecutionOptions()

    return openbis.executeOperations(operations, options).then(result => {
      const usagesMap = {}
      propertyTypeCodes.forEach((propertyTypeCode, index) => {
        const operationResult = result.getResults()[index]
        usagesMap[propertyTypeCode] = operationResult
          .getSearchResult()
          .getTotalCount()
      })
      return usagesMap
    })
  }

  async loadPropertyTypes() {
    const criteria = new openbis.PropertyTypeSearchCriteria()
    const fo = new openbis.PropertyTypeFetchOptions()
    fo.withMaterialType()
    fo.withVocabulary()
    fo.withRegistrator()
    return openbis.searchPropertyTypes(criteria, fo).then(results => {
      return results.getObjects()
    })
  }

  async loadValidationPlugins(type) {
    const criteria = new openbis.PluginSearchCriteria()
    criteria.withPluginType().thatEquals(openbis.PluginType.ENTITY_VALIDATION)
    const fo = new openbis.PluginFetchOptions()
    return this._loadPlugins(criteria, fo, type)
  }

  async loadDynamicPlugins(type) {
    const criteria = new openbis.PluginSearchCriteria()
    criteria.withPluginType().thatEquals(openbis.PluginType.DYNAMIC_PROPERTY)
    const fo = new openbis.PluginFetchOptions()
    return this._loadPlugins(criteria, fo, type)
  }

  async loadVocabularies() {
    let criteria = new openbis.VocabularySearchCriteria()
    let fo = new openbis.VocabularyFetchOptions()
    return openbis
      .searchVocabularies(criteria, fo)
      .then(result => result.objects)
  }

  async loadVocabularyTerms(vocabulary) {
    const criteria = new openbis.VocabularyTermSearchCriteria()
    criteria.withVocabulary().withCode().thatEquals(vocabulary)

    const fo = new openbis.VocabularyTermFetchOptions()
    fo.sortBy().code().asc()
    fo.from(0).count(10)

    return openbis
      .searchVocabularyTerms(criteria, fo)
      .then(result => result.objects)
  }

  async loadMaterialTypes() {
    let criteria = new openbis.MaterialTypeSearchCriteria()
    let fo = new openbis.MaterialTypeFetchOptions()
    return openbis
      .searchMaterialTypes(criteria, fo)
      .then(result => result.objects)
  }

  async loadSampleTypes() {
    let criteria = new openbis.SampleTypeSearchCriteria()
    let fo = new openbis.SampleTypeFetchOptions()
    return openbis
      .searchSampleTypes(criteria, fo)
      .then(result => result.objects)
  }

  async loadMaterials(materialType) {
    const criteria = new openbis.MaterialSearchCriteria()
    if (materialType) {
      criteria.withType().withCode().thatEquals(materialType)
    }

    const fo = new openbis.MaterialFetchOptions()
    fo.sortBy().code().asc()
    fo.from(0).count(10)

    return openbis.searchMaterials(criteria, fo).then(result => result.objects)
  }

  async loadSamples(sampleType) {
    const criteria = new openbis.SampleSearchCriteria()
    if (sampleType) {
      criteria.withType().withCode().thatEquals(sampleType)
    }

    const fo = new openbis.SampleFetchOptions()
    fo.sortBy().identifier().asc()
    fo.from(0).count(10)

    return openbis.searchSamples(criteria, fo).then(result => result.objects)
  }

  async executeOperations(operations, options) {
    return openbis.executeOperations(operations, options)
  }

  _loadPlugins(criteria, fo, type) {
    const strategy = this._getStrategy(type)
    return openbis.searchPlugins(criteria, fo).then(results => {
      return results.getObjects().filter(plugin => {
        return plugin.getEntityKinds().includes(strategy.getEntityKind())
      })
    })
  }

  _getStrategy(type) {
    const strategies = new EntityTypeFormControllerStrategies()
    strategies.extendObjectTypeStrategy(new ObjectTypeStrategy())
    strategies.extendCollectionTypeStrategy(new CollectionTypeStrategy())
    strategies.extendDataSetTypeStrategy(new DataSetTypeStrategy())
    strategies.extendMaterialTypeStrategy(new MaterialTypeStrategy())
    return strategies.getStrategy(type)
  }
}

class ObjectTypeStrategy {
  createTypeFetchOptions() {
    return new openbis.SampleTypeFetchOptions()
  }
  createEntityFetchOptions() {
    return new openbis.SampleFetchOptions()
  }
  createEntitySearchCriteria() {
    return new openbis.SampleSearchCriteria()
  }
  createEntitySearchOperation(criteria, fo) {
    return new openbis.SearchSamplesOperation(criteria, fo)
  }
  getTypes(ids, fo) {
    return openbis.getSampleTypes(ids, fo)
  }
}

class CollectionTypeStrategy {
  createTypeFetchOptions() {
    return new openbis.ExperimentTypeFetchOptions()
  }
  createEntityFetchOptions() {
    return new openbis.ExperimentFetchOptions()
  }
  createEntitySearchCriteria() {
    return new openbis.ExperimentSearchCriteria()
  }
  createEntitySearchOperation(criteria, fo) {
    return new openbis.SearchExperimentsOperation(criteria, fo)
  }
  getTypes(ids, fo) {
    return openbis.getExperimentTypes(ids, fo)
  }
}

class DataSetTypeStrategy {
  createTypeFetchOptions() {
    return new openbis.DataSetTypeFetchOptions()
  }
  createEntityFetchOptions() {
    return new openbis.DataSetFetchOptions()
  }
  createEntitySearchCriteria() {
    return new openbis.DataSetSearchCriteria()
  }
  createEntitySearchOperation(criteria, fo) {
    return new openbis.SearchDataSetsOperation(criteria, fo)
  }
  getTypes(ids, fo) {
    return openbis.getDataSetTypes(ids, fo)
  }
}

class MaterialTypeStrategy {
  createTypeFetchOptions() {
    return new openbis.MaterialTypeFetchOptions()
  }
  createEntityFetchOptions() {
    return new openbis.MaterialFetchOptions()
  }
  createEntitySearchCriteria() {
    return new openbis.MaterialSearchCriteria()
  }
  createEntitySearchOperation(criteria, fo) {
    return new openbis.SearchMaterialsOperation(criteria, fo)
  }
  getTypes(ids, fo) {
    return openbis.getMaterialTypes(ids, fo)
  }
}
