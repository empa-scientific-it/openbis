import UserBrowserCommon from '@src/js/components/users/browser/UserBrowserCommon.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'

export default class UserBrowserControllerLoadNodePath {
  async doLoadNodePath(params) {
    const { object } = params

    if (object.type === objectType.OVERVIEW) {
      if (object.id === objectType.USER) {
        return [UserBrowserCommon.usersFolderNode()]
      } else if (object.id === objectType.USER_GROUP) {
        return [UserBrowserCommon.groupsFolderNode()]
      }
    } else if (object.type === objectType.USER) {
      const user = await this.searchUser(object.id)
      if (user) {
        const folderNode = UserBrowserCommon.usersFolderNode()
        const userNode = UserBrowserCommon.userNode(object.id)
        return [folderNode, userNode]
      }
    } else if (object.type === objectType.USER_GROUP) {
      const group = await this.searchGroup(object.id)
      if (group) {
        const folderNode = UserBrowserCommon.groupsFolderNode()
        const groupNode = UserBrowserCommon.groupNode(object.id)
        return [folderNode, groupNode]
      }
    }

    return null
  }

  async searchGroup(groupCode) {
    const id = new openbis.AuthorizationGroupPermId(groupCode)
    const fetchOptions = new openbis.AuthorizationGroupFetchOptions()
    const groups = await openbis.getAuthorizationGroups([id], fetchOptions)
    return groups[groupCode]
  }

  async searchUser(userId) {
    const id = new openbis.PersonPermId(userId)
    const fetchOptions = new openbis.PersonFetchOptions()
    const persons = await openbis.getPersons([id], fetchOptions)
    return persons[userId]
  }
}
