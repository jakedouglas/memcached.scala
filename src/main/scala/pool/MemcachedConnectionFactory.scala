package com.bitlove.memcached.pool

import com.bitlove.memcached.Memcached
import com.bitlove.memcached.transcoding.{Transcoder, ByteArrayTranscoder}

class MemcachedConnectionFactory[T](host: String,
                                    port: Int,
                                    defaultTranscoder: Transcoder[T, Array[Byte]] = new ByteArrayTranscoder)
      extends ConnectionFactory[Memcached[T]] {

  def create: Memcached[T] = {
    new Memcached(host, port, defaultTranscoder)
  }

  def validate(client: Memcached[T]): Boolean = {
    true
  }

  def destroy(client: Memcached[T]): Unit = {
    client.close
  }
}
