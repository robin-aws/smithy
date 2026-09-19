$version: "2.0"

namespace smithy.contracts

/// Restricts the instances of a shape to those that satisfy one or more JMESPath
/// expressions. Each expression must produce `true`.
///
/// For data shapes an instance is a value of the shape. For operations an instance
/// is the `{input, output, error, before, after}` tuple of a call, so expressions
/// reference those members (for example `input.start < input.end`).
@trait(selector: ":not(:test(service, resource))")
map conditions {
    /// Name of the condition
    key: ConditionName

    /// Definition of the condition
    value: Condition
}

/// A name of a condition.
@pattern("^[A-Z]+[A-Za-z0-9]*$")
string ConditionName

/// Defines an individual condition.
structure Condition {
    /// JMESPath expression that must evaluate to `true`.
    @required
    expression: String

    /// Documentation about the condition. Can use CommonMark.
    @required
    documentation: String
}
