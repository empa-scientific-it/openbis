import _ from 'lodash'
import React from 'react'
import UserLink from '@src/js/components/common/link/UserLink.jsx'
import DateRangeField from '@src/js/components/common/form/DateRangeField.jsx'
import GridExportOptions from '@src/js/components/common/grid/GridExportOptions.js'
import date from '@src/js/common/date.js'
import messages from '@src/js/common/messages.js'

function userColumn(params) {
  return {
    ...params,
    getValue: ({ row }) => _.get(row, params.path),
    renderValue: ({ value }) => {
      return <UserLink userId={value} />
    }
  }
}

function registratorColumn(params) {
  return userColumn({
    ...params,
    name: 'registrator',
    label: messages.get(messages.REGISTRATOR),
    exportableField: GridExportOptions.EXPORTABLE_FIELD.REGISTRATOR,
    path: params.path
  })
}

function dateColumn(params) {
  return {
    ...params,
    getValue: ({ row, operation }) => {
      const value = _.get(row, params.path)
      if (operation === 'export') {
        return date.format(value)
      } else {
        return value
      }
    },
    renderValue: ({ value }) => date.format(value),
    renderFilter: ({ value, onChange }) => {
      return (
        <DateRangeField value={value} variant='standard' onChange={onChange} />
      )
    },
    matchesValue: ({ value, filter, defaultMatches }) => {
      if (_.isString(filter)) {
        return defaultMatches(value, filter)
      } else {
        return date.inRange(
          value,
          filter.from ? filter.from.dateObject : null,
          filter.to ? filter.to.dateObject : null
        )
      }
    }
  }
}

function registrationDateColumn(params) {
  return dateColumn({
    ...params,
    name: 'registrationDate',
    label: messages.get(messages.REGISTRATION_DATE),
    exportableField: GridExportOptions.EXPORTABLE_FIELD.REGISTRATION_DATE
  })
}

function modificationDateColumn(params) {
  return dateColumn({
    ...params,
    name: 'modificationDate',
    label: messages.get(messages.MODIFICATION_DATE),
    exportableField: GridExportOptions.EXPORTABLE_FIELD.MODIFICATION_DATE
  })
}

export default {
  userColumn,
  registratorColumn,
  dateColumn,
  registrationDateColumn,
  modificationDateColumn
}
