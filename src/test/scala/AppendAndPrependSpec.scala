package com.bitlove.memcached.spec

import org.specs.Specification
import org.specs.mock.Mockito
import org.mockito.Matchers._

import com.bitlove.memcached.Memcached

object AppendAndPrependSpec extends Specification with Mockito {
  val c        = new Memcached("localhost", 11211)
  val key      = "some key".getBytes
  val nonexist = "nonexist key".getBytes

  "works" in {
    c.flush()
    c.get(key) must beEqualTo(None)

    c.set(key, "bar".getBytes) must beEqualTo(true)

    c.prepend(key, "foo".getBytes) must beEqualTo(true)
    c.append(key, "baz".getBytes) must beEqualTo(true)
    new String(c.get(key).get) must beEqualTo("foobarbaz")

    c.append(nonexist, "blah".getBytes) must beEqualTo(false)
    c.prepend(nonexist, "blah".getBytes) must beEqualTo(false)
    c.get(nonexist) must beEqualTo(None)
  }
}