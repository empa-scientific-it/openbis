package ch.systemsx.cisd.common.process;

/**
 * Strategy object to determine how the I/O of a process is handled.
 * <p>
 * A note on the <code>_SAME_THREAD_</code> variants: Reading the I/O in a separate thread is more efficient due to a lower polling overhead. However,
 * in corner cases where the process is terminated prematurely, the <code>_SAME_THREAD_</code> variant may be able to catch more of the process'
 * output than the variant using a separate thread. If you do not care about such corner cases, the variant using the separate thread should be
 * preferred.
 * 
 * @author Bernd Rinn
 */
public class ProcessIOStrategy
{
    /**
     * <ul>
     * <li>No input provided</li>
     * <li>Output in <code>stdout</code> is expected to be text.</li>
     * <li><code>stderr</code> is merged with <code>stdout</code>.</li>
     * <li>The output is read in a separate thread.</li>
     * </ul>
     */
    public static final ProcessIOStrategy TEXT_IO_STRATEGY = new ProcessIOStrategy(false, false,
            true, false, false, null);

    /**
     * <ul>
     * <li>No input provided</li>
     * <li>Output in <code>stdout</code> is expected to be text.</li>
     * <li><code>stderr</code> and <code>stdout</code> are recorded separately.</li>
     * <li>The output is read in a separate thread.</li>
     * </ul>
     */
    public static final ProcessIOStrategy TEXT_STDERR_SEPARATE_IO_STRATEGY = new ProcessIOStrategy(
            false, false, false, false, false, null);

    /**
     * <ul>
     * <li>No input provided</li>
     * <li>All output in <code>stderr</code> and <code>stdout</code> are discarded.</li>
     * <li>The output is read (and then discarded) in a separate thread.</li>
     * </ul>
     */
    public static final ProcessIOStrategy DISCARD_IO_STRATEGY = new ProcessIOStrategy(true, true,
            true, false, false, null);

    /**
     * <ul>
     * <li>No input provided</li>
     * <li>All output in <code>stdout</code> is discarded (while <code>stderr</code> is kept).</li>
     * <li>The output is read (and then discarded) in a separate thread.</li>
     * </ul>
     */
    public static final ProcessIOStrategy DISCARD_STDOUT_IO_STRATEGY = new ProcessIOStrategy(true,
            false, false, false, false, null);

    /**
     * <ul>
     * <li>No input provided</li>
     * <li>Output in <code>stdout</code> is expected to be text.</li>
     * <li>All output in <code>stderr</code> is discarded (while <code>stdout</code> is kept).</li>
     * <li>The output is read (and then discarded) in a separate thread.</li>
     * </ul>
     */
    public static final ProcessIOStrategy TEXT_DISCARD_STDERR_IO_STRATEGY = new ProcessIOStrategy(
            false, true, false, false, false, null);

    /**
     * <ul>
     * <li>No input provided</li>
     * <li>Output in <code>stdout</code> is expected to be binary.</li>
     * <li>All output in <code>stderr</code> is discarded (while <code>stdout</code> is kept).</li>
     * <li>The output is read (and then discarded) in a separate thread.</li>
     * </ul>
     */
    public static final ProcessIOStrategy BINARY_DISCARD_STDERR_IO_STRATEGY =
            new ProcessIOStrategy(false, true, false, true, false, null);

    /**
     * <ul>
     * <li>No input provided</li>
     * <li>Output in <code>stdout</code> is expected to be binary.</li>
     * <li><code>stderr</code> and <code>stdout</code> are recorded separately.</li>
     * <li>The output is read in a separate thread.</li>
     * </ul>
     */
    public static final ProcessIOStrategy BINARY_IO_STRATEGY = new ProcessIOStrategy(false, false,
            false, true, false, null);

    /**
     * <ul>
     * <li>No input provided</li>
     * <li>Output in <code>stdout</code> is expected to be text.</li>
     * <li><code>stderr</code> is merged with <code>stdout</code>.</li>
     * <li>The output is read in the same thread that waits for the process.</li>
     * </ul>
     */
    public static final ProcessIOStrategy TEXT_SAME_THREAD_IO_STRATEGY = new ProcessIOStrategy(
            false, false, true, false, true, null);

    /**
     * <ul>
     * <li>No input provided</li>
     * <li>Output in <code>stdout</code> is expected to be text.</li>
     * <li><code>stderr</code> and <code>stdout</code> are recorded separately.</li>
     * <li>The output is read in the same thread that waits for the process.</li>
     * </ul>
     */
    public static final ProcessIOStrategy TEXT_STDERR_SEPARATE_SAME_THREAD_IO_STRATEGY =
            new ProcessIOStrategy(false, false, false, false, true, null);

    /**
     * <ul>
     * <li>No input provided</li>
     * <li>All output in <code>stderr</code> and <code>stdout</code> are discarded.</li>
     * <li>The output is read (and discarded) in the same thread that waits for the process.</li>
     * </ul>
     */
    public static final ProcessIOStrategy DISCARD_SAME_THREAD_IO_STRATEGY = new ProcessIOStrategy(
            true, true, true, false, true, null);

    /**
     * <ul>
     * <li>No input provided</li>
     * <li>All output in <code>stdout</code> is discarded (while <code>stderr</code> is kept).</li>
     * <li>The output is read (and discarded) in the same thread that waits for the process.</li>
     * </ul>
     */
    public static final ProcessIOStrategy DISCARD_STDOUT_SAME_THREAD_IO_STRATEGY =
            new ProcessIOStrategy(true, false, false, false, true, null);

    /**
     * <ul>
     * <li>No input provided</li>
     * <li>Output in <code>stdout</code> is expected to be text.</li>
     * <li>All output in <code>stderr</code> is discarded (while <code>stdout</code> is kept).</li>
     * <li>The output is read (and discarded) in the same thread that waits for the process.</li>
     * </ul>
     */
    public static final ProcessIOStrategy TEXT_DISCARD_STDERR_SAME_THREAD_IO_STRATEGY =
            new ProcessIOStrategy(false, true, false, false, true, null);

    /**
     * <ul>
     * <li>No input provided</li>
     * <li>Output in <code>stdout</code> is expected to be binary.</li>
     * <li>All output in <code>stderr</code> is discarded (while <code>stdout</code> is kept).</li>
     * <li>The output is read (and discarded) in the same thread that waits for the process.</li>
     * </ul>
     */
    public static final ProcessIOStrategy BINARY_DISCARD_STDERR_SAME_THREAD_IO_STRATEGY =
            new ProcessIOStrategy(false, true, false, true, true, null);

    /**
     * <ul>
     * <li>No input provided</li>
     * <li>Output in <code>stdout</code> is expected to be binary.</li>
     * <li><code>stderr</code> and <code>stdout</code> are recorded separately.</li>
     * <li>The output is read in the same thread that waits for the process.</li>
     * </ul>
     */
    public static final ProcessIOStrategy BINARY_SAME_THREAD_IO_STRATEGY = new ProcessIOStrategy(
            false, false, false, true, true, null);

    /**
     * <ul>
     * <li>No input provided</li>
     * <li>Output in <code>stdout</code> is expected to be text.</li>
     * <li><code>stderr</code> is merged with <code>stdout</code>.</li>
     * <li>The output is read in a separate thread.</li>
     * </ul>
     */
    public static final ProcessIOStrategy DEFAULT_IO_STRATEGY = TEXT_IO_STRATEGY;

    /**
     * Creates a new IO strategy with a custom I/O handler.
     * <p>
     * This is the only way to provide input to a process.
     */
    public static ProcessIOStrategy createCustom(IProcessIOHandler customIOHandler)
    {
        return new ProcessIOStrategy(false, false, false, false, false, customIOHandler);
    }

    final boolean discardStandardOutput;

    final boolean discardStandardError;

    private final boolean mergeStderr;

    private final boolean binaryOutput;

    private final boolean useNoIOHandler;

    private final IProcessIOHandler customIOHandlerOrNull;

    ProcessIOStrategy(boolean discardStandardOutput, boolean discardStandardError,
            boolean mergeStderr, boolean binaryOutput, boolean useNoIOHandler,
            IProcessIOHandler customIOHandler)
    {
        this.discardStandardOutput = discardStandardOutput;
        this.discardStandardError = discardStandardError;
        this.mergeStderr = mergeStderr;
        this.binaryOutput = binaryOutput;
        this.useNoIOHandler = useNoIOHandler;
        this.customIOHandlerOrNull = customIOHandler;
    }

    public boolean isDiscardStandardOutput()
    {
        return discardStandardOutput;
    }

    public boolean isDiscardStandardError()
    {
        return discardStandardError;
    }

    public boolean isMergeStderr()
    {
        return mergeStderr;
    }

    public boolean isBinaryOutput()
    {
        return binaryOutput;
    }

    public boolean isUseNoIOHandler()
    {
        return useNoIOHandler;
    }

    public IProcessIOHandler tryGetCustomIOHandler()
    {
        return customIOHandlerOrNull;
    }
}