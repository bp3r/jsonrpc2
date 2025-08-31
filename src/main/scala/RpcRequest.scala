package jsonrpc2

import io.circe.*, io.circe.syntax.*, io.circe.generic.semiauto.*

/** A full RPC request as recieved on the wire. Defined for (de-)serialization
  * purposes.
  */
private final case class RpcRequest(
    method: String,
    jsonrpc: String,
    params: Option[Json],
    id: Option[Long]
)

/** Parameters to pass to an RPC method. */
private final case class RpcParameters(
    id: Option[Long],
    params: Option[Json]
)

// Custom encoder necessary to avoid serializing empty parameters as `null`.
given Encoder[RpcRequest] with
  def apply(req: RpcRequest): Json =
    var fields: Json = Json.obj(protocolPair, ("method", req.method.asJson))
    if req.params.isDefined then
      fields = fields.deepMerge(Json.obj(("params", req.params.get)))
    if req.id.isDefined then
      fields = fields.deepMerge(Json.obj(("id", req.id.get.asJson)))
    fields

given Decoder[RpcRequest] = deriveDecoder[RpcRequest]
