import openbis from '@src/js/services/openbis.js'

export default class EntityType {
  constructor(value) {
    this.value = value
  }
  getLabel() {
    if (this.value === openbis.EntityType.SAMPLE) {
      return 'OBJECT'
    } else if (this.value === openbis.EntityType.EXPERIMENT) {
      return 'COLLECTION'
    } else {
      return this.value
    }
  }
}
