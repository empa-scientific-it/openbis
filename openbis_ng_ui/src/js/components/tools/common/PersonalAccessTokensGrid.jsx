import _ from 'lodash'
import React from 'react'
import GridWithSettings from '@src/js/components/common/grid/GridWithSettings.jsx'
import DateRangeField from '@src/js/components/common/form/DateRangeField.jsx'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import UserLink from '@src/js/components/common/link/UserLink.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import ServerInformation from '@src/js/components/common/dto/ServerInformation.js'
import AppController from '@src/js/components/AppController.js'
import ids from '@src/js/common/consts/ids.js'
import messages from '@src/js/common/messages.js'
import date from '@src/js/common/date.js'
import logger from '@src/js/common/logger.js'

const NOT_YET_VALID = '0'
const VALID = '1'
const VALID_WITH_WARNING = '2'
const INVALID = '3'

class PersonalAccessTokensGrid extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'PersonalAccessTokensGrid.render')

    const { rows, selectedRowId, onSelectedRowChange, controllerRef } =
      this.props

    return (
      <GridWithSettings
        id={ids.PERSONAL_ACCESS_TOKEN_GRID_ID}
        controllerRef={controllerRef}
        header={messages.get(messages.PERSONAL_ACCESS_TOKENS)}
        columns={[
          {
            name: 'hash',
            label: messages.get(messages.HASH),
            getValue: ({ row }) => row.hash.value,
            nowrap: true
          },
          {
            name: 'owner',
            label: messages.get(messages.OWNER),
            getValue: ({ row }) => row.owner.value,
            renderValue: ({ value }) => {
              return <UserLink userId={value} />
            }
          },
          {
            name: 'sessionName',
            label: messages.get(messages.SESSION_NAME),
            getValue: ({ row }) => row.sessionName.value
          },
          dateColumn('validFromDate', messages.get(messages.VALID_FROM)),
          dateColumn('validToDate', messages.get(messages.VALID_TO)),
          {
            name: 'validityPeriod',
            label: messages.get(messages.VALIDITY_PERIOD),
            getValue: ({ row }) => {
              if (
                !row.validFromDate.value ||
                !row.validFromDate.value.dateObject ||
                !row.validToDate.value ||
                !row.validToDate.value.dateObject
              ) {
                return null
              }

              return (
                row.validToDate.value.dateObject.getTime() -
                row.validFromDate.value.dateObject.getTime()
              )
            },
            renderValue: ({ value }) => {
              if (value) {
                return date.duration(value)
              } else {
                return null
              }
            },
            filterable: false,
            nowrap: true
          },
          {
            name: 'validityLeft',
            label: messages.get(messages.VALIDITY_LEFT),
            getValue: ({ row }) => {
              if (
                !row.validFromDate.value ||
                !row.validFromDate.value.dateObject ||
                !row.validToDate.value ||
                !row.validToDate.value.dateObject
              ) {
                return null
              }

              return (
                row.validToDate.value.dateObject.getTime() -
                Math.max(
                  row.validFromDate.value.dateObject.getTime(),
                  new Date().getTime()
                )
              )
            },
            renderValue: ({ value }) => {
              if (value) {
                return date.duration(value)
              } else {
                return null
              }
            },
            filterable: false,
            nowrap: true
          },
          {
            name: 'valid',
            label: messages.get(messages.VALID),
            getValue: ({ row }) => {
              if (
                !row.validFromDate.value ||
                !row.validFromDate.value.dateObject ||
                !row.validToDate.value ||
                !row.validToDate.value.dateObject
              ) {
                return null
              }

              const now = new Date()
              const validFrom = row.validFromDate.value.dateObject
              const validTo = row.validToDate.value.dateObject

              if (validFrom <= validTo) {
                if (validTo < now) {
                  return INVALID
                } else if (now < validFrom) {
                  return NOT_YET_VALID
                } else {
                  const validityPeriodWarning =
                    AppController.getInstance().getServerInformation(
                      ServerInformation.PERSONAL_ACCESS_TOKENS_VALIDITY_WARNING_PERIOD
                    )
                  const validityLeft = validTo.getTime() - now.getTime()

                  if (
                    validityPeriodWarning &&
                    validityLeft < validityPeriodWarning * 1000
                  ) {
                    return VALID_WITH_WARNING
                  } else {
                    return VALID
                  }
                }
              } else {
                return INVALID
              }
            },
            renderValue: ({ value }) => {
              if (value === NOT_YET_VALID) {
                return (
                  <Message type='info'>
                    {messages.get(messages.NOT_YET_VALID)}
                  </Message>
                )
              } else if (value === VALID) {
                return (
                  <Message type='success'>
                    {messages.get(messages.VALID)}
                  </Message>
                )
              } else if (value === VALID_WITH_WARNING) {
                return (
                  <Message type='warning'>
                    {messages.get(messages.VALID_WITH_WARNING)}
                  </Message>
                )
              } else if (value === INVALID) {
                return (
                  <Message type='error'>
                    {messages.get(messages.INVALID)}
                  </Message>
                )
              } else {
                return null
              }
            },
            renderFilter: ({ value, onChange }) => {
              return (
                <SelectField
                  label={messages.get(messages.VALID)}
                  value={value}
                  emptyOption={{}}
                  sort={false}
                  options={[
                    {
                      value: NOT_YET_VALID,
                      label: messages.get(messages.NOT_YET_VALID)
                    },
                    {
                      value: VALID,
                      label: messages.get(messages.VALID)
                    },
                    {
                      value: VALID_WITH_WARNING,
                      label: messages.get(messages.VALID_WITH_WARNING)
                    },
                    {
                      value: INVALID,
                      label: messages.get(messages.INVALID)
                    }
                  ]}
                  onChange={onChange}
                  variant='standard'
                />
              )
            },
            nowrap: true
          },
          {
            name: 'registrator',
            label: messages.get(messages.REGISTRATOR),
            getValue: ({ row }) => row.registrator.value,
            renderValue: ({ value }) => {
              return <UserLink userId={value} />
            }
          },
          dateColumn(
            'registrationDate',
            messages.get(messages.REGISTRATION_DATE)
          ),
          dateColumn('accessDate', messages.get(messages.ACCESS_DATE))
        ]}
        rows={rows}
        sortings={[
          { columnName: 'owner' },
          { columnName: 'sessionName' },
          { columnName: 'validFromDate' }
        ]}
        selectable={true}
        selectedRowId={selectedRowId}
        onSelectedRowChange={onSelectedRowChange}
      />
    )
  }
}

function dateColumn(name, message) {
  return {
    name: name,
    label: message,
    getValue: ({ row }) => {
      return date.format(row[name].value ? row[name].value.dateObject : null)
    },
    renderFilter: ({ value, onChange }) => {
      return (
        <DateRangeField value={value} variant='standard' onChange={onChange} />
      )
    },
    matchesValue: ({ row, value, filter, defaultMatches }) => {
      if (_.isString(filter)) {
        return defaultMatches(value, filter)
      } else {
        return date.inRange(
          row[name].value ? row[name].value.dateObject : null,
          filter.from ? filter.from.dateObject : null,
          filter.to ? filter.to.dateObject : null
        )
      }
    }
  }
}

export default PersonalAccessTokensGrid
