
// See notes in CONTRIBUTING.md on code coverage. Unfortunately we can't configure
// line-by-line code coverage overrides within the source code itself, because Jacoco
// operates on bytecode.

// These values are used by helpers/Jacoco.kt.

object TestCoverageOverrides {
    val prefixForAllMethodSignatures = ProjectValues.sdkBasePackage + "."

    // Each entry in methodsWithMissedLineCount is an override to tell the Jacoco plugin
    // that we're aware of a gap in our test coverage and are OK with it. In each entry,
    // the key is the method signature and the value is the number of lines that we
    // expect Jacoco to report as missed.
    val methodsWithMissedLineCount = mapOf(
        "EvaluationReason.error(com.launchdarkly.sdk.EvaluationReason.ErrorKind)" to 1,
        "EvaluationReasonTypeAdapter.parse(com.google.gson.stream.JsonReader)" to 1,
        "LDContext.urlEncodeKey(java.lang.String)" to 2,
        "LDValue.equals(java.lang.Object)" to 1,
        "LDValueTypeAdapter.read(com.google.gson.stream.JsonReader)" to 1,
        "json.LDGson.LDTypeAdapter.write(com.google.gson.stream.JsonWriter, java.lang.Object)" to 1,
        "json.LDJackson.GsonReaderToJacksonParserAdapter.peekInternal()" to 3
    ).mapKeys { prefixForAllMethodSignatures + it.key }

    // Each entry in methodsToSkip is an override to tell the Jacoco plugin to ignore
    // code coverage in the method with the specified signature.
    val methodsToSkip = listOf(
        "json.JsonSerialization.getDeserializableClasses()"
    ).map { prefixForAllMethodSignatures + it }
}
