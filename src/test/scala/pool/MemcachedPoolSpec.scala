package com.bitlove.memcached.pool.spec

import org.specs.Specification

import com.bitlove.memcached.pool.MemcachedPool

object MemcachedPoolSpec extends Specification {
  val pool = new MemcachedPool("localhost", 11211)

  "works" in {
    pool { c =>
      c.flush()
      c.get("key".getBytes) must beNone
      c.set("key".getBytes, "value".getBytes) must beTrue
      new String(c.get("key".getBytes).get) must beEqualTo("value")
    }
  }
}
