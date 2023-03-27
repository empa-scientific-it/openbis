import TypeBrowserCommon from '@src/js/components/types/browser/TypeBrowserCommon.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'

export default class TypeBrowserControllerLoadNodePath {
  async doLoadNodePath(params) {
    const { object } = params

    if (object.type === objectType.OVERVIEW) {
      if (object.id === objectType.OBJECT_TYPE) {
        return [TypeBrowserCommon.objectTypesFolderNode()]
      } else if (object.id === objectType.COLLECTION_TYPE) {
        return [TypeBrowserCommon.collectionTypesFolderNode()]
      } else if (object.id === objectType.DATA_SET_TYPE) {
        return [TypeBrowserCommon.dataSetTypesFolderNode()]
      } else if (object.id === objectType.MATERIAL_TYPE) {
        return [TypeBrowserCommon.materialTypesFolderNode()]
      } else if (object.id === objectType.VOCABULARY_TYPE) {
        return [TypeBrowserCommon.vocabularyTypesFolderNode()]
      } else if (object.id === objectType.PROPERTY_TYPE) {
        return [TypeBrowserCommon.propertyTypesFolderNode()]
      }
    } else if (object.type === objectType.OBJECT_TYPE) {
      const type = await this.searchObjectType(object.id)
      if (type) {
        const folderNode = TypeBrowserCommon.objectTypesFolderNode()
        const typeNode = TypeBrowserCommon.objectTypeNode(object.id)
        return [folderNode, typeNode]
      }
    } else if (object.type === objectType.COLLECTION_TYPE) {
      const type = await this.searchCollectionType(object.id)
      if (type) {
        const folderNode = TypeBrowserCommon.collectionTypesFolderNode()
        const typeNode = TypeBrowserCommon.collectionTypeNode(object.id)
        return [folderNode, typeNode]
      }
    } else if (object.type === objectType.DATA_SET_TYPE) {
      const type = await this.searchDataSetType(object.id)
      if (type) {
        const folderNode = TypeBrowserCommon.dataSetTypesFolderNode()
        const typeNode = TypeBrowserCommon.dataSetTypeNode(object.id)
        return [folderNode, typeNode]
      }
    } else if (object.type === objectType.MATERIAL_TYPE) {
      const type = await this.searchMaterialType(object.id)
      if (type) {
        const folderNode = TypeBrowserCommon.materialTypesFolderNode()
        const typeNode = TypeBrowserCommon.materialTypeNode(object.id)
        return [folderNode, typeNode]
      }
    } else if (object.type === objectType.VOCABULARY_TYPE) {
      const type = await this.searchVocabularyType(object.id)
      if (type) {
        const folderNode = TypeBrowserCommon.vocabularyTypesFolderNode()
        const typeNode = TypeBrowserCommon.vocabularyTypeNode(object.id)
        return [folderNode, typeNode]
      }
    }

    return null
  }

  async searchObjectType(typeCode) {
    const id = new openbis.EntityTypePermId(typeCode)
    const fetchOptions = new openbis.SampleTypeFetchOptions()
    const types = await openbis.getSampleTypes([id], fetchOptions)
    return types[typeCode]
  }

  async searchCollectionType(typeCode) {
    const id = new openbis.EntityTypePermId(typeCode)
    const fetchOptions = new openbis.ExperimentTypeFetchOptions()
    const types = await openbis.getExperimentTypes([id], fetchOptions)
    return types[typeCode]
  }

  async searchDataSetType(typeCode) {
    const id = new openbis.EntityTypePermId(typeCode)
    const fetchOptions = new openbis.DataSetTypeFetchOptions()
    const types = await openbis.getDataSetTypes([id], fetchOptions)
    return types[typeCode]
  }

  async searchMaterialType(typeCode) {
    const id = new openbis.EntityTypePermId(typeCode)
    const fetchOptions = new openbis.MaterialTypeFetchOptions()
    const types = await openbis.getMaterialTypes([id], fetchOptions)
    return types[typeCode]
  }

  async searchVocabularyType(typeCode) {
    const id = new openbis.VocabularyPermId(typeCode)
    const fetchOptions = new openbis.VocabularyFetchOptions()
    const types = await openbis.getVocabularies([id], fetchOptions)
    return types[typeCode]
  }
}
