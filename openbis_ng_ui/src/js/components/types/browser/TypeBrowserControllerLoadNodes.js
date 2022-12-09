import _ from 'lodash'
import TypeBrowserConsts from '@src/js/components/types/browser/TypeBrowserConsts.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'
import messages from '@src/js/common/messages.js'
import compare from '@src/js/common/compare.js'

const LOAD_LIMIT = 100
const TOTAL_LOAD_LIMIT = 500

export default class TypeBrowserConstsLoadNodesUnfiltered {
  async doLoadNodes(params) {
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
      const [
        objectTypes,
        collectionTypes,
        dataSetTypes,
        materialTypes,
        vocabularyTypes
      ] = await Promise.all([
        this.searchObjectTypes(params),
        this.searchCollectionTypes(params),
        this.searchDataSetTypes(params),
        this.searchMaterialTypes(params),
        this.searchVocabularyTypes(params)
      ])

      if (params.filter) {
        const totalCount =
          objectTypes.getTotalCount() +
          collectionTypes.getTotalCount() +
          dataSetTypes.getTotalCount() +
          materialTypes.getTotalCount() +
          vocabularyTypes.getTotalCount()

        if (totalCount > TOTAL_LOAD_LIMIT) {
          return this.tooManyResultsFound(node)
        }
      }

      let nodes = [
        this.createObjectTypesNode(node, objectTypes),
        this.createCollectionTypesNode(node, collectionTypes),
        this.createDataSetTypesNode(node, dataSetTypes),
        this.createMaterialTypesNode(node, materialTypes),
        this.createVocabularyTypesNode(node, vocabularyTypes),
        this.createPropertyTypesNode(node, null)
      ]

      if (params.filter) {
        nodes = nodes.filter(node => !_.isEmpty(node.children))
      }

      return {
        nodes: nodes
      }
    } else if (node.object.type === objectType.OVERVIEW) {
      let types = null

      if (node.object.id === objectType.OBJECT_TYPE) {
        types = await this.searchObjectTypes(params)
      } else if (node.object.id === objectType.COLLECTION_TYPE) {
        types = await this.searchCollectionTypes(params)
      } else if (node.object.id === objectType.DATA_SET_TYPE) {
        types = await this.searchDataSetTypes(params)
      } else if (node.object.id === objectType.MATERIAL_TYPE) {
        types = await this.searchMaterialTypes(params)
      } else if (node.object.id === objectType.VOCABULARY_TYPE) {
        types = await this.searchVocabularyTypes(params)
      }

      if (types) {
        return this.createNodes(node, types, node.object.id)
      } else {
        return {
          nodes: []
        }
      }
    } else {
      return null
    }
  }

  tooManyResultsFound(node) {
    return {
      nodes: [
        {
          id: TypeBrowserConsts.nodeId(node.id, TypeBrowserConsts.TYPE_WARNING),
          message: {
            type: 'warning',
            text: messages.get(messages.TOO_MANY_FILTERED_RESULTS_FOUND)
          },
          selectable: false
        }
      ]
    }
  }

  async searchObjectTypes(params) {
    const { filter, offset } = params

    const criteria = new openbis.SampleTypeSearchCriteria()
    if (filter) {
      criteria.withCode().thatContains(filter)
    }
    const fetchOptions = new openbis.SampleTypeFetchOptions()

    const result = await openbis.searchSampleTypes(criteria, fetchOptions)
    result.filter = filter
    result.offset = offset
    return result
  }

  async searchCollectionTypes(params) {
    const { filter, offset } = params

    const criteria = new openbis.ExperimentTypeSearchCriteria()
    if (filter) {
      criteria.withCode().thatContains(filter)
    }
    const fetchOptions = new openbis.ExperimentTypeFetchOptions()

    const result = await openbis.searchExperimentTypes(criteria, fetchOptions)
    result.filter = filter
    result.offset = offset
    return result
  }

  async searchDataSetTypes(params) {
    const { filter, offset } = params

    const criteria = new openbis.DataSetTypeSearchCriteria()
    if (filter) {
      criteria.withCode().thatContains(filter)
    }
    const fetchOptions = new openbis.DataSetTypeFetchOptions()

    const result = await openbis.searchDataSetTypes(criteria, fetchOptions)
    result.filter = filter
    result.offset = offset
    return result
  }

  async searchMaterialTypes(params) {
    const { filter, offset } = params

    const criteria = new openbis.MaterialTypeSearchCriteria()
    if (filter) {
      criteria.withCode().thatContains(filter)
    }
    const fetchOptions = new openbis.MaterialTypeFetchOptions()

    const result = await openbis.searchMaterialTypes(criteria, fetchOptions)
    result.filter = filter
    result.offset = offset
    return result
  }

  async searchVocabularyTypes(params) {
    const { filter, offset } = params

    const criteria = new openbis.VocabularySearchCriteria()
    if (filter) {
      criteria.withCode().thatContains(filter)
    }
    const fetchOptions = new openbis.VocabularyFetchOptions()

    const result = await openbis.searchVocabularies(criteria, fetchOptions)
    result.filter = filter
    result.offset = offset
    return result
  }

  createObjectTypesNode(parent, result) {
    return this.createFolderNode(
      parent,
      result,
      objectType.OBJECT_TYPE,
      TypeBrowserConsts.TEXT_OBJECT_TYPES
    )
  }

  createCollectionTypesNode(parent, result) {
    return this.createFolderNode(
      parent,
      result,
      objectType.COLLECTION_TYPE,
      TypeBrowserConsts.TEXT_COLLECTION_TYPES
    )
  }

  createDataSetTypesNode(parent, result) {
    return this.createFolderNode(
      parent,
      result,
      objectType.DATA_SET_TYPE,
      TypeBrowserConsts.TEXT_DATA_SET_TYPES
    )
  }

  createMaterialTypesNode(parent, result) {
    return this.createFolderNode(
      parent,
      result,
      objectType.MATERIAL_TYPE,
      TypeBrowserConsts.TEXT_MATERIAL_TYPES
    )
  }

  createVocabularyTypesNode(parent, result) {
    return this.createFolderNode(
      parent,
      result,
      objectType.VOCABULARY_TYPE,
      TypeBrowserConsts.TEXT_VOCABULARY_TYPES
    )
  }

  createPropertyTypesNode(parent, result) {
    return this.createFolderNode(
      parent,
      result,
      objectType.PROPERTY_TYPE,
      TypeBrowserConsts.TEXT_PROPERTY_TYPES
    )
  }

  createFolderNode(parent, result, folderObjectType, folderText) {
    const folderNode = {
      id: TypeBrowserConsts.nodeId(parent.id, folderObjectType),
      text: folderText,
      object: {
        type: objectType.OVERVIEW,
        id: folderObjectType
      },
      canHaveChildren: !!result,
      selectable: true,
      expanded: result && result.filter
    }

    if (result) {
      folderNode.children = this.createNodes(
        folderNode,
        result,
        folderObjectType
      )
    }

    return folderNode
  }

  createNodes(parent, result, objectType) {
    let objects = result.getObjects()
    objects.sort((o1, o2) => compare(o1.code, o2.code))
    objects = objects.slice(result.offset, result.offset + LOAD_LIMIT)

    let nodes = objects.map(type => ({
      id: TypeBrowserConsts.nodeId(parent.id, objectType, type.getCode()),
      text: type.getCode(),
      object: {
        type: objectType,
        id: type.getCode()
      }
    }))

    if (_.isEmpty(nodes)) {
      return null
    } else {
      return {
        nodes: nodes,
        loadMore: {
          offset: result.offset + nodes.length,
          loadedCount: result.offset + nodes.length,
          totalCount: result.getTotalCount(),
          append: true
        }
      }
    }
  }
}
