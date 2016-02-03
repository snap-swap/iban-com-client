package com.snapswap.ibancom

import scala.concurrent.Future

trait IbanComClient {
  def validate(iban: String): Future[IbanComResponse]

  def validate(account: String, sortCode: String): Future[IbanComResponse]
}