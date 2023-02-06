import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import GridHeaderWrapper from '@srcTest/js/components/common/grid/wrapper/GridHeaderWrapper.js'
import GridFilterWrapper from '@srcTest/js/components/common/grid/wrapper/GridFilterWrapper.js'

export default class GridColumnWrapper extends BaseWrapper {
  constructor(column, labelWrapper, filterWrapper, sortWrapper) {
    super(null)
    this.column = column
    this.labelWrapper = labelWrapper
    this.filterWrapper = filterWrapper
    this.sortWrapper = sortWrapper
  }

  getName() {
    return this.column.name
  }

  getLabel() {
    return new GridHeaderWrapper(this.labelWrapper)
  }

  getFilter() {
    return new GridFilterWrapper(this.filterWrapper)
  }

  getSort() {
    const active = this.sortWrapper.prop('active')
    const direction = this.sortWrapper.prop('direction')

    if (active) {
      return direction
    } else {
      return null
    }
  }

  toJSON() {
    return {
      name: this.getName(),
      label: this.getLabel().toJSON(),
      filter: this.getFilter().toJSON(),
      sort: this.getSort()
    }
  }
}
