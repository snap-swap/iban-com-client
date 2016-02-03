package com.snapswap.ibancom.unmarshaller

import com.snapswap.ibancom.model.{SortCodeValidationData, BankValidationData, IbancomError}
import com.snapswap.ibancom.{IbanComException, IbanComRequestException}

import scala.language.reflectiveCalls
import scala.util.{Failure, Success, Try}
import scala.xml.{Elem, NodeSeq, XML}

trait ResponseUnmarshaller {
  protected implicit def enrichNodeSeq(nodeSeq: NodeSeq) = new AnyRef {
    def textOption: Option[String] = {
      val text = nodeSeq.text
      if (text == null || text.length == 0) {
        None
      } else {
        Some(text)
      }
    }
  }

  def parseValidateResponse(responseStr: String): BankValidationData =
    Try(XML.loadString(responseStr)) match {
      case Success(response) =>
        val errors = parseErrors(response)

        if (errors.isEmpty) {
          val bankData = response \\ "bank_data"

          BankValidationData(
            (bankData \ "bic").textOption,
            (bankData \ "bank").textOption,
            (bankData \ "address").textOption,
            (bankData \ "zip").textOption,
            (bankData \ "city").textOption,
            (bankData \ "country").textOption,
            (bankData \ "country_iso").textOption,
            (bankData \ "phone").textOption,
            (bankData \ "fax").textOption,
            (bankData \ "email").textOption,
            (bankData \ "www").textOption,
            (bankData \ "account").textOption
          )
        } else {
          throw IbanComRequestException(errors)
        }
      case Failure(ex) =>
        throw IbanComException(s"Can't parse response xml because [${ex.getMessage}], response is [$responseStr]")
    }

  def parseValidateSortCodeResponse(responseStr: String): SortCodeValidationData =
    Try(XML.loadString(responseStr)) match {
      case Success(response) =>
        val errors = parseErrors(response)

        if (errors.isEmpty) {
          val bankData = response \\ "result"

          SortCodeValidationData(
            (bankData \ "sort_code").textOption,
            (bankData \ "account").textOption,
            (bankData \ "iban").textOption,
            (bankData \ "country").textOption,
            (bankData \ "bank_name").textOption,
            (bankData \ "bank_bic").textOption,
            (bankData \ "bank_address").textOption,
            (bankData \ "bank_city").textOption,
            (bankData \ "bank_postalcode").textOption,
            (bankData \ "bank_phone").textOption,
            (bankData \ "direct_debits").textOption,
            (bankData \ "pfs_payments").textOption,
            (bankData \ "chaps").textOption,
            (bankData \ "bacs").textOption,
            (bankData \ "ccc_payments").textOption
          )
        } else {
          throw IbanComRequestException(errors)
        }
      case Failure(ex) =>
        throw IbanComException(s"Can't parse response xml because [${ex.getMessage}], response is [$responseStr]")
    }

  protected def parseErrors(response: Elem): Seq[IbancomError] =
    (response \\ "errors").flatMap { error =>
      ((error \ "error" \ "code").textOption, (error \ "error" \ "message").textOption) match {
        case (Some(code), Some(message)) =>
          Some(IbancomError(code, message))
        case _ =>
          None
      }
    }
}

object ResponseUnmarshaller extends ResponseUnmarshaller