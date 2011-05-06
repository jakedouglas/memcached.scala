package com.bitlove.memcached.spec

import org.specs.Specification

import com.bitlove.memcached.Memcached

object CASSpec extends Specification {
  val c   = new Memcached("localhost", 11211)
  val c2  = new Memcached("localhost", 11211)
  val key = "some key".getBytes

  "CAS" should {
    doBefore {
      c.flush()
      c.get(key) must beEqualTo(None)
    }

    "with nothing there" in {
      var existing: Option[Array[Byte]] = None

      c.cas[Array[Byte]](key) { value =>
        existing = value
        Some("foo".getBytes)
      } must beEqualTo(true)

      existing must beEqualTo(None)
      new String(c.get(key).get) must beEqualTo("foo")
    }

    "with nothing there but the key is added during the operation" in {
      c.cas[Array[Byte]](key) { value =>
        c.set(key, "asdf".getBytes)
        Some("foo".getBytes)
      } must beEqualTo(false)

      new String(c.get(key).get) must beEqualTo("asdf")
    }

    "with the key already set" in {
      var existing: Option[Array[Byte]] = None
      c.set(key, "foo".getBytes)

      c.cas[Array[Byte]](key) { value =>
        existing = value
        Some("bar".getBytes)
      } must beEqualTo(true)

      new String(existing.get) must beEqualTo("foo")
      new String(c.get(key).get) must beEqualTo("bar")
    }

    "with the key already set and modified during the operation" in {
      c.set(key, "foo".getBytes)

      c.cas[Array[Byte]](key) { value =>
        c2.set(key, "baz".getBytes)
        Some("abc".getBytes)
      } must beEqualTo(false)

      new String(c.get(key).get) must beEqualTo("baz")
    }

    "returning None to delete the key" in {
      c.set(key, "foo".getBytes)

      c.cas[Array[Byte]](key) { value =>
        None
      } must beEqualTo(true)

      c.get(key) must beEqualTo(None)
    }

    "returning None to delete the key when it is modified during the operation" in {
      c.set(key, "foo".getBytes)

      c.cas[Array[Byte]](key) { value =>
        c2.set(key, "asdf".getBytes)
        None
      } must beEqualTo(false)

      new String(c.get(key).get) must beEqualTo("asdf")
    }
  }
}

