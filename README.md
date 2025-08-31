# jsonrpc2
*jsonrpc2* is a small implementation of [JSONRPC2](https://www.jsonrpc.org/specification]) for Scala 3. It supports both the (de-)serialisation of 'JSON types' (integers, arrays, etc) and user-defined Scala types, and is easy to use, well tested and ready for use in both clients and servers. Note that this library does _not_ handle transport, but purely JSONRPC (de-)serialization.

## Usage
- The usage of *jsonrpc2* differs depending on whether you are using it on the client or server. In either case, if you wish to send or receive custom types as RPC parameters, you must define `circe` de/encoders to do so. If you don't know what that means, see (here)[https://circe.github.io/circe/codec.html]. If you'd prefer, then simply note that an `import io.circe.generic.auto.*` where you define your data-types will likely mean everything will 'just work', though your mileage may vary.
- In any case, you _must_ import `jsonrpc2.given` to be able to use this library, or (de-)serialization will fail. Subsequent usage is now dependent on the role `jsonrpc2` is performing.

### Client
- `jsonrpc2` creates JSONRPC requests by using the `createRpcRequest` method. It returns the request itself and a UID identifying it (such that the subsequent response can be matched to it), and takes two arguments:
  1. The name of the method to call, such that the server will know what to invoke.
  2. A list of parameters to pass to the method at the remote end, as a tuple.

- If you would instead prefer to create a JSONRPC Notification (i.e. when the receiving server does not need to issue a response), use `createRpcNotification` instead. In this case, no ID will be returned.

```scala3
import jsonrpc2.{*, given}

val (req, id) = createRpcRequest("someRpcMethod", (3, 2, 1, "hi"))
// For example...
requests.post("http://somerpcserver.com", data = req, headers = Map("content-type" -> "application/json"))
```

### Server
- On the server, RPC methods can be created from existing functions using the `asRpcMethod` extension, defined in `RpcExtensions`. `asp RpcMethod` handles arbitrary return types and effectively arbitrary parameters, but there are several things to bear in mind:
    1. If the method takes more than one argument, then it must be converted to a single-arity method that takes a tuple. Use the built-in method `.tupled` to do so, e.g. `fooBar.tupled.asRpcMethod`.
    2. The function being converted must return an `Either`; encode any errors you wish as a `Left` and they will be serialized appropriately.
        - If you wish to map errors to particular JSONRPC error codes, then use the `jsonrpc.CodedExtensions` methods. For example, `Left("oh no!").withCode(4)` will create an `Either` that, once serialized, will create an RPC response that includes both the `"oh no!"` message and the code `4`.
        - If you wish to trigger a method that is guaranteed to succeed using RPC, you may use the `asRightMethod` extension method to convert a function that returns a `T` into one that returns a `Right[T]`, thus allowing is to be used as an RPC method.
- To deserialize an RPC request or notification (and thus ascertain which method should be called), use `readRpcRequest`.

```scala3
import jsonrpc2.{*, given}

def identity(i: Int) = i
def identityRpc = identity.asRightMethod.asRpcMethod

// Receive some RPC message via some transport, then...
val (methodToCall, argsForMethod) = readRpcRequest(rpcMessage)
if methodToCall == "identity" then
  identityRpc(argsForMethod) match 
    case Left(errString) =>   // The method could not be invoked.
    case Right(None) =>       // The received message was a notification; no response is created.
    case Right(Some(resp)) => // `resp` can be sent back to the client via the transport.
```

- Note that the response sent back to the client may be either an error or successful result, depending on whether or not the RPC method itself returned an error.

## Thanks to
(circe)[https://circe.github.io/], upon which this project depends.
