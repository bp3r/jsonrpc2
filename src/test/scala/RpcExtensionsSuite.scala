import jsonrpc2.{*, given}
import jsonrpc2.RpcExtensions.*

import io.circe.*


class RpcExtensionsSuite extends munit.FunSuite:
  test("A method that returns a T can be converted to one that returns a Right[T]") {
    def identity(i: Int) = i

    def identityRight = identity.asRightMethod
    assertEquals(identityRight(4), Right(4))
  }

  // See ServerSuite for tests for `asRpcMethod`