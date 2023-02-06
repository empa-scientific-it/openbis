import openbis from '@src/js/services/openbis.js'

export default class QueryType {
  constructor(value) {
    this.value = value
  }
  getLabel() {
    if (this.value === openbis.QueryType.SAMPLE) {
      return 'OBJECT'
    } else if (this.value === openbis.QueryType.EXPERIMENT) {
      return 'COLLECTION'
    } else {
      return this.value
    }
  }
}
