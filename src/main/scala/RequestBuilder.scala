package com.bitlove.memcached

import com.bitlove.memcached.protocol._

import java.nio.ByteBuffer

object RequestBuilder {
  def get(key: Array[Byte]): Array[ByteBuffer] = {
    val reqBuf = ByteBuffer.allocate(24)

    reqBuf.put      (0, Packets.Request)
    reqBuf.put      (1, Ops.Get)
    reqBuf.putShort (2, key.size.toShort)
    reqBuf.putInt   (8, key.size)

    Array(reqBuf, ByteBuffer.wrap(key))
  }

  def set(key:   Array[Byte],
          value: Array[Byte],
          flags: Int,
          ttl:   Int): Array[ByteBuffer] = {
    val reqBuf = ByteBuffer.allocate(32)

    reqBuf.put      (0,  Packets.Request)
    reqBuf.put      (1,  Ops.Set)
    reqBuf.putShort (2,  key.size.toShort)
    reqBuf.put      (4,  8.toByte)
    reqBuf.putInt   (8,  key.size + value.size + 8)
    reqBuf.putInt   (24, flags)
    reqBuf.putInt   (28, ttl)

    Array(reqBuf,
          ByteBuffer.wrap(key),
          ByteBuffer.wrap(value))
  }
}