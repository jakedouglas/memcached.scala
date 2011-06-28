package com.bitlove.memcached.spec.transcoding

import org.specs.Specification

import com.bitlove.memcached.transcoding.StringTranscoder

object StringTranscoderSpec extends Specification {
  val t = new StringTranscoder

  "this transcoder" in {
    val value   = "foo"
    val encoded = t.encode(value)

    "turns the string into bytes" in {
      new String(encoded.data) must beEqualTo("foo")
    }

    "doesn't set any flags" in {
      encoded.flags must beEqualTo(0)
    }

    "decodes" in {
      t.decode(encoded).data must beEqualTo("foo")
    }
  }
}
