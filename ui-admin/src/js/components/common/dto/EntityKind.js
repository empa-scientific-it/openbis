import openbis from '@src/js/services/openbis.js'

export default class EntityKind {
  constructor(value) {
    this.value = value
  }
  getLabel() {
    if (this.value === openbis.EntityKind.SAMPLE) {
      return 'OBJECT'
    } else if (this.value === openbis.EntityKind.EXPERIMENT) {
      return 'COLLECTION'
    } else {
      return this.value
    }
  }
}
