import diff from '@src/js/common/diff.js'

const ERROR = { name: 'ERROR', importance: 4 }
const INFO = { name: 'INFO', importance: 3 }
const DEBUG = { name: 'DEBUG', importance: 2 }
const TRACE = { name: 'TRACE', importance: 1 }

let currentLevel = INFO

const states = new Map()
const props = new Map()

let isLevelEnabled = level => {
  return level.importance >= currentLevel.importance
}

let log = (level, message, ...params) => {
  if (isLevelEnabled(level)) {
    // eslint-disable-next-line no-console
    console.log(new Date().toISOString() + ' ' + message, ...params)
  }
}

let logComponent = (level, message, component) => {
  if (isLevelEnabled(level)) {
    group(level, message)

    group(level, message + ': state')
    const prevState = states.get(component)
    const newState = component.state
    diff(level, prevState, newState, 'root')
    states.set(component, newState)
    groupEnd(level)

    group(level, message + ': props')
    const prevProps = props.get(component)
    const newProps = component.props
    diff(level, prevProps, newProps, 'root')
    props.set(component, newProps)
    groupEnd(level)

    groupEnd(level)
  }
}

let group = (level, message, ...params) => {
  if (isLevelEnabled(level)) {
    // eslint-disable-next-line no-console
    console.group(new Date().toISOString() + ' ' + message, ...params)
  }
}

let groupEnd = level => {
  if (isLevelEnabled(level)) {
    // eslint-disable-next-line no-console
    console.groupEnd()
  }
}

export default {
  ERROR,
  INFO,
  DEBUG,
  TRACE,
  log,
  logComponent,
  group,
  groupEnd,
  isLevelEnabled
}
