function [url, user, pass] = user_url_pw_input_dialog
%user_url_pw_input
%   Return the URL, user name and password for the openBIS server

url = 'https://XYZ.ethz.ch/openbis:8443';
user = '';
pass = '';


ScreenSize = get(0,'ScreenSize');
fig = uifigure('Name', 'Enter openBIS credentials', 'Position',[(ScreenSize(3:4)-[300 75])/2 400 150]);
fig.CloseRequestFcn = @(fig,event)my_closereq(fig);

% URL label and text field
lbl_url = uilabel(fig, 'Text', 'URL:', ...
    'Position',[10 120 80 20]);

txt_url = uieditfield(fig,...
    'Position',[70 120 280 20], ...
    'Value', url, ...
    'Tag', 'url_textfield');

% User label and text field
lbl_user = uilabel(fig, 'Text', 'User:', ...
    'Position',[10 90 80 20]);

txt_user = uieditfield(fig,...
    'Position',[70 90 280 20], ...
    'Value', user, ...
    'Tag', 'user_textfield');

% Password label and text field
lbl_pass = uilabel(fig, 'Text', 'Password:', ...
    'Position',[10 60 80 20]);

txt_pass = uieditfield(fig,...
    'Position',[70 60 280 20], ...
    'Tag', 'pass_textfield', ...
    'ValueChangingFcn', @textChanging, ...
    'UserData', '');

% Push button to accept entries
btn = uibutton(fig,'push', ...
               'Position',[150 10 100 40], ...
               'Text', 'Connect', ...
               'FontWeight', 'bold', ...
               'ButtonPushedFcn', @(btn,event) buttonPushed(btn, fig));

uiwait(fig)
    
    % run this when figure closes
    function my_closereq(fig,selection)
        
        url = get(txt_url, 'Value');
        user = get(txt_user, 'Value');
        pass = get(txt_pass,'UserData');
        
        delete(fig)
        
    end

end

% Callback functions
function textChanging(txt, event)
% replace typed text with stars
% Todo: handle delete / backspace

% disp(event.Value);

if isempty(txt.UserData)
    txt.UserData = event.Value;
else
    txt.UserData = append(txt.UserData, event.Value(end));
end

val = event.Value;
if ~isempty(val)
    val(1:length(val)) = '*';
else
   val = '*'; 
end
txt.Value = val;

end

function buttonPushed(btn, fig)
% close the figure, call CloseRequestFcn before
        
        close(fig)
end


