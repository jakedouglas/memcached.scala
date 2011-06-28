package com.bitlove.memcached.spec

import org.specs.Specification

import com.bitlove.memcached._

object TranscoderChainingSpec extends Specification {
  val transcoder = new StringTranscoder(new GzipByteArrayTranscoder)
  val value      = "foo" * 1000

  "encode" in {
    val encoded = transcoder.encode(value)
    encoded.data.size must beLessThan(3000)
    new String(encoded.data) mustNot beEqualTo(value)

    "decode" in {
      new String(transcoder.decode(encoded).data) must beEqualTo(value)
    }
  }
}

