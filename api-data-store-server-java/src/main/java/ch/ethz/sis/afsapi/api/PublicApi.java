package ch.ethz.sis.afsapi.api;

import ch.ethz.sis.afs.api.TwoPhaseTransactionAPI;

public interface PublicApi extends OperationsApi, AuthenticationApi, TwoPhaseTransactionAPI {
}
