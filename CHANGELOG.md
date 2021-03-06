# Change log

All notable changes to the project will be documented in this file. This project adheres to [Semantic Versioning](http://semver.org).

## [1.2.0] - 2021-06-17
### Added:
- The SDK now supports the ability to control the proportion of traffic allocation to an experiment. This works in conjunction with a new platform feature now available to early access customers.

## [1.1.2] - 2021-06-14
### Changed:
- Increased the compile-time dependency on `jackson-databind` to 2.10.5.1, due to [CVE-2020-25649](https://nvd.nist.gov/vuln/detail/CVE-2020-25649).
- Stopped including Gson and Jackson in the published runtime dependency list in Gradle module metadata. These artifacts were already being excluded from `pom.xml`, but were still showing up as transitive dependencies in any tools that used the module metadata. For the rationale behind excluding these dependencies, see `build-shared.gradle`.

## [1.1.1] - 2021-04-22
### Fixed:
- Fixed an issue in the Jackson integration that could cause `.0` to be added unnecessarily to integer numeric values when serializing objects with Jackson.

## [1.1.0] - 2021-04-22
This release makes improvements to the helper methods for using Gson and Jackson for JSON conversion.

### Added:
- `LDGson.valueToJsonElement` and `LDGson.valueMapToJsonElementMap`: convenience methods for applications that use Gson types.
- `LDValue.arrayOf()`

### Changed:
- In `com.launchdarkly.sdk.json`, the implementations of `LDGson.typeAdapters` and `LDJackson.module` have been changed for better efficiency in deserialization. Instead of creating an intermediate string representation and re-parsing that, they now have a more direct way for the internal deserialization logic to interact with the streaming parser in the application&#39;s Gson or Jackson instance.

### Fixed:
- `Gson.toJsonTree` now works with LaunchDarkly types, as long as you have configured it as described in `com.launchdarkly.sdk.json.LDGson`. Previously, Gson was able to convert these types to and from JSON string data, but `toJsonTree` did not work due to a [known issue](https://github.com/google/gson/issues/1289) with the `JsonWriter.jsonValue` method; the SDK code no longer uses that method.
- `LDValue.parse()` now returns `LDValue.ofNull()` instead of an actual null reference if the JSON string is `null`.
- Similarly, when deserializing an `EvaluationDetail<LDValue>` from JSON, if the `value` property is `null`, it will now translate this into `LDValue.ofNull()` rather than an actual null reference.

## [1.0.0] - 2020-06-01
Initial release, corresponding to version 5.0.0 of the server-side Java SDK.
