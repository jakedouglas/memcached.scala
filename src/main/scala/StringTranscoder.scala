package com.bitlove.memcached

class StringTranscoder[T](next: Transcoder[Array[Byte], T] = new ByteArrayTranscoder)
      extends Transcoder[String, T] {

  def encode(value: Transcodable[String]): Transcodable[T] = {
    next.encode(new Transcodable(data = value.data.getBytes, flags = value.flags))
  }

  def decode(encoded: Transcodable[T]): Transcodable[String] = {
    val decoded = next.decode(encoded)
    new Transcodable(data = new String(decoded.data), flags = decoded.flags)
  }
}
