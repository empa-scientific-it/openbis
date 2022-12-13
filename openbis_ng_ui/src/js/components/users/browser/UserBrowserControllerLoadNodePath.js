import UserBrowserConsts from '@src/js/components/users/browser/UserBrowserConsts.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'

export default class UserBrowserControllerLoadNodePath {
  async doLoadNodePath(params) {
    const { object } = params

    if (object.type === objectType.OVERVIEW) {
      if (object.id === objectType.USER) {
        return this.createFolderPath(
          objectType.USER,
          UserBrowserConsts.TEXT_USERS
        )
      } else if (object.id === objectType.USER_GROUP) {
        return this.createFolderPath(
          objectType.USER_GROUP,
          UserBrowserConsts.TEXT_GROUPS
        )
      }
    } else if (object.type === objectType.USER) {
      const id = new openbis.PersonPermId(object.id)
      const fetchOptions = new openbis.PersonFetchOptions()

      const persons = await openbis.getPersons([id], fetchOptions)
      const person = persons[object.id]

      return this.createNodePath(
        object,
        person,
        objectType.USER,
        UserBrowserConsts.TEXT_USERS
      )
    } else if (object.type === objectType.USER_GROUP) {
      const id = new openbis.AuthorizationGroupPermId(object.id)
      const fetchOptions = new openbis.AuthorizationGroupFetchOptions()

      const groups = await openbis.getAuthorizationGroups([id], fetchOptions)
      const group = groups[object.id]

      return this.createNodePath(
        object,
        group,
        objectType.USER_GROUP,
        UserBrowserConsts.TEXT_GROUPS
      )
    } else {
      return null
    }
  }

  createFolderPath(folderObjectType, folderText) {
    return [
      {
        id: UserBrowserConsts.nodeId(
          UserBrowserConsts.TYPE_ROOT,
          folderObjectType
        ),
        object: { type: objectType.OVERVIEW, id: folderObjectType },
        text: folderText
      }
    ]
  }

  createNodePath(object, loadedObject, folderObjectType, folderText) {
    if (loadedObject) {
      const folderPath = this.createFolderPath(folderObjectType, folderText)
      return [
        ...folderPath,
        {
          id: UserBrowserConsts.nodeId(
            UserBrowserConsts.TYPE_ROOT,
            folderObjectType,
            folderObjectType,
            object.id
          ),
          object,
          text: object.id
        }
      ]
    } else {
      return null
    }
  }
}
