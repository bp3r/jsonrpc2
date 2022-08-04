package jsonrpc2

import io.circe.syntax.EncoderOps

inline val protocolVersion = "2.0"
inline def protocolPair = ("jsonrpc", protocolVersion.asJson)
inline val defaultCode = -1