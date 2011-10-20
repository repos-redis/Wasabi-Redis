package org.wasabiredis

import redis.clients.jedis.Jedis
import collection.JavaConversions
import JavaConversions._

/**
 * Date: 10/19/11
 * Time: 5:53 PM
 */

sealed trait RValue {
  protected val jedis: Jedis
  protected val key: String

  def expire(secs: Int) {
    jedis.expire(key, secs)
  }
}

/**
 *
 */
sealed class RString private[wasabiredis](protected val jedis: Jedis, protected val key: String) extends RValue {

  def get: Option[String] = Option(jedis.get(key))

  def :=(value: String) = jedis.set(key, value)

  def +=(value: String) = jedis.append(key, value)
}

/**
 *
 */
sealed class RLong private[wasabiredis](protected val jedis: Jedis, protected val key: String) extends RValue {

  def get: Option[Long] = Option(jedis.get(key)).map(_.toLong)

  def :=(value: Long) = jedis.set(key, value.toString)

  def ++() = jedis.incr(key)

  def +=(value: Long) = jedis.incrBy(key, value)

  def --() = jedis.decr(key)

  def -=(value: Long) = jedis.decrBy(key, value)
}

/**
 *
 */
sealed class RList private[wasabiredis](protected val jedis: Jedis, protected val key: String) extends RValue {

}

/**
 *
 */
sealed class RHash private[wasabiredis](protected val jedis: Jedis, protected val key: String) extends RValue {

  def apply(name: String): Option[String] = Option(jedis.hget(key, name))

  def +=(tuple: (String, String)) = jedis.hset(key, tuple._1, tuple._2)

  def ++=(values: Map[String, String]) = jedis.hmset(key, values)

  def -=(name: String) = jedis.hdel(key, name)

  def get: Map[String, String] = jedis.hgetAll(key).toMap

  def hasKey(name: String): Boolean = jedis.hexists(key, name)

  def keys: Set[String] = jedis.hkeys(key).toSet

  def values: Set[String] = jedis.hvals(key).toSet
}

/**
 *
 */
sealed class RSet private[wasabiredis](protected val jedis: Jedis, protected val key: String) extends RValue {

  def +=(value: String): Boolean = jedis.sadd(key, value) == 1

  def -=(value: String): Boolean = jedis.srem(key, value) == 1

  def get: Set[String] = jedis.smembers(key).toSet

  def contains(value: String): Boolean = jedis.sismember(key, value)

  def size: Long = jedis.scard(key)

  def intersection(sets: RSet*)(destination: String): RSet = {
    jedis.sinterstore(destination, (key :: sets.map(_.key) :: Nil).asInstanceOf[List[String]].toArray: _*)
    new RSet(jedis, destination)
  }

  def union(sets: RSet*)(destination: String): RSet = {
    jedis.sunionstore(destination, (key :: sets.map(_.key) :: Nil).asInstanceOf[List[String]].toArray: _*)
    new RSet(jedis, destination)
  }
}

/**
 *
 */
sealed class RSortedSet private[wasabiredis](protected val jedis: Jedis, protected val key: String) extends RValue {

  def +=(value: (String, Double)): Boolean = jedis.zadd(key, value._2, value._1) == 1

  def -=(value: String): Boolean = jedis.zrem(key, value) == 1

  def -=(z: ZRange): Long = jedis.zremrangeByRank(key, z.start, z.end)

  def -=(z: ZScoreRange): Long = jedis.zremrangeByScore(key, z.min, z.max)

  def get(z: ZRange): List[String] = jedis.zrange(key, z.start, z.end).toList

  def get(z: ZScoreRange): List[String] = jedis.zrangeByScore(key, z.min, z.max).toList

  def size: Long = jedis.zcard(key)

  def size(z: ZRange): Long = jedis.zcount(key, z.start, z.end)

  def increment(value: (String, Double)): Double = jedis.zincrby(key, value._2, value._1)

  def rank(value: String): Option[Long] = Option(jedis.zrank(key, value))

  def score(value: String): Option[Double] = Option(jedis.zscore(key, value))

  def intersection(sets: RSortedSet*)(destination: String): RSortedSet = {
    jedis.zinterstore(destination, (key :: sets.map(_.key) :: Nil).asInstanceOf[List[String]].toArray: _*)
    new RSortedSet(jedis, destination)
  }

  def union(sets: RSortedSet*)(destination: String): RSortedSet = {
    jedis.zunionstore(destination, (key :: sets.map(_.key) :: Nil).asInstanceOf[List[String]].toArray: _*)
    new RSortedSet(jedis, destination)
  }
}
case class ZRange(start: Int, end: Int)
case class ZScoreRange(min: Double, max: Double)

