package com.bitlove.memcached.transcoding

case class Transcodable[T](data: T, flags: Int)
