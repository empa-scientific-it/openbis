function attribute(name) {
  return () => ({
    type: 'ATTRIBUTE',
    id: name
  })
}

function property(name) {
  return {
    type: 'PROPERTY',
    id: name
  }
}

export default {
  code: attribute('Code'),
  identifier: attribute('Identifier'),
  permId: attribute('PermId'),
  description: attribute('Description'),
  archivingStatus: attribute('Archiving status'),
  presentInArchive: attribute('Present in archive'),
  storageConfirmation: attribute('Storage confirmation'),
  urlTemplate: attribute('URL Template'),
  validationPlugin: attribute('Validation Plugin'),
  generatedCodePrefix: attribute('Generated code prefix'),
  generateCodes: attribute('Generate Codes'),
  uniqueSubcodes: attribute('Unique Subcodes'),
  mainDataSetPattern: attribute('Main Data Set Pattern'),
  mainDataSetPath: attribute('Main Data Set Path'),
  disallowDeletion: attribute('Disallow Deletion'),
  space: attribute('Space'),
  project: attribute('Project'),
  experiment: attribute('Experiment'),
  sample: attribute('Sample'),
  parents: attribute('Parents'),
  children: attribute('Children'),
  registrator: attribute('Registrator'),
  registrationDate: attribute('Registration Date'),
  modifier: attribute('Modifier'),
  modificationDate: attribute('Modification Date'),
  property: property
}
