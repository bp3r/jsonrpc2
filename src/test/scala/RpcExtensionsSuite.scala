import jsonrpc2.{*, given}
import jsonrpc2.RpcExtensions.*

import io.circe.*

class RpcExtensionsSuite extends munit.FunSuite:
  test(
    "A method that returns a T can be converted to one that returns a Right[T]"
  ) {
    def identity(i: Int) = i

    def identityRight = identity.asRightMethod
    assertEquals(identityRight(4), Right(4))
  }

  test("An RpcMethod can be created from a zero-arity function.") {
    def rpc = (() => Right(4)).asRpcMethod

    // See ServerSuite for usage tests.
  }

  test("An RpcMethod can be created from a function that takes arguments.") {
    def rpc = ((i: Int) => Right(i)).asRpcMethod

    // See ServerSuite for usage tests.
  }
