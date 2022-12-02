package ch.ethz.sis.afsserver.server;

import ch.ethz.sis.shared.startup.Configuration;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

public interface APIServerObserver<CONNECTION> {

    public void init(Configuration configuration) throws Exception;

    public List<Method> getAdditionalMethods();

    public boolean isAdditionalMethod(Method method);

    public Object getInvokableObject(Method method);

    public Parameter[] getAPIParameters(Method method);

    public void beforeAPICall(Worker<CONNECTION> worker, String method, Map<String, Object> params) throws Exception;

    public void afterAPICall(Worker<CONNECTION> worker, String method, Map<String, Object> params) throws Exception;
}
