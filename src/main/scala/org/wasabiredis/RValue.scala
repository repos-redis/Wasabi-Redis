package org.wasabiredis

import collection.JavaConversions
import JavaConversions._
import redis.clients.jedis.{BinaryClient, Jedis}

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

  /**
   * Redis command: LINDEX (http://redis.io/commands/lindex)
   *
   * @return Some of element at position 'index', or None if index out of range
   */
  def apply(index: Long): Option[String] = Option(jedis.lindex(key, index))

  /**
   * === Usage ===
   * {{{
   *  insert("nextValue").after("pivotValue")
   *  insert("previousValue").before("pivotValue")
   *  insert("someValue").at(1)
   * }}}
   *
   * @return an inserter
   */
  def insert(value: String) = Inserter(value)

  private[wasabiredis] case class Inserter(value: String) {
    /**
     * Redis command: LINSERT (http://redis.io/commands/linsert)
     *
     * Inserts 'value' into the list before the 'pivot' value, is pivot value is present in the list.
     * @return whether pivot value was found, and 'value' inserted.
     */
    def before(pivot: String): Boolean = jedis.linsert(key, BinaryClient.LIST_POSITION.BEFORE, pivot, value) != -1

    /**
     * Redis command: LINSERT (http://redis.io/commands/linsert)
     *
     * Inserts 'value' into the list after the 'pivot' value, is pivot value is present in the list.
     * @return whether pivot value was found, and 'value' inserted.
     */
    def after(pivot: String): Boolean = jedis.linsert(key, BinaryClient.LIST_POSITION.AFTER, pivot, value) != -1

    /**
     * Redis command: LSET (http://redis.io/commands/linsert)
     *
     * NOTE: will throw an exception for out-of-range indexes
     */
    def at(index: Long) = jedis.lset(key, index, value)
  }

  /**
   * Redis command: LLEN (http://redis.io/commands/llen)
   *
   * @return length of list
   */
  def size: Long = jedis.llen(key)

  /**
   * Redis command: LPOP (http://redis.io/commands/lpop)
   *
   * Removes the head of the list if any, and brings it to you.
   *
   * @return Some of head of list, or None if list is empty.
   */
  def pop: Option[String] = Option(jedis.lpop(key))

  /**
   * Redis command: LPUSH (http://redis.io/commands/lpush)
   *
   * A chainable cons, ALMOST like a Scala List, except these WILL MUTATE THE LIST in Redis.
   *
   * ===Usage===
   * {{{
   *  val wisecrack = wasabi.list("wisecrack")
   *  "Bring" :: "me" :: "the" :: "head" :: "of" :: "prince" :: "charming" :: wisecrack
   *  wisecrack.get.mkString(" ")             //  "Bring me the head of prince charming"
   * }}}
   */
  def ::(value: String): RList = {jedis.lpush(key, value); this}

  /**
   * Redis command: LRANGE (http://redis.io/commands/lpush)
   *
   * @return the entire list
   */
  def get: List[String] = jedis.lrange(key, 0, Int.MaxValue).toList

  /**
   * Redis command: LRANGE (http://redis.io/commands/lpush)
   *
   * NOTE: this will NEVER go out of range.
   * If either index is out of range, Redis automatically adjusts it to fall within range.
   * If both are out of range, an empty list is returned.
   *
   * ANOTHER NOTE: unlike Redis' command, this is collections-correct - the end index is NON-INCLUSIVE, just like a normal list.
   *
   * @return the slice of list as specified, possibly empty
   */
  def slice(r: Range): List[String] = jedis.lrange(key, r.start, r.end - 1).toList

  /**
   * Redis command: LREM with count of (http://redis.io/commands/lrem)
   *
   * Removes all elements equal to 'value' from the list
   */
  def -=(value: String) = jedis.lrem(key, 0, value)

  /**
   * Redis command: LTRIM (http://redis.io/commands/ltrim)
   *
   * Trims the list, leaving only the elements within range.
   */
  def trim(leave: Range) = jedis.ltrim(key, leave.start, leave.end)

  /**
   * Redis command: RPOP http://redis.io/commands/rpop
   *
   * Removes and returns the last element in the list, if any.
   *
   * @return Some of last element, or None.
   */
  def takeLast: Option[String] = Option(jedis.rpop(key))

  /**
   * Redis command: RPOPLPUSH (http://redis.io/commands/rpoplpush)
   *
   * Atomically removes last and prepends it to the head of 'destination'
   */
  def moveLastToHeadOf(destination: RList) = jedis.rpoplpush(key, destination.key)

  /**
   * Redis command: RPUSH (http://redis.io/commands/rpush)
   *
   * Appends 'value' to the tail.
   */
  def +=(value: String) = jedis.rpush(key, value)
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

  def -=(z: Range): Long = jedis.zremrangeByRank(key, z.start, z.end)

  def -=(z: ScoreRange): Long = jedis.zremrangeByScore(key, z.min, z.max)

  def get(z: Range): List[String] = jedis.zrange(key, z.start, z.end).toList

  def get(z: ScoreRange): List[String] = jedis.zrangeByScore(key, z.min, z.max).toList

  def size: Long = jedis.zcard(key)

  def size(z: Range): Long = jedis.zcount(key, z.start, z.end)

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

/**
 * Helper for addressing into lists and sorted sets
 */
case class Range(start: Int, end: Int)

/**
 * Helper for addressing into sorted sets.
 */
case class ScoreRange(min: Double, max: Double)

