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
    Restricts shape values to those which satisfy the given JMESPath expressions.
Trait selector
    ``:not(:test(service, resource))``

    *Any shape other than services and resources*
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

See the :ref:`JMESPath data model <waiter-jmespath-data-model>` for details on how Smithy types are mapped to JMESPath types.

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


-------------------------------------
Applying ``conditions`` to operations
-------------------------------------

The ``conditions`` trait MAY be applied to an operation. An operation condition
is not evaluated against a single shape value but against the tuple of a single
call, exposed as an object with the following members:

.. list-table::
    :header-rows: 1
    :widths: 12 28 60

    * - Member
      - Value
      - Description
    * - input
      - operation input
      - The input provided to the call.
    * - output
      - operation output
      - The output returned by a successful call. Absent on failure.
    * - error
      - ``{shapeId, content}``
      - The modeled error returned by a failed call, as an object carrying the
        error's ``shapeId`` and its ``content``. Absent on success. ``output``
        and ``error`` are mutually exclusive.
    * - before
      - world snapshot
      - A snapshot of resource state before the call. This is *ghost state*
        (see below).
    * - after
      - world snapshot
      - A snapshot of resource state after the call. This is *ghost state*
        (see below).

Expressions on an operation therefore reference these members, for example
``input.start < input.end``:

.. code-block:: smithy

    @conditions({
        StartBeforeEnd: {
            documentation: "The requested start time must be strictly less than the end time"
            expression: "input.start < input.end"
        }
    })
    operation FetchLogs {
        input: FetchLogsInput
    }

An operation instance can be materialized from an :ref:`examples-trait` value,
which is how operation conditions are checked at build time. The ``before`` and
``after`` snapshots are supplied through the ``before`` and ``after`` members of
the example.

Observable and ghost state
--------------------------

The ``input``, ``output``, and ``error`` members are *observable*: they are the
data a client actually sends and receives. The ``before`` and ``after``
snapshots are *ghost state*: specification-only values that describe resource
state for the purpose of reasoning, and are never present on the wire. A
resource handle projected from a named :ref:`references-trait` (see below) is
likewise ghost, because it is a pointer into a snapshot rather than wire data;
the identifiers inside it are observable, but the handle itself is not. Traits
that are evaluated at runtime, such as waiters, may reference only the
observable members. Ghost state is available only to validation-time contracts.

------------------
Contract functions
------------------

In addition to the built-in JMESPath_ functions, the following functions are
available in ``conditions`` expressions:

.. list-table::
    :header-rows: 1
    :widths: 26 74

    * - Function
      - Description
    * - ``requires(instance, predicate)``
      - Declares a necessary precondition. On a successful instance (its
        ``error`` is null), ``predicate`` MUST be truthy; on a failure instance
        the requirement is vacuously satisfied. ``predicate`` is evaluated
        eagerly, because a necessary precondition reads only pre-call state that
        is present on both the success and failure paths.
    * - ``resource(world, handle)``
      - Looks up a single resource instance in a ``before`` or ``after`` world
        snapshot and returns it, or null when none matches. The ``handle`` is an
        object ``{service, resource, ids}`` (``service`` optional) where ``ids``
        maps each resource identifier name to its value.

A named :ref:`references-trait` on the input structure projects a resource
handle reachable as ``input.<name>``, so a precondition over resource state can
be written directly:

.. code-block:: smithy

    @conditions({
        KeyEnabledOnSuccess: {
            documentation: "A successful call requires the referenced key to be ENABLED beforehand"
            expression: "requires(@, resource(before, input.key).keyState == 'ENABLED')"
        }
    })
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


.. _CommonMark: https://spec.commonmark.org/
.. _JMESPath: https://jmespath.org/