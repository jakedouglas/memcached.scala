package com.bitlove.memcached.spec

import org.specs.Specification
import org.specs.mock.Mockito
import org.mockito.Matchers._

import com.bitlove.memcached.Memcached

object AddAndReplaceSpec extends Specification with Mockito {
  val c           = new Memcached("localhost", 11211)
  val key         = "some key".getBytes
  val valueString = "blah"
  val value       = valueString.getBytes

  "add" in {
    c.flush()
    c.get(key) must beEqualTo(None)

    "returns true if it added it" in {
      c.add(key, value) must beEqualTo(true)
      new String(c.get(key).get) must beEqualTo(valueString)

      "returns false if it was already there" in {
        c.add(key, value) must beEqualTo(false)
      }
    }
  }

  "replace" in {
    c.flush()
    c.get(key) must beEqualTo(None)

    "returns false if the key didn't exist" in {
      c.replace(key, value) must beEqualTo(false)
      c.get(key) must beEqualTo(None)
    }

    "returns true if it replaced it" in {
      c.set(key, "sdfsdf".getBytes)
      new String(c.get(key).get) must beEqualTo("sdfsdf")

      c.replace(key, value) must beEqualTo(true)
      new String(c.get(key).get) must beEqualTo(valueString)
    }
  }
}