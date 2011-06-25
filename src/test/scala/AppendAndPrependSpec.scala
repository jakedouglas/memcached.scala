package com.bitlove.memcached.spec

import org.specs.Specification

import com.bitlove.memcached.Memcached

object AppendAndPrependSpec extends Specification {
  val c        = new Memcached("localhost", 11211)
  val key      = "some key".getBytes
  val nonexist = "nonexist key".getBytes

  "Append and Prepend" should {
    doBefore {
      c.flush()
      c.get(key) must beEqualTo(None)
    }

    "works with an existing key" in {
      c.set(key, "bar".getBytes) must beEqualTo(true)

      c.prepend(key, "foo".getBytes) must beEqualTo(true)
      c.append(key, "baz".getBytes) must beEqualTo(true)
      new String(c.get(key).get) must beEqualTo("foobarbaz")
    }

    "does nothing with a nonexistant key" in {
      c.append(nonexist, "blah".getBytes) must beEqualTo(false)
      c.prepend(nonexist, "blah".getBytes) must beEqualTo(false)
      c.get(nonexist) must beEqualTo(None)
    }
  }
}
