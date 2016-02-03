package com.snapswap.ibancom

import com.snapswap.ibancom.model.IbancomError

import scala.util.control.NoStackTrace

case class IbanComException(message: String) extends NoStackTrace

case class IbanComRequestException(errors: Seq[IbancomError]) extends NoStackTrace
