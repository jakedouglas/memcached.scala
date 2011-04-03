package com.bitlove.memcached

import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.net.InetSocketAddress
import com.bitlove.memcached.protocol._

class Memcached(host: String, port: Int) {
  class ProtocolError(message: String) extends Error(message)

  private val addr    = new InetSocketAddress(host, port)
  private val channel = SocketChannel.open(addr)
  private val header  = ByteBuffer.allocate(24)

  def append(key:   Array[Byte],
             value: Array[Byte]): Boolean = {
    channel.write(RequestBuilder.appendOrPrepend(Ops.Append, key, value))
    handleResponse(Ops.Append, handleStorageResponse)
  }

  def prepend(key:   Array[Byte],
              value: Array[Byte]): Boolean = {
    channel.write(RequestBuilder.appendOrPrepend(Ops.Prepend, key, value))
    handleResponse(Ops.Prepend, handleStorageResponse)
  }

  def incr(key:     Array[Byte],
           count:   Long = 1,
           ttl:     Int  = 0,
           default: Option[BigInt] = None): Option[BigInt] = {
    channel.write(RequestBuilder.incrOrDecr(Ops.Increment, key, count, ttl, default))
    handleResponse(Ops.Increment, handleIncrDecrResponse)
  }

  def decr(key:     Array[Byte],
           count:   Long = 1,
           ttl:     Int  = 0,
           default: Option[BigInt] = None): Option[BigInt] = {
    channel.write(RequestBuilder.incrOrDecr(Ops.Decrement, key, count, ttl, default))
    handleResponse(Ops.Decrement, handleIncrDecrResponse)
  }

  def flush(after: Option[Int] = None): Unit = {
    channel.write(RequestBuilder.flush(after))
    handleResponse(Ops.Flush, handleFlushResponse)
  }

  def get(key: Array[Byte]): Option[Array[Byte]] = {
    channel.write(RequestBuilder.get(key))
    handleResponse(Ops.Get, handleGetResponse)
  }

  def set(key:   Array[Byte],
          value: Array[Byte],
          ttl:   Int = 0) = {
    channel.write(RequestBuilder.storageRequest(Ops.Set, key, value, 0, ttl))
    handleResponse(Ops.Set, handleStorageResponse)
  }

  def add(key:   Array[Byte],
          value: Array[Byte],
          ttl:   Int = 0) = {
    channel.write(RequestBuilder.storageRequest(Ops.Add, key, value, 0, ttl))
    handleResponse(Ops.Add, handleStorageResponse)
  }

  def replace(key:   Array[Byte],
              value: Array[Byte],
              ttl:   Int = 0) = {
    channel.write(RequestBuilder.storageRequest(Ops.Replace, key, value, 0, ttl))
    handleResponse(Ops.Replace, handleStorageResponse)
  }

  def delete(key: Array[Byte]): Boolean = {
    channel.write(RequestBuilder.delete(key))
    handleResponse(Ops.Delete, handleStorageResponse)
  }

  private def handleIncrDecrResponse(header: ByteBuffer, body: ByteBuffer): Option[BigInt] = {
    header.getShort(6) match {
      case Status.Success     => Some(BigInt(1, body.array))
      case Status.KeyNotFound => None
      case Status.BadIncrDecr => throw new ProtocolError("Incr/decr on non-numeric value")
      case code               => throw new ProtocolError("Unexpected status code %d".format(code))
    }
  }

  private def handleFlushResponse(header: ByteBuffer, body: ByteBuffer): Unit = {
    header.getShort(6) match {
      case Status.Success  => ()
      case code            => throw new ProtocolError("Unexpected status code %d".format(code))
    }
  }

  private def handleStorageResponse(header: ByteBuffer, body: ByteBuffer): Boolean = {
    header.getShort(6) match {
      case Status.Success     => true
      case Status.KeyNotFound => false
      case Status.KeyExists   => false
      case Status.NotStored   => false
      case code               => throw new ProtocolError("Unexpected status code %d".format(code))
    }
  }

  private def handleGetResponse(header: ByteBuffer, body: ByteBuffer): Option[Array[Byte]] = {
    val extras = header.get(4).toInt

    header.getShort(6) match {
      case Status.Success => Some(body.array.slice(extras, body.capacity))
      case _              => None
    }
  }

  private def handleResponse[T](opcode:  Byte,
                                handler: (ByteBuffer, ByteBuffer) => T): T = {
    fillHeader
    verifyMagic
    verifyOpcode(opcode)
    handler(header, fillBodyFromHeader)
  }

  private def fillHeader: Unit = {
    header.clear
    fill(header, 24)
  }

  private def fillBodyFromHeader: ByteBuffer = {
    val len  = header.getInt(8)
    val body = ByteBuffer.allocate(len)
    fill(body, len)
    body
  }

  private def fill(buffer: ByteBuffer, len: Int): Unit = {
    var read = 0

    while (read < len) {
      read += channel.read(buffer)
    }

    buffer.flip
  }

  private def verifyMagic = {
    header.get(0) match {
      case Packets.Response => ()
      case byte             => {
        throw new ProtocolError("Unexpected header magic 0x%x".format(byte))
      }
    }
  }

  private def verifyOpcode(opcode: Byte) = {
    header.get(1) match {
      case x if x == opcode => ()
      case otherByte        => {
        throw new ProtocolError("Unexpected opcode 0x%x".format(otherByte))
      }
    }
  }
}