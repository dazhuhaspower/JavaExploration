package java8Features;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class LambdaExpressionExplr {
    static List<String> list = Arrays.asList("VMware", "RSA", "Pivotal", "EMC", "Dell");

    public static void main(String[] args) {
        runningTask1();
        runningTask2();
        runningTask3();
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

        Consumer<Boolean> boolConsumer = (x) -> System.out.println(x);
        Consumer<Integer> intConsumer = (x) -> System.out.println(x);
        intConsumer.accept(supplier.get());

        Comparator<String> comparator = (x, y) -> x.length()-y.length();
        boolConsumer.accept(comparator.compare("EMC", "VMware") < 0);
        boolConsumer.accept(comparator.reversed().compare("EMC", "VMware") < 0);
    }

    //streams
    public static void runningTask3() {
        //list.stream()
            //.filter((s) -> s.indexOf('a') > 0 )
    }
}
