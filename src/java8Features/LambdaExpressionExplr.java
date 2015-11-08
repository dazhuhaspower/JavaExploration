package java8Features;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class LambdaExpressionExplr {
    static List<String> list = Arrays.asList("VMware", "RSA", "Pivotal", "EMC", "Dell");
    static Consumer<Object> consumer = System.out::println;

    public static void main(String[] args) {
        runningTask1();
        runningTask2();
        runningTask3();
        runningTask4();
        runningTask5();
    }

    public static void runningTask1() {
        //functional interfaces: exactly one abstract method
        Collections.sort(list, (a, b) -> Integer.compare(a.length(), b.length()));
        printResult(list);

        Counter<String> counter = (input) -> input==null ? 0 : input.size();
        Integer size = counter.count(list);
        System.out.println("size of list: " + size);

        //method references
        Collections.sort(list, String::compareTo);
        printResult(list);

        Counter2 c2 = new Counter2();
        counter = c2::count;
        size = counter.count(list);
        System.out.println("size of list: " + size);

        //constructor references
        ConstructorRefFactory<ConstructorRef, Integer, String> constructorRefFactory = ConstructorRef::new;
        ConstructorRef constructorRef = constructorRefFactory.create(3401, "Hillview Avenue, Palo Alto, CA");
        System.out.println(constructorRef.e);
        System.out.println(constructorRef.t);
    }

    private static void printResult(List<String> list) {
        for(String str: list) {
            System.out.println(str);
        }
    }

    @FunctionalInterface
    interface Counter<E> {
        Integer count(List<E> input);
    }

    private static class Counter2 {
        public Integer count(List<String> input) {
            return input == null ? 0 : input.size();
        }
    }

    private static class ConstructorRef<E, T> {
        E e;
        T t;
        public ConstructorRef(E e, T t) {
            this.e = e;
            this.t = t;
        }
    }

    interface ConstructorRefFactory<P extends ConstructorRef, E, T> {
        P create(E e, T t);
    }

    //built-in functional interfaces
    public static void runningTask2() {
        Predicate<String> predicate = (s) -> s.indexOf('a') > 0;
        System.out.println(predicate.test("VMware"));
        System.out.println(predicate.negate().test("EMC"));

        Function<Integer, Integer> plusOne = (a) -> a+1;
        System.out.println(plusOne.apply(2));
        Function<Integer, Integer> plusTwo = (a) -> a+2;
        Function<Integer, Integer> plusThree = plusTwo.compose(plusOne);
        Function<Integer, Integer> plusFour = plusThree.andThen(plusOne);
        System.out.println(plusFour.apply(2));

        Supplier<Integer> supplier = () -> (int) (Math.random() * 10);
        System.out.println(supplier.get());

        Consumer<Object> consumer = System.out::println;
        consumer.accept(supplier.get());

        Comparator<String> comparator = (x, y) -> x.length()-y.length();
        consumer.accept(comparator.compare("EMC", "VMware") < 0);
        consumer.accept(comparator.reversed().compare("EMC", "VMware") < 0);
    }

    //streams
    public static void runningTask3() {
        Comparator<String> comparator = (x, y) -> x.length()-y.length();
        Predicate<String> predicate = (s) -> s.toLowerCase().indexOf('a') > 0;

        //sequential
        list.stream()
            .sorted(comparator)
            .map(String::toUpperCase)
            .filter(predicate)
            .forEach(System.out::println);

        boolean anyContainsA = list.stream().anyMatch(predicate);
        consumer.accept(anyContainsA);
        boolean allContainsA = list.stream().allMatch(predicate);
        consumer.accept(allContainsA);
        boolean noneContainsA = list.stream().noneMatch(predicate);
        consumer.accept(noneContainsA);
        long countAnyContainsA = list.stream().filter(predicate).count();
        consumer.accept(countAnyContainsA);
        String reduced = list.stream().filter(predicate).reduce("", (a, b) -> a + "." + b);
        consumer.accept(reduced);
        Optional<String> reduced2 = list.stream().filter(predicate).reduce((a, b) -> a + "." + b);
        reduced2.ifPresent(consumer);

        //parallel
        long slowSortedCount = list.stream().sorted().count();
        long fastSortedCount = list.parallelStream().sorted().count();
        consumer.accept(slowSortedCount);
        consumer.accept(fastSortedCount);
    }

    //map
    public static void runningTask4() {
        Map<Integer, String> map = new HashMap<Integer, String>();
        for(int i=0; i<list.size(); i++) {
            map.putIfAbsent(i, list.get(i));
        }
        map.forEach((key, val) -> System.out.println(val));
        map.computeIfPresent(0, (k, v) -> "Dazhu");
        consumer.accept(map);
        map.computeIfPresent(1, (k, v) -> null);
        consumer.accept(map);
        map.computeIfAbsent(1, k -> "EMC");
        consumer.accept(map);
        map.computeIfAbsent(1, k -> "haspower");
        consumer.accept(map);
        map.remove(1, "Dazhu");
        consumer.accept(map);
        map.remove(0, "Dazhu");
        consumer.accept(map);
        consumer.accept(map.getOrDefault(0, "Dazhu has power"));
        map.merge(0, "Dazhu", (val, newVal) -> val.concat(newVal));
        consumer.accept(map);
        map.merge(0, "haspower", (val, newVal) -> val.concat(newVal));
        consumer.accept(map);
    }

    //date
    public static void runningTask5() {
        Clock clock = Clock.systemDefaultZone();
        long millis = clock.millis();
        Instant instant = clock.instant();
        Date legacyDate = Date.from(instant);

        Set<String> zones = ZoneId.getAvailableZoneIds();
        zones.stream().filter((x) -> x.toLowerCase().contains("pacific")).forEach(System.out::println);
        zones.stream().filter((x) -> x.toLowerCase().contains("8")).forEach(System.out::println);
        ZoneId zone1 = ZoneId.of("US/Pacific");
        ZoneId zone2 = ZoneId.of("Etc/GMT-8");
        consumer.accept(zone1.getRules());
        consumer.accept(zone2.getRules());

        LocalTime localTime1 = LocalTime.now(zone1);
        consumer.accept(localTime1);
        LocalTime localTime2 = LocalTime.now(zone2);
        consumer.accept(localTime2);
        consumer.accept(localTime1.isAfter(localTime2));
        consumer.accept(ChronoUnit.HOURS.between(localTime1, localTime2));
    }
}
