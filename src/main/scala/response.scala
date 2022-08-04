package jsonrpc2

import io.circe.*, io.circe.syntax.*, io.circe.generic.semiauto.*

/** Represents a JSONRPC error. */
private final case class RpcError(
    code: Int,
    message: String
)

/** Represents a full response to a JSONRPC method call. Defined for
  * (de-)serialization purposes.
  */
private final case class RpcResponse[R](
    jsonrpc: String = protocolVersion,
    result: Option[R] = None,
    error: Option[RpcError] = None,
    id: Long
)

/** Represents a received response to an RPC method call that has been partially
  * deserialized and from which a result can be extracted.
  */
opaque type RpcResult = JsonObject

// Custom implementation to ensure only one of `result` or `error` are serialized.
given [R: Encoder]: Encoder[RpcResponse[R]] with
  def apply(resp: RpcResponse[R]): Json =
    val invariantFields = Json.obj(
      protocolPair,
      ("id", resp.id.asJson)
    )

    if resp.error.isDefined then
      if resp.result.isDefined then
        throw Exception(
          "Produced impossible message: `result` and `error` cannot both be defined!"
        )
      else
        invariantFields.deepMerge(
          Json.obj(
            ("error", resp.error.get.message.asJson),
            ("code", resp.error.get.code.asJson)
          )
        )
    else if resp.result.isDefined then
      invariantFields.deepMerge(
        Json.obj(("result", resp.result.get.asJson))
      )
    else invariantFields.deepMerge(Json.obj(("result", None.asJson)))

// Implementation for `Nothing` required to resolve ambiguous given instances when an RPC method
// returns `Option[Nothing]`.
given Encoder[RpcResponse[Nothing]] with
  def apply(resp: RpcResponse[Nothing]): Json =
    Json.obj(
      protocolPair,
      ("id", resp.id.asJson)
    )

given [R: Decoder]: Decoder[RpcResponse[R]] = deriveDecoder

object RpcResponse:
  /** Create an `RpcResponse` from an invocation of a local method. True to
    * JSONRPC semantics, if no `id` was present in the request then the call is
    * treated as a notification requiring no response, and this method no-ops.
    */
  def fromMethodCall[L, R](id: Option[Long], result: Either[L, R]): Option[RpcResponse[R]] =
    if id.isDefined then
      result match
        case Left(e) =>
          if e.isInstanceOf[RpcError] then
            Some(
              RpcResponse(
                id = id.get,
                error = Some(e.asInstanceOf[RpcError])
              )
            )

          else
            Some(
              RpcResponse(
                id = id.get,
                error = Some(
                  RpcError(
                    code = defaultCode,
                    message = e.toString
                  )
                )
              )
            )

        case Right(v) =>
          Some(
            RpcResponse(
              id = id.get,
              result = Some(v)
            )
          )
    else None