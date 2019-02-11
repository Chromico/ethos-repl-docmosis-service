package uk.gov.hmcts.ethos.replacement.docmosis.model.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CaseDataTest {

    private CaseData caseData;

    @Before
    public void setUp() throws Exception {
        String json = "{"
                + " \"caseNote\" : \"1111\", "
                + " \"positionType\" : \"Single\", "
                + " \"receiptDate\" : \"20 Jan 2019\", "
                + " \"userLocation\" : \"Bath\", "
                + " \"locationType\" : \"City\", "
                + " \"caseType\" : \"Single\", "
                + " \"feeGroupReference\" : \"1212\", "
                + " \"tribunalOffice\" : \"tribunalOffice\" "
                + "} ";
        ObjectMapper mapper = new ObjectMapper();
        caseData = mapper.readValue(json, CaseData.class);
    }


    @Test
    public void shouldCreateCaseDataFromJson() {
        assertThat(caseData.getCaseNote(), is("1111"));
        assertThat(caseData.getPositionType(), is("Single"));
        assertThat(caseData.getReceiptDate(), is("20 Jan 2019"));
        assertThat(caseData.getUserLocation(), is("Bath"));
        assertThat(caseData.getLocationType(), is("City"));
        assertThat(caseData.getCaseType(), is("Single"));
        assertThat(caseData.getFeeGroupReference(), is("1212"));
        assertThat(caseData.getTribunalOffice(), is("tribunalOffice"));
    }
}