package com.bitlove.memcached.protocol

object Packets {
  val Request     = 0x80.toByte
  val Response    = 0x81.toByte
}

object Ops {
  val Get         = 0x00.toByte
  val Set         = 0x01.toByte
  val Add         = 0x02.toByte
  val Replace     = 0x03.toByte
  val Delete      = 0x04.toByte
  val Increment   = 0x05.toByte
  val Decrement   = 0x06.toByte
  val Quit        = 0x07.toByte
  val Flush       = 0x08.toByte
  val GetQ        = 0x09.toByte
  val NoOp        = 0x0A.toByte
  val Version     = 0x0B.toByte
  val GetK        = 0x0C.toByte
  val GetKQ       = 0x0D.toByte
  val Append      = 0x0E.toByte
  val Prepend     = 0x0F.toByte
  val Stat        = 0x10.toByte
  val SetQ        = 0x11.toByte
  val AddQ        = 0x12.toByte
  val ReplaceQ    = 0x13.toByte
  val DeleteQ     = 0x14.toByte
  val IncrementQ  = 0x15.toByte
  val DecrementQ  = 0x16.toByte
  val QuitQ       = 0x17.toByte
  val FlushQ      = 0x18.toByte
  val AppendQ     = 0x19.toByte
  val PrependQ    = 0x1A.toByte
}

object Status {
  val Success     = 0x0.toShort
  val KeyNotFound = 0x1.toShort
  val KeyExists   = 0x2.toShort
  val TooLarge    = 0x3.toShort
  val InvalidArgs = 0x4.toShort
  val NotStored   = 0x5.toShort
  val BadIncrDecr = 0x6.toShort
}