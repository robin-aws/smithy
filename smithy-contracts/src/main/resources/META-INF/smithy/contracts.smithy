$version: "2.0"

namespace smithy.contracts

/// Restricts shape values to those that satisfy one or more JMESPath expressions.
/// Each expression must produce 'true'.
@trait(selector: "*")
list conditions {
    member: Condition
}

structure Condition {
    /// JMESPath expression that must evaluate to true.
    @required
    expression: String

    /// Description of the condition. Used in error messages when violated.
    description: String
}
