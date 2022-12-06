import messages from '@src/js/common/messages.js'

const TYPE_ROOT = 'root'
const TYPE_WARNING = 'warning'

const TEXT_OBJECT_TYPES = messages.get(messages.OBJECT_TYPES)
const TEXT_COLLECTION_TYPES = messages.get(messages.COLLECTION_TYPES)
const TEXT_DATA_SET_TYPES = messages.get(messages.DATA_SET_TYPES)
const TEXT_MATERIAL_TYPES = messages.get(messages.MATERIAL_TYPES)
const TEXT_VOCABULARY_TYPES = messages.get(messages.VOCABULARY_TYPES)
const TEXT_PROPERTY_TYPES = messages.get(messages.PROPERTY_TYPES)

function nodeId(...parts) {
  return parts.join('__')
}

export default {
  nodeId,
  TYPE_ROOT,
  TYPE_WARNING,
  TEXT_OBJECT_TYPES,
  TEXT_COLLECTION_TYPES,
  TEXT_DATA_SET_TYPES,
  TEXT_MATERIAL_TYPES,
  TEXT_VOCABULARY_TYPES,
  TEXT_PROPERTY_TYPES
}
