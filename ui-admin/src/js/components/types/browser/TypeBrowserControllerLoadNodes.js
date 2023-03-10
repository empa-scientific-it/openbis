import _ from 'lodash'
import BrowserCommon from '@src/js/components/common/browser/BrowserCommon.js'
import TypeBrowserCommon from '@src/js/components/types/browser/TypeBrowserCommon.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'
import compare from '@src/js/common/compare.js'

export default class TypeBrowserControllerLoadNodes {
  async doLoadNodes(params) {
    const { node } = params

    const rootNode = BrowserCommon.rootNode()

    if (node.internalRoot) {
      return {
        nodes: [rootNode]
      }
    } else if (node.object.type === rootNode.object.type) {
      if (!_.isNil(params.filter)) {
        const [
          objectTypes,
          collectionTypes,
          dataSetTypes,
          materialTypes,
          vocabularyTypes
        ] = await Promise.all([
          this.searchObjectTypes({
            ...params,
            limit: TypeBrowserCommon.LOAD_LIMIT
          }),
          this.searchCollectionTypes({
            ...params,
            limit: TypeBrowserCommon.LOAD_LIMIT
          }),
          this.searchDataSetTypes({
            ...params,
            limit: TypeBrowserCommon.LOAD_LIMIT
          }),
          this.searchMaterialTypes({
            ...params,
            limit: TypeBrowserCommon.LOAD_LIMIT
          }),
          this.searchVocabularyTypes({
            ...params,
            limit: TypeBrowserCommon.LOAD_LIMIT
          })
        ])

        const totalCount =
          objectTypes.totalCount +
          collectionTypes.totalCount +
          dataSetTypes.totalCount +
          materialTypes.totalCount +
          vocabularyTypes.totalCount

        if (totalCount > TypeBrowserCommon.TOTAL_LOAD_LIMIT) {
          return {
            nodes: [BrowserCommon.tooManyResultsFound()]
          }
        }

        const nodes = []

        if (!_.isEmpty(objectTypes.objects)) {
          const folderNode = TypeBrowserCommon.objectTypesFolderNode()
          const typesNodes = this.createNodes(
            objectTypes,
            objectType.OBJECT_TYPE
          )
          folderNode.children = typesNodes
          folderNode.expanded = true
          nodes.push(folderNode)
        }

        if (!_.isEmpty(collectionTypes.objects)) {
          const folderNode = TypeBrowserCommon.collectionTypesFolderNode()
          const typesNodes = this.createNodes(
            collectionTypes,
            objectType.COLLECTION_TYPE
          )
          folderNode.children = typesNodes
          folderNode.expanded = true
          nodes.push(folderNode)
        }

        if (!_.isEmpty(dataSetTypes.objects)) {
          const folderNode = TypeBrowserCommon.dataSetTypesFolderNode()
          const typesNodes = this.createNodes(
            dataSetTypes,
            objectType.DATA_SET_TYPE
          )
          folderNode.children = typesNodes
          folderNode.expanded = true
          nodes.push(folderNode)
        }

        if (!_.isEmpty(materialTypes.objects)) {
          const folderNode = TypeBrowserCommon.materialTypesFolderNode()
          const typesNodes = this.createNodes(
            materialTypes,
            objectType.MATERIAL_TYPE
          )
          folderNode.children = typesNodes
          folderNode.expanded = true
          nodes.push(folderNode)
        }

        if (!_.isEmpty(vocabularyTypes.objects)) {
          const folderNode = TypeBrowserCommon.vocabularyTypesFolderNode()
          const typesNodes = this.createNodes(
            vocabularyTypes,
            objectType.VOCABULARY_TYPE
          )
          folderNode.children = typesNodes
          folderNode.expanded = true
          nodes.push(folderNode)
        }

        return {
          nodes: nodes
        }
      } else {
        return {
          nodes: [
            TypeBrowserCommon.objectTypesFolderNode(),
            TypeBrowserCommon.collectionTypesFolderNode(),
            TypeBrowserCommon.dataSetTypesFolderNode(),
            TypeBrowserCommon.materialTypesFolderNode(),
            TypeBrowserCommon.vocabularyTypesFolderNode(),
            TypeBrowserCommon.propertyTypesFolderNode()
          ]
        }
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
        return this.createNodes(types, node.object.id)
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
    const { filter, offset, limit, childrenIn, childrenNotIn } = params

    const criteria = new openbis.SampleTypeSearchCriteria()
    if (!_.isNil(filter)) {
      criteria.withCode().thatContains(filter)
    }
    if (!_.isEmpty(childrenIn)) {
      criteria.withCodes().thatIn(childrenIn.map(child => child.object.id))
    }
    const fetchOptions = new openbis.SampleTypeFetchOptions()

    const result = await openbis.searchSampleTypes(criteria, fetchOptions)

    if (!_.isEmpty(childrenNotIn)) {
      const childrenNotInMap = {}
      childrenNotIn.forEach(child => {
        childrenNotInMap[child.object.id] = child
      })
      result.objects = result.objects.filter(object =>
        _.isNil(childrenNotInMap[object.getCode()])
      )
      result.totalCount = result.objects.length
    }

    let objects = result.objects.map(o => ({
      id: o.getCode(),
      text: o.getCode()
    }))

    objects.sort((o1, o2) => compare(o1.text, o2.text))

    if (!_.isNil(offset) && !_.isNil(limit)) {
      objects = objects.slice(offset, offset + limit)
    }

    return {
      objects: objects,
      totalCount: result.totalCount
    }
  }

  async searchCollectionTypes(params) {
    const { filter, offset, limit, childrenIn, childrenNotIn } = params

    const criteria = new openbis.ExperimentTypeSearchCriteria()
    if (!_.isNil(filter)) {
      criteria.withCode().thatContains(filter)
    }
    if (!_.isEmpty(childrenIn)) {
      criteria.withCodes().thatIn(childrenIn.map(child => child.object.id))
    }
    const fetchOptions = new openbis.ExperimentTypeFetchOptions()

    const result = await openbis.searchExperimentTypes(criteria, fetchOptions)

    if (!_.isEmpty(childrenNotIn)) {
      const childrenNotInMap = {}
      childrenNotIn.forEach(child => {
        childrenNotInMap[child.object.id] = child
      })
      result.objects = result.objects.filter(object =>
        _.isNil(childrenNotInMap[object.getCode()])
      )
      result.totalCount = result.objects.length
    }

    let objects = result.objects.map(o => ({
      id: o.getCode(),
      text: o.getCode()
    }))

    objects.sort((o1, o2) => compare(o1.text, o2.text))

    if (!_.isNil(offset) && !_.isNil(limit)) {
      objects = objects.slice(offset, offset + limit)
    }

    return {
      objects: objects,
      totalCount: result.totalCount
    }
  }

  async searchDataSetTypes(params) {
    const { filter, offset, limit, childrenIn, childrenNotIn } = params

    const criteria = new openbis.DataSetTypeSearchCriteria()
    if (!_.isNil(filter)) {
      criteria.withCode().thatContains(filter)
    }
    if (!_.isEmpty(childrenIn)) {
      criteria.withCodes().thatIn(childrenIn.map(child => child.object.id))
    }
    const fetchOptions = new openbis.DataSetTypeFetchOptions()

    const result = await openbis.searchDataSetTypes(criteria, fetchOptions)

    if (!_.isEmpty(childrenNotIn)) {
      const childrenNotInMap = {}
      childrenNotIn.forEach(child => {
        childrenNotInMap[child.object.id] = child
      })
      result.objects = result.objects.filter(object =>
        _.isNil(childrenNotInMap[object.getCode()])
      )
      result.totalCount = result.objects.length
    }

    let objects = result.objects.map(o => ({
      id: o.getCode(),
      text: o.getCode()
    }))

    objects.sort((o1, o2) => compare(o1.text, o2.text))

    if (!_.isNil(offset) && !_.isNil(limit)) {
      objects = objects.slice(offset, offset + limit)
    }

    return {
      objects: objects,
      totalCount: result.totalCount
    }
  }

  async searchMaterialTypes(params) {
    const { filter, offset, limit, childrenIn, childrenNotIn } = params

    const criteria = new openbis.MaterialTypeSearchCriteria()
    if (!_.isNil(filter)) {
      criteria.withCode().thatContains(filter)
    }
    if (!_.isEmpty(childrenIn)) {
      criteria.withCodes().thatIn(childrenIn.map(child => child.object.id))
    }
    const fetchOptions = new openbis.MaterialTypeFetchOptions()

    const result = await openbis.searchMaterialTypes(criteria, fetchOptions)

    if (!_.isEmpty(childrenNotIn)) {
      const childrenNotInMap = {}
      childrenNotIn.forEach(child => {
        childrenNotInMap[child.object.id] = child
      })
      result.objects = result.objects.filter(object =>
        _.isNil(childrenNotInMap[object.getCode()])
      )
      result.totalCount = result.objects.length
    }

    let objects = result.objects.map(o => ({
      id: o.getCode(),
      text: o.getCode()
    }))

    objects.sort((o1, o2) => compare(o1.text, o2.text))

    if (!_.isNil(offset) && !_.isNil(limit)) {
      objects = objects.slice(offset, offset + limit)
    }

    return {
      objects: objects,
      totalCount: result.totalCount
    }
  }

  async searchVocabularyTypes(params) {
    const { filter, offset, limit, childrenIn, childrenNotIn } = params

    const criteria = new openbis.VocabularySearchCriteria()
    if (!_.isNil(filter)) {
      criteria.withCode().thatContains(filter)
    }
    if (!_.isEmpty(childrenIn)) {
      criteria.withCodes().thatIn(childrenIn.map(child => child.object.id))
    }
    const fetchOptions = new openbis.VocabularyFetchOptions()

    const result = await openbis.searchVocabularies(criteria, fetchOptions)

    if (!_.isEmpty(childrenNotIn)) {
      const childrenNotInMap = {}
      childrenNotIn.forEach(child => {
        childrenNotInMap[child.object.id] = child
      })
      result.objects = result.objects.filter(object =>
        _.isNil(childrenNotInMap[object.getCode()])
      )
      result.totalCount = result.objects.length
    }

    let objects = result.objects.map(o => ({
      id: o.getCode(),
      text: o.getCode()
    }))

    objects.sort((o1, o2) => compare(o1.text, o2.text))

    if (!_.isNil(offset) && !_.isNil(limit)) {
      objects = objects.slice(offset, offset + limit)
    }

    return {
      objects: objects,
      totalCount: result.totalCount
    }
  }

  createNodes(result, objectType) {
    const nodes = result.objects.map(object => ({
      text: object.text,
      object: {
        type: objectType,
        id: object.id
      }
    }))

    return {
      nodes: nodes,
      totalCount: result.totalCount
    }
  }
}
