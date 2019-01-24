package zuo.biao.apijson.server;

import org.junit.Test;
import org.springframework.test.annotation.Repeat;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by zhangls on 2019/1/24.
 *
 * @author zhangls
 */
public class OptionalTest {


    @Test
    public void jdbcTemplateTest() {
        Optional<Integer> optional1 = Optional.ofNullable(1);
        Optional<Integer> optional2 = Optional.ofNullable(null);

        // 如果不是null,调用Consumer
        optional1.ifPresent(new Consumer<Integer>() {
            @Override
            public void accept(Integer t) {
                System.out.println("value is #" + t);
            }
        });

        // null,不调用Consumer
        optional2.ifPresent(new Consumer<Integer>() {
            @Override
            public void accept(Integer t) {
                System.out.println("value is @" + t);
            }
        });

        // orElse(value)：如果optional对象保存的值不是null，则返回原来的值，否则返回value
        System.out.println(optional1.orElse(1000) == 1);// true
        System.out.println(optional2.orElse(1000) == 1000);// true

        // orElseGet(Supplier supplier)：功能与orElse一样，只不过orElseGet参数是一个对象
        System.out.println(optional1.orElseGet(() -> {
            return 1000;
        }) == 1);//true

        System.out.println(optional2.orElseGet(() -> {
            return 1000;
        }) == 1000);//true


        // orElseThrow()：值不存在则抛出异常，存在则什么不做，有点类似Guava的Precoditions
//        optional1.orElseThrow(() -> {
//            throw new IllegalStateException();
//        });

//        try {
//            // 抛出异常
//            optional2.orElseThrow(() -> {
//                throw new IllegalStateException();
//            });
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        }



        //filter(Predicate)：判断Optional对象中保存的值是否满足Predicate，并返回新的Optional。
        Optional<Integer> filter1 = optional1.filter((a) -> a == null);
        Optional<Integer> filter2 = optional1.filter((a) -> a == 1);
        Optional<Integer> filter3 = optional2.filter((a) -> a == null);
        System.out.println(filter1.isPresent());// false
        System.out.println(filter2.isPresent());// true
        System.out.println(filter2.get().intValue() == 1);// true
        System.out.println(filter3.isPresent());// false



        //map(Function)：对Optional中保存的值进行函数运算，并返回新的Optional(可以是任何类型)
        Optional<String> str1Optional = optional1.map((a) -> "key" + a);
        Optional<String> str2Optional = optional2.map((a) -> "key" + a);

        System.out.println(str1Optional.get());// key1
        System.out.println(str2Optional.isPresent());// false



        //flatMap()：功能与map()相似，差别请看如下代码。flatMap方法与map方法类似，区别在于mapping函数的返回值不同。
        // map方法的mapping函数返回值可以是任何类型T，而flatMap方法的mapping函数必须是Optional。
        Optional<Optional<String>> str3Optional = optional1.map((a) -> {
            return Optional.<String>of("key" + a);
        });

        Optional<String> str4Optional = optional1.flatMap((a) -> {
            return Optional.<String>of("key" + a);
        });

        System.out.println(str3Optional.get().get());// key1
        System.out.println(str4Optional.get());// key1

        System.out.println("*****************************************************************");// key1
        String msg = null;
        Optional<String> optional = Optional.ofNullable(msg);
        // 判断是否有值，不为空
        boolean present = optional.isPresent();
        // 如果有值，则返回值，如果等于空则抛异常
        String value = optional.get();
        // 如果为空，返回else指定的值
        String hi = optional.orElse("hi");
        // 如果值不为空，就执行Lambda表达式
        optional.ifPresent(opt -> System.out.println(opt));


    }
}
