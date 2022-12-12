import messages from '@src/js/common/messages.js'

const TYPE_ROOT = 'root'
const TYPE_HISTORY = 'history'
const TYPE_IMPORT = 'import'
const TYPE_ACCESS = 'access'
const TYPE_ACTIVE_USERS_REPORT = 'active_users_report'
const TYPE_WARNING = 'warning'

const TEXT_DYNAMIC_PROPERTY_PLUGINS = messages.get(
  messages.DYNAMIC_PROPERTY_PLUGINS
)
const TEXT_ENTITY_VALIDATION_PLUGINS = messages.get(
  messages.ENTITY_VALIDATION_PLUGINS
)
const TEXT_QUERIES = messages.get(messages.QUERIES)
const TEXT_HISTORY = messages.get(messages.HISTORY)
const TEXT_IMPORT = messages.get(messages.IMPORT)
const TEXT_ACCESS = messages.get(messages.ACCESS)
const TEXT_ACTIVE_USERS_REPORT = messages.get(messages.ACTIVE_USERS_REPORT)

function nodeId(...parts) {
  return parts.join('__')
}

export default {
  nodeId,
  TYPE_ROOT,
  TYPE_HISTORY,
  TYPE_IMPORT,
  TYPE_ACCESS,
  TYPE_ACTIVE_USERS_REPORT,
  TYPE_WARNING,
  TEXT_DYNAMIC_PROPERTY_PLUGINS,
  TEXT_ENTITY_VALIDATION_PLUGINS,
  TEXT_QUERIES,
  TEXT_HISTORY,
  TEXT_IMPORT,
  TEXT_ACCESS,
  TEXT_ACTIVE_USERS_REPORT
}
