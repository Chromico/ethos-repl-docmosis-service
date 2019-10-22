package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ethos.replacement.docmosis.idam.models.UserDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.model.ccd.types.DateListedType;
import uk.gov.hmcts.ethos.replacement.docmosis.model.ccd.types.HearingType;
import uk.gov.hmcts.ethos.replacement.docmosis.model.listing.ListingData;
import uk.gov.hmcts.ethos.replacement.docmosis.model.listing.items.ListingTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.model.listing.types.ListingType;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.formatCurrentDate;
import static uk.gov.hmcts.ethos.replacement.docmosis.model.helper.Constants.*;

@Slf4j
public class ListingHelper {

    public static String getCaseTypeId(String caseTypeId) {
        switch (caseTypeId) {
            case MANCHESTER_LISTING_CASE_TYPE_ID:
                return MANCHESTER_USERS_CASE_TYPE_ID;
            case LEEDS_LISTING_CASE_TYPE_ID:
                return LEEDS_USERS_CASE_TYPE_ID;
            default:
                return SCOTLAND_USERS_CASE_TYPE_ID;
        }
    }

    public static ListingType getListingTypeFromSubmitData(SubmitEvent submitEvent, HearingType hearingType, DateListedType dateListedType, int index, int hearingCollectionSize) {
        CaseData caseData = submitEvent.getCaseData();
        ListingType listingType = new ListingType();

        listingType.setElmoCaseReference(caseData.getEthosCaseReference());
        String listedDate = dateListedType.getListedDate();
        listingType.setCauseListDate(!isNullOrEmpty(listedDate) ? Helper.formatLocalDate(listedDate) : " ");
        listingType.setCauseListTime(!isNullOrEmpty(listedDate) ? Helper.formatLocalTime(listedDate) : " ");
        listingType.setJurisdictionCodesList(BulkHelper.getJurCodesCollection(caseData.getJurCodesCollection()));
        listingType.setHearingType(!isNullOrEmpty(hearingType.getHearingType()) ? hearingType.getHearingType() : " ");
        listingType.setPositionType(!isNullOrEmpty(caseData.getPositionType()) ? caseData.getPositionType() : " ");
        listingType.setHearingJudgeName(!isNullOrEmpty(dateListedType.getHearingJudgeName()) ? dateListedType.getHearingJudgeName() : " ");
        listingType.setHearingEEMember(!isNullOrEmpty(hearingType.getHearingEEMember()) ? hearingType.getHearingEEMember() : " ");
        listingType.setHearingERMember(!isNullOrEmpty(hearingType.getHearingERMember()) ? hearingType.getHearingERMember() : " ");
        listingType.setClerkResponsible(!isNullOrEmpty(caseData.getClerkResponsible()) ? caseData.getClerkResponsible() : " ");
        listingType.setHearingPanel(!isNullOrEmpty(hearingType.getHearingSitAlone()) ? hearingType.getHearingSitAlone() : " ");

        listingType.setCauseListVenue(getVenue(dateListedType));
        listingType.setHearingRoom(getHearingRoom(dateListedType));

        listingType.setHearingNotes(!isNullOrEmpty(hearingType.getHearingNotes()) ? hearingType.getHearingNotes() : " ");
        listingType.setHearingDay(index+1 + " of " + hearingCollectionSize);
        listingType.setEstHearingLength(!isNullOrEmpty(Helper.getHearingDuration(hearingType)) ? Helper.getHearingDuration(hearingType) : " ");

        if (!isNullOrEmpty(caseData.getClaimantCompany())) {
            listingType.setClaimantName(caseData.getClaimantCompany());
        } else {
            listingType.setClaimantName(caseData.getClaimantIndType() != null && caseData.getClaimantIndType().getClaimantLastName() != null ?
                    caseData.getClaimantIndType().claimantFullName() : " ");
        }
        listingType.setClaimantTown(caseData.getClaimantType() != null && caseData.getClaimantType().getClaimantAddressUK() != null &&
                caseData.getClaimantType().getClaimantAddressUK().getPostTown() != null ?
                caseData.getClaimantType().getClaimantAddressUK().getPostTown() : " ");
        listingType.setClaimantRepresentative(caseData.getRepresentativeClaimantType() != null && caseData.getRepresentativeClaimantType().getNameOfOrganisation() != null ?
                caseData.getRepresentativeClaimantType().getNameOfOrganisation() : " ");

        listingType.setRespondentTown(caseData.getRespondentCollection() != null && !caseData.getRespondentCollection().isEmpty() &&
                caseData.getRespondentCollection().get(0).getValue() != null  && caseData.getRespondentCollection().get(0).getValue().getRespondentAddress().getPostTown() != null ?
                caseData.getRespondentCollection().get(0).getValue().getRespondentAddress().getPostTown() : " ");

        listingType.setRespondent(caseData.getRespondentCollection() != null && !caseData.getRespondentCollection().isEmpty() &&
                caseData.getRespondentCollection().get(0).getValue() != null ?
                caseData.getRespondentCollection().get(0).getValue().getRespondentName() : " ");
        listingType.setRespondentOthers(!isNullOrEmpty(getRespOthersName(caseData)) ? getRespOthersName(caseData) : " ");

        listingType.setRespondentRepresentative(caseData.getRepCollection() != null && !caseData.getRepCollection().isEmpty() &&
                caseData.getRepCollection().get(0).getValue() != null && caseData.getRepCollection().get(0).getValue().getNameOfOrganisation() != null ?
                caseData.getRepCollection().get(0).getValue().getNameOfOrganisation() : " ");

        return listingType;
    }

    public static StringBuilder buildListingDocumentContent(ListingData listingData, String accessKey, String templateName, UserDetails userDetails, String caseType) {
        String FILE_EXTENSION = ".docx";
        StringBuilder sb = new StringBuilder();

        // Start building the instruction
        sb.append("{\n");
        sb.append("\"accessKey\":\"").append(accessKey).append(NEW_LINE);
        sb.append("\"templateName\":\"").append(templateName).append(FILE_EXTENSION).append(NEW_LINE);
        sb.append("\"outputName\":\"").append(OUTPUT_FILE_NAME).append(NEW_LINE);

        // Building the document data
        sb.append("\"data\":{\n");
        sb.append(getCourtListingData(listingData));
        sb.append(getLogo(caseType));
        if (listingData.getListingCollection() != null && !listingData.getListingCollection().isEmpty()) {
            sb.append("\"Listed_date\":\"").append(listingData.getListingCollection().get(0).getValue().getCauseListDate()).append(NEW_LINE);
            sb.append("\"Hearing_location\":\"").append(listingData.getListingCollection().get(0).getValue().getCauseListVenue()).append(NEW_LINE);
        }
        sb.append(getListingRangeDates(listingData));

        String userName = userDetails.getForename() + " " + userDetails.getSurname().orElse("");
        sb.append("\"Clerk\":\"").append(nullCheck(userName)).append(NEW_LINE);

        sb.append(getDocumentData(listingData, templateName, caseType));

        sb.append("\"case_total\":\"").append(getCaseTotal(listingData.getListingCollection())).append(NEW_LINE);
        sb.append("\"Today_date\":\"").append(formatCurrentDate(LocalDate.now())).append("\"\n");
        sb.append("}\n");
        sb.append("}\n");

        return sb;
    }

    private static StringBuilder getLogo(String caseType) {
        StringBuilder sb = new StringBuilder();
        if (caseType.equals(MANCHESTER_LISTING_CASE_TYPE_ID)) {
            sb.append("\"listing_logo\":\"").append("[userImage:").append("enhmcts.png]").append(NEW_LINE);
        } else {
            sb.append("\"listing_logo\":\"").append("[userImage:").append("schmcts.png]").append(NEW_LINE);
        }
        return sb;
    }

    private static StringBuilder getDocumentData(ListingData listingData, String templateName, String caseType) {
        if (Arrays.asList(IT56_TEMPLATE, IT57_TEMPLATE, PUBLIC_CASE_CAUSE_LIST_TEMPLATE, STAFF_CASE_CAUSE_LIST_TEMPLATE,
                PRESS_LIST_CAUSE_LIST_RANGE_TEMPLATE, PRESS_LIST_CAUSE_LIST_SINGLE_TEMPLATE).contains(templateName)) {
            return getCaseCauseList(listingData, caseType);
        } else if (Arrays.asList(PUBLIC_CASE_CAUSE_LIST_ROOM_TEMPLATE, STAFF_CASE_CAUSE_LIST_ROOM_TEMPLATE).contains(templateName)) {
            return getCaseCauseListByRoom(listingData, caseType);
        } else {
            return new StringBuilder();
        }
    }

    private static StringBuilder getListingRangeDates(ListingData listingData) {
        StringBuilder sb = new StringBuilder();
        if (listingData.getHearingDateType().equals(RANGE_HEARING_DATE_TYPE)) {
            sb.append("\"Listed_date_from\":\"").append(Helper.listingFormatLocalDate(listingData.getListingDateFrom())).append(NEW_LINE);
            sb.append("\"Listed_date_to\":\"").append(Helper.listingFormatLocalDate(listingData.getListingDateTo())).append(NEW_LINE);
        }
        return sb;
    }

    private static StringBuilder getCaseCauseListByRoom(ListingData listingData, String caseType) {
        StringBuilder sb = new StringBuilder();
        Map<String, List<ListingTypeItem>> unsortedMap = listingData.getListingCollection().stream()
                .collect(Collectors.groupingBy(listingTypeItem -> listingTypeItem.getValue().getHearingRoom()));
        sb.append("\"location\":[\n");
        Iterator<Map.Entry<String, List<ListingTypeItem>>> entries = new TreeMap<>(unsortedMap).entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, List<ListingTypeItem>> listingEntry = entries.next();
            sb.append("{\"Hearing_room\":\"").append(listingEntry.getKey()).append(NEW_LINE);
            //sb.append("\"Floor\":\"").append("6th Floor").append(NEW_LINE);
            sb.append("\"listing\":[\n");
            for (int i = 0; i < listingEntry.getValue().size(); i++) {
                sb.append(getListingTypeRow(listingEntry.getValue().get(i).getValue(), caseType));
                if (i != listingEntry.getValue().size() - 1) {
                    sb.append(",\n");
                }
            }
            sb.append("]\n");
            if (entries.hasNext()) {
                sb.append("},\n");
            } else {
                sb.append("}],\n");
            }
        }
        return sb;
    }

    private static StringBuilder getCaseCauseList(ListingData listingData, String caseType) {
        List<ListingTypeItem> listingTypeItems = listingData.getListingCollection();
        StringBuilder sb = new StringBuilder();
        sb.append("\"listing\":[\n");
        for (int i = 0; i < listingTypeItems.size(); i++) {
            sb.append(getListingTypeRow(listingTypeItems.get(i).getValue(), caseType));
            if (i != listingTypeItems.size() - 1) {
                sb.append(",\n");
            }
        }
        sb.append("],\n");
        return sb;
    }

    private static StringBuilder getListingTypeRow(ListingType listingType, String caseType) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"Judge\":\"").append(listingType.getHearingJudgeName()).append(NEW_LINE);
        sb.append(getLogo(caseType));
        sb.append("\"ERMember\":\"").append(listingType.getHearingERMember()).append(NEW_LINE);
        sb.append("\"EEMember\":\"").append(listingType.getHearingEEMember()).append(NEW_LINE);
        sb.append("\"Case_No\":\"").append(listingType.getElmoCaseReference()).append(NEW_LINE);
        sb.append("\"Hearing_type\":\"").append(listingType.getHearingType()).append(NEW_LINE);
        sb.append("\"Jurisdictions\":\"").append(listingType.getJurisdictionCodesList()).append(NEW_LINE);
        sb.append("\"Hearing_date\":\"").append(listingType.getCauseListDate()).append(NEW_LINE);
        sb.append("\"Hearing_date_time\":\"").append(listingType.getCauseListDate()).append(" at ").append(listingType.getCauseListTime()).append(NEW_LINE);
        sb.append("\"Hearing_time\":\"").append(listingType.getCauseListTime()).append(NEW_LINE);
        sb.append("\"Hearing_duration\":\"").append(listingType.getEstHearingLength()).append(NEW_LINE);
        sb.append("\"Hearing_clerk\":\"").append(listingType.getClerkResponsible()).append(NEW_LINE);
        sb.append("\"Claimant\":\"").append(listingType.getClaimantName()).append(NEW_LINE);
        sb.append("\"claimant_town\":\"").append(listingType.getClaimantTown()).append(NEW_LINE);
        sb.append("\"claimant_representative\":\"").append(listingType.getClaimantRepresentative()).append(NEW_LINE);
        sb.append("\"Respondent\":\"").append(listingType.getRespondent()).append(NEW_LINE);
        sb.append("\"resp_others\":\"").append(listingType.getRespondentOthers()).append(NEW_LINE);
        sb.append("\"respondent_town\":\"").append(listingType.getRespondentTown()).append(NEW_LINE);
        sb.append("\"Hearing_location\":\"").append(listingType.getCauseListVenue()).append(NEW_LINE);
        sb.append("\"Hearing_room\":\"").append(listingType.getHearingRoom()).append(NEW_LINE);
        sb.append("\"Hearing_dayofdays\":\"").append(listingType.getHearingDay()).append(NEW_LINE);
        sb.append("\"Hearing_panel\":\"").append(listingType.getHearingPanel()).append(NEW_LINE);
        sb.append("\"Hearing_notes\":\"").append(listingType.getHearingNotes()).append(NEW_LINE);
        sb.append("\"respondent_representative\":\"").append(listingType.getRespondentRepresentative()).append("\"}");
        return sb;
    }

    private static StringBuilder getCourtListingData(ListingData listingData) {
        StringBuilder sb = new StringBuilder();
        if (listingData.getTribunalCorrespondenceAddress() != null) {
            sb.append("\"Court_addressLine1\":\"").append(nullCheck(listingData.getTribunalCorrespondenceAddress().getAddressLine1())).append(NEW_LINE);
            sb.append("\"Court_addressLine2\":\"").append(nullCheck(listingData.getTribunalCorrespondenceAddress().getAddressLine2())).append(NEW_LINE);
            sb.append("\"Court_addressLine3\":\"").append(nullCheck(listingData.getTribunalCorrespondenceAddress().getAddressLine3())).append(NEW_LINE);
            sb.append("\"Court_town\":\"").append(nullCheck(listingData.getTribunalCorrespondenceAddress().getPostTown())).append(NEW_LINE);
            sb.append("\"Court_county\":\"").append(nullCheck(listingData.getTribunalCorrespondenceAddress().getCounty())).append(NEW_LINE);
            sb.append("\"Court_postCode\":\"").append(nullCheck(listingData.getTribunalCorrespondenceAddress().getPostCode())).append(NEW_LINE);
        }
        sb.append("\"Court_telephone\":\"").append(nullCheck(listingData.getTribunalCorrespondenceTelephone())).append(NEW_LINE);
        sb.append("\"Court_fax\":\"").append(nullCheck(listingData.getTribunalCorrespondenceFax())).append(NEW_LINE);
        sb.append("\"Court_DX\":\"").append(nullCheck(listingData.getTribunalCorrespondenceDX())).append(NEW_LINE);
        sb.append("\"Court_Email\":\"").append(nullCheck(listingData.getTribunalCorrespondenceEmail())).append(NEW_LINE);
        return sb;
    }

    private static String nullCheck(String value) {
        return Optional.ofNullable(value).orElse("");
    }

    public static String getListingDocName(ListingData listingData) {
        if (listingData.getHearingDocType().equals(HEARING_DOC_ETCL) && listingData.getHearingDocETCL().equals(HEARING_ETCL_STAFF)) {
            return STAFF_CASE_CAUSE_LIST_TEMPLATE;
        } else if (listingData.getHearingDocType().equals(HEARING_DOC_ETCL) && listingData.getHearingDocETCL().equals(HEARING_ETCL_PUBLIC)) {
            return PUBLIC_CASE_CAUSE_LIST_TEMPLATE;
        } else if (listingData.getHearingDocType().equals(HEARING_DOC_ETCL) && listingData.getHearingDocETCL().equals(HEARING_ETCL_PRESS_LIST) &&
                listingData.getHearingDateType().equals(RANGE_HEARING_DATE_TYPE)) {
            return PRESS_LIST_CAUSE_LIST_SINGLE_TEMPLATE;
        } else if (listingData.getHearingDocType().equals(HEARING_DOC_ETCL) && listingData.getHearingDocETCL().equals(HEARING_ETCL_PRESS_LIST) &&
                !listingData.getHearingDateType().equals(RANGE_HEARING_DATE_TYPE)) {
            return PRESS_LIST_CAUSE_LIST_SINGLE_TEMPLATE;
        } else if (listingData.getHearingDocType().equals(HEARING_DOC_IT56)) {
            return STAFF_CASE_CAUSE_LIST_TEMPLATE;
        } else if (listingData.getHearingDocType().equals(HEARING_DOC_IT57)) {
            return STAFF_CASE_CAUSE_LIST_TEMPLATE;
        }
        return STAFF_CASE_CAUSE_LIST_TEMPLATE;
    }

    private static String getVenue(DateListedType dateListedType) {
        if (!isNullOrEmpty(dateListedType.getHearingGlasgow())) {
            return dateListedType.getHearingGlasgow();
        } else if (!isNullOrEmpty(dateListedType.getHearingAberdeen())) {
            return dateListedType.getHearingAberdeen();
        } else if (!isNullOrEmpty(dateListedType.getHearingDundee())) {
            return dateListedType.getHearingDundee();
        } else if (!isNullOrEmpty(dateListedType.getHearingEdinburgh())) {
            return dateListedType.getHearingEdinburgh();
        } return " ";
    }

    private static String getHearingRoom(DateListedType dateListedType) {
        if (!isNullOrEmpty(dateListedType.getHearingRoomGlasgow())) {
            return dateListedType.getHearingRoomGlasgow();
        } else if (!isNullOrEmpty(dateListedType.getHearingRoomCambeltown())) {
            return dateListedType.getHearingRoomCambeltown();
        } else if (!isNullOrEmpty(dateListedType.getHearingRoomDumfries())) {
            return dateListedType.getHearingRoomDumfries();
        } else if (!isNullOrEmpty(dateListedType.getHearingRoomOban())) {
            return dateListedType.getHearingRoomOban();
        } else if (!isNullOrEmpty(dateListedType.getHearingRoomFortWilliam())) {
            return dateListedType.getHearingRoomFortWilliam();
        } else if (!isNullOrEmpty(dateListedType.getHearingRoomKirkcubright())) {
            return dateListedType.getHearingRoomKirkcubright();
        } else if (!isNullOrEmpty(dateListedType.getHearingRoomLockmaddy())) {
            return dateListedType.getHearingRoomLockmaddy();
        } else if (!isNullOrEmpty(dateListedType.getHearingRoomPortree())) {
            return dateListedType.getHearingRoomPortree();
        } else if (!isNullOrEmpty(dateListedType.getHearingRoomStirling())) {
            return dateListedType.getHearingRoomStirling();
        } else if (!isNullOrEmpty(dateListedType.getHearingRoomStornowaySC())) {
            return dateListedType.getHearingRoomStornowaySC();
        } else if (!isNullOrEmpty(dateListedType.getHearingRoomStranraer())) {
            return dateListedType.getHearingRoomStranraer();
        } else if (!isNullOrEmpty(dateListedType.getHearingRoomAberdeen())) {
            return dateListedType.getHearingRoomAberdeen();
        } else if (!isNullOrEmpty(dateListedType.getHearingRoomLerwick())) {
            return dateListedType.getHearingRoomLerwick();
        } else if (!isNullOrEmpty(dateListedType.getHearingRoomRRShetland())) {
            return dateListedType.getHearingRoomRRShetland();
        } else if (!isNullOrEmpty(dateListedType.getHearingRoomStornoway())) {
            return dateListedType.getHearingRoomStornoway();
        } else if (!isNullOrEmpty(dateListedType.getHearingRoomWick())) {
            return dateListedType.getHearingRoomWick();
        } else if (!isNullOrEmpty(dateListedType.getHearingRoomInverness())) {
            return dateListedType.getHearingRoomInverness();
        } else if (!isNullOrEmpty(dateListedType.getHearingRoomKirkawall())) {
            return dateListedType.getHearingRoomKirkawall();
        } return " ";
    }

    public static String getVenueToSearch(ListingData listingData) {
        if (!isNullOrEmpty(listingData.getListingVenueOfficeGlas())) {
            return listingData.getListingVenueOfficeGlas();
        } else if (!isNullOrEmpty(listingData.getListingVenueOfficeAber())) {
            return listingData.getListingVenueOfficeAber();
        } return " ";
    }

    public static String getVenueFromDateListedType(DateListedType dateListedType) {
        if (!isNullOrEmpty(dateListedType.getHearingGlasgow())) {
            return dateListedType.getHearingGlasgow();
        } else if (!isNullOrEmpty(dateListedType.getHearingDundee())) {
            return dateListedType.getHearingDundee();
        } else if (!isNullOrEmpty(dateListedType.getHearingEdinburgh())) {
            return dateListedType.getHearingEdinburgh();
        } else if (!isNullOrEmpty(dateListedType.getHearingAberdeen())) {
            return dateListedType.getHearingAberdeen();
        } return " ";
    }

    private static String getRespOthersName(CaseData caseData) {
        if (caseData.getRespondentCollection() != null) {
            List<String> respOthers = caseData.getRespondentCollection()
                    .stream()
                    .skip(1)
                    .map(respondentSumTypeItem -> respondentSumTypeItem.getValue().getRespondentName())
                    .collect(Collectors.toList());
            return String.join("\\n", respOthers);
        } return " ";
    }

    private static String getCaseTotal(List<ListingTypeItem> listingTypeItems) {
        return String.valueOf(listingTypeItems
                .stream()
                .map(listingTypeItem -> listingTypeItem.getValue().getElmoCaseReference())
                .distinct()
                .count());
    }

    public static boolean getListingDateBetween(String dateToSearchFrom, String dateToSearchTo, String dateToSearch) {
        LocalDate localDateFrom = LocalDate.parse(dateToSearchFrom, OLD_DATE_TIME_PATTERN2);
        LocalDate localDate = LocalDate.parse(dateToSearch, OLD_DATE_TIME_PATTERN);
        if (dateToSearchTo.equals("")) {
            return localDateFrom.isEqual(localDate);
        } else {
            LocalDate localDateTo = LocalDate.parse(dateToSearchTo, OLD_DATE_TIME_PATTERN2);
            return (!localDate.isBefore(localDateFrom)) && (!localDate.isAfter(localDateTo));
        }
    }

}