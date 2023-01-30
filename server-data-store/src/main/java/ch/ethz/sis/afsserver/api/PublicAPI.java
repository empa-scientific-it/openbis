package ch.ethz.sis.afsserver.api;

import ch.ethz.sis.afs.api.TwoPhaseTransactionAPI;

public interface PublicAPI extends OperationsAPI, AuthenticationAPI, TwoPhaseTransactionAPI {
}
