package com.bitlove.memcached

import com.bitlove.memcached.protocol._

import java.nio.ByteBuffer

object RequestBuilder {
  def flush(after: Option[Int] = None): Array[ByteBuffer] = {
    after match {
      case None          => {
        Array(newRequest(24, Ops.Flush))
      }
      case Some(seconds) => {
        Array(newRequest(28, Ops.Flush).
                put(4, 4.toByte).
                  putInt(8, 4).
                    putInt(24, seconds))
      }
    }
  }

  def get(key: Array[Byte]): Array[ByteBuffer] = {
    val reqBuf = newRequest(24, Ops.Get)

    reqBuf.putShort (2, key.size.toShort)
    reqBuf.putInt   (8, key.size)

    Array(reqBuf, ByteBuffer.wrap(key))
  }

  def storageRequest(opcode: Byte,
                     key:    Array[Byte],
                     value:  Array[Byte],
                     flags:  Int,
                     ttl:    Int): Array[ByteBuffer] = {
    val reqBuf = newRequest(32, opcode)

    reqBuf.putShort (2,  key.size.toShort)
    reqBuf.put      (4,  8.toByte)
    reqBuf.putInt   (8,  key.size + value.size + 8)
    reqBuf.putInt   (24, flags)
    reqBuf.putInt   (28, ttl)

    Array(reqBuf,
          ByteBuffer.wrap(key),
          ByteBuffer.wrap(value))
  }

  private def newRequest(size: Int, opcode: Byte): ByteBuffer = {
    ByteBuffer.
      allocate(size).
        put(0, Packets.Request).
          put(1, opcode)
  }
}