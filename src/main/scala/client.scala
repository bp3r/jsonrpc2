package jsonrpc2

import collection.mutable.{Set => MutSet}
import util.Random

import io.circe._, io.circe.parser._, io.circe.syntax._

private val idsInUse = MutSet[Long]()

/** Generates a UID that is guaranteed not in use by another extant request.
  */
private def idGenerator: () => Long =
  val random = Random()

  def generateId(): Long =
    val id = random.nextLong
    if idsInUse contains id then generateId()
    else
      idsInUse += id
      id

  generateId _

private val generateId = () => idGenerator()

/** Creates a JSONRPC request for the passed method with the passed parameters.
  *
  * The serialized message body and an the ID for the message (that can
  * subsequently be used to match the response) are returned.
  */
def createRpcRequest[P: Encoder](method: String, params: P = ())(using
    Encoder[RpcRequest]
): (Long, String) =
  val id = generateId()

  (
    id,
    RpcRequest(
      method,
      jsonrpc = "2.0",
      id = Some(id),
      params = if params != () then Some(params.asJson) else None
    ).asJson.noSpaces
  )

/** Creates a JSONRPC notification for the passed method with the passed
  * parameters.
  *
  * The serialized message body is returned.
  */
def createRpcNotification[P: Encoder](method: String, params: P = ())(using
    Encoder[RpcRequest]
): String =
  RpcRequest(
    method,
    jsonrpc = "2.0",
    id = None,
    params = if params != () then Some(params.asJson) else None
  ).asJson.noSpaces

/** Unpacks a response received on the wire to a `RpcResult` suitable for
  * passing to `decodeRpcResult`. Returns that result and a preceding identifier
  * for the response that associates it with the invoking request.
  */
def readRpcResponse(responseBody: String): Either[String, (Long, RpcResult)] =
  parse(responseBody) match
    case Left(error) => Left(error.toString)
    case Right(res) =>
      val ret = for {
        obj <- res.asObject
        id <- obj("id")
        idVal <- id.as[Long].toOption
      } yield (idVal, obj.asInstanceOf[RpcResult])

      ret match
        case None      => Left(s"Failed to deserialize: $responseBody")
        case Some(ret) => Right(ret)

/** Decodes an `RpcResult` to a native type `R`. */
def decodeRpcResult[R: Decoder](rpcResult: RpcResult): Either[String, R] =
  rpcResult.asInstanceOf[JsonObject]("result") match
    case None         => Left("No result encoded in response message.")
    case Some(result) => result.as[R].fold(err => Left(err.toString), Right(_))
