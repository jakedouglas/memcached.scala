package com.bitlove.memcached

class ByteArrayTranscoder extends Transcoder[Array[Byte]] {
  def encode(value: Array[Byte]): EncodedValue = {
    new EncodedValue(data = value, flags = 0)
  }

  def decode(encoded: EncodedValue): Array[Byte] = {
    encoded.data
  }
}
