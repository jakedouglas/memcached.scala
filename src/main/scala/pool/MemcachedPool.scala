package com.bitlove.memcached.pool

import com.bitlove.memcached.transcoding.{Transcoder, ByteArrayTranscoder}

class MemcachedPool[T](host:    String,
                       port:    Int = 11211,
                       defaultTranscoder: Transcoder[T, Array[Byte]] = new ByteArrayTranscoder,
                       max:     Int = 20,
                       timeout: Int = 500000)
      extends SimpleConnectionPool(connectionFactory = new MemcachedConnectionFactory(host, port, defaultTranscoder),
                                   max               = max,
                                   timeout           = timeout) {
}
