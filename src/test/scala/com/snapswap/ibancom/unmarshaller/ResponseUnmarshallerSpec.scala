package com.snapswap.ibancom.unmarshaller

import com.snapswap.ibancom.{IbanValidationException, InvalidRequestException}
import com.snapswap.ibancom.model.{BankValidationData, SortCodeValidationData}
import org.scalatest.{Matchers, FlatSpec}

class ResponseUnmarshallerSpec extends FlatSpec with Matchers {

  "ResponseUnmarshaller" should "correctly parse successful bank validation response" in {
    val result = ResponseUnmarshaller.parseValidateResponse(successValidationResponse)

    result shouldBe BankValidationData(
      bic = Some("DEUTDEFFXXX"),
      bank = Some("Deutsche Bank Filiale"),
      address = Some("Theodor-Heuss-Allee 70"),
      zip = Some("60254"),
      city = Some("Frankfurt am Main"),
      country = Some("Germany"),
      countryIso = Some("DE"),
      phone = None,
      fax = None,
      email = None,
      www = None,
      account = Some("0927353010")
    )
  }

  it should "correctly parse successful bank validation response with successful validations" in {
    val result = ResponseUnmarshaller.parseValidateResponse(successResponseWithSuccessValidations)

    result shouldBe a[BankValidationData]
  }

  it should "correctly handle errors in response" in {
    val result = intercept[InvalidRequestException] {
      ResponseUnmarshaller.parseValidateResponse(errorsValidationResponse)
    }

    result shouldBe InvalidRequestException(Seq("API Key is invalid"))
  }

  it should "correctly handle validation errors" in {
    val result = intercept[IbanValidationException] {
      ResponseUnmarshaller.parseValidateResponse(successsResponseWithValidationFailure)
    }

    result shouldBe a[IbanValidationException]
  }

  it should "correctly parse successful sort code in validation response" in {
    val result = ResponseUnmarshaller.parseValidateSortCodeResponse(successValidateSortCodeResponse)

    result shouldBe SortCodeValidationData(
      sortCode = Some("200415"),
      account = Some("38290008"),
      iban = Some("GB37BARC20041538290008"),
      country = Some("GB"),
      bankName = Some("BARCLAYS BANK PLC"),
      bankBic = Some("BARCGB22"),
      bankAddress = Some("Dept AC Barclaycard House"),
      bankCity = Some("Northampton"),
      bankPostalcode = Some("NN4 7SG"),
      bankPhone = Some("01604 234234"),
      directDebits = Some("NO"),
      pfsPayments = Some("YES"),
      chaps = Some("YES"),
      bacs = Some("YES"),
      cccPayments = Some("NO")
    )
  }

  val successValidationResponse =
    """<result>
      |<bank_data>
      |<bic>DEUTDEFFXXX</bic>
      |<bank>Deutsche Bank Filiale</bank>
      |<address>Theodor-Heuss-Allee 70</address>
      |<city>Frankfurt am Main</city>
      |<zip>60254</zip>
      |<country>Germany</country>
      |<country_iso>DE</country_iso>
      |<account>0927353010</account>
      |</bank_data>
      |<validations>
      |<check>
      |<code>001</code>
      |<message>IBAN Check digit is correct</message>
      |</check>
      |<check>
      |<code>003</code>
      |<message>IBAN Length is correct</message>
      |</check>
      |</validations>
      |<errors/>
      |</result>""".stripMargin

  val errorsValidationResponse =
    """<result>
      |<bank_data/>
      |<errors>
      |<error>
      |<code>301</code>
      |<message>API Key is invalid</message>
      |</error>
      |</errors>
      |</result>""".stripMargin

  val successValidateSortCodeResponse =
    """<result>
      |<sort_code>200415</sort_code>
      |<account>38290008</account>
      |<iban>GB37BARC20041538290008</iban>
      |<country>GB</country>
      |<bank_name>BARCLAYS BANK PLC</bank_name>
      |<bank_bic>BARCGB22</bank_bic>
      |<bank_address>Dept AC Barclaycard House</bank_address>
      |<bank_city>Northampton</bank_city>
      |<bank_postalcode>NN4 7SG</bank_postalcode>
      |<bank_phone>01604 234234</bank_phone>
      |<direct_debits>NO</direct_debits>
      |<pfs_payments>YES</pfs_payments>
      |<chaps>YES</chaps>
      |<bacs>YES</bacs>
      |<ccc_payments>NO</ccc_payments>
      |</result>""".stripMargin

  val successsResponseWithValidationFailure =
    """<result>
      |   <bank_data>
      |      <bic>BILLLULL</bic>
      |      <branch />
      |      <bank>Banque Internationale à Luxembourg S.A.</bank>
      |      <address>69 ROUTE D'ESCH</address>
      |      <city>LUXEMBOURG</city>
      |      <state />
      |      <zip>2953</zip>
      |      <phone />
      |      <fax />
      |      <www />
      |      <email />
      |      <country>Luxembourg</country>
      |      <country_iso>LU</country_iso>
      |      <account>521011775650</account>
      |   </bank_data>
      |   <sepa_data>
      |      <SCT>YES</SCT>
      |      <SDD>YES</SDD>
      |      <COR1>NO</COR1>
      |      <B2B>YES</B2B>
      |      <SCC>NO</SCC>
      |   </sepa_data>
      |   <validations>
      |      <check>
      |         <code>202</code>
      |         <message>IBAN Check digit not correct</message>
      |      </check>
      |      <check>
      |         <code>205</code>
      |         <message>IBAN structure is not correct</message>
      |      </check>
      |      <check>
      |         <code>203</code>
      |         <message>IBAN Length is not correct. Luxembourg IBAN must be 20 characters long.</message>
      |      </check>
      |   </validations>
      |   <errors />
      |</result>""".stripMargin

  val successResponseWithSuccessValidations =
    """<result>
      |   <bank_data>
      |      <bic>BILLLULL</bic>
      |      <branch />
      |      <bank>Banque Internationale à Luxembourg S.A.</bank>
      |      <address>69 ROUTE D'ESCH</address>
      |      <city>LUXEMBOURG</city>
      |      <state />
      |      <zip>2953</zip>
      |      <phone />
      |      <fax />
      |      <www />
      |      <email />
      |      <country>Luxembourg</country>
      |      <country_iso>LU</country_iso>
      |      <account>5210117756500</account>
      |   </bank_data>
      |   <sepa_data>
      |      <SCT>YES</SCT>
      |      <SDD>YES</SDD>
      |      <COR1>NO</COR1>
      |      <B2B>YES</B2B>
      |      <SCC>NO</SCC>
      |   </sepa_data>
      |   <validations>
      |      <check>
      |         <code>001</code>
      |         <message>IBAN Check digit is correct</message>
      |      </check>
      |      <check>
      |         <code>005</code>
      |         <message>IBAN structure is correct</message>
      |      </check>
      |      <check>
      |         <code>003</code>
      |         <message>IBAN Length is correct</message>
      |      </check>
      |   </validations>
      |   <errors />
      |</result>""".stripMargin
}
