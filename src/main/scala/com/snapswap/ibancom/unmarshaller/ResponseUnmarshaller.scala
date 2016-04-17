package com.snapswap.ibancom.unmarshaller

import com.snapswap.ibancom.{IbanValidationException, InvalidRequestException, UnexpectedResponseException}
import com.snapswap.ibancom.model.{BankValidationData, IbancomError, SortCodeValidationData, Validation}

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
    validateAndParse(responseStr) { response =>
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
    }

  def parseValidateSortCodeResponse(responseStr: String): SortCodeValidationData =
    validateAndParse(responseStr) { response =>
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
    }

  protected def validateAndParse[T](responseStr: String)(parser: NodeSeq => T): T =
    Try(XML.loadString(responseStr)) match {
      case Success(response) =>
        val errors = parseErrors(response)

        if (errors.isEmpty) {
          val t = parseValidations(response)
          val failedValidations = parseValidations(response).filter(_.code > 200)

          if (failedValidations.isEmpty) {
            parser(response)
          } else {
            throw IbanValidationException(failedValidations.map(_.message))
          }
        } else {
          throw InvalidRequestException(errors.map(_.message))
        }
      case Failure(ex) =>
        throw UnexpectedResponseException(s"Can't parse response xml because [${ex.getMessage}], response is [$responseStr]")
    }

  protected def parseErrors(response: Elem): Seq[IbancomError] =
    (response \\ "errors").flatMap { error =>
      (error \ "error" \ "code").zip(error \ "error" \ "message").map {
        case (code, message) =>
          IbancomError(code.text, message.text)
      }
    }

  protected def parseValidations(response: Elem): Seq[Validation] =
    (response \\ "validations").flatMap { error =>
      (error \ "check" \ "code").zip(error \ "check" \ "message").map {
        case (code, message) =>
          Validation(code.text.toInt, message.text)
      }
    }
}

object ResponseUnmarshaller extends ResponseUnmarshaller