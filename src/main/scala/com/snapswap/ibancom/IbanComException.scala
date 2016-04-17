package com.snapswap.ibancom

import scala.util.control.NoStackTrace

trait IbanComException extends NoStackTrace

case class IbanValidationException(messages: Seq[String]) extends IbanComException

case class InvalidRequestException(messages: Seq[String]) extends IbanComException

case class UnexpectedResponseException(message: String) extends IbanComException