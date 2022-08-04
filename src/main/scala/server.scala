package jsonrpc2

import util.{Try, Success, Failure}

import io.circe.parser.*, io.circe.*

/** Deserializes and validates a JSONRPC request or notification.
  *
  * If the message fails validation, then a `Left` is returned.
  *
  * Otherwise the name of the method to invoke and the parameters to pass to it
  * are returned.
  */
def readRpcRequest(req: String)(using
    Decoder[RpcRequest]
): Either[String, (String, RpcParameters)] =
  decode[RpcRequest](req) match
    case Left(err) => Left(err.toString)
    case Right(partial) =>
      if partial.jsonrpc != protocolVersion then
        Left("Protocol version mismatch.")
      else Right(partial.method, RpcParameters(partial.id, partial.params))
