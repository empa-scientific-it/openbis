function [user, pw] = user_pass_input
%user_pw_input
%   UI window to obtain user name and pwword for openBIS

% check if Java is available (Matlab not started with -nojvm flag)
if ~usejava('awt') 
    error('This function requires Java. Start Matlab with Java enabled.')
end

% default values
user = ''; pw = '';

% Setup figure for UI window
sz = get(0, 'ScreenSize');
dlgName = 'openBIS Credentials';

% setup figure window
hFig  = figure(WindowStyle='modal', Position=[(sz(3:4)-[350 100])/2 350 100], Name=dlgName, ...
    Resize='off', NumberTitle='off', Menubar='none', Color=[0.9 0.9 0.9], CloseRequestFcn=@(~,~)uiresume);

% setup text field for user name
hUser = uicontrol(hFig, Style='edit', Position=[80 70 250 20], KeyPressFcn=@userKeyPress, ...
    FontSize=10, BackGroundColor='w', String=user);

% setup text field for password
hPw = uicontrol(hFig, Style='edit', Position=[80 40 250 20], KeyPressFcn=@pwKeyPress, ...
    FontSize=10, BackGroundColor='w', String='');

% labels for text fields
annotation(hFig, 'textbox', Units='pixels', Position=[00 70 80 20], String='Username', ...
    EdgeColor='n', VerticalAlignment='middle', HorizontalAlignment='right')
annotation(hFig, 'textbox', Units='pixels', Position=[00 40 80 20], String='Password', ...
    EdgeColor='n', VerticalAlignment='middle', HorizontalAlignment='right')

% OK / cancel buttons
hOK = uicontrol(hFig, Style="pushbutton", Position=[140 7 50 20], Callback=@okClick, String='OK');
hCancel = uicontrol(hFig, Style="pushbutton", Position=[215 7 50 20], Callback=@cancelClick, String='Cancel');

uicontrol(hUser) % give focuse to username field
uiwait % wait for uiresume command
drawnow

user = hUser.String;
delete(hFig)


    function userKeyPress(~, event)
        if event.Key == "return"
            uiresume, return %done
        elseif event.Key == "escape"
            hUser.String = ''; pw = '';
            uiresume, return %abort
        end
    end

    function pwKeyPress(~, event)
        if event.Key == "backspace"
            pw = pw(1:end-1); %shorten pwword
        elseif event.Key == "return"
            uiresume, return %done
        elseif event.Key == "escape"
            hUser.String = ''; pw = '';
            uiresume, return %abort
        elseif contains(event.Character,num2cell(char(32:126)))
            pw(end+1) = event.Character; % append key to password
        end
        redrawPassField(pw)
    end

    function redrawPassField(pw)
        % redraw the entire password text field with the entered value
        % hidden
        hPw = uicontrol(hFig, Style='edit', Position=[80 40 250 20], KeyPressFcn=@pwKeyPress, ...
            FontSize=10, BackGroundColor='w', String=repmat(char(8226),size(pw)));
    end

    function okClick(source, event)
        uiresume, return
    end

    function cancelClick(source, event)
        % default values
        hUser.String = ''; pw = '';
        uiresume, return
    end

end