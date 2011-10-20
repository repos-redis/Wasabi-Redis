Wasabi Redis
============

Wasabi Redis is a Scala-centric Redis API. It wraps a (somewhat) type-safe veil around [Jedis](https://github.com/xetorthio/jedis), while attempting to make the result look more like Scala and less like the Redis wire protocol.

Redis has what's essentially a dynamic type system, consisting of _ordinals_ (integers), _byte arrays_ (strings), _lists_, _hashes_, _sets_ and _sorted sets_. Wasabi expresses Redis cache entries as *RValue*, of which there are:

*   RString
*   RLong
*   RHash
*   RList
*   RSet
*   RSortedSet

Each has a Scala-looking API.

Installation
------------
At the moment, old-school: 

1. have SBT 0.10.1+
2. clone this repo
3. go

    sbt publish_local

Usage
-----

Get in instance of Wasabi:

    import org.wasabiredis._
    
    val wasabi = new Wasabi()
    val detailOrientedWasabi = new Wasabi("localhost", 6973)

Ask it for the _RValue_ at the key:

    val rl = wasabi.long("some_counter")
    val rs = wasabi.string("I've seen the future!")
    val rh = wasabi.hash("map_of_strings")
    val rl = wasabi.list("shopping")
    val rs = wasabi.set("disheveled_set")
    val ss = wasabi.sortedSet("sheveled_set")

