import openbis from '@src/js/services/openbis.js'

export default class ActiveUserReportFacade {
  async sendReport() {
    const serviceId = new openbis.CustomASServiceCode('openbis-ng-ui-service')
    const serviceOptions = new openbis.CustomASServiceExecutionOptions()
    serviceOptions.withParameter('method', 'sendCountActiveUsersEmail')
    return openbis.executeService(serviceId, serviceOptions)
  }
}
