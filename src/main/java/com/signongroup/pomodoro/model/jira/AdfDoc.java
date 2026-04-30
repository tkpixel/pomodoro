package com.signongroup.pomodoro.model.jira;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdfDoc(
    @JsonProperty("type") String type,
    @JsonProperty("version") int version,
    @JsonProperty("content") List<AdfParagraph> content) {
  public static AdfDoc ofText(String text) {
    if (text == null || text.isEmpty()) {
      return null;
    }
    return new AdfDoc(
        "doc", 1, List.of(new AdfParagraph("paragraph", List.of(new AdfText("text", text)))));
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record AdfParagraph(
      @JsonProperty("type") String type, @JsonProperty("content") List<AdfText> content) {}

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record AdfText(@JsonProperty("type") String type, @JsonProperty("text") String text) {}
}
