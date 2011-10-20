package org.wasabiredis

import redis.clients.jedis.Jedis

/**
 * Date: 10/19/11
 * Time: 5:52 PM
 */

trait WithJedis {
  val redisHost: String
  val redisPort: Int

  lazy val jedis = new Jedis(redisHost, redisPort)
}