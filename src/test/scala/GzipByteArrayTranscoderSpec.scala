package com.bitlove.memcached.spec

import org.specs.Specification
import org.specs.mock.Mockito
import org.mockito.Matchers._

import com.bitlove.memcached.Memcached
import com.bitlove.memcached.GzipByteArrayTranscoder

object GzipByteArrayTranscoderSpec extends Specification with Mockito {
  val t = new GzipByteArrayTranscoder(10)

  "with a value smaller than the minimum size" in {
    val value   = "foobarbaz"
    val encoded = t.encode(value.getBytes)

    "does not compress it" in {
      new String(encoded.data) must beEqualTo(value)
    }

    "does not set the compressed flag" in {
      encoded.flags must beEqualTo(0)
    }

    "can decode it properly" in {
      new String(t.decode(encoded)) must beEqualTo(value)
    }
  }

  "with a value larger than the minimum size" in {
    val value   = "abcdefghijklmn"
    val encoded = t.encode(value.getBytes)

    "compresses the value" in {
      new String(encoded.data) mustNot beEqualTo(value)
    }

    "sets the compressed flag" in {
      encoded.flags must beEqualTo(2)
    }

    "can decode it properly" in {
      new String(t.decode(encoded)) must beEqualTo(value)
    }

    "actually makes it smaller if its huge" in {
      val value   = ("abcdefg" * 100).getBytes
      val encoded = t.encode(value)
      encoded.data.size must beLessThan(value.size)
    }
  }
}
