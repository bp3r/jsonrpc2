import jsonrpc2.{given, *}
import jsonrpc2.RpcExtensions.*
import io.circe.{Encoder, Decoder}, io.circe.generic.semiauto.*

class ServerSuite extends munit.FunSuite:
  test("reading RPC requests bearing tuple parameters") {
    val req = createRpcNotification("test", (2, 4))
    val (method, params) = readRpcRequest(req).toOption.get

    assertEquals(method, "test")

    def parameterInspector(i: Int, j: Int): Right[Nothing, Unit] =
      assertEquals(i, 2)
      assertEquals(j, 4)
      Right(())

    def piRpc = parameterInspector.tupled.asRpcMethod
    piRpc(params)
  }

  test("reading RPC requests bearing an object parameter") {
    case class Foo(a: Int, b: String)
    given Encoder[Foo] = deriveEncoder[Foo]
    given Decoder[Foo] = deriveDecoder[Foo]

    val req = createRpcNotification("test", (Foo(4, "hello"), Foo(5, "hi")))
    val (method, params) = readRpcRequest(req).toOption.get

    assertEquals(method, "test")

    def parameterInspector(f: Foo, g: Foo): Right[Nothing, Unit] =
      assertEquals(f.a, 4)
      assertEquals(f.b, "hello")
      assertEquals(g.a, 5)
      assertEquals(g.b, "hi")
      Right(())

    def piRpc = parameterInspector.tupled.asRpcMethod
    piRpc(params)
  }

  test("handling invalid RPC requests") {
    var req = "{\"jsonrpc\": \"1.9\", \"method\": \"test\"}"
    assert(readRpcRequest(req).isLeft)

    req = "{\"jsonrpc\": \"2.0\", \"method\": \"test"
    assert(readRpcRequest(req).isLeft)

    req = "{\"jsonrpc\": \"2.0\"}"
    assert(readRpcRequest(req).isLeft)

    req = "{\"jsonrpc\": \"2.0\", \"method\": \"test\", \"params\"}"
    assert(readRpcRequest(req).isLeft)

    req = "{\"method\": \"test\"}"
    assert(readRpcRequest(req).isLeft)
  }
