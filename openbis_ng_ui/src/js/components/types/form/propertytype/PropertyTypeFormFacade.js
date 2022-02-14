import openbis from '@src/js/services/openbis.js'

export default class PropertyTypeFormFacade {
  async loadPropertyType(code) {
    const id = new openbis.PropertyTypePermId(code)
    const fo = new openbis.PropertyTypeFetchOptions()
    fo.withVocabulary()
    fo.withMaterialType()
    fo.withSampleType()
    return openbis.getPropertyTypes([id], fo).then(map => {
      return map[code]
    })
  }

  async loadVocabularies() {
    let criteria = new openbis.VocabularySearchCriteria()
    let fo = new openbis.VocabularyFetchOptions()
    return openbis
      .searchVocabularies(criteria, fo)
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

  async executeOperations(operations, options) {
    return openbis.executeOperations(operations, options)
  }
}
