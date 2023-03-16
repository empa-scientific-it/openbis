import openbis from '@src/js/services/openbis.js'

export default class UserFormFacade {
  async loadUser(userId) {
    const id = new openbis.PersonPermId(userId)
    const fo = new openbis.PersonFetchOptions()
    fo.withSpace()
    fo.withRoleAssignments().withAuthorizationGroup()
    fo.withRoleAssignments().withSpace()
    fo.withRoleAssignments().withProject().withSpace()
    fo.withRoleAssignments().withRegistrator()
    return openbis.getPersons([id], fo).then(map => {
      return map[userId]
    })
  }

  async loadUserGroups(userId) {
    const criteria = new openbis.AuthorizationGroupSearchCriteria()
    const fo = new openbis.AuthorizationGroupFetchOptions()
    fo.withUsers()
    fo.withRegistrator()
    fo.withRoleAssignments().withAuthorizationGroup()
    fo.withRoleAssignments().withSpace()
    fo.withRoleAssignments().withProject().withSpace()
    fo.withRoleAssignments().withRegistrator()
    return openbis.searchAuthorizationGroups(criteria, fo).then(result => {
      return result.getObjects().filter(group => {
        return group.getUsers().some(user => {
          return user.userId === userId
        })
      })
    })
  }

  async loadGroups() {
    const criteria = new openbis.AuthorizationGroupSearchCriteria()
    const fo = new openbis.AuthorizationGroupFetchOptions()
    fo.withRegistrator()
    fo.withRoleAssignments().withAuthorizationGroup()
    fo.withRoleAssignments().withSpace()
    fo.withRoleAssignments().withProject().withSpace()
    fo.withRoleAssignments().withRegistrator()
    return openbis.searchAuthorizationGroups(criteria, fo).then(result => {
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
