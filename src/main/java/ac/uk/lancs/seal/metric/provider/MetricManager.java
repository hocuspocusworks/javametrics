package ac.uk.lancs.seal.metric.provider;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class MetricManager implements MetricProvider {
    private static final Logger LOGGER = Logger.getLogger(MetricManager.class.getName());
    private List<File> files;
    private ResultMap results;

    @Override
    public void setInputFiles(List<File> files) {
        this.files = files;
    }

    @Override
    public void setOutputResult(ResultMap results) {
        this.results = results;
    }

    @Override
    public void start() {
        Queue<Metric> metrics = getMetrics();
        int queueSize = metrics.size();

        for (int i = 0; i < queueSize; i++) {
            GenericMetric metric = metrics.remove().getMetric();
            String metricFqn = metric.getFqn();
            MetricCalculator metricCalculator = metric.getMetricCalculator();
            PreprocessStorage<?> storage = metric.getPreprocessStorage();
            Map<String, String> tmpResult = new HashMap<>();
            for (File file : files) {
                try {
                    LOGGER.log(Level.INFO, "processing {0} file", file.getCanonicalPath().toString());
                } catch (Exception e) {
                }
                metricCalculator.process(file, tmpResult, storage);
            }
            metricCalculator.postprocess(tmpResult, storage);
            mergeResults(metricFqn, tmpResult);
        }
    }

    private void mergeResults(String metricFqn, Map<String, String> metricResults) {
        results.put(metricFqn, metricResults);
    }

    protected abstract Queue<Metric> getMetrics();
}
