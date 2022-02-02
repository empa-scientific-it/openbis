import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import ImportAllUpdateMode from '@src/js/components/tools/form/import/all/ImportAllUpdateMode.js'
import Container from '@src/js/components/common/form/Container.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import FileChooserField from '@src/js/components/common/form/FileChooserField.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  field: {
    paddingBottom: theme.spacing(1)
  }
})

class ImportAllFormParameters extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
    this.state = {}
  }

  handleChange(event) {
    this.props.onChange({
      field: event.target.name,
      value: event.target.value
    })
  }

  handleBlur() {
    this.props.onBlur()
  }

  render() {
    logger.log(logger.DEBUG, 'ImportAllFormParameters.render')

    return (
      <Container>
        {this.renderHeader()}
        {this.renderFileChooser()}
        {this.renderUpdateMode()}
      </Container>
    )
  }

  renderHeader() {
    return <Header>{messages.get(messages.IMPORT)}</Header>
  }

  renderFileChooser() {
    const { fields, classes } = this.props
    const { error } = { ...fields.file }

    return (
      <div className={classes.field}>
        <FileChooserField
          label={messages.get(messages.XSL_FILE)}
          name='file'
          mandatory={true}
          error={error}
          onChange={this.handleChange}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderUpdateMode() {
    const { fields, classes } = this.props
    const { error, value } = { ...fields.updateMode }

    const options = [
      {
        label: ImportAllUpdateMode.IGNORE_EXISTING,
        value: ImportAllUpdateMode.IGNORE_EXISTING
      },
      {
        label: ImportAllUpdateMode.UPDATE_IF_EXISTS,
        value: ImportAllUpdateMode.UPDATE_IF_EXISTS
      },
      {
        label: ImportAllUpdateMode.FAIL_IF_EXISTS,
        value: ImportAllUpdateMode.FAIL_IF_EXISTS
      }
    ]

    return (
      <div className={classes.field}>
        <SelectField
          label={messages.get(messages.UPDATE_MODE)}
          name='updateMode'
          mandatory={true}
          error={error}
          value={value}
          options={options}
          emptyOption={{}}
          onChange={this.handleChange}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }
}

export default withStyles(styles)(ImportAllFormParameters)
