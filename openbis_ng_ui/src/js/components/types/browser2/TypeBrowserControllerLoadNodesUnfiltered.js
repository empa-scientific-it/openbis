import _ from 'lodash'
import TypeBrowserConsts from '@src/js/components/types/browser2/TypeBrowserConsts.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'
import compare from '@src/js/common/compare.js'

const LOAD_LIMIT = 50

export default class TypeBrowserConstsLoadNodesUnfiltered {
  async doLoadUnfilteredNodes(params) {
    const { node } = params

    if (node.internalRoot) {
      return {
        nodes: [
          {
            id: TypeBrowserConsts.TYPE_ROOT,
            object: {
              type: TypeBrowserConsts.TYPE_ROOT
            },
            canHaveChildren: true
          }
        ]
      }
    } else if (node.object.type === TypeBrowserConsts.TYPE_ROOT) {
      const nodes = []

      await this.addObjectTypesNode(params, nodes)
      await this.addCollectionTypesNode(params, nodes)
      await this.addDataSetTypesNode(params, nodes)
      await this.addMaterialTypesNode(params, nodes)
      await this.addVocabularyTypesNode(params, nodes)
      await this.addPropertyTypesNode(params, nodes)

      return {
        nodes: nodes
      }
    } else if (node.object.type === objectType.OVERVIEW) {
      if (node.object.id === objectType.OBJECT_TYPE) {
        return await this.searchObjectTypes(params)
      } else if (node.object.id === objectType.COLLECTION_TYPE) {
        return await this.searchCollectionTypes(params)
      } else if (node.object.id === objectType.DATA_SET_TYPE) {
        return await this.searchDataSetTypes(params)
      } else if (node.object.id === objectType.MATERIAL_TYPE) {
        return await this.searchMaterialTypes(params)
      } else if (node.object.id === objectType.VOCABULARY_TYPE) {
        return await this.searchVocabularyTypes(params)
      } else if (node.object.id === objectType.PROPERTY_TYPE) {
        return await this.searchPropertyTypes(params)
      }
    } else {
      return null
    }
  }

  async searchObjectTypes(params) {
    const criteria = new openbis.SampleTypeSearchCriteria()
    const fetchOptions = new openbis.SampleTypeFetchOptions()

    const result = await openbis.searchSampleTypes(criteria, fetchOptions)

    return this.createNodes(params, objectType.OBJECT_TYPE, result)
  }

  async searchCollectionTypes(params) {
    const criteria = new openbis.ExperimentTypeSearchCriteria()
    const fetchOptions = new openbis.ExperimentTypeFetchOptions()

    const result = await openbis.searchExperimentTypes(criteria, fetchOptions)

    return this.createNodes(params, objectType.COLLECTION_TYPE, result)
  }

  async searchDataSetTypes(params) {
    const criteria = new openbis.DataSetTypeSearchCriteria()
    const fetchOptions = new openbis.DataSetTypeFetchOptions()

    const result = await openbis.searchDataSetTypes(criteria, fetchOptions)

    return this.createNodes(params, objectType.DATA_SET_TYPE, result)
  }

  async searchMaterialTypes(params) {
    const criteria = new openbis.MaterialTypeSearchCriteria()
    const fetchOptions = new openbis.MaterialTypeFetchOptions()

    const result = await openbis.searchMaterialTypes(criteria, fetchOptions)

    return this.createNodes(params, objectType.MATERIAL_TYPE, result)
  }

  async searchVocabularyTypes(params) {
    const criteria = new openbis.VocabularySearchCriteria()
    const fetchOptions = new openbis.VocabularyFetchOptions()

    const result = await openbis.searchVocabularies(criteria, fetchOptions)

    return this.createNodes(params, objectType.VOCABULARY_TYPE, result)
  }

  async addObjectTypesNode(params, nodes) {
    const folderNode = await this.createNodesFolder(
      params,
      objectType.OBJECT_TYPE,
      TypeBrowserConsts.TEXT_OBJECT_TYPES,
      this.searchObjectTypes.bind(this)
    )
    if (folderNode) {
      nodes.push(folderNode)
    }
  }

  async addCollectionTypesNode(params, nodes) {
    const folderNode = await this.createNodesFolder(
      params,
      objectType.COLLECTION_TYPE,
      TypeBrowserConsts.TEXT_COLLECTION_TYPES,
      this.searchCollectionTypes.bind(this)
    )
    if (folderNode) {
      nodes.push(folderNode)
    }
  }

  async addDataSetTypesNode(params, nodes) {
    const folderNode = await this.createNodesFolder(
      params,
      objectType.DATA_SET_TYPE,
      TypeBrowserConsts.TEXT_DATA_SET_TYPES,
      this.searchDataSetTypes.bind(this)
    )
    if (folderNode) {
      nodes.push(folderNode)
    }
  }

  async addMaterialTypesNode(params, nodes) {
    const folderNode = await this.createNodesFolder(
      params,
      objectType.MATERIAL_TYPE,
      TypeBrowserConsts.TEXT_MATERIAL_TYPES,
      this.searchMaterialTypes.bind(this)
    )
    if (folderNode) {
      nodes.push(folderNode)
    }
  }

  async addVocabularyTypesNode(params, nodes) {
    const folderNode = await this.createNodesFolder(
      params,
      objectType.VOCABULARY_TYPE,
      TypeBrowserConsts.TEXT_VOCABULARY_TYPES,
      this.searchVocabularyTypes.bind(this)
    )
    if (folderNode) {
      nodes.push(folderNode)
    }
  }

  async addPropertyTypesNode(params, nodes) {
    const folderNode = await this.createNodesFolder(
      params,
      objectType.PROPERTY_TYPE,
      TypeBrowserConsts.TEXT_PROPERTY_TYPES,
      () => []
    )
    if (folderNode) {
      folderNode.canHaveChildren = false
      nodes.push(folderNode)
    }
  }

  createNodes(params, objectType, result) {
    const { node, offset } = params

    let nodes = result.getObjects().map(type => ({
      id: TypeBrowserConsts.nodeId(node.id, objectType, type.getCode()),
      text: type.getCode(),
      object: {
        type: objectType,
        id: type.getCode()
      }
    }))

    nodes.sort((n1, n2) => compare(n1.text, n2.text))
    nodes = nodes.slice(offset, offset + LOAD_LIMIT)

    if (_.isEmpty(nodes)) {
      return null
    } else {
      return {
        nodes: nodes,
        loadMore: {
          offset: offset + nodes.length,
          loadedCount: offset + nodes.length,
          totalCount: result.getTotalCount(),
          append: offset > 0
        }
      }
    }
  }

  async createNodesFolder(params, folderObjectType, folderText, search) {
    const { node } = params

    const folderNode = {
      id: TypeBrowserConsts.nodeId(node.id, folderObjectType),
      text: folderText,
      object: {
        type: objectType.OVERVIEW,
        id: folderObjectType
      },
      canHaveChildren: true,
      selectable: true
    }

    const nodes = await search({
      ...params,
      node: folderNode
    })

    if (nodes) {
      folderNode.children = nodes
      return folderNode
    } else {
      return null
    }
  }
}
