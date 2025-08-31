package jsonrpc2

import io.circe.*, io.circe.parser.*, io.circe.syntax.*

object RpcExtensions:
  extension [P: Decoder, L, R: Encoder](
      fn: P => Either[L, R]
  )(using Encoder[RpcResponse[R]])
    /** Converts a function into an RPC method implementation that takes
      * serialized RPC request parameters and returns a serialized RPC response.
      *
      * The function must take a single parameter, which may be a tuple or other
      * collection, and return an `Either`. It is recommended to use
      * `Function.tupled` to simplify converting functions of arbitrary arity to
      * tuple-accepting functions.
      *
      * If the invoked method encounters an error, the issued response will
      * encapsulate it. If the method cannot be invoked, a `Left` is returned.
      * `Right(None)` is returned upon successful execution if no id was present
      * in the original RPC request, as per the JSONRPC spec.
      */
    def asRpcMethod: RpcParameters => Either[String, Option[String]] =
      (params) =>
        if params.params.isEmpty then
          Left("Received no parameters, but some are required for this method.")
        else
          params.params.get.as[P] match
            case Left(error) => Left(error.toString)
            case Right(args) =>
              val resp = RpcResponse.fromMethodCall(params.id, fn(args))
              if resp.isDefined then Right(Some(resp.get.asJson.noSpaces))
              else Right(None)

  extension [L, R: Encoder](
      fn: () => Either[L, R]
  )(using Encoder[RpcResponse[R]])
    // Handles methods without present arguments.
    def asRpcMethod: RpcParameters => Either[String, Option[String]] =
      (params) =>
        if params.params.isDefined then
          Left(
            "Received parameters, but none are expected for this method."
          )
        else
          val resp = RpcResponse.fromMethodCall(params.id, fn())
          if resp.isDefined then Right(Some(resp.get.asJson.noSpaces))
          else Right(None)

  /** Extends arbitrary single-arity functions so that they can be easily
    * converted to a successful `Either` required for `asRpcMethod`.
    */
  extension [P, R](fn: P => R)
    def asRightMethod: P => Right[Nothing, R] = (params) => Right(fn(params))

  extension [R](fn: () => R)
    def asRightMethod: () => Right[Nothing, R] = () => Right(fn())
