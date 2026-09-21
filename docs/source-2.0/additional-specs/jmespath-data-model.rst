.. _jmespath-data-model:

===================
JMESPath data model
===================

Several Smithy features use JMESPath_ expressions over model data, including
:ref:`waiters <waiters>` and the :ref:`conditions trait <conditions-trait>`.
This section defines the data model those expressions are evaluated against:
how Smithy types are exposed as `JMESPath types`_, and how an operation is
exposed as an instance.

--------------------
Type conversions
--------------------

The data model exposed to JMESPath_ for a shape's value is converted from
Smithy types to `JMESPath types`_ using the following conversion table:

.. list-table::
    :header-rows: 1

    * - Smithy type
      - JMESPath type
    * - blob
      - string (base64 encoded)
    * - boolean
      - boolean
    * - byte
      - number
    * - short
      - number
    * - integer
      - number
    * - long
      - number [#fnumbers]_
    * - float
      - number
    * - double
      - number
    * - bigDecimal
      - number [#fnumbers]_
    * - bigInteger
      - number [#fnumbers]_
    * - string
      - string
    * - timestamp
      - number [#ftimestamp]_
    * - document
      - any type
    * - list and set
      - array
    * - map
      - object
    * - structure
      - object [#fstructure]_
    * - union
      - object [#funion]_

Footnotes
~~~~~~~~~~~~

.. [#fnumbers] ``long``, ``bigInteger``, ``bigDecimal`` are exposed as
   numbers to JMESPath. If a value for one of these types truly exceeds
   the value of a double (the native numeric type of JMESPath), then
   querying these types is a bad idea.
.. [#ftimestamp] ``timestamp`` values are represented in JMESPath expressions
   as epoch seconds with optional decimal precision. This allows for
   timestamp values to be used with relative comparators like ``<`` and ``>``.
.. [#fstructure] Structure members are referred to by member name and not
   the data sent over the wire. For example, the :ref:`jsonname-trait` is not
   respected in JMESPath expressions that select structure members.
.. [#funion] ``union`` values are represented exactly like structures except
   only a single member is set to a non-null value.

--------------------
Operations
--------------------

An operation does not have a single value. It is exposed to JMESPath as an
*instance*: the tuple of a single call, represented as an object with the
following members.

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
      - state snapshot
      - A snapshot of resource state before the call. Model-only (see below).
    * - after
      - state snapshot
      - A snapshot of resource state after the call. Model-only (see below).

Expressions on an operation therefore reference these members, for example
``input.start < input.end``.

Resource and service instances are not yet exposed to JMESPath; they are
reserved for a future revision that maps them to their state.

--------------------
Model-only members
--------------------

The ``input``, ``output``, and ``error`` members are *observable*: they are the
data a client actually sends and receives. The ``before`` and ``after``
snapshots are *model-only*: they exist in the model to describe resource state
for reasoning, and are never present on the wire. Features that are evaluated
at runtime, such as waiters, may reference only the observable members.
Model-only members are available only to build-time analysis such as the
:ref:`conditions trait <conditions-trait>`.

--------------------
Reference handles
--------------------

A named :ref:`references-trait` on a structure is projected into the data model
as a resource *handle* reachable as ``<member-root>.<name>`` (for example
``input.key``). The handle is an object ``{service, resource, ids}`` (with
``service`` optional) synthesized from the reference's identifier bindings, and
is intended to be passed to functions that resolve a resource against a state
snapshot. A handle is model-only: it is a pointer synthesized from the
observable identifier members rather than data on the wire. The identifiers
inside it are observable, but the handle itself is not.


.. _JMESPath: https://jmespath.org/
.. _JMESPath types: https://jmespath.org/specification.html#data-types
