package jsonrpc2

object CodedExtensions:
    extension[A, B](e: Either[A, B])
      def withCode(i: Int): Either[RpcError, B] = 
          e match
            case Right(v) => Right(v)
            case Left(err) => Left(RpcError(i, err.toString))