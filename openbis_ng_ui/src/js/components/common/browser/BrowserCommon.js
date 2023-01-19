import messages from '@src/js/common/messages.js'

const TYPE_ROOT = 'root'

function nodeId(...parts) {
  return parts.join('__')
}

function rootNode() {
  return {
    id: TYPE_ROOT,
    object: {
      type: TYPE_ROOT
    },
    canHaveChildren: true
  }
}

function loadMoreResults(parentId, count) {
  const TYPE_LOAD_MORE = 'loadMore'
  return {
    id: nodeId(parentId, TYPE_LOAD_MORE),
    message: {
      type: TYPE_LOAD_MORE,
      text: messages.get(messages.LOAD_MORE, count)
    },
    selectable: false
  }
}

function tooManyResultsFound(parentId) {
  const TYPE_WARNING = 'warning'
  return {
    id: nodeId(parentId, TYPE_WARNING),
    message: {
      type: TYPE_WARNING,
      text: messages.get(messages.TOO_MANY_FILTERED_RESULTS_FOUND)
    },
    selectable: false
  }
}

export default {
  TYPE_ROOT,
  nodeId,
  rootNode,
  loadMoreResults,
  tooManyResultsFound
}
