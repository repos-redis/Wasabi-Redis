package org.wasabiredis

import collection.JavaConversions
import JavaConversions._
import redis.clients.jedis.{BinaryClient, Jedis}

/**
 * Date: 10/19/11
 * Time: 5:53 PM
 */

trait RValue {
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