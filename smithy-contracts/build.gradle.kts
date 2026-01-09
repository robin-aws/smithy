/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
plugins {
    id("smithy.module-conventions")
    id("smithy.profiling-conventions")
    id("software.amazon.smithy.gradle.smithy-trait-package").version("1.3.0")
}

description = "Smithy contracts."

extra["displayName"] = "Smithy :: Contracts"
extra["moduleName"] = "software.amazon.smithy.contracts"

dependencies {
    api(project(":smithy-model-jmespath"))
}
