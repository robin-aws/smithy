$version: "2"

namespace smithy.example

use smithy.contracts#contracts

service Crypto {
    operations: [Encrypt]
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

// The reference pins an explicit service. A world snapshot resolved by resource() holds
// exactly one service, so the handle carries no service coordinate and the explicit
// service does not affect resolution; the "Enabled key succeeds" example passes all the
// same.
@input
@references([
    {resource: Key, service: Crypto, name: "key", ids: {keyId: "keyRef"}}
])
structure EncryptInput {
    @required
    keyRef: String
}

@output
structure EncryptOutput {}
