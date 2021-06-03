import _ from 'lodash'
import { compile, match } from 'path-to-regexp'
import pages from '@src/js/common/consts/pages.js'
import objectTypes from '@src/js/common/consts/objectType.js'

class Route {
  constructor(pattern, params) {
    this.pattern = pattern
    this.params = params
  }

  format(params) {
    if (_.isMatch(params, this.params)) {
      let toPath = compile(this.pattern, { encode: encodeURIComponent })
      return {
        path: toPath(params),
        specificity: Object.keys(this.params).length
      }
    } else {
      return null
    }
  }

  parse(path) {
    let toPathObject = match(this.pattern, { decode: decodeURIComponent })
    let pathObject = toPathObject(path)
    if (pathObject) {
      return {
        path: pathObject.path,
        ...pathObject.params,
        ...this.params
      }
    } else {
      return null
    }
  }
}

class DefaultRoute {
  format() {
    return {
      path: '/',
      specificity: 0
    }
  }
  parse() {
    return {
      path: '/',
      page: pages.TYPES
    }
  }
}

const routes = {
  TYPES: new Route('/types', {
    page: pages.TYPES
  }),
  TYPES_SEARCH: new Route('/types-search/:id', {
    page: pages.TYPES,
    type: objectTypes.SEARCH
  }),
  NEW_OBJECT_TYPE: new Route('/new-object-type/:id', {
    page: pages.TYPES,
    type: objectTypes.NEW_OBJECT_TYPE
  }),
  OBJECT_TYPE: new Route('/object-type/:id', {
    page: pages.TYPES,
    type: objectTypes.OBJECT_TYPE
  }),
  OBJECT_TYPE_OVERVIEW: new Route('/object-type-overview', {
    page: pages.TYPES,
    type: objectTypes.OVERVIEW,
    id: objectTypes.OBJECT_TYPE
  }),
  NEW_COLLECTION_TYPE: new Route('/new-collection-type/:id', {
    page: pages.TYPES,
    type: objectTypes.NEW_COLLECTION_TYPE
  }),
  COLLECTION_TYPE: new Route('/collection-type/:id', {
    page: pages.TYPES,
    type: objectTypes.COLLECTION_TYPE
  }),
  COLLECTION_TYPE_OVERVIEW: new Route('/collection-type-overview', {
    page: pages.TYPES,
    type: objectTypes.OVERVIEW,
    id: objectTypes.COLLECTION_TYPE
  }),
  NEW_DATA_SET_TYPE: new Route('/new-dataset-type/:id', {
    page: pages.TYPES,
    type: objectTypes.NEW_DATA_SET_TYPE
  }),
  DATA_SET_TYPE: new Route('/dataset-type/:id', {
    page: pages.TYPES,
    type: objectTypes.DATA_SET_TYPE
  }),
  DATA_SET_TYPE_OVERVIEW: new Route('/dataset-type-overview', {
    page: pages.TYPES,
    type: objectTypes.OVERVIEW,
    id: objectTypes.DATA_SET_TYPE
  }),
  NEW_MATERIAL_TYPE: new Route('/new-material-type/:id', {
    page: pages.TYPES,
    type: objectTypes.NEW_MATERIAL_TYPE
  }),
  MATERIAL_TYPE: new Route('/material-type/:id', {
    page: pages.TYPES,
    type: objectTypes.MATERIAL_TYPE
  }),
  MATERIAL_TYPE_OVERVIEW: new Route('/material-type-overview', {
    page: pages.TYPES,
    type: objectTypes.OVERVIEW,
    id: objectTypes.MATERIAL_TYPE
  }),
  NEW_VOCABULARY_TYPE: new Route('/new-vocabulary-type/:id', {
    page: pages.TYPES,
    type: objectTypes.NEW_VOCABULARY_TYPE
  }),
  VOCABULARY_TYPE: new Route('/vocabulary-type/:id', {
    page: pages.TYPES,
    type: objectTypes.VOCABULARY_TYPE
  }),
  VOCABULARY_TYPE_OVERVIEW: new Route('/vocabulary-type-overview', {
    page: pages.TYPES,
    type: objectTypes.OVERVIEW,
    id: objectTypes.VOCABULARY_TYPE
  }),
  USERS: new Route('/users', {
    page: pages.USERS
  }),
  USERS_SEARCH: new Route('/users-search/:id', {
    page: pages.USERS,
    type: objectTypes.SEARCH
  }),
  NEW_USER: new Route('/new-user/:id', {
    page: pages.USERS,
    type: objectTypes.NEW_USER
  }),
  USER: new Route('/user/:id', {
    page: pages.USERS,
    type: objectTypes.USER
  }),
  USER_OVERVIEW: new Route('/user-overview', {
    page: pages.USERS,
    type: objectTypes.OVERVIEW,
    id: objectTypes.USER
  }),
  NEW_USER_GROUP: new Route('/new-user-group/:id', {
    page: pages.USERS,
    type: objectTypes.NEW_USER_GROUP
  }),
  USER_GROUP: new Route('/user-group/:id', {
    page: pages.USERS,
    type: objectTypes.USER_GROUP
  }),
  USER_GROUP_OVERVIEW: new Route('/user-group-overview', {
    page: pages.USERS,
    type: objectTypes.OVERVIEW,
    id: objectTypes.USER_GROUP
  }),
  TOOLS: new Route('/tools', {
    page: pages.TOOLS
  }),
  TOOLS_SEARCH: new Route('/tools-search/:id', {
    page: pages.TOOLS,
    type: objectTypes.SEARCH
  }),
  NEW_DYNAMIC_PROPERTY_PLUGIN: new Route('/new-dynamic-property-plugin/:id', {
    page: pages.TOOLS,
    type: objectTypes.NEW_DYNAMIC_PROPERTY_PLUGIN
  }),
  DYNAMIC_PROPERTY_PLUGIN: new Route('/dynamic-property-plugin/:id', {
    page: pages.TOOLS,
    type: objectTypes.DYNAMIC_PROPERTY_PLUGIN
  }),
  DYNAMIC_PROPERTY_PLUGIN_OVERVIEW: new Route(
    '/dynamic-property-plugin-overview',
    {
      page: pages.TOOLS,
      type: objectTypes.OVERVIEW,
      id: objectTypes.DYNAMIC_PROPERTY_PLUGIN
    }
  ),
  NEW_ENTITY_VALIDATION_PLUGIN: new Route('/new-entity-validation-plugin/:id', {
    page: pages.TOOLS,
    type: objectTypes.NEW_ENTITY_VALIDATION_PLUGIN
  }),
  ENTITY_VALIDATION_PLUGIN: new Route('/entity-validation-plugin/:id', {
    page: pages.TOOLS,
    type: objectTypes.ENTITY_VALIDATION_PLUGIN
  }),
  ENTITY_VALIDATION_PLUGIN_OVERVIEW: new Route(
    '/entity-validation-plugin-overview',
    {
      page: pages.TOOLS,
      type: objectTypes.OVERVIEW,
      id: objectTypes.ENTITY_VALIDATION_PLUGIN
    }
  ),
  NEW_QUERY: new Route('/new-query/:id', {
    page: pages.TOOLS,
    type: objectTypes.NEW_QUERY
  }),
  QUERY: new Route('/query/:id', {
    page: pages.TOOLS,
    type: objectTypes.QUERY
  }),
  QUERY_OVERVIEW: new Route('/query-overview', {
    page: pages.TOOLS,
    type: objectTypes.OVERVIEW,
    id: objectTypes.QUERY
  }),
  DEFAULT: new DefaultRoute()
}

function format(params) {
  let keys = Object.keys(routes)
  let best = { specificity: 0, path: null }

  for (let i = 0; i < keys.length; i++) {
    let route = routes[keys[i]]
    try {
      let result = route.format(params)
      if (result && result.specificity > best.specificity) {
        best = result
      }
    } catch (err) {
      // ignore problems with incorrect params
    }
  }

  return best.path
}

function parse(path) {
  let keys = Object.keys(routes)
  for (let i = 0; i < keys.length; i++) {
    let route = routes[keys[i]]
    let params = route.parse(path)
    if (params) {
      return params
    }
  }
  return null
}

export default {
  ...routes,
  format,
  parse
}
