classdef OpenBis
    % OpenBis   High-level class for interacting with Python (pyBIS) Openbis objects
    % This class creates a MATLAB OpenBis object that encapsulates the Python (pyBIS) Openbis object
    % and provides methods for interacting with the Python (pyBIS) Openbis object.
    %
    % Usage:
    % Construct the MATLAB OpenBis object like this:
    % obi = OpenBis()
    % This will ask for URL, user name and password to connect to openBIS server.
    % These can also be provided as optional input arguments.
    %
    % Methods are generally called like this:
    % spaces = obi.get_spaces()
    % space = obi.get_space(code)
    %
    % Logout:
    % obi.logout()
    
    
    properties
        pybis % Python Openbis object
    end
    
    
    methods
        
        %% Constructor method
        function obj = OpenBis(url)
            % OpenBis   Constructor method for class OpenBis
            % Creates the Python Openbis object for openBIS server
            % Usage:
            % url ... URL of the openBIS server
            % Example
            % obi = OpenBis('server_url')
            o = py.pybis.Openbis(url, pyargs('verify_certificates', 0));
            obj.pybis = o;
        end

        function login(obj)
            %login
            % Login to openBIS with username and password
            [user, pw] = user_pass_input;
            obj.pybis.login(user, pw, pyargs('save_token', 1))
        end

        function set_token(obj, token)
            %set_token
            % Login to openBIS with a session token
            obj.pybis.set_token(token)
        end

        function get_or_create_personal_access_token(obj, sessionName)
            %get_or_create_personal_access_token
            % Creates a new personal access token (PAT).  If a PAT with the 
            % given sessionName already exists and its expiry date (validToDate) 
            % is not within the warning period, the existing PAT is returned instead.
            pat = obj.pybis.get_or_create_personal_access_token(pyargs('sessionName', sessionName));
            obj.pybis.set_token(pat.permId, pyargs('save_token', 1))
        end

        function get_personal_access_tokens(obj)
            %get_personal_access_tokens
            % Return registered PATs
            obj.pybis.get_personal_access_tokens()
        end
        
        function logout(obj)
            %logout
            % Log out of openBIS.
            % Usage:
            % obi.logout()
            %
            % After logout, the session token is no longer valid.
            obj.pybis.logout();
        end
        
        function tf= is_session_active(obj)
            %is_session_active
            % Check if the session token is still active. Returns true or false.
            % Usage:
            % true_false = obi.is_session_active()
            
            tf  = obj.pybis.is_session_active();
        end
        
        function token = token(obj)
            %token
            % Returns the current session token.
            % Usage:
            % token = obi.token()
            
            token  = char(obj.pybis.token);
        end
        
        %% Masterdata methods
        % this section defines Matlab equivalents of the following pyBIS methods:
        %   get_experiment_types
        %   get_sample_types
        %   get_material_types
        %   get_dataset_types
        %   get_terms
        %   get_tags
        
        function experiment_types = get_experiment_types(obj)
            %get_experiment_types
            % Return a table of all available experiment types.
            % Usage:
            % experiment_types = obi.get_experiment_types()
            
            experiment_types = obj.pybis.get_experiment_types();
            experiment_types = df_to_table(experiment_types.df);
        end
        
        function sample_types = get_sample_types(obj)
            %get_sample_types
            % Return table of all available sample types.
            % Usage:
            % sample_types = obi.get_sample_types()
            
            sample_types = obj.pybis.get_sample_types();
            sample_types = df_to_table(sample_types.df);
        end
        
        function material_types = get_material_types(obj)
            %get_material_types
            % Return table of all available material types.
            % Usage:
            % material_types = obi.get_material_types()
            
            material_types = obj.pybis.get_material_types();
            material_types = df_to_table(material_types.df);
        end
        
        function dataset_types = get_dataset_types(obj)
            %get_dataset_types
            % Return table of all available dataset types.
            % Usage:
            % dataset_types = obi.get_dataset_types()
            
            dataset_types = obj.pybis.get_dataset_types();
            dataset_types = df_to_table(dataset_types.df);
        end
        
        function terms = get_terms(obj)
            %get_terms
            % Return table of all available terms.
            % Usage:
            % terms = obi.get_terms()
            
            terms = obj.pybis.get_terms();
            terms = df_to_table(terms.df);
        end
        
        function tags = get_tags(obj)
            %get_tags
            % Return table of all available tags.
            % Usage:
            % tags = obi.get_tags()
            
            tags = obj.pybis.get_tags();
            tags = df_to_table(tags.df);
        end
        
        
        %% Space methods
        % this section defines Matlab equivalents of the following pyBIS methods:
        %   get_spaces
        %   get_space
        %   new_space
        %   space.delete
        
        function spaces = get_spaces(obj)
            %get_spaces
            % Return table of all available spaces.
            % Usage:
            % spaces = obi.get_spaces()
            
            spaces = obj.pybis.get_spaces();
            spaces = df_to_table(spaces.df);
        end
        
        function space = get_space(obj, code)
            %get_space
            % Fetch space with matching space code and return the space object.
            % An error is raised if a space with the code is not found on the server.
            % Required input arguments:
            % code ... space code
            % Usage:
            % space = obi.get_space('code')
            
            space = obj.pybis.get_space(code);
        end
        
        function space = new_space(obj, code, description)
            %new_space
            % Create a new space with code and description and return the space object
            % Required input arguments:
            % code ... Space code
            % description ... Space description
            % Usage:
            % space = obi.new_space('code', 'description')
            
            space = obj.pybis.new_space(pyargs('code',  code, ...
                'description', description));
            space.save;
        end
        
        function delete_space(obj, code, reason)
            %delete_space
            % Delete space with code and provide a reason for deletion
            % Required input arguments:
            % code ... Space code
            % reason ... reason for deletion
            % Usage:
            % obi.delete_space('code', 'reason')
            
            space = obj.pybis.get_space(code);
            space.delete(reason);
        end
        
        
        %% Project methods
        % this section defines Matlab equivalents of the following pyBIS methods:
        %   get_projects
        %   get_project
        %   new_project
        %   project.delete
        
        function projects = get_projects(obj, space, code)
            %get_projects
            % Return table of matching projects.
            % Input arguments:
            % space ... space to fetch projects from
            % project ... fetch projects matching code
            % Usage:
            % projects = obi.get_projects('space', 'code')
            
            projects = obj.pybis.get_projects(pyargs('space',  space, 'code', code));
            projects = df_to_table(projects.df);
        end
        
        function project = get_project(obj, id)
            %get_project
            % Fetch project with matching project id and return the project object.
            % An error is raised if a project with the id is not found on the server.
            % Required input arguments:
            % id ... project id
            % Usage:
            % project = obi.get_project('id')
            
            project = obj.pybis.get_project(id);
        end
        
        function project = new_project(obj, space, code, description)
            %new_project
            % Create a new project in space with code and description
            % Return the project object
            % Input arguments
            % space ... Space code
            % code ... Project code / id
            % description ... Project description
            % Usage:
            % project = obi.new_project('space', 'code', 'description')
            
            space = obj.pybis.get_space(space);
            project = space.new_project(pyargs('code', code,  'description', description));
            project.save();
        end
        
        function delete_project(obj, code, reason)
            %delete_project
            % Delete project with code and provide a reason for deletion
            % Required input arguments:
            % code ... Project code
            % reason ... reason for deletion
            % Usage:
            % obi.delete_project('code', 'reason')
            
            project = obj.pybis.get_project(code);
            project.delete(reason);
        end
        
        
        %% Experiment methods
        % this section defines the following Matlab methods:
        %   get_experiments
        %   get_experiment
        %   new_experiment
        %   delete_experiment
        
        function experiments = get_experiments(obj, varargin)
            %get_experiments
            % Return table of matching experiments.
            % Optional input arguments:
            % space ... space to fetch experiments from
            % type ... fetch experiments of specific type
            % project ... project to fetch experiments from
            % Usage:
            % experiments = obi.get_experiments()
            % experiments = obi.get_experiments('space', 'SPACE')
            % experiments = obi.get_experiments('space', 'SPACE', 'type', 'UNKNOWN')
            
            space = '';
            type = '';
            project = '';
            
            p = inputParser;
            addRequired(p, 'obj');
            addParameter(p, 'space', space, @ischar);
            addParameter(p, 'type', type, @ischar);
            addParameter(p, 'project', project, @ischar);
            parse(p, obj, varargin{:});
            a = p.Results;
            
            experiments = obj.pybis.get_experiments(pyargs('space', a.space, 'type', a.type, 'project', a.project));
            experiments = df_to_table(experiments.df);
        end
        
        function experiment = get_experiment(obj, id)
            %get_experiment
            % Return experiment with identifier
            % ID can be either the Space + Object code (e.g. /SPACE/123456789) or the PermID (e.g. 20181002164551373-1234)
            % Usage:
            % exp = obi.get_experiment('/SPACE/PROJECT/EXP')
            % exp = obi.get_experiment('permID')
            
            experiment = obj.pybis.get_experiment(id);
        end
        
        function exp = new_experiment(obj, type, code, project)
            %new_experiment
            % Create a new experiment of specific type in a defined project
            % Required input arguments:
            % type ... new experiment type - see: obi.get_experiment_types()
            % code ... new experiment code
            % project ... project for new experiment ('/SPACE/Project')
            % Usage:
            % exp = obi.new_experiment('DEFAULT_EXPERIMENT', 'EXP1234', '/SPACE/Project')
            
            % determine type object
            t = obj.pybis.get_experiment_type(type);
            
            % determine project type
            p = obj.get_project(project);
            
            % instantiate a new experiment object
            exp = py.pybis.pybis.Experiment(obj.pybis, pyargs('type', t, 'code', code, 'project', p));
            
            % save experiment
            exp.save();
        end
        
        function experiment = delete_experiment(obj, experiment, reason)
            %delete_experiment
            % Delete experiment and provide a reason for deletion
            % Note that the experiment will be moved to the openBIS trashcan
            % Required input arguments:
            % experiment... experiment returned by get_experiment / new_experiment methods
            % reason ... reason for deletion
            % Usage:
            % obi.delete_experiment(experiment, 'reason')
            
            experiment.delete(reason)
        end
        
        
        %% Object methods
        % this section defines following Matlab methods related to openBIS objects / samples:
        %   get_object
        %   get_objects
        %   new_object
        %   delete_object
        
        function objects = get_objects(obj, varargin)
            %get_objects
            % Return a table of objects matching specified criteria
            % Optional keyword arguments:
            % id ... object identifier ('SPACE/PROJECT/')
            % Usage:
            % objects = obi.get_objects()
            % objects = obi.get_objects('id', 'SPACE/')
            
            defaultId = '';
            
            p = inputParser;
            addRequired(p, 'obj');
            addParameter(p, 'id', defaultId, @ischar);
            parse(p, obj, varargin{:});
            a = p.Results;
            
            objects = obj.pybis.get_objects(a.id);
            objects = df_to_table(objects.df);
        end
        
        function object = get_object(obj, id)
            %get_object
            % Return object (sample) corresponding to the id
            % ID can be either the Space + Object code (e.g. /SPACE/123456789) or the PermID (e.g. 20181002164551373-1234)
            % An error is raised if an object with the id is not found on the server.
            % Required input arguments:
            % id ... object id
            % Usage:
            % object = obi.get_object('id')
            
            object = obj.pybis.get_object(id);
        end
        
        function object = new_object(obj, type, space, code)
            %new_object
            % Create a new object of type in space with code
            % Return the object
            % Input arguments
            % type ... object type
            % space ... Space code
            % code ... object code
            % Usage:
            % object = obi.new_object('type', 'space', 'code')
            
            object = obj.pybis.new_object(pyargs('type', type, 'space', space, 'code', code));
            object.save();
        end
        
        function object = delete_object(obj, object, reason)
            %delete_object
            % Delete object and provide a reason for deletion
            % Required input arguments:
            % object ... object returned by get_object / new_object methods
            % reason ... reason for deletion
            % Usage:
            % obi.delete_object(object, 'reason')
            
            object.delete(reason)
        end
        
        
        %% Dataset methods
        % this section defines following Matlab methods:
        %   get_datasets
        %   get_dataset
        %   get_dataset_files
        %   dataset_download
        %   new_dataset
        %   new_dataset_container
        
        function datasets = get_datasets(obj, varargin)
            %get_datasets
            % Return table of matching datasets.
            % Optional input arguments:
            % code ... dataset code / permId
            % type ... dataset type
            % experiment ... datasets in experiment
            % project ... datasets in project
            % tags ... datasets with tags
            % Usage:
            % datasets = obi.get_datasets()
            % datasets = obi.get_datasets('type', 'RAW_DATA')
            % datasets = obi.get_datasets('experiment', '/SPACE/PROJECT/EXPERIMENT')
            
            defaultCode = '';
            defaultType = '';
            defaultExp = '';
            defaultProj = '';
            defaultTags = '';
            
            p = inputParser;
            addRequired(p, 'obj');
            addParameter(p, 'code', defaultCode, @ischar);
            addParameter(p, 'type', defaultType, @ischar);
            addParameter(p, 'experiment', defaultExp, @ischar);
            addParameter(p, 'project', defaultProj, @ischar);
            addParameter(p, 'tags', defaultTags, @ischar);
            parse(p, obj, varargin{:});
            a = p.Results;
            
            datasets = obj.pybis.get_datasets(pyargs('code', a.code, 'type', a.type, 'experiment', a.experiment, ...
                'project', a.project, 'tags', a.tags));
            datasets = df_to_table(datasets.df);
        end
        
        function dataset = get_dataset(obj, permid, varargin)
            %get_dataset
            % Get dataset with permId. An error is raised if a dataset with the id is not found on the server.
            % Input arguments:
            % permId ... dataset permId
            % Usage:
            % dataset = obi.get_dataset('permId')
            
            only_data = false;
            
            p = inputParser;
            addRequired(p, 'obj');
            addRequired(p, 'permid', @ischar);
            addOptional(p, 'only_data', only_data, @islogical);
            parse(p, obj, permid, varargin{:});
            a = p.Results;
            
            %             dataset = obj.pybis.get_dataset(pyargs('permid', a.permid, 'only_data', a.only_data));
            dataset = obj.pybis.get_dataset(a.permid);
        end
        
        
        function files = get_dataset_files(obj, dataset, varargin)
            %get_dataset_files
            % Get list of files in a dataset starting with start_folder.
            % Input arguments:
            % dataset ... dataset object returned by get_dataset
            % start_folder ... starting folder for files (default: '/')
            % Usage:
            % files = obi.get_dataset_files(dataset)
            
            start_folder = '/';
            
            p = inputParser;
            addRequired(p, 'obj');
            addRequired(p, 'dataset');
            addOptional(p, 'start_folder', start_folder, @ischar);
            parse(p, obj, dataset, varargin{:});
            a = p.Results;
            
            files = dataset.get_files(pyargs('start_folder', a.start_folder));
            files = df_to_table(files);
        end
        
        function path_to_file = dataset_download(obj, dataset, files, varargin)
            %dataset_download
            % Download files in a dataset
            % dataset ... dataset object returned by get_dataset
            % files ... cell array of files
            % destination ... folder to download to (default: data)
            % wait_until_finished ... wait or download in the background (default: true)
            % workers ... number of workers to use for download (default: 10)
            % Usage:
            % path_to_files = obi.dataset_download(dataset, {'file1', 'file2'})
            
            destination = 'data';
            wait_until_finished = true;
            workers = 10;
            
            p = inputParser;
            addRequired(p, 'obj');
            addRequired(p, 'dataset');
            addRequired(p, 'files', @iscellstr);
            addParameter(p, 'destination', destination, @ischar);
            addParameter(p, 'wait_until_finished', wait_until_finished, @islogical);
            addParameter(p, 'workers', workers, @isscalar);
            
            parse(p, obj, dataset, files, varargin{:});
            a = p.Results;
            
            % ensure that files are passed as 1-N cell array (required for
            % Matlab to Python conversion)
            a.files = reshape(a.files, 1, numel(a.files));
            
            dataset.download(pyargs('files', a.files, 'destination', a.destination, 'wait_until_finished', a.wait_until_finished, 'workers', int16(a.workers)));
            
            path_to_file = fullfile(a.destination, dataset.char, a.files);
            
        end
        
        function dataset = new_dataset(obj, type, object, file_list, varargin)
            %new_dataset
            % Create a new dataset with files
            % type ... dataset type
            % object ... object for dataset (experiment)
            % file_list ... list of files (cell string) to upload to new dataset
            % properties ... structure with dataset properties (meta-data)
            % Usage:
            % dataset = obi.new_dataset('RAW_DATA', '/SPACE/PROJECT/OBJECT', {'file1', 'file2'}, 'properties', props)
            
            properties = struct;
            
            p = inputParser;
            addRequired(p, 'obj');
            addRequired(p, 'type', @ischar);
            addRequired(p, 'object', @ischar);
            addRequired(p, 'file_list', @iscellstr);
            addParameter(p, 'properties', properties, @isstruct);
            
            parse(p, obj, type, object, file_list, varargin{:});
            a = p.Results;
            
            dataset = obj.pybis.new_dataset(pyargs('type', a.type, 'experiment', a.object, ...
                'files', a.file_list, 'props', a.properties));
            dataset.save();
            
        end
        
        function dataset = new_dataset_container(obj, type, experiment, object)
            %new_dataset_container
            % Create a new dataset container
            % type ... dataset container type
            % experiment ... experiment for dataset container
            % object ... object for dataset container
            % Usage:
            % dataset = obi.new_dataset_container('type', 'RAW_DATA', 'experiment', 'MY_EXP', 'object', 'MY_SAMPLE')
            
            p = inputParser;
            addRequired(p, 'obj');
            addRequired(p, 'type', @ischar);
            addRequired(p, 'experiment', @ischar);
            addRequired(p, 'object', @ischar);
            
            parse(p, obj, type, experiment, object, file_list);
            a = p.Results;
            
            dataset = obj.pybis.new_dataset(pyargs('type', a.type, 'experiment', a.experiment, 'sample', a.object, 'kind', 'CONTAINER'));
            dataset.save();
            
        end
        
        
    end
    
end


