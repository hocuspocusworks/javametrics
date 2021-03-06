package ac.uk.lancs.seal.metric;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.StaticJavaParser;

import ac.uk.lancs.seal.metric.calculator.JavaMetricFactory;
import ac.uk.lancs.seal.metric.io.CsvOutputProcessor;
import ac.uk.lancs.seal.metric.io.DefaultPathUtil;
import ac.uk.lancs.seal.metric.io.PathUtil;
import ac.uk.lancs.seal.metric.io.ResultManager;
import ac.uk.lancs.seal.metric.provider.JavaMetricSelectionManager;
import ac.uk.lancs.seal.metric.provider.MetricManager;
import ac.uk.lancs.seal.metric.provider.ResultMap;

public class Client {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    public Client() {
        StaticJavaParser.getConfiguration().setLanguageLevel(LanguageLevel.CURRENT);
        StaticJavaParser.getConfiguration().setPreprocessUnicodeEscapes(true);
    }

    public static void main(String[] args) throws IOException {
        LOGGER.log(Level.INFO, "loading configurations");
        Properties properties = new Properties();
        CommandLineArgument.process(properties, args);
        loadConfig(properties);

        String path = properties.getProperty("projectRoot");
        List<Path> includePaths = stringsToPaths(Arrays.asList(properties.getProperty("includePaths").split(",")));
        List<Path> excludePaths = stringsToPaths(Arrays.asList(properties.getProperty("excludePaths").split(",")));
        List<String> includeFiles = Arrays.asList(properties.getProperty("includeFiles").split(","));
        List<String> excludeFiles = Arrays.asList(properties.getProperty("excludeFiles").split(","));
        List<String> selectedMetrics = Arrays.asList(properties.getProperty("selectedMetrics").split(","));
        String resultOutput = properties.getProperty("resultOutput");
        Global.DEBUG = properties.getProperty("debug").equals("true") ? true : false;

        LOGGER.log(Level.INFO, "resolving paths");
        ResultMap results = new ResultMap();
        PathUtil pathUtil = new DefaultPathUtil();
        List<Path> pathsList = pathUtil.getFilePathsRecursively(path);
        pathsList = pathUtil.filterIncludeFilesThatMatch(pathsList, includeFiles);
        pathsList = pathUtil.filterExcludeFilesThatMatch(pathsList, excludeFiles);
        pathsList = pathUtil.filterIncludeAbsolutePaths(pathsList, includePaths);
        pathsList = pathUtil.filterExcludeAbsolutePaths(pathsList, excludePaths);
        List<File> filesList = pathUtil.pathToFiles(pathsList);

        LOGGER.log(Level.INFO, "setting up and starting metric collection");
        MetricManager metricManager = null;
        JavaMetricSelectionManager jsm = new JavaMetricSelectionManager();
        jsm.addAll(JavaMetricFactory.getInstance().getMetrics(selectedMetrics));
        metricManager = jsm;
        metricManager.setInputFiles(filesList);
        metricManager.setOutputResult(results);
        metricManager.start();

        LOGGER.log(Level.INFO, "exporting metrics");
        ResultManager resultManager = new ResultManager(results, new CsvOutputProcessor(resultOutput));
        resultManager.export();

        LOGGER.log(Level.INFO, "finished");
    }

    private static List<Path> stringsToPaths(List<String> paths) {
        List<Path> result = new LinkedList<Path>();
        result = paths.stream().map(p -> Paths.get(p)).collect(Collectors.toList());
        return result;
    }

    private static Properties loadConfig(Properties properties) {
        String configFile = properties.getProperty("configFile");
        InputStream inStream;
        try {
            inStream = new FileInputStream(configFile);
            properties.load(inStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties;
    }
}
