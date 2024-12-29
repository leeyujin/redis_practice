package org.example;

import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.args.GeoUnit;
import redis.clients.jedis.params.GeoSearchParam;
import redis.clients.jedis.resps.GeoRadiusResponse;
import redis.clients.jedis.resps.Tuple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        try (var jedisPool = new JedisPool("127.0.0.1", 6379)) {
            try (Jedis jedis = jedisPool.getResource()) {
                getspatialPractice(jedis);

            }
        }
    }

    private static void getspatialPractice(Jedis jedis) {
        String key = "stores2:geo";
        jedis.geoadd(key, 127.029, 37.499, "some1");
        jedis.geoadd(key, 127.033, 37.491, "some2");

        Double geodist = jedis.geodist(key, "some1", "some2", GeoUnit.M);
        System.out.println("geodist = " + geodist);

        GeoCoordinate geoCoordinate = new GeoCoordinate(127.031, 37.495);
        List<GeoRadiusResponse> geosearch = jedis.geosearch(key, geoCoordinate, 500, GeoUnit.M);
        // 아래 에러가 발생함
        // Cannot invoke "redis.clients.jedis.GeoCoordinate.getLatitude()" because the return value of "redis.clients.jedis.resps.GeoRadiusResponse.getCoordinate()" is null
//                geosearch.forEach(response -> {
//                    System.out.printf("%s, %f %f \n ", response.getMemberByString(), response.getCoordinate().getLatitude(), response.getCoordinate().getLongitude());
//                });

        // 아래처럼 파라미터 지정해야 coordinate 정보 가져올수 있음
        List<GeoRadiusResponse> geosearchWithCoord = jedis.geosearch(key,
                new GeoSearchParam()
                        .fromLonLat(geoCoordinate)
                        .byRadius(500, GeoUnit.M)
                        .withCoord()
        );
        geosearchWithCoord.forEach(response -> {
            System.out.printf("%s, %f %f \n ", response.getMemberByString(), response.getCoordinate().getLatitude(), response.getCoordinate().getLongitude());
        });
    }

    private static void sortedSetPractice(Jedis jedis) {
        String key = "game2:scores";
        HashMap<String, Double> scores = new HashMap<>();
        scores.put("user1", 100.0);
        scores.put("user2", 30.0);
        scores.put("user3", 50.0);
        scores.put("user4", 80.0);
        scores.put("user5", 15.0);
        jedis.zadd(key, scores);

        List<String> zrange = jedis.zrange(key, 0, Long.MAX_VALUE);
        zrange.forEach(System.out::println);

        List<Tuple> zrangeWithScores = jedis.zrangeWithScores(key, 0, Long.MAX_VALUE);
        zrangeWithScores.forEach(i -> System.out.printf("%s -> %s \n", i.getElement(), i.getScore()));

        System.out.println(jedis.zcard(key));

        jedis.zincrby(key, 100.0, "user5");
        List<Tuple> zrangeWithScores2 = jedis.zrangeWithScores(key, 0, Long.MAX_VALUE);
        zrangeWithScores2.forEach(i -> System.out.printf("%s -> %s \n", i.getElement(), i.getScore()));

        List<Tuple> zrevrange = jedis.zrevrangeWithScores(key, 0, Long.MAX_VALUE);
        zrevrange.forEach(System.out::println);
    }

    private static void hashPractice(Jedis jedis) {
        String key = "users:2:info";
        jedis.hset(key, "name", "greg2");
        HashMap<String, String> userInfoMap = new HashMap<>();
        userInfoMap.put("email", "greg2@gmail.com");
        userInfoMap.put("phone", "010-1111-2222");

        jedis.hset(key, userInfoMap);

        jedis.hdel(key, "phone");

        String email = jedis.hget(key, "email");
        System.out.println("email = " + email);
        Map<String, String> hgetAllResult = jedis.hgetAll(key);
        hgetAllResult.forEach((k, v) -> {
            System.out.printf("%s=%s\n", k, v);
        });

        long visit = jedis.hincrBy(key, "visit", 1);
        System.out.println("visit = " + visit);
    }

    private static void setPractice(Jedis jedis) {
        jedis.sadd("users:500:follow", "100", "200", "300");
        jedis.srem("users:500:follow", "100");
        Set<String> smembers = jedis.smembers("users:500:follow");
        smembers.forEach(System.out::println);

        boolean sismemberTrue = jedis.sismember("users:500:follow", "200");
        boolean sismemberFalse = jedis.sismember("users:500:follow", "100");
        System.out.println("sismemberTrue = " + sismemberTrue);
        System.out.println("sismemberFalse = " + sismemberFalse);

        System.out.println("SCARD : " + jedis.scard("users:500:follow"));

        jedis.sadd("users:600:follow", "200", "999", "9999");
        Set<String> sinter = jedis.sinter("users:500:follow", "users:600:follow");
        System.out.print("SINTER : ");
        sinter.forEach(System.out::println);
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