import autoBind from 'auto-bind'
import ErrorObject from '@src/js/components/common/error/ErrorObject.js'
import AppController from '@src/js/components/AppController.js'

export default class ActiveUserReportController {
  constructor(facade) {
    autoBind(this)
    this.facade = facade
  }

  init(context) {
    this.context = context
  }

  async sendReport() {
    try {
      await this.context.setState({
        loading: true,
        result: null
      })

      const output = await this.facade.sendReport()

      this.context.setState({
        result: {
          success: output.status === 'OK',
          output: output
        }
      })
    } catch (error) {
      this.context.setState({
        result: {
          success: false,
          output: new ErrorObject(error).getMessage()
        }
      })
    } finally {
      await this.context.setState({
        loading: false
      })
    }
  }

  async loadActiveUsersCount() {
    try {
      const activeUsersCount = await this.facade.loadActiveUsersCount()

      await this.context.setState({
        activeUsersCount: activeUsersCount
      })
    } catch (error) {
      AppController.getInstance().errorChange(error)
    }
  }

  async loadOpenbisSupportEmail() {
    try {
      const openbisSupportEmail = await this.facade.loadOpenbisSupportEmail()

      await this.context.setState({
        openbisSupportEmail: openbisSupportEmail
      })
    } catch (error) {
      AppController.getInstance().errorChange(error)
    }
  }
}
