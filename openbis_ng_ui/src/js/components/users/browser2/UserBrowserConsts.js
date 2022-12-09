import messages from '@src/js/common/messages.js'

const TYPE_ROOT = 'root'
const TYPE_WARNING = 'warning'

const TEXT_USERS = messages.get(messages.USERS)
const TEXT_GROUPS = messages.get(messages.GROUPS)

function nodeId(...parts) {
  return parts.join('__')
}

export default {
  nodeId,
  TYPE_ROOT,
  TYPE_WARNING,
  TEXT_USERS,
  TEXT_GROUPS
}
