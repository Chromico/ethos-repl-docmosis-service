package uk.gov.hmcts.ethos.replacement.docmosis.model.notification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationRequest {
    @JsonProperty("caseReferenceNumber")
    private String caseReferenceNumber;
    @JsonProperty("solicitorReferenceNumber")
    private String solicitorReferenceNumber;
    @JsonProperty("name")
    private String name;
    @JsonProperty("notificationEmail")
    private String notificationEmail;
}