package uk.gov.hmcts.ethos.replacement.functional.dataloader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.RandomStringUtils;
import uk.gov.hmcts.ecm.common.model.ccd.Address;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ecm.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.types.CasePreAcceptType;
import uk.gov.hmcts.ecm.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.ecm.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.ecm.common.model.ccd.types.ClaimantWorkAddressType;
import uk.gov.hmcts.ecm.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.ecm.common.model.ccd.types.RespondentSumType;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CCDReq {

    public CCDRequest ccdRequest() throws IOException, URISyntaxException {
        CCDRequest ccdRequest = new CCDRequest();
        var caseDetails = caseDetailsJsonToObject("dataLoader.json");
        ccdRequest.setCaseDetails(generateCase(caseDetails));
        return ccdRequest;
    }

    public CaseDetails generateCase(CaseDetails caseDetails) {
        caseDetails.setCaseId("1234567812345678");
        caseDetails.setJurisdiction("EMPLOYMENT");
        caseDetails.setState("Accepted");
        caseDetails.setCaseTypeId("LondonSouth");
        caseDetails.setCreatedDate(LocalDateTime.now());
        caseDetails.setLastModified(LocalDateTime.now());
        caseDetails.setCaseData(caseData());
        caseDetails.setDataClassification(null);
        return caseDetails;
    }

    public CaseData caseData() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String stringDate = dateFormat.format(date);
        CaseData cd = new CaseData();

        cd.setPreAcceptCase(casePreAcceptType());
        cd.setStateAPI("Accepted");
        cd.setPositionType("Manually Created");
        cd.setClaimantRepresentedQuestion("Yes");
        cd.setRespondentCollection(Collections.singletonList(respondentSumTypeItem()));
        cd.setTribunalCorrespondenceDX("DX 155061 Croydon 39");
        cd.setRespondent("Test Respondent");
        cd.setTribunalCorrespondenceFax("08703240174");
        cd.setCaseType("Single");
        cd.setFeeGroupReference(RandomStringUtils.randomNumeric(12));
        cd.setClaimantIndType(claimantIndType());
        cd.setClaimantTypeOfClaimant("Individual");
        cd.setTribunalCorrespondenceAddress(address(
                "Montague Court",
                "101 London Road",
                "West Croydon",
                "London",
                "CR0 2RF"));
        cd.setTribunalCorrespondenceEmail("londonsouthet@Justice.gov.uk");
        cd.setClaimant("Claimant Test");
        cd.setDateToPosition(stringDate);
        cd.setClaimantWorkAddress(claimantWorkAddressType());
        cd.setMultipleFlag("No");
        cd.setCurrentPosition("Manually Created");
        cd.setReceiptDate(stringDate);
        cd.setCaseSource("Manually Created");
        cd.setTribunalCorrespondenceTelephone("01132459741");
        cd.setClaimantWorkAddressQuestion("Yes");
        cd.setClaimantType(claimantType());
        cd.setFlagsImageFileName("EMP-TRIB-00000000.jpg");
        cd.setJurCodesCollection(jurCodesTypeItems());

        return cd;
    }

    public List<JurCodesTypeItem> jurCodesTypeItems() {
        List<JurCodesTypeItem> jurCodesTypeItems = new ArrayList<>();
        JurCodesType jurCodesType = new JurCodesType();
        JurCodesTypeItem jurCodesTypeItem = new JurCodesTypeItem();
        jurCodesType.setJuridictionCodesList("ADT");
        jurCodesType.setJudgmentOutcome("Input in error");
        jurCodesTypeItem.setValue(jurCodesType);
        jurCodesTypeItem.setId(UUID.randomUUID().toString());
        return jurCodesTypeItems;
    }

    public CasePreAcceptType casePreAcceptType() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String stringDate = dateFormat.format(date);
        CasePreAcceptType casePreAcceptType = new CasePreAcceptType();
        casePreAcceptType.setCaseAccepted("Yes");
        casePreAcceptType.setDateAccepted(stringDate);
        casePreAcceptType.setDateRejected(null);
        return casePreAcceptType;

    }

    public ClaimantType claimantType() {
        ClaimantType claimantType = new ClaimantType();
        claimantType.setClaimantAddressUK(address("12 The Grove", "", "New Ridlet", "Stocksfield", "NE43 7RD"));
        return claimantType;
    }

    public ClaimantWorkAddressType claimantWorkAddressType() {
        ClaimantWorkAddressType claimantWorkAddressType = new ClaimantWorkAddressType();
        claimantWorkAddressType.setClaimantWorkAddress(address(
                "The Gables",
                "Prune Hill",
                "Englefield Green",
                "Egham",
                "TW20 9TR"
        ));
        return claimantWorkAddressType;
    }

    public ClaimantIndType claimantIndType() {
        ClaimantIndType claimantIndType = new ClaimantIndType();
        claimantIndType.setClaimantFirstNames("Claimant");
        claimantIndType.setClaimantLastName("Test");
        claimantIndType.setClaimantDateOfBirth("1891-01-01");
        claimantIndType.setClaimantGender("Male");
        return claimantIndType;
    }

    public RespondentSumTypeItem respondentSumTypeItem() {
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setId(String.valueOf(UUID.randomUUID()));
        respondentSumTypeItem.setValue(respondentSumType());
        return respondentSumTypeItem;
    }

    public RespondentSumType respondentSumType() {
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("Test Respondent");
        respondentSumType.setRespondentACASQuestion("No");
        respondentSumType.setRespondentACASNo("Employer already in touch");
        respondentSumType.setRespondentAddress(address("12 The Grove", "", "New Ridlet", "Stocksfield", "NE43 7RD"));
        respondentSumType.setResponseStruckOut("No");
        respondentSumType.setResponseReceived("Yes");
        return respondentSumType;
    }

    public Address address(String addressLine1, String addressLine2, String addressLine3, String postTown,
                           String postCode) {
        Address address = new Address();
        address.setAddressLine1(addressLine1);
        address.setAddressLine2(addressLine2);
        address.setAddressLine3(addressLine3);
        address.setPostTown(postTown);
        address.setPostCode(postCode);
        address.setCountry("United Kingdom");
        return address;
    }

    private CaseDetails caseDetailsJsonToObject(String jsonFileName) throws IOException, URISyntaxException {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader()
                .getResource(jsonFileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }
}
