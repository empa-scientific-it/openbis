function [matlab_cell] = df_to_cell(df)
csv_temp = sprintf('%s.csv', tempname);
df.to_csv(csv_temp);
matlab_cell = readtable(csv_temp);
delete(csv_temp);
end