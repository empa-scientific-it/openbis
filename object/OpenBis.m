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
        function obj = OpenBis(varargin)
            % OpenBis   Constructor method for class OpenBis
            % Creates the Python Openbis object and logs into the server
            % Optional input arguments:
            % url ... URL of the openBIS server (incl. port)
            % user ... user name for openBIS
            % pw ... password for openBIS
            
            if nargin > 0
                url = varargin{1};
                user = varargin{2};
                pw = varargin{3};
            else
                [url, user, pw] = user_url_pw_inputdlg;
            end
            
            o = py.pybis.Openbis(url, pyargs('verify_certificates', 0));
            o.login(user, pw, pyargs('save_token', 1));
            obj.pybis = o;
        end
        
        function logout(obj)
            % logout Log out of openBIS.
            % After logout, the session token is no longer valid.
            obj.pybis.logout();
        end
        
        
        %% Masterdata methods
        % this section defines Matlab equivalents of the following pyBIS methods:
        % get_experiment_types
        % get_sample_types
        % get_material_types
        % get_dataset_types
        % get_terms
        % get_tags
        
        function experiment_types = get_experiment_types(obj)
            % Return table of all available experiment types.
            experiment_types = obj.pybis.get_experiment_types();
            experiment_types = df_to_table(experiment_types.df);
        end
        
        function sample_types = get_sample_types(obj)
            % Return table of all available sample types.
            sample_types = obj.pybis.get_sample_types();
            sample_types = df_to_table(sample_types.df);
        end
        
        function material_types = get_material_types(obj)
            % Return table of all available material types.
            material_types = obj.pybis.get_material_types();
            material_types = df_to_table(material_types.df);
        end
        
        function dataset_types = get_dataset_types(obj)
            % Return table of all available dataset types.
            dataset_types = obj.pybis.get_dataset_types();
            dataset_types = df_to_table(dataset_types.df);
        end
        
        function terms = get_terms(obj)
            % Return table of all available terms.
            terms = obj.pybis.get_terms();
            terms = df_to_table(terms.df);
        end
        
        function tags = get_tags(obj)
            % Return table of all available tags.
            tags = obj.pybis.get_tags();
            tags = df_to_table(tags.df);
        end
        
        
        %% Space methods
        % this section defines Matlab equivalents of the following pyBIS methods:
        % get_spaces
        % get_space
        % new_space
        % space.delete
        
        function spaces = get_spaces(obj)
            % Return table of all available spaces.
            spaces = obj.pybis.get_spaces();
            spaces = df_to_table(spaces.df);
        end
        
        function space = get_space(obj, code)
            % Get space with code and return the space object
            % Input arguments
            % code ... space code
            space = obj.pybis.get_space(code);
        end
        
        function space = new_space(obj, code, description)
            % Create a new space with code and description and return the space object
            % Input arguments
            % code ... Space code
            % description ... Space description
            space = obj.pybis.new_space(pyargs('code',  code, ...
                'description', description));
            space.save;
        end
        
        function delete_space(obj, code, reason)
            % Delete space with code and provide a reason for deletion
            % Input arguments
            % code ... Space code
            % reason ... reason for deletion
            space = obj.pybis.get_space(code);
            space.delete(reason);
        end
        
        
        %% Project methods
        % this section defines Matlab equivalents of the following pyBIS methods:
        % get_projects
        % get_project
        % new_project
        
        function projects = get_projects(obj, space, code)
            % Return table of matching projects.
            % Input arguments:
            % space ... space to fetch projects from
            % project ... fetch projects matching code
            projects = obj.pybis.get_projects(pyargs('space',  space, 'code', code));
            projects = df_to_table(projects.df);
        end
        
        function project = get_project(obj, id)
            % Return matching project.
            % Input arguments:
            % id ... project permID
            project = obj.pybis.get_project(id);
        end
        
        function project_cell = get_project_by_code(obj, space, code)
            % Return matching project.
            % Input arguments:
            % space ... space code
            % code ... project code
            projects = obj.get_projects(space, code);
            project_ids = projects.permId;
            project_cell = cell(1, numel(project_ids));
            for ix = 1:numel(project_ids)
                project_cell{ix} = obj.get_project(project_ids{ix});
            end
        end
        
        function project = new_project(obj, space, code, description)
            % Create a new project in space with code and description
            % Return the project object
            % Input arguments
            % space ... Space code
            % code ... Project code
            % description ... Project description
            space = obj.pybis.get_space(space);
            project = space.new_project(pyargs('code', code,  'description', description));
            project.save();
        end
        
        
    end
    
end





%         function obj = login(obj, varargin)
%             if nargin >1
%                 user = varargin{1};
%                 pw = varargin{2};
%             else
%                 [user, pw] = user_url_pw_inputdlg;
%             end
%
%             o = py.pybis.Openbis(obj.url, pyargs('verify_certificates', 0));
%             o.login(user, pw, pyargs('save_token', 1));
%             obj.pybis = o;
%         end
