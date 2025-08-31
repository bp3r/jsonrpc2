import jsonrpc2.{*, given}
import jsonrpc2.RpcExtensions.*

import io.circe.parser.*, io.circe.*

class ClientSuite extends munit.FunSuite:
  test("generating different ids") {
    val ids = (0 until 100).map(_ => createRpcRequest("test")._1)
    assertEquals(ids.toSet.size, ids.length)
  }

  test("creating requests") {
    val reqNoParams = parse(createRpcRequest("test")._2)
    val reqArrayParams = parse(createRpcRequest("test", (1, 2))._2)
    val reqObjectParams = parse(createRpcRequest("test", Map("a" -> "b"))._2)

    val reqs = Seq(reqNoParams, reqArrayParams, reqObjectParams)
    reqs.foreach(
      _.fold(
        l => assert(false, l.toString),
        r =>
          val obj = r.asObject.get

          obj("jsonrpc").get
            .as[String]
            .fold(
              l => assert(false, l.toString),
              r => assertEquals(r, protocolVersion)
            )

          obj("method").get
            .as[String]
            .fold(
              l => assert(false, l.toString),
              r => assertEquals(r, "test")
            )

          assert(obj("id").isDefined)
      )
    )

    reqNoParams.foreach(r => assertEquals(r.asObject.get.apply("params"), None))

    reqArrayParams.foreach(r =>
      r.asObject.get
        .apply("params")
        .get
        .as[(Int, Int)]
        .fold(l => assert(false, l.toString), r => assertEquals(r, (1, 2)))
    )

    reqObjectParams.foreach(r =>
      r.asObject.get
        .apply("params")
        .get
        .as[Map[String, String]]
        .fold(
          l => assert(false, l.toString),
          r => assertEquals(r, Map("a" -> "b"))
        )
    )
  }

  test("notifications have no ID") {
    val notification = parse(createRpcNotification("test"))
    notification.fold(
      l => assert(false, l.toString),
      r => assert(r.asObject.get.apply("id").isEmpty)
    )
  }

  test("reading RPC responses") {
    val expectedResult = 2
    val requestId = 4L
    val resp = s"{\"jsonrpc\": \"2.0\", \"id\": $requestId, \"result\": $expectedResult}"

    val (responseId, rpcResult) = readRpcResponse(resp).toOption.get
    assertEquals(requestId, responseId)

    val result = decodeRpcResult[Int](rpcResult).toOption.get
    assertEquals(result, expectedResult)
  }

  test("handling invalid RPC responses") {
    var resp = "{\"jsonrpc\": \"2.0\", \"id\": 4, \"result\": }"
    assert(readRpcResponse(resp).isLeft)

    resp = "{\"jsonrpc\": \"2.0\", \"result\": 4}"
    assert(readRpcResponse(resp).isLeft)

    resp = "{\"jsonrpc\": \"1.0\", \"result\": 4}"
    assert(readRpcResponse(resp).isLeft)

    resp = "{\"jsonrpc\": \"2.0\", \"id\": 4, \"result\": \"{\"truncated\":\"}}"
    assert(readRpcResponse(resp).isLeft)
  }