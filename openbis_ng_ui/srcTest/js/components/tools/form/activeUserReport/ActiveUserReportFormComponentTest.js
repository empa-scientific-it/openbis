import React from 'react'
import ComponentTest from '@srcTest/js/components/common/ComponentTest.js'
import ComponentContext from '@srcTest/js/components/common/ComponentContext.js'
import ActiveUserReportWrapper from '@srcTest/js/components/tools/form/activeUserReport/wrapper/ActiveUserReportWrapper.js'
import ActiveUserReportController from '@src/js/components/tools/form/activeUserReport/ActiveUserReportController.js'
import ActiveUserReportFacade from '@src/js/components/tools/form/activeUserReport/ActiveUserReportFacade.js'
import ActiveUserReportForm from '@src/js/components/tools/form/activeUserReport/ActiveUserReportForm.jsx'

jest.mock('@src/js/components/tools/form/activeUserReport/ActiveUserReportFacade.js')

export default class ActiveUserReportFormComponentTest extends ComponentTest {
  static SUITE = 'ActiveUserReportFormComponentTest'

  constructor() {
    super(
      activeUsersCount => <ActiveUserReportForm activeUsersCount={activeUsersCount}/>,
      wrapper => new ActiveUserReportWrapper(wrapper)
    )
    this.facade = null
    this.context = null
    this.controller = null
  }

  async beforeEach() {
    super.beforeEach()

    this.facade = new ActiveUserReportFacade()
    this.context = new ComponentContext()
    this.controller = new ActiveUserReportController(this.facade)
    this.controller.init(this.context)
  }
}
