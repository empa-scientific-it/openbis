import openbis from '@src/js/services/openbis.js'

export default class UserGroupFormFacade {
  async loadGroup(groupCode) {
    const id = new openbis.AuthorizationGroupPermId(groupCode)
    const fo = new openbis.AuthorizationGroupFetchOptions()
    fo.withUsers().withSpace()
    fo.withRoleAssignments().withSpace()
    fo.withRoleAssignments().withProject().withSpace()
    return openbis.getAuthorizationGroups([id], fo).then(map => {
      return map[groupCode]
    })
  }

  async loadUsers() {
    const criteria = new openbis.PersonSearchCriteria()
    const fo = new openbis.PersonFetchOptions()
    fo.withSpace()
    return openbis.searchPersons(criteria, fo).then(result => {
      return result.getObjects()
    })
  }

  async loadSpaces() {
    const criteria = new openbis.SpaceSearchCriteria()
    const fo = new openbis.SpaceFetchOptions()
    return openbis.searchSpaces(criteria, fo).then(result => {
      return result.getObjects()
    })
  }

  async loadProjects() {
    const criteria = new openbis.ProjectSearchCriteria()
    const fo = new openbis.ProjectFetchOptions()
    fo.withSpace()
    return openbis.searchProjects(criteria, fo).then(result => {
      return result.getObjects()
    })
  }

  async executeOperations(operations, options) {
    return openbis.executeOperations(operations, options)
  }
}
