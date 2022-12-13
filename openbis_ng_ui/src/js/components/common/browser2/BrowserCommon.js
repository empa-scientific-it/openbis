import messages from '@src/js/common/messages.js'

function nodeId(...parts) {
  return parts.join('__')
}

function rootNode() {
  const TYPE_ROOT = 'root'
  return {
    id: TYPE_ROOT,
    object: {
      type: TYPE_ROOT
    },
    canHaveChildren: true
  }
}

function tooManyResultsFound(parentId) {
  const TYPE_WARNING = 'warning'
  return {
    nodes: [
      {
        id: nodeId(parentId, TYPE_WARNING),
        message: {
          type: TYPE_WARNING,
          text: messages.get(messages.TOO_MANY_FILTERED_RESULTS_FOUND)
        },
        selectable: false
      }
    ]
  }
}

export default {
  nodeId,
  rootNode,
  tooManyResultsFound
}
