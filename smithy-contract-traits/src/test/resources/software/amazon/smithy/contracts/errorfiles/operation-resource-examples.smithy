$version: "2"

namespace smithy.example

use smithy.contracts#contracts

@contracts({
    KeyEnabledOnSuccess: {
        documentation: "A successful Encrypt requires the referenced key to have been ENABLED beforehand"
        expression: "requires(@, resource(before, {resource: 'Key', ids: {keyId: input.keyId}}).keyState == 'ENABLED')"
    }
})
@examples([
    {
        title: "Enabled key succeeds"
        input: {keyId: "k1"}
        output: {}
        before: {Key: [{keyId: "k1", keyState: "ENABLED"}]}
    }
    {
        title: "Disabled key must not succeed"
        input: {keyId: "k1"}
        output: {}
        before: {Key: [{keyId: "k1", keyState: "DISABLED"}]}
    }
])
operation Encrypt {
    input: EncryptInput
    output: EncryptOutput
}

@input
structure EncryptInput {
    @required
    keyId: String
}

@output
structure EncryptOutput {}
