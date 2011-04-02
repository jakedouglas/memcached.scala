package com.bitlove.memcached.spec

import org.specs.Specification
import org.specs.mock.Mockito
import org.mockito.Matchers._

import com.bitlove.memcached.Memcached

object IncrAndDecrSpec extends Specification with Mockito {
  val c   = new Memcached("localhost", 11211)
  val key = "some key".getBytes

  "normal" in {
    c.flush()
    c.get(key) must beEqualTo(None)

    c.set(key, 0.toString.getBytes) must beEqualTo(true)
    c.incr(key, 1)                  must beEqualTo(Some(1))
    c.incr(key, 1)                  must beEqualTo(Some(2))
    c.incr(key, 1)                  must beEqualTo(Some(3))
    c.decr(key, 2)                  must beEqualTo(Some(1))
    new String(c.get(key).get)      must beEqualTo("1")
  }

  "with default value" in {
    c.flush()
    c.get(key) must beEqualTo(None)

    c.incr(key, 1)                    must beEqualTo(None)
    c.incr(key, 1, default = Some(2)) must beEqualTo(Some(2))
    c.incr(key, 1)                    must beEqualTo(Some(3))
  }

  "with huge numbers" in {
    c.flush()
    c.get(key) must beEqualTo(None)

    c.incr(key, 100, default = Some(BigInt("FFFFFFFFFFFFFFFE", 16))).
      must(beEqualTo(Some(BigInt("FFFFFFFFFFFFFFFE", 16))))

    c.incr(key, 1) must beEqualTo(Some(BigInt("FFFFFFFFFFFFFFFF", 16)))

    "undefined overflow" in {
      c.incr(key, 1) mustNot beEqualTo(Some(BigInt("10000000000000000", 16)))
    }
  }
}