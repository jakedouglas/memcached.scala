package com.bitlove.memcached

trait Transcoder[T] {
  def encode(value: T): EncodedValue
  def decode(encoded:   EncodedValue): T
}
