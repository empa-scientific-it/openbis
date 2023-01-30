package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import org.python27.google.common.collect.Sets;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.dto.ContentCopyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LinkDataPE;

public class VerifyDataSetContentCopyExecutorTest {

	private VerifyDataSetContentCopyExecutor verifyDataSetContentCopyExecutor = new VerifyDataSetContentCopyExecutor();

	/**
	 * should not validate
	 */
	@Test
	public void testVerifyNonLinkDataPE()
	{
		// given
		DataPE dataPE = new DataPE();

		// when
		verifyDataSetContentCopyExecutor.verify(dataPE);

		// then
		// no exception
	}

	/**
	 * should be valid
	 */
	@Test
	private void testVerifyWithSameRepositoryId()
	{
		// given
		LinkDataPE linkDataPE = new LinkDataPE();
		linkDataPE.setContentCopies(Sets.newHashSet(contentCopyWithRepoId("repo1"), contentCopyWithRepoId("repo1")));

		// when
		verifyDataSetContentCopyExecutor.verify(linkDataPE);

		// then
		// no exception
	}

	/**
	 * should be invalid - same repository id has to be used within one dataset
	 */
	@Test(expectedExceptions = { IllegalArgumentException.class })
	private void testVerifyWithDifferentRepositoryId()
	{
		// given
		LinkDataPE linkDataPE = new LinkDataPE();
		linkDataPE.setContentCopies(Sets.newHashSet(contentCopyWithRepoId("repo1"), contentCopyWithRepoId("repo2")));

		// when
		verifyDataSetContentCopyExecutor.verify(linkDataPE);

		// then
		// exception
	}

	private ContentCopyPE contentCopyWithRepoId(String gitRepositoryId)
	{
		ContentCopyPE contentCopyPE2 = new ContentCopyPE();
		contentCopyPE2.setGitRepositoryId(gitRepositoryId);
		return contentCopyPE2;
	}

}
