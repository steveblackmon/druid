package com.metamx.druid.aggregation;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class HistogramAggregatorTest
{
  private void aggregate(TestFloatMetricSelector selector, Aggregator agg)
  {
    agg.aggregate();
    selector.increment();
  }

  @Test
  public void testAggregate() throws Exception {
    final float[] values = {0.55f, 0.27f, -0.3f, -.1f, -0.8f, -.7f, -.5f, 0.25f, 0.1f, 2f, -3f};
    final float[] breaks = {-1f, -0.5f, 0.0f, 0.5f, 1f};

    final TestFloatMetricSelector selector = new TestFloatMetricSelector(values);

    HistogramAggregator agg = new HistogramAggregator("billy", selector, breaks);

    Assert.assertEquals("billy", agg.getName());

    Assert.assertArrayEquals(new long[]{0,0,0,0,0,0}, ((Histogram)agg.get()).bins);
    Assert.assertArrayEquals(new long[]{0,0,0,0,0,0}, ((Histogram)agg.get()).bins);
    Assert.assertArrayEquals(new long[]{0,0,0,0,0,0}, ((Histogram)agg.get()).bins);
    aggregate(selector, agg);
    Assert.assertArrayEquals(new long[]{0, 0, 0, 0, 1, 0}, ((Histogram) agg.get()).bins);
    Assert.assertArrayEquals(new long[]{0, 0, 0, 0, 1, 0}, ((Histogram) agg.get()).bins);
    Assert.assertArrayEquals(new long[]{0, 0, 0, 0, 1, 0}, ((Histogram) agg.get()).bins);
    aggregate(selector, agg);
    Assert.assertArrayEquals(new long[]{0, 0, 0, 1, 1, 0}, ((Histogram) agg.get()).bins);
    Assert.assertArrayEquals(new long[]{0, 0, 0, 1, 1, 0}, ((Histogram) agg.get()).bins);
    Assert.assertArrayEquals(new long[]{0, 0, 0, 1, 1, 0}, ((Histogram) agg.get()).bins);
    aggregate(selector, agg);
    Assert.assertArrayEquals(new long[]{0, 0, 1, 1, 1, 0}, ((Histogram) agg.get()).bins);
    aggregate(selector, agg);
    Assert.assertArrayEquals(new long[]{0, 0, 2, 1, 1, 0}, ((Histogram) agg.get()).bins);
    aggregate(selector, agg);
    Assert.assertArrayEquals(new long[]{0, 1, 2, 1, 1, 0}, ((Histogram) agg.get()).bins);
    aggregate(selector, agg);
    Assert.assertArrayEquals(new long[]{0, 2, 2, 1, 1, 0}, ((Histogram) agg.get()).bins);
    aggregate(selector, agg);
    Assert.assertArrayEquals(new long[]{0, 3, 2, 1, 1, 0}, ((Histogram) agg.get()).bins);
    aggregate(selector, agg);
    Assert.assertArrayEquals(new long[]{0, 3, 2, 2, 1, 0}, ((Histogram) agg.get()).bins);
    aggregate(selector, agg);
    Assert.assertArrayEquals(new long[]{0, 3, 2, 3, 1, 0}, ((Histogram) agg.get()).bins);
    aggregate(selector, agg);
    Assert.assertArrayEquals(new long[]{0, 3, 2, 3, 1, 1}, ((Histogram) agg.get()).bins);
    aggregate(selector, agg);
    Assert.assertArrayEquals(new long[]{1,3,2,3,1,1}, ((Histogram)agg.get()).bins);
  }

  private void aggregateBuffer(TestFloatMetricSelector selector, BufferAggregator agg, ByteBuffer buf, int position)
  {
    agg.aggregate(buf, position);
    selector.increment();
  }

  @Test
  public void testBufferAggregate() throws Exception {
    final float[] values = {0.55f, 0.27f, -0.3f, -.1f, -0.8f, -.7f, -.5f, 0.25f, 0.1f, 2f, -3f};
    final float[] breaks = {-1f, -0.5f, 0.0f, 0.5f, 1f};

    final TestFloatMetricSelector selector = new TestFloatMetricSelector(values);

    ArrayList<Float> b = Lists.newArrayList();
    for (int i = 0; i < breaks.length; ++i) {
      b.add(breaks[i]);
    }
    HistogramAggregatorFactory factory = new HistogramAggregatorFactory(
        "billy",
        "billy",
        b
    );
    HistogramBufferAggregator agg = new HistogramBufferAggregator(selector, breaks);

    ByteBuffer buf = ByteBuffer.allocateDirect(factory.getMaxIntermediateSize());
    int position = 0;

    agg.init(buf, position);
    Assert.assertArrayEquals(new long[]{0, 0, 0, 0, 0, 0}, ((Histogram) agg.get(buf, position)).bins);
    Assert.assertArrayEquals(new long[]{0, 0, 0, 0, 0, 0}, ((Histogram) agg.get(buf, position)).bins);
    Assert.assertArrayEquals(new long[]{0, 0, 0, 0, 0, 0}, ((Histogram) agg.get(buf, position)).bins);

    aggregateBuffer(selector, agg, buf, position);
    Assert.assertArrayEquals(new long[]{0, 0, 0, 0, 1, 0}, ((Histogram) agg.get(buf, position)).bins);
    Assert.assertArrayEquals(new long[]{0, 0, 0, 0, 1, 0}, ((Histogram) agg.get(buf, position)).bins);
    Assert.assertArrayEquals(new long[]{0, 0, 0, 0, 1, 0}, ((Histogram) agg.get(buf, position)).bins);

    aggregateBuffer(selector, agg, buf, position);
    Assert.assertArrayEquals(new long[]{0, 0, 0, 1, 1, 0}, ((Histogram) agg.get(buf, position)).bins);
    Assert.assertArrayEquals(new long[]{0, 0, 0, 1, 1, 0}, ((Histogram) agg.get(buf, position)).bins);
    Assert.assertArrayEquals(new long[]{0, 0, 0, 1, 1, 0}, ((Histogram) agg.get(buf, position)).bins);

    aggregateBuffer(selector, agg, buf, position);
    Assert.assertArrayEquals(new long[]{0, 0, 1, 1, 1, 0}, ((Histogram) agg.get(buf, position)).bins);

    aggregateBuffer(selector, agg, buf, position);
    Assert.assertArrayEquals(new long[]{0, 0, 2, 1, 1, 0}, ((Histogram) agg.get(buf, position)).bins);

    aggregateBuffer(selector, agg, buf, position);
    Assert.assertArrayEquals(new long[]{0, 1, 2, 1, 1, 0}, ((Histogram) agg.get(buf, position)).bins);

    aggregateBuffer(selector, agg, buf, position);
    Assert.assertArrayEquals(new long[]{0, 2, 2, 1, 1, 0}, ((Histogram) agg.get(buf, position)).bins);

    aggregateBuffer(selector, agg, buf, position);
    Assert.assertArrayEquals(new long[]{0, 3, 2, 1, 1, 0}, ((Histogram) agg.get(buf, position)).bins);

    aggregateBuffer(selector, agg, buf, position);
    Assert.assertArrayEquals(new long[]{0, 3, 2, 2, 1, 0}, ((Histogram) agg.get(buf, position)).bins);

    aggregateBuffer(selector, agg, buf, position);
    Assert.assertArrayEquals(new long[]{0, 3, 2, 3, 1, 0}, ((Histogram) agg.get(buf, position)).bins);

    aggregateBuffer(selector, agg, buf, position);
    Assert.assertArrayEquals(new long[]{0, 3, 2, 3, 1, 1}, ((Histogram) agg.get(buf, position)).bins);

    aggregateBuffer(selector, agg, buf, position);
    Assert.assertArrayEquals(new long[]{1,3,2,3,1,1}, ((Histogram)agg.get(buf, position)).bins);
  }
}