import openbis from '@src/js/services/openbis.js'

export default class DataType {
  constructor(value) {
    this.value = value
  }
  getLabel() {
    if (this.value === openbis.DataType.SAMPLE) {
      return 'OBJECT'
    } else {
      return this.value
    }
  }
}
