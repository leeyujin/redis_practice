package org.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");


        try (var jedisPool = new JedisPool("127.0.0.1", 6379)) {
            try (Jedis jedis = jedisPool.getResource()) {
                blockPop(jedis);
            }
        }
    }

    private static void blockPop(Jedis jedis) {
        List<String> blpop = jedis.blpop(10, "queue:blocking");
        if (blpop != null) {
            blpop.forEach(System.out::println);
        }
    }

    private static void queuePractice(Jedis jedis) {
        jedis.rpush("queue2", "aaaa");
        jedis.rpush("queue2", "bbbb");
        jedis.rpush("queue2", "cccc");

        List<String> queue2 = jedis.lrange("queue2", 0, -1);
        queue2.forEach(System.out::println);

        System.out.println(jedis.lpop("queue2"));
        System.out.println(jedis.lpop("queue2"));
        System.out.println(jedis.lpop("queue2"));
    }

    private static void stackPractice(Jedis jedis) {
        jedis.rpush("stack1", "aaaa");
        jedis.rpush("stack1", "bbbb");
        jedis.rpush("stack1", "cccc");

        List<String> stack1 = jedis.lrange("stack1", 0, -1);
        stack1.forEach(System.out::println);

        System.out.println(jedis.rpop("stack1"));
        System.out.println(jedis.rpop("stack1"));
        System.out.println(jedis.rpop("stack1"));
    }

    private static void useStringPipeline(Jedis jedis) {
        Pipeline pipelined = jedis.pipelined();
        pipelined.set("users:400:email", "greg@fastcampus.co.kr");
        pipelined.set("users:400:name", "greg");
        pipelined.set("users:400:age", "15");
        List<Object> objects = pipelined.syncAndReturnAll();
        objects.forEach(System.out::println);
    }

    private static void StringTest(Jedis jedis) {
        jedis.set("users:300:email", "lee@fastcampus.co.kr");
        jedis.set("users:300:name", "kim 00");
        jedis.set("users:300:age", "100");

        String userEmail = jedis.get("users:300:email");
        System.out.println("userEmail = " + userEmail);

        List<String> userInfo = jedis.mget("users:300:email", "users:300:name", "users:300:age");
        userInfo.forEach(System.out::println);

        long counter = jedis.incr("counter");
        System.out.println("counter = " + counter);

        counter = jedis.incrBy("counter", 10L);
        System.out.println("counter = " + counter);

        counter = jedis.decr("counter");
        System.out.println("counter = " + counter);

        counter = jedis.decrBy("counter", 20L);
        System.out.println("counter = " + counter);
    }
}