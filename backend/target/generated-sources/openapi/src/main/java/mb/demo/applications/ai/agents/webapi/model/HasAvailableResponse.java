package mb.demo.applications.ai.agents.webapi.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * HasAvailableResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.15.0")
public class HasAvailableResponse {

  private Boolean hasAvailable;

  public HasAvailableResponse hasAvailable(Boolean hasAvailable) {
    this.hasAvailable = hasAvailable;
    return this;
  }

  /**
   * True if there are available pets.
   * @return hasAvailable
   */
  
  @Schema(name = "hasAvailable", example = "true", description = "True if there are available pets.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("hasAvailable")
  public Boolean getHasAvailable() {
    return hasAvailable;
  }

  public void setHasAvailable(Boolean hasAvailable) {
    this.hasAvailable = hasAvailable;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HasAvailableResponse hasAvailableResponse = (HasAvailableResponse) o;
    return Objects.equals(this.hasAvailable, hasAvailableResponse.hasAvailable);
  }

  @Override
  public int hashCode() {
    return Objects.hash(hasAvailable);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class HasAvailableResponse {\n");
    sb.append("    hasAvailable: ").append(toIndentedString(hasAvailable)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

