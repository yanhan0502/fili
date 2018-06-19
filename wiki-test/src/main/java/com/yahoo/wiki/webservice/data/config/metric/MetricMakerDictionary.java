package com.yahoo.wiki.webservice.data.config.metric;

import com.yahoo.bard.webservice.data.config.metric.makers.*;
import com.yahoo.bard.webservice.data.dimension.DimensionDictionary;
import com.yahoo.bard.webservice.data.metric.MetricDictionary;
import com.yahoo.bard.webservice.druid.model.filter.SelectorFilter;
import com.yahoo.bard.webservice.druid.model.postaggregation.ArithmeticPostAggregation;
import com.yahoo.bard.webservice.druid.model.postaggregation.SketchSetOperationPostAggFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.*;

import static com.yahoo.bard.webservice.data.time.DefaultTimeGrain.DAY;

/**
 * MetricMaker dictionary.
 */
@Singleton
public class MetricMakerDictionary {

    private static final Logger LOG = LoggerFactory.getLogger(MetricMakerDictionary.class);

    /**
     * Maps for Metric Maker names to Metric Makers.
     */
    private final LinkedHashMap<String, MetricMaker> nameToMetricMaker;

    /**
     * Constructor
     */
    public MetricMakerDictionary() {
        nameToMetricMaker = new LinkedHashMap<>();
    }

    public MetricMakerDictionary(boolean useDefault, MetricDictionary metricDictionary) {
        nameToMetricMaker = new LinkedHashMap<>();
        if (!useDefault) return;

        add(new AggregationAverageMaker(metricDictionary, DAY));
        add(new CardinalityMaker(metricDictionary, new DimensionDictionary(),true));
        add(new ConstantMaker(metricDictionary));
        add(new CountMaker(metricDictionary));
        add(new DoubleMaxMaker(metricDictionary));
        add(new DoubleMinMaker(metricDictionary));
        add(new DoubleSumMaker(metricDictionary));
        add(new FilteredAggregationMaker(metricDictionary, new SelectorFilter(null, "1")));
        add(new LongMaxMaker(metricDictionary));
        add(new LongMinMaker(metricDictionary));
        add(new LongSumMaker(metricDictionary));
        add(new MaxMaker(metricDictionary));
        add(new MinMaker(metricDictionary));
        add(new RowNumMaker(metricDictionary));

        add(new SketchCountMaker(metricDictionary, 16000));
        add(new ThetaSketchMaker(metricDictionary, 16384));

        nameToMetricMaker.put("arithmeticplus", new ArithmeticMaker(metricDictionary, ArithmeticPostAggregation.ArithmeticPostAggregationFunction.PLUS));
        nameToMetricMaker.put("arithmeticminus", new ArithmeticMaker(metricDictionary, ArithmeticPostAggregation.ArithmeticPostAggregationFunction.MINUS));
        nameToMetricMaker.put("arithmeticmultiply", new ArithmeticMaker(metricDictionary, ArithmeticPostAggregation.ArithmeticPostAggregationFunction.MULTIPLY));
        nameToMetricMaker.put("arithmeticdivide", new ArithmeticMaker(metricDictionary, ArithmeticPostAggregation.ArithmeticPostAggregationFunction.DIVIDE));

        nameToMetricMaker.put("sketchsetoperationnot", new SketchSetOperationMaker(metricDictionary, SketchSetOperationPostAggFunction.NOT));
        nameToMetricMaker.put("sketchsetoperationintersect", new SketchSetOperationMaker(metricDictionary, SketchSetOperationPostAggFunction.INTERSECT));
        nameToMetricMaker.put("sketchsetoperationunion", new SketchSetOperationMaker(metricDictionary, SketchSetOperationPostAggFunction.UNION));

        nameToMetricMaker.put("thetasketchsetoperationnot", new ThetaSketchSetOperationMaker(metricDictionary, SketchSetOperationPostAggFunction.NOT));
        nameToMetricMaker.put("thetasketchsetoperationsect", new ThetaSketchSetOperationMaker(metricDictionary, SketchSetOperationPostAggFunction.INTERSECT));
        nameToMetricMaker.put("thetasketchsetoperationunion", new ThetaSketchSetOperationMaker(metricDictionary, SketchSetOperationPostAggFunction.UNION));
    }

    public MetricMakerDictionary(Set<MetricMaker> metricMakers) {
        this();
        addAll(metricMakers);
    }

    /**
     * Find a Metric Maker given a Metric Maker Name.
     *
     * @param metricMakerName  Name to search
     *
     * @return the first Metric Maker found (if exists)
     */
    public MetricMaker findByName(String metricMakerName) {
        return nameToMetricMaker.get(metricMakerName);
    }

    /**
     * Get all Metric Makers available in MetricMaker dictionary.
     *
     * @return a set of Metric Makers
     */
    public Set<MetricMaker> findAll() {
        return Collections.unmodifiableSet(new HashSet<>(nameToMetricMaker.values()));
    }

    /**
     * Adds the specified element to the dictionary if it is not already present.
     *
     * @param metricMaker element to add to dictionary
     *
     * @return <tt>true</tt> if the dictionary did not already contain the specified Metric Maker
     * @see Set#add(Object)
     */
    public boolean add(MetricMaker metricMaker) {
        String makerName = metricMaker.getClass().getSimpleName().replace("Maker","").toLowerCase();
        if (nameToMetricMaker.containsKey(makerName)) {
            return false;
        }
        MetricMaker metricMakers = nameToMetricMaker.put(makerName, metricMaker);
        if (metricMakers != null) {
            // should never happen unless multiple loaders are running in race-condition
            ConcurrentModificationException e = new ConcurrentModificationException();
            LOG.error("Multiple loaders updating MetricMakerDictionary", e);
            throw e;
        }
        return true;
    }

    /**
     * Adds all of the metricMakers in the specified collection to the dictionary.
     *
     * @param metricMakers collection of Metric Makers to add
     *
     * @return <tt>true</tt> if the dictionary changed as a result of the call
     * @see Set#addAll(Collection)
     */
    public boolean addAll(Collection<MetricMaker> metricMakers) {
        boolean flag = false;
        for (MetricMaker metricMaker : metricMakers) {
            flag = add(metricMaker) || flag;
        }
        return flag;
    }

    @Override
    public String toString() {
        return "MetricMaker Dictionary: " + nameToMetricMaker;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nameToMetricMaker);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj instanceof MetricMakerDictionary) {
            MetricMakerDictionary that = (MetricMakerDictionary) obj;
            return nameToMetricMaker.equals(that.nameToMetricMaker);
        }
        return false;
    }
}
