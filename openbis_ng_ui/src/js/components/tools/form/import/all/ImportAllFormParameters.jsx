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
    this.references = {
      file: React.createRef(),
      updateMode: React.createRef()
    }
  }

  componentDidMount() {
    this.focus()
  }

  componentDidUpdate(prevProps) {
    const prevSelection = prevProps.selection
    const selection = this.props.selection

    if (prevSelection !== selection) {
      this.focus()
    }
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

  focus() {
    const { selection } = this.props
    if (selection && selection.field) {
      const reference = this.references[selection.field]
      if (reference && reference.current) {
        reference.current.focus()
      }
    }
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
    const { error, value } = { ...fields.file }

    return (
      <div className={classes.field}>
        <FileChooserField
          reference={this.references.file}
          label={messages.get(messages.IMPORT_FILE)}
          name='file'
          description={messages.get(messages.IMPORT_FILE_DESCRIPTION)}
          mandatory={true}
          error={error}
          value={value}
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
        label: messages.get(messages.IGNORE_EXISTING),
        value: ImportAllUpdateMode.IGNORE_EXISTING
      },
      {
        label: messages.get(messages.UPDATE_IF_EXISTS),
        value: ImportAllUpdateMode.UPDATE_IF_EXISTS
      },
      {
        label: messages.get(messages.FAIL_IF_EXISTS),
        value: ImportAllUpdateMode.FAIL_IF_EXISTS
      }
    ]

    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.updateMode}
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
