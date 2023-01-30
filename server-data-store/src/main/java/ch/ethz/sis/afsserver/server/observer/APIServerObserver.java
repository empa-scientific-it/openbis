package ch.ethz.sis.afsserver.server.observer;

import ch.ethz.sis.afsserver.server.Worker;
import ch.ethz.sis.shared.startup.Configuration;

import java.util.Map;

public interface APIServerObserver<CONNECTION> {

    public void init(Configuration configuration) throws Exception;

    public void beforeAPICall(Worker<CONNECTION> worker, String method, Map<String, Object> params) throws Exception;

    public void afterAPICall(Worker<CONNECTION> worker, String method, Map<String, Object> params) throws Exception;
}
