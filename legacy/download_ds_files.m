function [file_list] = download_ds_files(obi, ds_id, data_dir)
%UNTITLED Summary of this function goes here
%   Detailed explanation goes here

ds = obi.get_dataset(ds_id);
% download
ds.download(pyargs('destination', data_dir, 'wait_until_finished', false));

% get list and convert to Matlab cell array
file_list = cell(ds.get_file_list);

end
