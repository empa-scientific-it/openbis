import openbis from '@src/js/services/openbis.js'

export default class ImportAllFormFacade {
  async import(file, updateMode) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader()
      reader.onload = () => {
        const resultBase64 = reader.result.split(',')[1]
        const serviceId = new openbis.CustomASServiceCode('xls-import')
        const serviceOptions = new openbis.CustomASServiceExecutionOptions()
        serviceOptions.withParameter('method', 'import')
        serviceOptions.withParameter(
          'zip',
          file.name.toLowerCase().endsWith('.zip')
        )
        serviceOptions.withParameter('xls_name', 'DEFAULT')
        serviceOptions.withParameter('xls_base64', resultBase64)
        serviceOptions.withParameter('update_mode', updateMode)
        openbis.executeService(serviceId, serviceOptions).then(result => {
          resolve(result)
        }, reject)
      }
      reader.onerror = reject
      reader.readAsDataURL(file)
    })
  }
}
