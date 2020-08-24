package hvac.util;

import jade.util.leap.ArrayList;
import jade.util.leap.List;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class JadeCollectors {
    public static <T> Collector<T, ?, jade.util.leap.List> toLeapList() {
        return new LeapListCollector<T>();
    }
    private static class LeapListCollector<T> implements Collector<T, jade.util.leap.ArrayList, jade.util.leap.List> {

        @Override
        public Supplier<ArrayList> supplier() {
            return ArrayList::new;
        }

        @Override
        public BiConsumer<ArrayList, T> accumulator() {
            return ArrayList::add;
        }

        @Override
        public BinaryOperator<ArrayList> combiner() {
            return (list1, list2)->{
                ArrayList res = new ArrayList();
                list1.iterator().forEachRemaining(res::add);
                list2.iterator().forEachRemaining(res::add);
                return res;
            };
        }

        @Override
        public Function<ArrayList, List> finisher() {
            return list->list;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Collections.emptySet();
        }
    }
}
