package com.bitlove.memcached

trait Transcoder[A,B] {
  def encode(value: Transcodable[A]): Transcodable[B]
  def decode(value: Transcodable[B]): Transcodable[A]
  // A convenience method to avoid instantiating a Transcodable at every call site
  def encode(value: A): Transcodable[B] = encode(new Transcodable(value, 0))
}
