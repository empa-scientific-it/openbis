package ch.ethz.sis.startup;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import ch.ethz.sis.benchmark.Benchmark;
import ch.ethz.sis.benchmark.BenchmarkConfig;
import ch.ethz.sis.json.jackson.JacksonObjectMapper;
import ch.ethz.sis.logging.LogManager;
import ch.ethz.sis.logging.Logger;
import ch.ethz.sis.logging.log4j.Log4J2LogFactory;

public class Main
{
    static
    {
        // Configuring Logging
        LogManager.setLogFactory(new Log4J2LogFactory());
    }

    private static Logger logger = LogManager.getLogger(Main.class);
    
    public static void main(String[] args) throws Exception
    {
		logger.info("Current Workspace: " + (new File("").getAbsolutePath()));
        File configFile;
        if (args.length < 1)
        {
            configFile = new File("../openbis_benchmark/conf/config-dev.json");
            if(configFile.exists()) {
            		logger.info("No arguments given, starting with development config file: " + (configFile.getAbsolutePath()));
            } else {
            		configFile = new File("./config-default.json");
            		if(configFile.exists()) {
                		logger.info("No arguments given, starting with default config file: " + (configFile.getAbsolutePath()));
                }
            }
        } else
        {
            configFile = new File(args[0]);
        }

        BenchmarkConfig[] benchmarkConfigs = JacksonObjectMapper.getInstance().readValue(new FileInputStream(configFile), BenchmarkConfig[].class);
        logger.info("REPORT MAIN\ttotalRealTime\taggAVGOpTime\ttotalOpTime\tnumOps\tavgOpTime\tmaxOpTime\tminOpTime");
        for(BenchmarkConfig benchmarkConfig:benchmarkConfigs) { // For each benchmark
        		logger.traceAccess(null, benchmarkConfig);
        		long start = System.currentTimeMillis();
        		List<BenchmarkThread> threadsToJoin = new ArrayList<>();
        		for(int t = 0; t < benchmarkConfig.getThreads(); t++) {
        			BenchmarkThread thread = new BenchmarkThread(benchmarkConfig) {
        				public void run() {
        	        			benchmark.start();
        				}
        			};
        			threadsToJoin.add(thread);
        			thread.start();
        		}
                
        		for(BenchmarkThread thread:threadsToJoin) {
        			thread.join();
        		}
        		long end = System.currentTimeMillis();
        		logger.traceExit(benchmarkConfig);
        		logger.info("Benchmark took: " + (end-start) + " millis");
        		logJointStats(start, end, threadsToJoin);
        }
    }
    
    private static abstract class BenchmarkThread extends Thread {
    		protected Benchmark benchmark;
    		
    		public BenchmarkThread(BenchmarkConfig benchmarkConfig) {
    			super();
			try {
					benchmark = (Benchmark) Class.forName(benchmarkConfig.getClassName()).newInstance();
					benchmark.setConfiguration(benchmarkConfig);
			} catch (Exception e) {
				e.printStackTrace();
			}
    		}
    		
    		public Benchmark getBenchmark() {
    			return benchmark;
    		}
    }
    
    private static void logJointStats(long start, long end, List<BenchmarkThread> benchmarkThreads) {
    		long maxOpTime = Long.MIN_VALUE;
        long minOpTime = Long.MAX_VALUE;
        long numOps = 0;
        long totalOpTime = 0;
        
    		for(BenchmarkThread benchmarkThread:benchmarkThreads) {
        		totalOpTime += benchmarkThread.getBenchmark().getTotalOpTime();
        		numOps += benchmarkThread.getBenchmark().getNumOps();
        		if(benchmarkThread.getBenchmark().getMinOpTime() < minOpTime) {
        			minOpTime = benchmarkThread.getBenchmark().getMinOpTime();
        		}
        		if(benchmarkThread.getBenchmark().getMaxOpTime() > maxOpTime) {
        			maxOpTime = benchmarkThread.getBenchmark().getMaxOpTime();
        		}
    		}
    		
    		if(numOps > 0) {
    			logger.info("REPORT MAIN\t" + (end-start) + "\t" + ((end-start)/numOps) + "\t" + totalOpTime + "\t" + numOps + "\t" + (totalOpTime/numOps) + "\t" + maxOpTime + "\t" + minOpTime);
    		} else {
    			logger.info("REPORT MAIN\tNO-OP");
    		}
    }
}
