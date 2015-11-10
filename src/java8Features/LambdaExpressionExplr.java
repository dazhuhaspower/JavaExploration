package java8Features;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LambdaExpressionExplr {
    static List<String> list = Arrays.asList("VMware", "RSA", "Pivotal", "EMC", "Dell");
    private static boolean isVerbose = false; 
    static Consumer<Object> consumer = (x) -> {
            if(isVerbose) {
                System.out.println(x);
            }
        };
    static Consumer<Object> printer = System.out::println;

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
        consumer.accept("size of list: " + size);

        //method references
        Collections.sort(list, String::compareTo);
        printResult(list);

        Counter2 c2 = new Counter2();
        counter = c2::count;
        size = counter.count(list);
        consumer.accept("size of list: " + size);

        //constructor references
        ConstructorRefFactory<ConstructorRef, Integer, String> constructorRefFactory = ConstructorRef::new;
        ConstructorRef constructorRef = constructorRefFactory.create(3401, "Hillview Avenue, Palo Alto, CA");
        consumer.accept(constructorRef.e);
        consumer.accept(constructorRef.t);
    }

    private static void printResult(List<String> list) {
        for(String str: list) {
            consumer.accept(str);
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
        consumer.accept(predicate.test("VMware"));
        consumer.accept(predicate.negate().test("EMC"));

        Function<Integer, Integer> plusOne = (a) -> a+1;
        consumer.accept(plusOne.apply(2));
        Function<Integer, Integer> plusTwo = (a) -> a+2;
        Function<Integer, Integer> plusThree = plusTwo.compose(plusOne);
        Function<Integer, Integer> plusFour = plusThree.andThen(plusOne);
        consumer.accept(plusFour.apply(2));

        Supplier<Integer> supplier = () -> (int) (Math.random() * 10);
        consumer.accept(supplier.get());

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
            .forEach(consumer);

        list.stream()
            .findFirst()
            .ifPresent(consumer);

        boolean anyContainsA = list.stream().anyMatch(predicate);
        consumer.accept(anyContainsA);
        boolean allContainsA = list.stream().allMatch(predicate);
        consumer.accept(allContainsA);
        boolean noneContainsA = list.stream().noneMatch(predicate);
        consumer.accept(noneContainsA);
        long countAnyContainsA = list.stream().filter(predicate).count();
        consumer.accept(countAnyContainsA);
        String reduced = list.stream().filter(predicate).reduce("", (a, b) -> a + "." + b);
        printer.accept(reduced);
        Optional<String> reduced2 = list.stream().filter(predicate).reduce((a, b) -> a + "." + b);
        reduced2.ifPresent(printer);

        IntStream.range(1990, 2015)
            .forEach((x) -> {if(x%10==0) consumer.accept(x);});

        Arrays.stream(new int[] {1, 2, 3})
            .map(x -> 3*x)
            .average()
            .ifPresent(System.out::println);

        Stream.of("a", "bc", "xy", "abc")
            .map(s -> String.valueOf(s.length()))
            .mapToInt(Integer::parseInt)
            .max()
            .ifPresent(System.out::println);

        IntStream.range(0, 3)
            .mapToObj(i -> new SomeObject(i))
            .forEach(consumer);

        //processing order: each element moves along the chain vertically
        //hence might reduce the number of operations
        //tricks: filter first
        Stream.of(1, 2, 3, 4, 5, 6)
            .filter(x -> {
                consumer.accept("filter step: " + x);
                if(x > 5) {
                    return true;
                }
                return false;
            })
            .sorted((i1, i2) -> {
                consumer.accept("sort step: " + i1 + " " + i2);
                return i1 - i2;
            })
            .forEach(x -> {
                consumer.accept("forEach step: " + x);
            });

        //reuse streams: In Java 8, after any terminal operation the stream is closed. Further use of the stream will lead to Exception.
        Supplier<IntStream> streamSupplier = () -> IntStream.range(-10, 5).filter(x -> x<-9);
        streamSupplier.get().forEach(System.out::println);
        streamSupplier.get().average().ifPresent(System.out::println);

        //collect
        List<SomeObject> someObjects = Arrays.asList(
                new SomeObject(0, "EMC"),
                new SomeObject(1, "VMware"),
                new SomeObject(2, "RSA"),
                new SomeObject(3, "Pivotal"),
                new SomeObject(4, "DELL"));
        List<String> nameList = someObjects.stream().map(SomeObject::getName).collect(Collectors.toList());
        consumer.accept(nameList);
        Set<String> nameSet = someObjects.stream().map(SomeObject::getName).collect(Collectors.toCollection(TreeSet::new));
        consumer.accept(nameSet);
        String joined = someObjects.stream().map(Object::toString).collect(Collectors.joining(" #", "Result: ", "."));
        consumer.accept(joined);
        int total = someObjects.stream().collect(Collectors.summingInt(SomeObject::getVal));
        consumer.accept(total);
        IntSummaryStatistics summary = someObjects.stream().collect(Collectors.summarizingInt(SomeObject::getVal));
        consumer.accept(summary);
        Map<Integer, List<SomeObject>> byVal = someObjects.stream().collect(Collectors.groupingBy(SomeObject::getVal));
        consumer.accept(byVal);
        Map<Integer, Double> totalByNameLen = someObjects.stream()
                .collect(Collectors.groupingBy(x -> x.getName().length(), Collectors.averagingInt(SomeObject::getVal)));
        consumer.accept(totalByNameLen);
        Map<Boolean, List<SomeObject>> partition = someObjects.stream()
                .collect(Collectors.partitioningBy( s -> s.getName().length()>4));
        consumer.accept(partition);
        Collector<SomeObject, StringJoiner, String> nameCollector = Collector.of(
                () -> new StringJoiner(" # "), 
                (x, y) -> x.add(y.getName()), 
                (a, b) -> a.merge(b), 
                StringJoiner::toString);
        consumer.accept(someObjects.stream().collect(nameCollector));

        IntStream.range(0, 2)
            .mapToObj(i -> new Container("Container" + i))
            .peek(c -> IntStream.range(0, 2)
                    .mapToObj(i -> new SomeObject(i, "Object" + i + "/" + c.name))
                    .forEach(c::add))
            .flatMap(c -> c.objectList.stream())
            .forEach(s -> consumer.accept(s.name));

        someObjects.stream()
            .reduce((o1, o2) -> o1.val > o2.val ? o1 : o2)
            .ifPresent(printer);

        SomeObject so = someObjects.stream()
                            .reduce(new SomeObject(0, "sum val"), (o1, o2) -> {
                                o1.val += o2.val;
                                return o1;
                            });
        printer.accept(so);

        //parallel
        long slowSortedCount = list.stream().sorted().count();
        long fastSortedCount = list.parallelStream().sorted().count();
        consumer.accept(slowSortedCount);
        consumer.accept(fastSortedCount);

        Integer valSum = someObjects
                .parallelStream()
                .reduce(0, (sum, object) -> sum += object.val, (sum1, sum2) -> sum1 + sum2);
        printer.accept(valSum);
    }

    private static class Container {
        String name;
        List<SomeObject> objectList = new ArrayList<SomeObject>();
        public Container(String name) {
            this.name = name;
        }
        public void add(SomeObject object) {
            objectList.add(object);
        }
    }

    private static class SomeObject {
        int val;
        String name = null;

        public SomeObject(int val) {
            this.val = val;
        }
        public SomeObject(int val, String name) {
            this.val = val;
            this.name = name;
        }

        public int getVal() {
            return val;
        }
        public String getName() {
            return name;
        }

        public String toString() {
            if(name == null) {
                return String.valueOf(val);
            }
            return String.valueOf(val) + " : " + name;
        }
    }

    //map
    public static void runningTask4() {
        Map<Integer, String> map = new HashMap<Integer, String>();
        for(int i=0; i<list.size(); i++) {
            map.putIfAbsent(i, list.get(i));
        }
        map.forEach((key, val) -> {
            if(key > 3) {
                System.out.println(val);
            }
        });
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
        consumer.accept(millis);
        Instant instant = clock.instant();
        Date legacyDate = Date.from(instant);
        consumer.accept(legacyDate);

        Set<String> zones = ZoneId.getAvailableZoneIds();
        zones.stream().filter((x) -> x.toLowerCase().contains("pacific")).forEach(consumer);
        zones.stream().filter((x) -> x.toLowerCase().contains("8")).forEach(consumer);
        ZoneId zone1 = ZoneId.of("US/Pacific");
        ZoneId zone2 = ZoneId.of("Etc/GMT-8");
        consumer.accept(zone1.getRules());
        consumer.accept(zone2.getRules());

        //local time
        LocalTime localTime1 = LocalTime.now(zone1);
        consumer.accept(localTime1);
        LocalTime localTime2 = LocalTime.now(zone2);
        consumer.accept(localTime2);
        consumer.accept(localTime1.isAfter(localTime2));
        consumer.accept(ChronoUnit.HOURS.between(localTime1, localTime2));

        LocalTime localTime = LocalTime.of(11, 16, 58);
        consumer.accept(localTime);
        DateTimeFormatter germanFormatter = 
                DateTimeFormatter
                .ofLocalizedTime(FormatStyle.SHORT)
                .withLocale(Locale.GERMAN);
        LocalTime usLocalTime = LocalTime.parse("11:16", germanFormatter);
        consumer.accept(usLocalTime);

        //local date
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plus(1, ChronoUnit.DAYS);
        LocalDate yesterday = tomorrow.minusDays(3);
        consumer.accept(yesterday);
        LocalDate someDay = LocalDate.of(2015, Month.NOVEMBER, 9);
        DayOfWeek dayOfWeek = someDay.getDayOfWeek();
        consumer.accept(dayOfWeek);

        germanFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.GERMAN);
        LocalDate someDate = LocalDate.parse("24.12.2014", germanFormatter);
        consumer.accept(someDate);

        //local date time
        LocalDateTime localDateTime = LocalDateTime.of(2015, Month.NOVEMBER, 9, 14, 46, 59);
        consumer.accept(localDateTime.getDayOfWeek());
        consumer.accept(localDateTime.getMonth());
        long minuteOfDay = localDateTime.getLong(ChronoField.MINUTE_OF_DAY);
        consumer.accept(minuteOfDay);

        instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        legacyDate = Date.from(instant);
        consumer.accept(legacyDate);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm MMM-dd-yyyy");
        LocalDateTime parsed = LocalDateTime.parse("14:54 Nov-09-2015", formatter);
        String str = formatter.format(parsed);
        consumer.accept(str);
    }
}
