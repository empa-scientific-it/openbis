import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Popover from '@material-ui/core/Popover'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import Container from '@src/js/components/common/form/Container.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import GridExportOptions from '@src/js/components/common/grid/GridExportOptions.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    display: 'flex',
    alignItems: 'center'
  },
  popup: {
    minWidth: '200px',
    maxWidth: '300px'
  },
  field: {
    paddingBottom: theme.spacing(1),
    '&:first-child': {
      paddingTop: 0
    },
    '&:last-child': {
      paddingBottom: 0
    }
  },
  button: {
    paddingTop: theme.spacing(1)
  }
})

class GridExports extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {
      el: null,
      validate: false,
      importCompatibleError: null
    }
    this.handleOpen = this.handleOpen.bind(this)
    this.handleClose = this.handleClose.bind(this)
    this.handleChange = this.handleChange.bind(this)
    this.handleExport = this.handleExport.bind(this)
  }

  handleOpen(event) {
    this.setState({
      el: event.currentTarget
    })
  }

  handleClose() {
    const { exportOptions, onExportOptionsChange } = this.props

    this.setState({
      el: null,
      validate: false,
      importCompatibleError: null
    })

    if (onExportOptionsChange) {
      const newExportOptions = {
        ...exportOptions,
        importCompatible: null
      }
      onExportOptionsChange(newExportOptions)
    }
  }

  async handleChange(event) {
    const { exportOptions, onExportOptionsChange } = this.props

    if (onExportOptionsChange) {
      const newExportOptions = {
        ...exportOptions,
        [event.target.name]: event.target.value
      }

      if (newExportOptions.importCompatible === true) {
        newExportOptions.values = GridExportOptions.VALUES.RICH_TEXT
      }

      await onExportOptionsChange(newExportOptions)

      this.validate()
    }
  }

  handleExport() {
    const { onExport } = this.props

    this.setState({ validate: true }, () => {
      if (this.validate()) {
        this.handleClose()
        if (onExport) {
          onExport()
        }
      }
    })
  }

  validate() {
    const { exportable } = this.props
    const { validate } = this.state

    if (!validate) {
      return true
    }

    const isXLSExport =
      exportable.fileFormat === GridExportOptions.FILE_FORMAT.XLS

    if (isXLSExport) {
      const { importCompatible } = this.props.exportOptions

      let importCompatibleError = null

      if (_.isNil(importCompatible) || importCompatible === '') {
        importCompatibleError = messages.get(
          messages.VALIDATION_CANNOT_BE_EMPTY,
          messages.get(messages.IMPORT_COMPATIBLE)
        )
      } else {
        importCompatibleError = null
      }

      this.setState({
        importCompatibleError
      })

      return _.isNil(importCompatibleError)
    } else {
      return true
    }
  }

  render() {
    logger.log(logger.DEBUG, 'GridExports.render')

    const {
      id,
      exportable,
      exportOptions,
      disabled,
      multiselectable,
      multiselectedRows,
      visibleColumns,
      classes
    } = this.props
    const { el, importCompatibleError } = this.state

    const rowsOptions = [
      {
        label: messages.get(messages.ALL_PAGES),
        value: GridExportOptions.ROWS.ALL_PAGES
      },
      {
        label: messages.get(messages.CURRENT_PAGE),
        value: GridExportOptions.ROWS.CURRENT_PAGE
      }
    ]

    if (multiselectable) {
      rowsOptions.push({
        label: messages.get(messages.SELECTED_ROWS),
        value: GridExportOptions.ROWS.SELECTED_ROWS
      })
    }

    const isXLSExport =
      exportable.fileFormat === GridExportOptions.FILE_FORMAT.XLS

    const isTSVExport =
      exportable.fileFormat === GridExportOptions.FILE_FORMAT.TSV

    const isXLSEntityExport =
      isXLSExport &&
      exportable.fileContent === GridExportOptions.FILE_CONTENT.ENTITIES

    const isXLSTypesExport =
      isXLSExport &&
      exportable.fileContent === GridExportOptions.FILE_CONTENT.TYPES

    return (
      <div className={classes.container}>
        <Button
          id={id + '.exports-button-id'}
          label={messages.get(messages.EXPORTS)}
          color='default'
          disabled={disabled}
          variant='outlined'
          onClick={this.handleOpen}
        />
        <Popover
          id={id + '.exports-popup-id'}
          open={Boolean(el)}
          anchorEl={el}
          onClose={this.handleClose}
          anchorOrigin={{
            vertical: 'bottom',
            horizontal: 'left'
          }}
          transformOrigin={{
            vertical: 'top',
            horizontal: 'left'
          }}
        >
          <Container square={true} className={classes.popup}>
            <div>
              {isXLSExport && (
                <div className={classes.field}>
                  <SelectField
                    label={messages.get(messages.IMPORT_COMPATIBLE)}
                    name='importCompatible'
                    error={importCompatibleError}
                    mandatory={true}
                    sort={false}
                    emptyOption={{}}
                    options={[
                      {
                        label: messages.get(messages.YES),
                        value: true
                      },
                      {
                        label: messages.get(messages.NO),
                        value: false
                      }
                    ]}
                    value={exportOptions.importCompatible}
                    variant='standard'
                    onChange={this.handleChange}
                  />
                </div>
              )}
              {isXLSExport && exportOptions.importCompatible === true && (
                <div className={classes.field}>
                  <Message type='info'>
                    {messages.get(messages.EXPORT_IMPORT_COMPATIBLE_INFO)}
                  </Message>
                </div>
              )}
              {isXLSExport && exportOptions.importCompatible === false && (
                <div className={classes.field}>
                  <Message type='info'>
                    {messages.get(messages.EXPORT_IMPORT_INCOMPATIBLE_INFO)}
                  </Message>
                </div>
              )}
              <div className={classes.field}>
                <SelectField
                  label={messages.get(messages.COLUMNS)}
                  name='columns'
                  options={[
                    {
                      label: messages.get(messages.ALL_COLUMNS),
                      value: GridExportOptions.COLUMNS.ALL
                    },
                    {
                      label: messages.get(messages.VISIBLE_COLUMNS),
                      value: GridExportOptions.COLUMNS.VISIBLE
                    }
                  ]}
                  value={exportOptions.columns}
                  variant='standard'
                  onChange={this.handleChange}
                />
              </div>
              <div className={classes.field}>
                <SelectField
                  label={messages.get(messages.ROWS)}
                  name='rows'
                  options={rowsOptions}
                  value={exportOptions.rows}
                  variant='standard'
                  onChange={this.handleChange}
                />
              </div>
              {(isTSVExport || isXLSEntityExport) && (
                <div className={classes.field}>
                  <SelectField
                    label={messages.get(messages.VALUES)}
                    name='values'
                    options={[
                      {
                        label: messages.get(messages.PLAIN_TEXT),
                        value: GridExportOptions.VALUES.PLAIN_TEXT
                      },
                      {
                        label: messages.get(messages.RICH_TEXT),
                        value: GridExportOptions.VALUES.RICH_TEXT
                      }
                    ]}
                    disabled={exportOptions.importCompatible}
                    value={exportOptions.values}
                    variant='standard'
                    onChange={this.handleChange}
                  />
                </div>
              )}
              {(isTSVExport || isXLSEntityExport) &&
                exportOptions.values ===
                  GridExportOptions.VALUES.PLAIN_TEXT && (
                  <div className={classes.field}>
                    <Message type='warning'>
                      {messages.get(messages.EXPORT_PLAIN_TEXT_WARNING)}
                    </Message>
                  </div>
                )}
            </div>
            {isXLSTypesExport && (
              <div className={classes.field}>
                <SelectField
                  label={messages.get(messages.INCLUDE_DEPENDENCIES)}
                  name='includeDependencies'
                  options={[
                    {
                      label: messages.get(messages.YES),
                      value: true
                    },
                    {
                      label: messages.get(messages.NO),
                      value: false
                    }
                  ]}
                  sort={false}
                  value={exportOptions.includeDependencies}
                  variant='standard'
                  onChange={this.handleChange}
                />
              </div>
            )}
            <div className={classes.button}>
              <Button
                id={id + '.trigger-exports-button-id'}
                label={messages.get(messages.EXPORT)}
                type='neutral'
                disabled={
                  (exportOptions.columns ===
                    GridExportOptions.COLUMNS.VISIBLE &&
                    _.isEmpty(visibleColumns)) ||
                  (exportOptions.rows ===
                    GridExportOptions.ROWS.SELECTED_ROWS &&
                    _.isEmpty(multiselectedRows))
                }
                onClick={this.handleExport}
              />
            </div>
          </Container>
        </Popover>
      </div>
    )
  }
}

export default _.flow(withStyles(styles))(GridExports)
