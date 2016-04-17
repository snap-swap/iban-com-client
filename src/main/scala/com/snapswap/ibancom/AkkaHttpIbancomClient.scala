package com.snapswap.ibancom

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.snapswap.ibancom.model.{BankValidationData, SortCodeValidationData}
import com.snapswap.ibancom.unmarshaller.ResponseUnmarshaller

import scala.concurrent.Future
import scala.language.reflectiveCalls

class AkkaHttpIbancomClient(apiKey: String)
                           (implicit system: ActorSystem, materializer: Materializer) extends IbanComClient with ResponseUnmarshaller {

  import system.dispatcher

  private val log = Logging(system, this.getClass)
  private val baseURL = "/clients/api"

  override def validate(iban: String): Future[IbanComResponse] =
    send(get(s"/ibanv2.php?format=xml&api_key=$apiKey&iban=$iban"))(parseValidateResponse)
      .map {
        case BankValidationData(_, Some(bank), _, _, _, _, Some(countryIso), _, _, _, _, _) =>
          IbanComResponse(iban, bank, countryIso)
        case other =>
          throw UnexpectedResponseException("Response from iban.com contains less information than need")
      }

  override def validate(account: String, sortCode: String): Future[IbanComResponse] =
    send(get(s"/sort-api.php?api_key=$apiKey&sortcode=$sortCode&account=$account"))(parseValidateSortCodeResponse)
      .map {
        case SortCodeValidationData(_, _, Some(iban), Some(countryIso), Some(bank), _, _, _, _, _, _, _, _, _, _) =>
          IbanComResponse(iban, bank, countryIso)
        case other =>
          throw UnexpectedResponseException("Response from iban.com contains less information than need")
      }

  private lazy val layerConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http()
      .outgoingConnectionHttps("www.iban.com", 443)
      .log("ibancom")

  private def http(request: HttpRequest): Future[HttpResponse] =
    Source.single(
      request
    ).via(layerConnectionFlow).runWith(Sink.head)

  private def send[T](request: HttpRequest)(handler: String => T): Future[T] = {
    http(request).flatMap { response =>
      Unmarshal(response.entity)
        .to[String]
        .map(handler)
    }
  }

  private def get(path: String): HttpRequest = Get(baseURL + path)
}