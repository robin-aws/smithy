.. _contract-traits:

===============
Contract traits
===============

Contract traits are used to further constrain the valid values and behaviors of a model.
Like constraint traits, contract traits are for validation only and SHOULD NOT
impact the types signatures of generated code.

--------------------------
Contract trait enforcement
--------------------------

Contract traits provide structured documentation of implicit API constraints,
and are useful for generating tests or applying static analysis to client or service code.

Contract traits SHOULD NOT be directly enforced by default when serializing or deserializing.
These traits often express contracts using higher-level constructs and simpler but less efficient expressions.
Services will usually check these contracts outside of service frameworks in more efficient ways.

.. smithy-trait:: smithy.contracts#conditions
.. _conditions-trait:

--------------------
``conditions`` trait
--------------------

Summary
    Restricts the values of a shape to those which satisfy the given JMESPath expressions.
Trait selector
    ``:not(:test(service, operation, resource))``

    *Any shape other than services, operations, and resources*
Value type
    ``map``

The ``conditions`` trait is a map from condition names to ``Condition`` structures that contain
the following members:

.. list-table::
    :header-rows: 1
    :widths: 10 23 67

    * - Property
      - Type
      - Description
    * - expression
      - ``string``
      - **Required**. JMESPath_ expression that must evaluate to true.
    * - documentation
      - ``string``
      - **Required**. Documentation about the condition defined using CommonMark_.

See the :ref:`JMESPath data model <jmespath-data-model>` for details on how Smithy types are mapped to JMESPath types.

.. code-block:: smithy

    @conditions({
        StartBeforeEnd: {
            documentation: "The start time must be strictly less than the end time",
            expression: "start < end"
        }
    })
    structure FetchLogsInput {
        @required
        start: Timestamp

        @required
        end: Timestamp
    }

    @conditions({
        NoKeywords: {
            documentation: "The name cannot contain either 'id' or 'name', as these are reserved keywords"
            expression: "!contains(@, 'id') && !contains(@, 'name')"
        }
    })
    string Name


.. smithy-trait:: smithy.contracts#contracts
.. _contracts-trait:

-------------------
``contracts`` trait
-------------------

Summary
    Constrains the calls of an operation to those which satisfy the given JMESPath
    expressions. Each expression must hold for every call.
Trait selector
    ``operation``
Value type
    ``map``

Where ``conditions`` restricts the *values* of a data shape, ``contracts``
constrains the *calls* of an operation. An operation is a procedure and has no
value; a contract is a predicate that must hold for every call, evaluated over
the operation's :ref:`call <jmespath-operation-call>`, the
``{input, output, error, before, after}`` object. The value type is the same
map of named ``Condition`` structures as ``conditions``.

Preconditions and postconditions are ordinary entries gated on the call outcome
(``error``). A call is materialized from an :ref:`examples-trait` value at build
time, with ``before`` and ``after`` supplied by the example's ``before`` and
``after`` members.

.. code-block:: smithy

    @contracts({
        StartBeforeEnd: {
            documentation: "The requested start time must be strictly less than the end time"
            expression: "input.start < input.end"
        }
    })
    operation FetchLogs {
        input: FetchLogsInput
    }

------------------
Contract functions
------------------

In addition to the built-in JMESPath_ functions, the following functions are
available in ``contracts`` expressions:

.. list-table::
    :header-rows: 1
    :widths: 26 74

    * - Function
      - Description
    * - ``requires(instance, predicate)``
      - Declares a necessary precondition. On a successful call (its ``error`` is
        null), ``predicate`` MUST be truthy; on a failed call the requirement is
        vacuously satisfied. ``predicate`` is evaluated eagerly, because a
        necessary precondition reads only pre-call state that is present on both
        the success and failure paths.
    * - ``resource(world, handle)``
      - Looks up a single resource instance in a ``before`` or ``after`` world
        snapshot and returns it, or null when none matches. The ``handle`` is an
        object ``{service, resource, ids}`` (``service`` optional) where ``ids``
        maps each resource identifier name to its value.

A named :ref:`references-trait` on the input structure projects a resource
handle reachable as ``input.<name>``, so a precondition over resource state can
be written directly:

.. code-block:: smithy

    @contracts({
        KeyEnabledOnSuccess: {
            documentation: "A successful call requires the referenced key to be ENABLED beforehand"
            expression: "requires(@, resource(before, input.key).keyState == 'ENABLED')"
        }
    })
    resource Key {
        identifiers: {keyId: String}
    }

    operation Encrypt {
        input: EncryptInput
        output: EncryptOutput
    }

    @input
    @references([
        {resource: Key, name: "key", ids: {keyId: "keyId"}}
    ])
    structure EncryptInput {
        @required
        keyId: String
    }

    @output
    structure EncryptOutput {}


.. _CommonMark: https://spec.commonmark.org/
.. _JMESPath: https://jmespath.org/