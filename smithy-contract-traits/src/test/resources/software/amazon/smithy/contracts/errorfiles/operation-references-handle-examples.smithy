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
        expression: "requires(@, resource(before, input.key).keyState == 'ENABLED')"
    }
})
@examples([
    {
        title: "Enabled key succeeds"
        input: {keyRef: "k1"}
        output: {}
        before: {Key: [{keyId: "k1", keyState: "ENABLED"}]}
    }
    {
        title: "Disabled key must not succeed"
        input: {keyRef: "k1"}
        output: {}
        before: {Key: [{keyId: "k1", keyState: "DISABLED"}]}
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
