import openbis from '@src/js/services/openbis.js'

export default class ActiveUserReportFacade {
  async sendReport() {
    return new Promise((resolve, reject) => {
      const serviceId = new openbis.CustomASServiceCode('openbis-ng-ui-service')
      const serviceOptions = new openbis.CustomASServiceExecutionOptions()
      serviceOptions.withParameter('method', 'sendCountActiveUsersEmail')
      openbis.executeService(serviceId, serviceOptions).then(result => {
        resolve(result)
      }, reject)
    })
  }
}
