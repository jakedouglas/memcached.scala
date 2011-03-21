package com.bitlove.memcached.protocol

object Packets {
  val Request  = 0x80.toByte
  val Response = 0x81.toByte
}

object Ops {
  val Get   = 0x00.toByte
  val Set   = 0x01.toByte
  val Flush = 0x08.toByte
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