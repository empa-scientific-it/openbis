import _ from 'lodash'
import TypeBrowserConsts from '@src/js/components/types/browser2/TypeBrowserConsts.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'
import messages from '@src/js/common/messages.js'
import compare from '@src/js/common/compare.js'

const LOAD_LIMIT = 50
const TOTAL_LOAD_LIMIT = 500

export default class TypeBrowserConstsLoadNodesFiltered {
  async doLoadFilteredNodes(params) {
    const { node } = params

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

    const loadedCount =
      objectTypes.getObjects().length +
      collectionTypes.getObjects().length +
      dataSetTypes.getObjects().length +
      materialTypes.getObjects().length +
      vocabularyTypes.getObjects().length

    const totalCount =
      objectTypes.getTotalCount() +
      collectionTypes.getTotalCount() +
      dataSetTypes.getTotalCount() +
      materialTypes.getTotalCount() +
      vocabularyTypes.getTotalCount()

    if (totalCount > TOTAL_LOAD_LIMIT) {
      return this.tooManyResultsFound(node)
    }

    const result = {
      nodes: [],
      loadMore: {
        offset: 0,
        limit: TOTAL_LOAD_LIMIT,
        loadedCount: loadedCount,
        totalCount: totalCount,
        append: false
      }
    }

    if (node.internalRoot) {
      const root = {
        id: TypeBrowserConsts.TYPE_ROOT,
        object: {
          type: TypeBrowserConsts.TYPE_ROOT
        },
        canHaveChildren: true
      }
      root.children = this.doLoadFilteredNodes({
        ...params,
        node: root
      })
      result.nodes.push(root)
    } else if (node.object.type === TypeBrowserConsts.TYPE_ROOT) {
      if (!_.isEmpty(objectTypes.getObjects())) {
        const objectTypesNode = this.createObjectTypesNode(
          params,
          objectTypes.getObjects()
        )
        result.nodes.push(objectTypesNode)
      }
      if (!_.isEmpty(collectionTypes.getObjects())) {
        const collectionTypesNode = this.createCollectionTypesNode(
          params,
          collectionTypes.getObjects()
        )
        result.nodes.push(collectionTypesNode)
      }
      if (!_.isEmpty(dataSetTypes.getObjects())) {
        const dataSetTypesNode = this.createDataSetTypesNode(
          params,
          dataSetTypes.getObjects()
        )
        result.nodes.push(dataSetTypesNode)
      }
      if (!_.isEmpty(materialTypes.getObjects())) {
        const materialTypesNode = this.createMaterialTypesNode(
          params,
          materialTypes.getObjects()
        )
        result.nodes.push(materialTypesNode)
      }
      if (!_.isEmpty(vocabularyTypes.getObjects())) {
        const vocabularyTypesNode = this.createVocabularyTypesNode(
          params,
          vocabularyTypes.getObjects()
        )
        result.nodes.push(vocabularyTypesNode)
      }
    }

    return result
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
    const { filter, offset, limit } = params

    const criteria = new openbis.SampleTypeSearchCriteria()
    criteria.withCode().thatContains(filter)

    const fetchOptions = new openbis.SampleTypeFetchOptions()
    fetchOptions.from(offset)
    fetchOptions.count(limit || LOAD_LIMIT)

    const result = await openbis.searchSampleTypes(criteria, fetchOptions)

    return result
  }

  async searchCollectionTypes(params) {
    const { filter, offset, limit } = params

    const criteria = new openbis.ExperimentTypeSearchCriteria()
    criteria.withCode().thatContains(filter)

    const fetchOptions = new openbis.ExperimentTypeFetchOptions()
    fetchOptions.from(offset)
    fetchOptions.count(limit || LOAD_LIMIT)

    const result = await openbis.searchExperimentTypes(criteria, fetchOptions)

    return result
  }

  async searchDataSetTypes(params) {
    const { filter, offset, limit } = params

    const criteria = new openbis.DataSetTypeSearchCriteria()
    criteria.withCode().thatContains(filter)

    const fetchOptions = new openbis.DataSetTypeFetchOptions()
    fetchOptions.from(offset)
    fetchOptions.count(limit || LOAD_LIMIT)

    const result = await openbis.searchDataSetTypes(criteria, fetchOptions)

    return result
  }

  async searchMaterialTypes(params) {
    const { filter, offset, limit } = params

    const criteria = new openbis.MaterialTypeSearchCriteria()
    criteria.withCode().thatContains(filter)

    const fetchOptions = new openbis.MaterialTypeFetchOptions()
    fetchOptions.from(offset)
    fetchOptions.count(limit || LOAD_LIMIT)

    const result = await openbis.searchMaterialTypes(criteria, fetchOptions)

    return result
  }

  async searchVocabularyTypes(params) {
    const { filter, offset, limit } = params

    const criteria = new openbis.VocabularySearchCriteria()
    criteria.withCode().thatContains(filter)

    const fetchOptions = new openbis.VocabularyFetchOptions()
    fetchOptions.from(offset)
    fetchOptions.count(limit || LOAD_LIMIT)

    const result = await openbis.searchVocabularies(criteria, fetchOptions)

    return result
  }

  createObjectTypesNode(params, types) {
    return this.createNodesFolder(
      params,
      types,
      objectType.OBJECT_TYPE,
      TypeBrowserConsts.TEXT_OBJECT_TYPES
    )
  }

  createCollectionTypesNode(params, types) {
    return this.createNodesFolder(
      params,
      types,
      objectType.COLLECTION_TYPE,
      TypeBrowserConsts.TEXT_COLLECTION_TYPES
    )
  }

  createDataSetTypesNode(params, types) {
    return this.createNodesFolder(
      params,
      types,
      objectType.DATA_SET_TYPE,
      TypeBrowserConsts.TEXT_DATA_SET_TYPES
    )
  }

  createMaterialTypesNode(params, types) {
    return this.createNodesFolder(
      params,
      types,
      objectType.MATERIAL_TYPE,
      TypeBrowserConsts.TEXT_MATERIAL_TYPES
    )
  }

  createVocabularyTypesNode(params, types) {
    return this.createNodesFolder(
      params,
      types,
      objectType.VOCABULARY_TYPE,
      TypeBrowserConsts.TEXT_VOCABULARY_TYPES
    )
  }

  createNodesFolder(params, types, folderObjectType, folderText) {
    const { node } = params

    const folderNode = {
      id: TypeBrowserConsts.nodeId(node.id, folderObjectType),
      text: folderText,
      object: {
        type: objectType.OVERVIEW,
        id: folderObjectType
      },
      canHaveChildren: true,
      children: { nodes: [] },
      expanded: true
    }

    types.sort((o1, o2) => compare(o1.code, o2.code))

    types.forEach(type => {
      const typeNode = {
        id: TypeBrowserConsts.nodeId(
          folderNode.id,
          folderObjectType,
          type.code
        ),
        text: type.code,
        object: {
          type: folderObjectType,
          id: type.code
        }
      }
      folderNode.children.nodes.push(typeNode)
    })

    return folderNode
  }
}
