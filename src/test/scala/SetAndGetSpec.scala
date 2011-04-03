package com.bitlove.memcached.spec

import org.specs.Specification
import org.specs.mock.Mockito
import org.mockito.Matchers._

import com.bitlove.memcached.Memcached

object SetAndGetSpec extends Specification with Mockito {
  val c   = new Memcached("localhost", 11211)
  val key = "some key".getBytes

  "normal" in {
    c.flush()
    c.get(key) must beEqualTo(None)

    c.set(key, "blah".getBytes)
    new String(c.get(key).get) must beEqualTo("blah")

    c.set(key, "boom".getBytes)
    new String(c.get(key).get) must beEqualTo("boom")

    "delete" in {
      c.delete(key) must beEqualTo(true)
      c.get(key) must beEqualTo(None)
      c.delete(key) must beEqualTo(false)
    }
  }

  "with ttl" in {
    c.flush()
    c.get(key) must beEqualTo(None)

    c.set(key, "blah".getBytes, ttl = 1)
    new String(c.get(key).get) must beEqualTo("blah")

    Thread.sleep(2000)

    c.get(key) must beEqualTo(None)
  }
}