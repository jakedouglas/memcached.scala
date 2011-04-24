package com.bitlove.memcached.spec

import org.specs.Specification

import com.bitlove.memcached.Memcached
import com.bitlove.memcached.GzipByteArrayTranscoder

object MiscSpec extends Specification {
  val c = new Memcached("localhost", 11211)

  "isConnected" in {
    c.isConnected must beEqualTo(true)
  }

  "noop" in {
    c.noop
  }

  "close" in {
    c.close
    c.isConnected must beEqualTo(false)
  }
}
