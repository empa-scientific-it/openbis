import messages from '@src/js/common/messages.js'

function nodeId(...parts) {
  return parts.join('__')
}

function rootNode() {
  const TYPE_ROOT = 'root'
  return {
    id: TYPE_ROOT,
    object: {
      type: TYPE_ROOT,
      id: TYPE_ROOT
    },
    text: '',
    root: true,
    canHaveChildren: true
  }
}

function loadMoreResults(parentId, count) {
  const TYPE_LOAD_MORE = 'loadMore'
  return {
    id: nodeId(parentId, TYPE_LOAD_MORE),
    object: {
      type: TYPE_LOAD_MORE,
      id: TYPE_LOAD_MORE
    },
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
    object: {
      type: TYPE_WARNING,
      id: TYPE_WARNING
    },
    message: {
      type: TYPE_WARNING,
      text: messages.get(messages.TOO_MANY_FILTERED_RESULTS_FOUND)
    },
    selectable: false
  }
}

export default {
  nodeId,
  rootNode,
  loadMoreResults,
  tooManyResultsFound
}
