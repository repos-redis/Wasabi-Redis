Wasabi Redis
============

Wasabi Redis is a Scala-centric Redis API. It wraps a (somewhat) type-safe veil around [Jedis](https://github.com/xetorthio/jedis), while attempting to make the result look more like Scala and less like the Redis wire protocol.

Redis has what's essentially a dynamic type system, consisting of _ordinals_ (integers), _byte arrays_ (strings), _lists_, _hashes_, _sets_ and _sorted sets_. Wasabi expresses Redis entries as *RValue*s, of which there are:
* RString
* RLong
* RHash
* RList
* RSet
* RSortedSet

Each has a Scala-looking API.