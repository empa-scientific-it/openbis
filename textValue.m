function pass = textValue
% Create figure and components.

ScreenSize = get(0,'ScreenSize');
fig = uifigure('Position',[(ScreenSize(3:4)-[300 75])/2 300 75]);
fig.CloseRequestFcn = @(fig,event)my_closereq(fig);

lbl = uilabel(fig, ...
    'Position',[10 10 150 20]);

txt = uieditfield(fig,...
    'Position',[10 40 150 20], ...
    'Tag', 'my_textfield', ...
    'ValueChangedFcn', @textChanged, ...
    'ValueChangingFcn', @textChanging, ...
    'UserData', '');


uiwait(fig)
    
    function my_closereq(fig,selection)
        pass = get(txt,'UserData');
        delete(fig)
        
    end

disp(pass)

end

% Callback functions
function textChanged(txt)
% disp(txt.UserData)
end

function textChanging(txt, event)
disp(event.Value);

if isempty(txt.UserData)
    txt.UserData = event.Value;
else
    txt.UserData = append(txt.UserData, event.Value(end));
end

val = event.Value;
val(1:length(val)) = '*';
txt.Value = val;

end


