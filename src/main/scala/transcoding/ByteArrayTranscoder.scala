package com.bitlove.memcached.transcoding

class ByteArrayTranscoder extends Transcoder[Array[Byte], Array[Byte]] {
  def encode(value: Transcodable[Array[Byte]]): Transcodable[Array[Byte]] = value
  def decode(value: Transcodable[Array[Byte]]): Transcodable[Array[Byte]] = value
}
