import jsonrpc2.{*, given}
import jsonrpc2.CodedExtensions.*

class RpcResponseSuite extends munit.FunSuite:
  val testValue = "test"
  val testId = 4L

  test("no response is created from a notification") {
    assert(RpcResponse.fromMethodCall(None, Right(testValue)).isEmpty)
  }

  test("a response is created from a request") {
    val resp = RpcResponse.fromMethodCall(Some(testId), Right(testValue))
    assert(resp.isDefined)
    assert(resp.get.result.isDefined)
    assertEquals(resp.get.id, testId)
    assertEquals(resp.get.result.get, testValue)
  }

  test("an error (and code) may be encoded") {
    val resp = RpcResponse.fromMethodCall(Some(testId), Left(testValue).withCode(5))
    assert(resp.isDefined)
    assertEquals(resp.get.id, testId)
    assert(resp.get.error.isDefined)
    assertEquals(resp.get.error.get.code, 5)
    assertEquals(resp.get.error.get.message, testValue)
  }

  test("an error without an available code is assigned a default code") {
    val resp = RpcResponse.fromMethodCall(Some(testId), Left(testValue))
    assert(resp.isDefined)
    assert(resp.get.error.isDefined)
    assertEquals(resp.get.error.get.code, defaultCode)
    assertEquals(resp.get.error.get.message, testValue)
  }

  