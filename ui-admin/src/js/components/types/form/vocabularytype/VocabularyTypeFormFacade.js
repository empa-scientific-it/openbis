import openbis from '@src/js/services/openbis.js'

export default class VocabularyTypeFormFacade {
  async loadVocabulary(code) {
    const id = new openbis.VocabularyPermId(code)
    const fo = new openbis.VocabularyFetchOptions()
    fo.withTerms().withRegistrator()
    fo.withRegistrator()
    return openbis.getVocabularies([id], fo).then(map => {
      return map[code]
    })
  }

  async executeOperations(operations, options) {
    return openbis.executeOperations(operations, options)
  }
}
