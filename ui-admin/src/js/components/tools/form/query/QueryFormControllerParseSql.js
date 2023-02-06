export default class QueryFormControllerParseSql {
  parse(sql) {
    const parameterRegex = /\$\{(.+?)(::.*)?\}/g
    const parameterNames = []

    let match = null
    do {
      match = parameterRegex.exec(sql)
      if (match) {
        parameterNames.push(match[1])
      }
    } while (match !== null)

    return {
      parameterNames
    }
  }
}
