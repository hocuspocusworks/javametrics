package ac.uk.lancs.seal.metric.provider;

import java.util.LinkedList;
import java.util.Queue;

import ac.uk.lancs.seal.metric.calculator.ClassCountMetric;
import ac.uk.lancs.seal.metric.calculator.FanInMetric;
import ac.uk.lancs.seal.metric.calculator.FanOutMetric;

public class JavaPackageManager extends MetricManager {

    @Override
    protected Queue<Metric> getMetrics() {
        Queue<Metric> metrics = new LinkedList<>();
        metrics.add(new ClassCountMetric());
        metrics.add(new FanInMetric());
        metrics.add(new FanOutMetric());
        return metrics;
    }
}
