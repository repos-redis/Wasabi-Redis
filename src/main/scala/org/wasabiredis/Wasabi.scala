package org.wasabiredis

/**
 * Date: 10/18/11
 * Time: 7:51 PM
 */

/**
 *
 */
class Wasabi(val redisHost: String = "localhost", val redisPort: Int = 6379) extends WithJedis {

  def exists(key: String) = jedis.exists(key)
  def delete(key: String) = jedis.del(key)

  def string(key: String) = new RString(jedis, key)
  def long(key: String) = new RLong(jedis, key)
  def hash(key: String) = new RHash(jedis, key)
  def set(key: String) = new RSet(jedis, key)
  def sortedSet(key: String) = new RSortedSet(jedis, key)
}