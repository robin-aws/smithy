$version: "2"

namespace smithy.example

use smithy.contracts#contracts

service Crypto {
    operations: [Encrypt]
    resources: [Key]
}

// A second service contains the resource but NOT the operation. An omitted `service` must
// resolve to the service that contains the operation (Crypto), not be left unset because
// two services contain the resource; otherwise the handle has no service and the correct
// "Enabled key succeeds" example fails.
service Admin {
    resources: [Key]
}

resource Key {
    identifiers: {keyId: String}
}

@contracts({
    KeyEnabledOnSuccess: {
        documentation: "A successful Encrypt requires the referenced key to have been ENABLED beforehand"
        expression: "requires(@, resource(before, input.key).properties.keyState == 'ENABLED')"
    }
})
@examples([
    {
        title: "Enabled key succeeds"
        input: {keyRef: "k1"}
        output: {}
        before: {"smithy.example#Crypto": [{resources: {"smithy.example#Key": [{identifiers: {keyId: "k1"}, properties: {keyState: "ENABLED"}}]}}]}
    }
    {
        title: "Disabled key must not succeed"
        input: {keyRef: "k1"}
        output: {}
        before: {"smithy.example#Crypto": [{resources: {"smithy.example#Key": [{identifiers: {keyId: "k1"}, properties: {keyState: "DISABLED"}}]}}]}
    }
])
operation Encrypt {
    input: EncryptInput
    output: EncryptOutput
}

@input
@references([
    {resource: Key, name: "key", ids: {keyId: "keyRef"}}
])
structure EncryptInput {
    @required
    keyRef: String
}

@output
structure EncryptOutput {}
