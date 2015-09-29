package yield.core.windows;

import org.junit.Assert;
import org.junit.Test;

import yield.core.Aggregator;
import yield.core.EventQueue;
import yield.core.Producer;
import yield.core.Query;
import yield.core.Window;
import yield.core.Yielder;
import yield.core.aggregators.Count;
import yield.test.Collector;

public class AgingTest {
	class TestableAgingWindow extends AgingOut<String> {

		TestableAgingWindow() {
			super(5000);
		}

		@Override
		public void refreshAggregates() {
			super.refreshAggregates();
		}
	}

	@Test
	public void test() throws InterruptedException {
		EventQueue<String> dummies = new EventQueue<>(String.class);
		final TestableAgingWindow testWindow = new TestableAgingWindow();
		Query<Integer> windowLength = new Query<>(dummies).within(
				new Producer<Window<String>>() {

					@Override
					public Window<String> make() {
						return testWindow;
					}
				}, new Producer<Aggregator<String, Integer>>() {

					@Override
					public Aggregator<String, Integer> make() {
						return new Count<>();
					}
				});
		Yielder<Integer> aggregates = windowLength.getQueue();
		Collector<Integer> cAgg = new Collector<>();
		aggregates.bind(cAgg);
		testWindow.refreshAggregates();

		Assert.assertEquals(0, cAgg.get(0).intValue());

		dummies.feed("test 1");
		dummies.feed("test 2");
		Assert.assertEquals(1, cAgg.get(1).intValue());
		Assert.assertEquals(2, cAgg.get(2).intValue());

		Thread.sleep(3000);
		dummies.feed("test 3");
		Assert.assertEquals(3, cAgg.get(3).intValue());

		Thread.sleep(3000);
		Assert.assertEquals(1, cAgg.get(4).intValue());
	}

}
