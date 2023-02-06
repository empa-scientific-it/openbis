import openbis from '@src/js/services/openbis.js'
import ids from '@src/js/common/consts/ids.js'

export default class ActiveUserReportFacade {
  async sendReport() {
    const serviceId = new openbis.CustomASServiceCode(ids.WEB_APP_SERVICE)
    const serviceOptions = new openbis.CustomASServiceExecutionOptions()
    serviceOptions.withParameter('method', 'sendCountActiveUsersEmail')
    return openbis.executeService(serviceId, serviceOptions)
  }

  async loadActiveUsersCount() {
    const result = await openbis.searchPersons(
      new openbis.PersonSearchCriteria(),
      new openbis.PersonFetchOptions()
    )
    return result.objects.filter(user => {
      return user.active === true
    }).length
  }

  async loadOpenbisSupportEmail() {
    const serverInformation = await openbis.getServerPublicInformation()
    console.log(serverInformation)
    return serverInformation ? serverInformation['openbis.support.email'] : null
  }
}
