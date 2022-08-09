import openbis from '@src/js/services/openbis.js'

export default class PersonalAccessTokenFormFacade {
  async loadPats() {
    const criteria = new openbis.PersonalAccessTokenSearchCriteria()
    const fo = new openbis.PersonalAccessTokenFetchOptions()
    fo.withOwner()
    fo.withRegistrator()
    fo.withModifier()
    return openbis.searchPersonalAccessTokens(criteria, fo).then(result => {
      return result.getObjects()
    })
  }

  async executeOperations(operations, options) {
    return openbis.executeOperations(operations, options)
  }
}
