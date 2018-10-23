function [datasets] = get_datasets(ds_type)
%UNTITLED2 Summary of this function goes here
%   Detailed explanation goes here

global obi

datasets = obi.get_datasets(pyargs('type',ds_type));
datasets = df_to_cell(datasets.df);
end
