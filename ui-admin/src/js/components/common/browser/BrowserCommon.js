import React from 'react'
import BrowserNodeTooManyResultsFound from '@src/js/components/common/browser/BrowserNodeTooManyResultsFound.jsx'
import BrowserNodeLoadMore from '@src/js/components/common/browser/BrowserNodeLoadMore.jsx'

function rootNode() {
  const TYPE_ROOT = 'root'
  return {
    object: {
      type: TYPE_ROOT,
      id: TYPE_ROOT
    },
    text: '',
    root: true,
    canHaveChildren: true
  }
}

function loadMoreResults(count) {
  const TYPE_LOAD_MORE = 'loadMore'
  return {
    object: {
      type: TYPE_LOAD_MORE,
      id: TYPE_LOAD_MORE
    },
    render: () => <BrowserNodeLoadMore count={count} />,
    selectable: false
  }
}

function tooManyResultsFound() {
  const TYPE_WARNING = 'warning'
  return {
    object: {
      type: TYPE_WARNING,
      id: TYPE_WARNING
    },
    render: () => <BrowserNodeTooManyResultsFound />,
    selectable: false
  }
}

export default {
  rootNode,
  loadMoreResults,
  tooManyResultsFound
}
