import React from 'react'
import ComponentTest from '@srcTest/js/components/common/ComponentTest.js'
import QueryForm from '@src/js/components/tools/form/query/QueryForm.jsx'
import QueryFormWrapper from '@srcTest/js/components/tools/form/query/wrapper/QueryFormWrapper.js'
import QueryFormController from '@src/js/components/tools/form/query/QueryFormController.js'
import QueryFormFacade from '@src/js/components/tools/form/query/QueryFormFacade'
import objectTypes from '@src/js/common/consts/objectType.js'

jest.mock('@src/js/components/tools/form/query/QueryFormFacade')

export default class QueryFormComponentTest extends ComponentTest {
  static SUITE = 'QueryFormComponent'

  constructor() {
    super(
      object => <QueryForm object={object} controller={this.controller} />,
      wrapper => new QueryFormWrapper(wrapper)
    )
    this.facade = null
    this.controller = null
  }

  async beforeEach() {
    super.beforeEach()

    this.facade = new QueryFormFacade()
    this.controller = new QueryFormController(this.facade)

    this.facade.loadExperimentTypes.mockReturnValue(Promise.resolve([]))
    this.facade.loadSampleTypes.mockReturnValue(Promise.resolve([]))
    this.facade.loadDataSetTypes.mockReturnValue(Promise.resolve([]))
    this.facade.loadMaterialTypes.mockReturnValue(Promise.resolve([]))
  }

  async mountNew() {
    return await this.mount({
      type: objectTypes.NEW_QUERY
    })
  }

  async mountExisting(query) {
    this.facade.loadQuery.mockReturnValue(Promise.resolve(query))

    return await this.mount({
      id: query.getName(),
      type: objectTypes.QUERY
    })
  }
}
