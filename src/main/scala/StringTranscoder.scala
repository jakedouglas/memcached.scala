package com.bitlove.memcached

class StringTranscoder extends Transcoder[String] {
  def encode(value: String): EncodedValue = {
    new EncodedValue(data = value.getBytes, flags = 0)
  }

  def decode(encoded: EncodedValue): String = {
    new String(encoded.data)
  }
}
