import TypeBrowserConsts from '@src/js/components/types/browser/TypeBrowserConsts.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'

export default class TypeBrowserControllerLoadNodePath {
  async doLoadNodePath(params) {
    const { object } = params

    if (object.type === objectType.OVERVIEW) {
      if (object.id === objectType.OBJECT_TYPE) {
        return this.createFolderPath(
          objectType.OBJECT_TYPE,
          TypeBrowserConsts.TEXT_OBJECT_TYPES
        )
      } else if (object.id === objectType.COLLECTION_TYPE) {
        return this.createFolderPath(
          objectType.COLLECTION_TYPE,
          TypeBrowserConsts.TEXT_COLLECTION_TYPES
        )
      } else if (object.id === objectType.DATA_SET_TYPE) {
        return this.createFolderPath(
          objectType.DATA_SET_TYPE,
          TypeBrowserConsts.TEXT_DATA_SET_TYPES
        )
      } else if (object.id === objectType.MATERIAL_TYPE) {
        return this.createFolderPath(
          objectType.MATERIAL_TYPE,
          TypeBrowserConsts.TEXT_MATERIAL_TYPES
        )
      } else if (object.id === objectType.VOCABULARY_TYPE) {
        return this.createFolderPath(
          objectType.VOCABULARY_TYPE,
          TypeBrowserConsts.TEXT_VOCABULARY_TYPES
        )
      } else if (object.id === objectType.PROPERTY_TYPE) {
        return this.createFolderPath(
          objectType.PROPERTY_TYPE,
          TypeBrowserConsts.TEXT_PROPERTY_TYPES
        )
      }
    } else if (object.type === objectType.OBJECT_TYPE) {
      const id = new openbis.EntityTypePermId(object.id)
      const fetchOptions = new openbis.SampleTypeFetchOptions()

      const types = await openbis.getSampleTypes([id], fetchOptions)
      const type = types[object.id]

      return this.createNodePath(
        object,
        type,
        objectType.OBJECT_TYPE,
        TypeBrowserConsts.TEXT_OBJECT_TYPES
      )
    } else if (object.type === objectType.COLLECTION_TYPE) {
      const id = new openbis.EntityTypePermId(object.id)
      const fetchOptions = new openbis.ExperimentTypeFetchOptions()

      const types = await openbis.getExperimentTypes([id], fetchOptions)
      const type = types[object.id]

      return this.createNodePath(
        object,
        type,
        objectType.COLLECTION_TYPE,
        TypeBrowserConsts.TEXT_COLLECTION_TYPES
      )
    } else if (object.type === objectType.DATA_SET_TYPE) {
      const id = new openbis.EntityTypePermId(object.id)
      const fetchOptions = new openbis.DataSetTypeFetchOptions()

      const types = await openbis.getDataSetTypes([id], fetchOptions)
      const type = types[object.id]

      return this.createNodePath(
        object,
        type,
        objectType.DATA_SET_TYPE,
        TypeBrowserConsts.TEXT_DATA_SET_TYPES
      )
    } else if (object.type === objectType.MATERIAL_TYPE) {
      const id = new openbis.EntityTypePermId(object.id)
      const fetchOptions = new openbis.MaterialTypeFetchOptions()

      const types = await openbis.getMaterialTypes([id], fetchOptions)
      const type = types[object.id]

      return this.createNodePath(
        object,
        type,
        objectType.MATERIAL_TYPE,
        TypeBrowserConsts.TEXT_MATERIAL_TYPES
      )
    } else if (object.type === objectType.VOCABULARY_TYPE) {
      const id = new openbis.VocabularyPermId(object.id)
      const fetchOptions = new openbis.VocabularyFetchOptions()

      const types = await openbis.getVocabularies([id], fetchOptions)
      const type = types[object.id]

      return this.createNodePath(
        object,
        type,
        objectType.VOCABULARY_TYPE,
        TypeBrowserConsts.TEXT_VOCABULARY_TYPES
      )
    } else {
      return null
    }
  }

  createFolderPath(folderObjectType, folderText) {
    return [
      {
        id: TypeBrowserConsts.nodeId(
          TypeBrowserConsts.TYPE_ROOT,
          folderObjectType
        ),
        object: { type: objectType.OVERVIEW, id: folderObjectType },
        text: folderText
      }
    ]
  }

  createNodePath(object, type, folderObjectType, folderText) {
    if (type) {
      const folderPath = this.createFolderPath(folderObjectType, folderText)
      return [
        ...folderPath,
        {
          id: TypeBrowserConsts.nodeId(
            TypeBrowserConsts.TYPE_ROOT,
            folderObjectType,
            folderObjectType,
            type.getCode()
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
