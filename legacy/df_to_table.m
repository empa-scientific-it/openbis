function [matlab_table] = df_to_table(df)
%df_to_table
%   Returns a Matlab table for a Python dataframe

csv_temp = sprintf('%s.csv', tempname);
df.to_csv(csv_temp);
matlab_table = readtable(csv_temp);
delete(csv_temp);

end