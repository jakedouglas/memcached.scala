package com.bitlove.memcached.spec

import org.specs.Specification

import com.bitlove.memcached.Memcached

object IncrAndDecrSpec extends Specification {
  val c   = new Memcached("localhost", 11211)
  val key = "some key".getBytes

  "Incr and Decr" should {
    doBefore {
      c.flush()
      c.get(key) must beEqualTo(None)
    }

    "normal" in {
      c.set(key, 0.toString.getBytes) must beEqualTo(true)
      c.incr(key, 1)                  must beEqualTo(Some(1))
      c.incr(key, 1)                  must beEqualTo(Some(2))
      c.incr(key, 1)                  must beEqualTo(Some(3))
      c.decr(key, 2)                  must beEqualTo(Some(1))
      new String(c.get(key).get)      must beEqualTo("1")
    }

    "with default value" in {
      c.incr(key, 1)                    must beEqualTo(None)
      c.incr(key, 1, default = Some(2)) must beEqualTo(Some(2))
      c.incr(key, 1)                    must beEqualTo(Some(3))
    }

    "with default value and ttl" in {
      c.incr(key, 1, default = Some(2),
                     ttl     = Some(1)) must beEqualTo(Some(2))

      Thread.sleep(2000)
      c.get(key) must beEqualTo(None)
    }

    "with huge numbers" in {
      c.incr(key, 0, default = Some(BigInt("FFFFFFFFFFFFFFFE", 16))).
        must(beEqualTo(Some(BigInt("FFFFFFFFFFFFFFFE", 16))))

      c.incr(key, 1) must beEqualTo(Some(BigInt("FFFFFFFFFFFFFFFF", 16)))
    }

    "has undefined overflow behavior" in {
      c.incr(key, 0, default = Some(BigInt("FFFFFFFFFFFFFFFF", 16))).
        must(beEqualTo(Some(BigInt("FFFFFFFFFFFFFFFF", 16))))

      c.incr(key, 1) mustNot beEqualTo(Some(BigInt("10000000000000000", 16)))
    }
  }
}
