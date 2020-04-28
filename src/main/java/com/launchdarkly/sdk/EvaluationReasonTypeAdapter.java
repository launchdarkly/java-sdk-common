package com.launchdarkly.sdk;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import static com.launchdarkly.sdk.Helpers.readNonNullableString;
import static com.launchdarkly.sdk.Helpers.readNullableString;

final class EvaluationReasonTypeAdapter extends TypeAdapter<EvaluationReason> {
  @Override
  public EvaluationReason read(JsonReader reader) throws IOException {
    return parse(reader);
  }

  static EvaluationReason parse(JsonReader reader) throws IOException {
    EvaluationReason.Kind kind = null;
    int ruleIndex = -1;
    String ruleId = null;
    String prereqKey = null;
    EvaluationReason.ErrorKind errorKind = null;
    
    reader.beginObject();
    while (reader.peek() != JsonToken.END_OBJECT) {
      String key = reader.nextName();
      switch (key) { // COVERAGE: may have spurious "branches missed" warning, see https://stackoverflow.com/questions/28013717/eclemma-branch-coverage-for-switch-7-of-19-missed
      case "kind":
        kind = Enum.valueOf(EvaluationReason.Kind.class, readNonNullableString(reader));
        break;
      case "ruleIndex":
        ruleIndex = reader.nextInt();
        break;
      case "ruleId":
        ruleId = readNullableString(reader);
        break;
      case "prerequisiteKey":
        prereqKey = reader.nextString();
        break;
      case "errorKind":
        errorKind = Enum.valueOf(EvaluationReason.ErrorKind.class, readNonNullableString(reader));
        break;
      default:
        reader.skipValue(); // ignore any unexpected property
      }
    }
    reader.endObject();
    
    if (kind == null) {
      throw new JsonParseException("EvaluationReason missing required property \"kind\"");
    }
    switch (kind) {
    case OFF:
      return EvaluationReason.off();
    case FALLTHROUGH:
      return EvaluationReason.fallthrough();
    case TARGET_MATCH:
      return EvaluationReason.targetMatch();
    case RULE_MATCH:
      return EvaluationReason.ruleMatch(ruleIndex, ruleId);
    case PREREQUISITE_FAILED:
      return EvaluationReason.prerequisiteFailed(prereqKey);
    case ERROR:
      return EvaluationReason.error(errorKind);
    default:
      // COVERAGE: compiler requires default but there are no other values
      return null;
    }
  }

  @Override
  public void write(JsonWriter writer, EvaluationReason reason) throws IOException {
    writer.beginObject();
    writer.name("kind");
    writer.value(reason.getKind().name());
    
    switch (reason.getKind()) {
    case RULE_MATCH:
      writer.name("ruleIndex");
      writer.value(reason.getRuleIndex());
      if (reason.getRuleId() != null) {
        writer.name("ruleId");
        writer.value(reason.getRuleId());
      }
      break;
    case PREREQUISITE_FAILED:
      writer.name("prerequisiteKey");
      writer.value(reason.getPrerequisiteKey());
      break;
    case ERROR:
      writer.name("errorKind");
      writer.value(reason.getErrorKind().name());
      // The exception field is not included in the JSON representation, since we do not want it to appear in
      // analytics events (the LD event service wouldn't know what to do with it, and it would include a
      // potentially large amount of stacktrace data including application code details).
      break;
    default:
      break;
    }
    
    writer.endObject();
  }
}
