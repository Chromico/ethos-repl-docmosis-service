package uk.gov.hmcts.ethos.replacement.docmosis.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ReferToETJType {

    @JsonProperty("referralJudge")
    private String referralJudge;
    @JsonProperty("referralOutcome")
    private String referralOutcome;
    @JsonProperty("referralExplanation")
    private String referralExplanation;

}