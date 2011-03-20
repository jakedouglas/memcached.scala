package com.bitlove.memcached

import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.net.InetSocketAddress
import com.bitlove.memcached.protocol._

class Memcached(host: String, port: Int) {
  class ProtocolError(message: String) extends Error(message)

  private val addr            = new InetSocketAddress(host, port)
  private val channel         = SocketChannel.open(addr)
  private val header          = ByteBuffer.allocate(24)

  def get(key: Array[Byte]): Option[Array[Byte]] = {
    channel.write(RequestBuilder.get(key))
    handleResponse(Ops.Get, handleGetResponse)
  }

  def set(key:   Array[Byte],
          value: Array[Byte],
          ttl:   Int = 0,
          flags: Int = 0) = {
    channel.write(RequestBuilder.set(key, value, flags, ttl))
    handleResponse(Ops.Set, handleSetResponse)
  }

  private def handleSetResponse(header: ByteBuffer): Unit = {
    val extras  = header.get(4).toInt
    val dataLen = header.getInt(8)
    val dataBuf = ByteBuffer.allocate(dataLen)
    fill(dataBuf, dataLen)

    header.getShort(6) match {
      case Status.Success => ()
      case code           => throw new ProtocolError("Unexpected status code %d".format(code))
    }
  }

  private def handleGetResponse(header: ByteBuffer): Option[Array[Byte]] = {
    val extras  = header.get(4).toInt
    val dataLen = header.getInt(8)
    val dataBuf = ByteBuffer.allocate(dataLen)
    fill(dataBuf, dataLen)

    header.getShort(6) match {
      case Status.Success => Some(dataBuf.array.slice(extras, dataLen))
      case _              => None
    }
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

  private def handleResponse[T](opcode:  Byte,
                                handler: ByteBuffer => T): T = {
    header.clear
    fill(header, 24)
    verifyMagic
    verifyOpcode(opcode)
    handler(header)
  }

  private def fill(buffer: ByteBuffer, len: Int): Unit = {
    var read = 0

    while (read < len) {
      read += channel.read(buffer)
    }
  }
}