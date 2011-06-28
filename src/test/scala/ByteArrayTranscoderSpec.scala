package com.bitlove.memcached.spec

import org.specs.Specification

import com.bitlove.memcached.ByteArrayTranscoder

object ByteArrayTranscoderSpec extends Specification {
  val t = new ByteArrayTranscoder

  "this transcoder" in {
    val value   = "foo"
    val encoded = t.encode(value.getBytes)

    "just passes the bytes through" in {
      new String(encoded.data) must beEqualTo("foo")
    }

    "doesn't set any flags" in {
      encoded.flags must beEqualTo(0)
    }

    "decodes" in {
      new String(t.decode(encoded).data) must beEqualTo("foo")
    }
  }
}
