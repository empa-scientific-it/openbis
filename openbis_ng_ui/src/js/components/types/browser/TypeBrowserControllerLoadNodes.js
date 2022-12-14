import _ from 'lodash'
import BrowserCommon from '@src/js/components/common/browser/BrowserCommon.js'
import TypeBrowserCommon from '@src/js/components/types/browser/TypeBrowserCommon.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'
import compare from '@src/js/common/compare.js'

const LOAD_LIMIT = 100
const TOTAL_LOAD_LIMIT = 500

export default class TypeBrowserControllerLoadNodes {
  async doLoadNodes(params) {
    const { node } = params

    const rootNode = BrowserCommon.rootNode()

    if (node.internalRoot) {
      return {
        nodes: [rootNode]
      }
    } else if (node.object.type === rootNode.object.type) {
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
          objectTypes.totalCount +
          collectionTypes.totalCount +
          dataSetTypes.totalCount +
          materialTypes.totalCount +
          vocabularyTypes.totalCount

        if (totalCount > TOTAL_LOAD_LIMIT) {
          return BrowserCommon.tooManyResultsFound(node.id)
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
        nodes.forEach(node => {
          node.expanded = true
        })
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

  async searchObjectTypes(params) {
    const { filter, offset } = params

    const criteria = new openbis.SampleTypeSearchCriteria()
    if (filter) {
      criteria.withCode().thatContains(filter)
    }
    const fetchOptions = new openbis.SampleTypeFetchOptions()

    const result = await openbis.searchSampleTypes(criteria, fetchOptions)

    return {
      objects: result.getObjects().map(o => ({
        id: o.getCode(),
        text: o.getCode()
      })),
      totalCount: result.getTotalCount(),
      filter,
      offset
    }
  }

  async searchCollectionTypes(params) {
    const { filter, offset } = params

    const criteria = new openbis.ExperimentTypeSearchCriteria()
    if (filter) {
      criteria.withCode().thatContains(filter)
    }
    const fetchOptions = new openbis.ExperimentTypeFetchOptions()

    const result = await openbis.searchExperimentTypes(criteria, fetchOptions)

    return {
      objects: result.getObjects().map(o => ({
        id: o.getCode(),
        text: o.getCode()
      })),
      totalCount: result.getTotalCount(),
      filter,
      offset
    }
  }

  async searchDataSetTypes(params) {
    const { filter, offset } = params

    const criteria = new openbis.DataSetTypeSearchCriteria()
    if (filter) {
      criteria.withCode().thatContains(filter)
    }
    const fetchOptions = new openbis.DataSetTypeFetchOptions()

    const result = await openbis.searchDataSetTypes(criteria, fetchOptions)

    return {
      objects: result.getObjects().map(o => ({
        id: o.getCode(),
        text: o.getCode()
      })),
      totalCount: result.getTotalCount(),
      filter,
      offset
    }
  }

  async searchMaterialTypes(params) {
    const { filter, offset } = params

    const criteria = new openbis.MaterialTypeSearchCriteria()
    if (filter) {
      criteria.withCode().thatContains(filter)
    }
    const fetchOptions = new openbis.MaterialTypeFetchOptions()

    const result = await openbis.searchMaterialTypes(criteria, fetchOptions)

    return {
      objects: result.getObjects().map(o => ({
        id: o.getCode(),
        text: o.getCode()
      })),
      totalCount: result.getTotalCount(),
      filter,
      offset
    }
  }

  async searchVocabularyTypes(params) {
    const { filter, offset } = params

    const criteria = new openbis.VocabularySearchCriteria()
    if (filter) {
      criteria.withCode().thatContains(filter)
    }
    const fetchOptions = new openbis.VocabularyFetchOptions()

    const result = await openbis.searchVocabularies(criteria, fetchOptions)

    return {
      objects: result.getObjects().map(o => ({
        id: o.getCode(),
        text: o.getCode()
      })),
      totalCount: result.getTotalCount(),
      filter,
      offset
    }
  }

  createObjectTypesNode(parent, result) {
    const folderNode = TypeBrowserCommon.objectTypesFolderNode(parent.id)

    if (result) {
      folderNode.children = this.createNodes(
        folderNode,
        result,
        objectType.OBJECT_TYPE
      )
    }

    return folderNode
  }

  createCollectionTypesNode(parent, result) {
    const folderNode = TypeBrowserCommon.collectionTypesFolderNode(parent.id)

    if (result) {
      folderNode.children = this.createNodes(
        folderNode,
        result,
        objectType.COLLECTION_TYPE
      )
    }

    return folderNode
  }

  createDataSetTypesNode(parent, result) {
    const folderNode = TypeBrowserCommon.dataSetTypesFolderNode(parent.id)

    if (result) {
      folderNode.children = this.createNodes(
        folderNode,
        result,
        objectType.DATA_SET_TYPE
      )
    }

    return folderNode
  }

  createMaterialTypesNode(parent, result) {
    const folderNode = TypeBrowserCommon.materialTypesFolderNode(parent.id)

    if (result) {
      folderNode.children = this.createNodes(
        folderNode,
        result,
        objectType.MATERIAL_TYPE
      )
    }

    return folderNode
  }

  createVocabularyTypesNode(parent, result) {
    const folderNode = TypeBrowserCommon.vocabularyTypesFolderNode(parent.id)

    if (result) {
      folderNode.children = this.createNodes(
        folderNode,
        result,
        objectType.VOCABULARY_TYPE
      )
    }

    return folderNode
  }

  createPropertyTypesNode(parent) {
    return TypeBrowserCommon.propertyTypesFolderNode(parent.id)
  }

  createNodes(parent, result, objectType) {
    let objects = result.objects
    objects.sort((o1, o2) => compare(o1.text, o2.text))
    objects = objects.slice(result.offset, result.offset + LOAD_LIMIT)

    let nodes = objects.map(object => ({
      id: BrowserCommon.nodeId(parent.id, object.id),
      text: object.text,
      object: {
        type: objectType,
        id: object.id
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
          totalCount: result.totalCount,
          append: true
        }
      }
    }
  }
}
