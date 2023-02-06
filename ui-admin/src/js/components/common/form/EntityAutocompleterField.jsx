import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import AutocompleterField from '@src/js/components/common/form/AutocompleterField.jsx'
import AppController from '@src/js/components/AppController.js'
import openbis from '@src/js/services/openbis.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

const ENTITY_NAME_PROPERTY = '$NAME'
const LOADED_OPTIONS_COUNT = 100

class EntityAutocompleterField extends React.PureComponent {
  constructor(props) {
    super(props)

    autoBind(this)

    this.state = {
      loading: false,
      options: []
    }
  }

  async load(value) {
    this.cancelScheduledLoad()

    const loadId = setTimeout(async () => {
      try {
        logger.log(
          logger.DEBUG,
          `EntityAutocompleterField - executing load request (id: ${loadId}, value: ${value})`
        )

        const results = await this.loadEntities(value, LOADED_OPTIONS_COUNT)

        let options = []
        if (results.options.length > 0) {
          if (results.totalCount > results.options.length) {
            options.push({
              label: messages.get(
                messages.ONLY_FIRST_RESULTS_SHOWN,
                LOADED_OPTIONS_COUNT,
                results.totalCount
              )
            })
          }
          results.options.forEach(option => options.push(option))
        } else {
          options = [
            {
              label: messages.get(messages.NO_RESULTS_FOUND)
            }
          ]
        }

        if (loadId === this.state.loadId) {
          logger.log(
            logger.DEBUG,
            `EntityAutocompleterField - received valid load response (id: ${loadId}, value: ${value})`
          )

          this.setState({
            loading: false,
            options
          })
        } else {
          logger.log(
            logger.DEBUG,
            `EntityAutocompleterField - ignoring old load response (id: ${loadId}, value: ${value})`
          )
        }
      } catch (error) {
        AppController.getInstance().errorChange(error)
        this.setState({
          loading: false
        })
      }
    }, 250)

    logger.log(
      logger.DEBUG,
      `EntityAutocompleterField - scheduled load request (id: ${loadId}, value: ${value})`
    )

    this.setState({
      loading: true,
      loadId
    })
  }

  cancelScheduledLoad() {
    const lastLoadId = this.state.loadId

    if (lastLoadId) {
      logger.log(
        logger.DEBUG,
        `EntityAutocompleterField - cancelling scheduled load request (id: ${lastLoadId})`
      )

      clearTimeout(lastLoadId)
      this.setState({
        loadId: null
      })
    }
  }

  async loadEntities(value, count) {
    const { entityKind } = this.props

    if (entityKind === openbis.EntityKind.EXPERIMENT) {
      return await this.loadExperiments(value, count)
    } else if (entityKind === openbis.EntityKind.SAMPLE) {
      return await this.loadSamples(value, count)
    } else if (entityKind === openbis.EntityKind.DATA_SET) {
      return await this.loadDataSets(value, count)
    } else if (entityKind === openbis.EntityKind.MATERIAL) {
      return await this.loadMaterials(value, count)
    } else {
      return []
    }
  }

  async loadExperiments(value, count) {
    const criteria = new openbis.ExperimentSearchCriteria()
    criteria.withOrOperator()

    if (value && value.trim().length > 0) {
      //criteria.withCode().thatContains(value)
      criteria.withIdentifier().thatContains(value)
      criteria.withProperty(ENTITY_NAME_PROPERTY).thatContains(value)
    }

    const fo = new openbis.ExperimentFetchOptions()
    fo.withProperties()
    fo.from(0).count(count)
    fo.sortBy().identifier().asc()

    const results = await openbis.searchExperiments(criteria, fo)

    return {
      options: results.getObjects().map(object => {
        return {
          label: this.createOptionLabel(openbis.EntityKind.EXPERIMENT, object),
          fullLabel: this.createOptionFullLabel(
            openbis.EntityKind.EXPERIMENT,
            object
          ),
          entityKind: openbis.EntityKind.EXPERIMENT,
          entityId: object.identifier.identifier
        }
      }),
      totalCount: results.totalCount
    }
  }

  async loadSamples(value, count) {
    const criteria = new openbis.SampleSearchCriteria()
    criteria.withOrOperator()

    if (value && value.trim().length > 0) {
      //criteria.withCode().thatContains(value)
      criteria.withIdentifier().thatContains(value)
      criteria.withProperty(ENTITY_NAME_PROPERTY).thatContains(value)
    }

    const fo = new openbis.SampleFetchOptions()
    fo.withProperties()
    fo.from(0).count(count)
    fo.sortBy().identifier().asc()

    const results = await openbis.searchSamples(criteria, fo)

    return {
      options: results.getObjects().map(object => {
        return {
          label: this.createOptionLabel(openbis.EntityKind.SAMPLE, object),
          fullLabel: this.createOptionFullLabel(
            openbis.EntityKind.SAMPLE,
            object
          ),
          entityKind: openbis.EntityKind.SAMPLE,
          entityId: object.identifier.identifier
        }
      }),
      totalCount: results.totalCount
    }
  }

  async loadMaterials(value, count) {
    const criteria = new openbis.MaterialSearchCriteria()
    criteria.withOrOperator()

    if (value && value.trim().length > 0) {
      criteria.withCode().thatContains(value)
      criteria.withProperty(ENTITY_NAME_PROPERTY).thatContains(value)
    }

    const fo = new openbis.MaterialFetchOptions()
    fo.withProperties()
    fo.from(0).count(count)
    fo.sortBy().code().asc()

    const results = await openbis.searchMaterials(criteria, fo)

    return {
      options: results.getObjects().map(object => {
        return {
          label: this.createOptionLabel(openbis.EntityKind.MATERIAL, object),
          fullLabel: this.createOptionFullLabel(
            openbis.EntityKind.MATERIAL,
            object
          ),
          entityKind: openbis.EntityKind.MATERIAL,
          entityId: {
            code: object.permId.code,
            typeCode: object.permId.typeCode
          }
        }
      }),
      totalCount: results.totalCount
    }
  }

  async loadDataSets(value, count) {
    const criteria = new openbis.DataSetSearchCriteria()
    criteria.withOrOperator()

    if (value && value.trim().length > 0) {
      criteria.withCode().thatContains(value)
      criteria.withProperty(ENTITY_NAME_PROPERTY).thatContains(value)

      const experimentCriteria = criteria.withExperiment()
      experimentCriteria.withOrOperator()
      //experimentCriteria.withCode().thatContains(value)
      experimentCriteria.withIdentifier().thatContains(value)
      experimentCriteria.withProperty(ENTITY_NAME_PROPERTY).thatContains(value)

      const sampleCriteria = criteria.withSample()
      sampleCriteria.withOrOperator()
      //sampleCriteria.withCode().thatContains(value)
      sampleCriteria.withIdentifier().thatContains(value)
      sampleCriteria.withProperty(ENTITY_NAME_PROPERTY).thatContains(value)
    }

    const fo = new openbis.DataSetFetchOptions()
    fo.withProperties()
    fo.withExperiment()
    fo.withSample()
    fo.from(0).count(count)
    fo.sortBy().code().asc()

    const results = await openbis.searchDataSets(criteria, fo)

    return {
      options: results.getObjects().map(object => {
        return {
          label: this.createOptionLabel(openbis.EntityKind.DATA_SET, object),
          fullLabel: this.createOptionFullLabel(
            openbis.EntityKind.DATA_SET,
            object
          ),
          entityKind: openbis.EntityKind.DATA_SET,
          entityId: object.code
        }
      }),
      totalCount: results.totalCount
    }
  }

  createOptionLabel(entityKind, object) {
    if (
      entityKind === openbis.EntityKind.EXPERIMENT ||
      entityKind === openbis.EntityKind.SAMPLE
    ) {
      return object.identifier.identifier
    } else if (
      entityKind === openbis.EntityKind.MATERIAL ||
      entityKind === openbis.EntityKind.DATA_SET
    ) {
      return object.code
    }
  }

  createOptionFullLabel(entityKind, object) {
    let name = object.properties[ENTITY_NAME_PROPERTY]

    if (name && name.trim().length > 0) {
      name = ' (' + name.trim() + ')'
    } else {
      name = ''
    }

    if (
      entityKind === openbis.EntityKind.EXPERIMENT ||
      entityKind === openbis.EntityKind.SAMPLE ||
      entityKind === openbis.EntityKind.MATERIAL
    ) {
      return this.createOptionLabel(entityKind, object) + name
    } else if (entityKind === openbis.EntityKind.DATA_SET) {
      let owner = null

      if (object.experiment) {
        owner = this.createOptionLabel(
          openbis.EntityKind.EXPERIMENT,
          object.experiment
        )
      } else if (object.sample) {
        owner = this.createOptionLabel(openbis.EntityKind.SAMPLE, object.sample)
      }

      if (owner) {
        owner = ' [' + messages.get(messages.OWNER) + ': ' + owner + ']'
      } else {
        owner = ''
      }

      return this.createOptionLabel(entityKind, object) + name + owner
    }
  }

  handleFocus(event) {
    this.load(event.target.value)
  }

  handleInputChange(event) {
    this.load(event.target.value)
  }

  handleBlur() {
    this.cancelScheduledLoad()

    this.setState({
      loading: false,
      options: []
    })
  }

  renderOption(option) {
    if (option) {
      return option.fullLabel || option.label
    } else {
      return ''
    }
  }

  filterOptions(options) {
    // do not filter options on the client side
    return options
  }

  getOptionLabel(option) {
    if (option) {
      return option.label || ''
    } else {
      return ''
    }
  }

  getOptionSelected(option, value) {
    return (
      _.isEqual(option.entityKind, value.entityKind) &&
      _.isEqual(option.entityId, value.entityId)
    )
  }

  getOptionDisabled(option) {
    return !option || !option.entityId
  }

  render() {
    logger.log(logger.DEBUG, 'EntityAutocompleterField.render')

    const { value } = this.props
    const { loading, options } = this.state

    return (
      <AutocompleterField
        {...this.props}
        loading={loading}
        freeSolo={false}
        value={value}
        renderOption={this.renderOption}
        filterOptions={this.filterOptions}
        getOptionLabel={this.getOptionLabel}
        getOptionSelected={this.getOptionSelected}
        getOptionDisabled={this.getOptionDisabled}
        onInputChange={this.handleInputChange}
        onFocus={this.handleFocus}
        onBlur={this.handleBlur}
        options={options}
      />
    )
  }
}

export default withStyles(styles)(EntityAutocompleterField)
