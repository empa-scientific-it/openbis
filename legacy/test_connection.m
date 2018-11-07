function test_connection(obi)

if ~ismethod(obi, 'is_session_active')
    error('No connection to openBIS. Did you log in?') 
end
if ~obi.is_session_active
   error('No active connection found. Try to re-connect.') 
end

end

