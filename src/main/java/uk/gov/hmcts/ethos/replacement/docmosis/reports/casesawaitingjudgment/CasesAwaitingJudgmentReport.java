package uk.gov.hmcts.ethos.replacement.docmosis.reports.casesawaitingjudgment;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ecm.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.ecm.common.model.listing.ListingData;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.casesawaitingjudgment.ReportDetail.NO_MULTIPLE_REFERENCE;

public class CasesAwaitingJudgmentReport {

    static final Collection<String> VALID_POSITION_TYPES = List.of(
            "Draft with members",
            "Heard awaiting judgment being sent to the parties",
            "Awaiting judgment being sent to the parties, other",
            "Awaiting chairman's notes of evidence",
            "Awaiting draft judgment from chairman",
            "Draft judgment received, awaiting typing",
            "Draft judgment typed, to chairman for amendment",
            "Revised draft received, awaiting typing",
            "Fair copy, to chairman for signature",
            "Signed fair copy received",
            "Judgment photocopied, awaiting being sent to the parties",
            "Awaiting written reasons"
    );

    private final ReportDataSource reportDataSource;
    private final Clock clock;

    public CasesAwaitingJudgmentReport(ReportDataSource reportDataSource) {
        this(reportDataSource, Clock.systemDefaultZone());
    }

    public CasesAwaitingJudgmentReport(ReportDataSource reportDataSource, Clock clock) {
        this.reportDataSource = reportDataSource;
        this.clock = clock;
    }

    public CasesAwaitingJudgmentReportData runReport(ListingData listingData, String caseTypeId, String user) {
        var submitEvents = getCases(caseTypeId);

        var reportData = initReport(listingData, caseTypeId, user);
        populateData(reportData, submitEvents);

        return reportData;
    }

    private CasesAwaitingJudgmentReportData initReport(ListingData listingData, String caseTypeId, String user) {
        var office = UtilHelper.getListingCaseTypeId(caseTypeId);
        var reportSummary = new ReportSummary(office, user, LocalDate.now(clock));
        return new CasesAwaitingJudgmentReportData(listingData, reportSummary);
    }

    private List<SubmitEvent> getCases(String caseTypeId) {
        return reportDataSource.getData(caseTypeId);
    }

    private void populateData(CasesAwaitingJudgmentReportData reportData, List<SubmitEvent> submitEvents) {
        for (SubmitEvent submitEvent : submitEvents) {
            if (!isValidCase(submitEvent)) {
                continue;
            }

            var reportDetail = new ReportDetail();
            var caseData = submitEvent.getCaseData();
            var heardHearing = getLatestHeardHearing(caseData.getHearingCollection());
            LocalDate today = LocalDate.now(clock);
            LocalDate listedDate = LocalDate.parse(heardHearing.listedDate, OLD_DATE_TIME_PATTERN);

            reportDetail.setPositionType(caseData.getPositionType());
            reportDetail.setWeeksSinceHearing(getWeeksSinceHearing(listedDate, today));
            reportDetail.setDaysSinceHearing(getDaysSinceHearing(listedDate, today));
            reportDetail.setCaseNumber(caseData.getEthosCaseReference());
            if (MULTIPLE_CASE_TYPE.equals(caseData.getCaseType())) {
                reportDetail.setMultipleReference(caseData.getMultipleReference());
            } else {
                reportDetail.setMultipleReference(NO_MULTIPLE_REFERENCE);
            }

            reportDetail.setHearingNumber(heardHearing.hearingNumber);
            reportDetail.setHearingType(heardHearing.hearingType);
            reportDetail.setLastHeardHearingDate(heardHearing.listedDate);
            reportDetail.setJudge(heardHearing.judge);
            reportDetail.setCurrentPosition(caseData.getCurrentPosition());
            reportDetail.setDateToPosition(caseData.getDateToPosition());
            reportDetail.setConciliationTrack(caseData.getConciliationTrack());

            reportData.addReportDetail(reportDetail);
        }

        addReportSummary(reportData);
    }

    private boolean isValidCase(SubmitEvent submitEvent) {
        if (CLOSED_STATE.equals(submitEvent.getState())) {
            return false;
        }

        var caseData = submitEvent.getCaseData();
        if (!VALID_POSITION_TYPES.contains(caseData.getPositionType())) {
            return false;
        }

        if (!isCaseWithValidHearing(caseData)) {
            return false;
        }

        return isCaseAwaitingJudgment(caseData);
    }

    private boolean isCaseWithValidHearing(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getHearingCollection())) {
            return false;
        }

        for (var hearingTypeItem : caseData.getHearingCollection()) {
            if (isValidHearing(hearingTypeItem)) {
                return true;
            }

        }

        return false;
    }

    private boolean isValidHearing(HearingTypeItem hearingTypeItem) {
        var hearingType = hearingTypeItem.getValue();
        if (hearingType == null || CollectionUtils.isEmpty(hearingType.getHearingDateCollection())) {
            return false;
        }

        for (var dateListedItemType : hearingType.getHearingDateCollection()) {
            if (Constants.HEARING_STATUS_HEARD.equals(dateListedItemType.getValue().getHearingStatus())) {
                return true;
            }
        }

        return false;
    }

    private boolean isCaseAwaitingJudgment(CaseData caseData) {
        return CollectionUtils.isEmpty(caseData.getJudgementCollection());
    }

    private void addReportSummary(CasesAwaitingJudgmentReportData reportData) {
        var positionTypes = new HashMap<String, Integer>();
        reportData.getReportDetails().forEach(rd -> positionTypes.merge(rd.getPositionType(), 1, Integer::sum));

        reportData.getReportSummary().getPositionTypes().putAll(positionTypes);
    }

    private HeardHearing getLatestHeardHearing(List<HearingTypeItem> hearings) {
        var heardHearings = new ArrayList<HeardHearing>();
        for (var hearingTypeItem : hearings) {
            var hearingType = hearingTypeItem.getValue();
            for (var dateListedTypeItem : hearingType.getHearingDateCollection()) {
                var dateListedType = dateListedTypeItem.getValue();
                if (HEARING_STATUS_HEARD.equals(dateListedType.getHearingStatus())) {
                    var heardHearing = new HeardHearing();
                    heardHearing.listedDate = dateListedType.getListedDate();
                    heardHearing.hearingNumber = hearingType.getHearingNumber();
                    heardHearing.hearingType = hearingType.getHearingType();
                    heardHearing.judge = hearingType.getJudge();

                    heardHearings.add(heardHearing);
                }
            }
        }

        return Collections.max(heardHearings, Comparator.comparing(h -> h.listedDate));
    }

    private long getWeeksSinceHearing(LocalDate listedDate, LocalDate today) {
        return ChronoUnit.WEEKS.between(listedDate, today);
    }

    private long getDaysSinceHearing(LocalDate listedDate, LocalDate today) {
        return ChronoUnit.DAYS.between(listedDate, today);
    }

}

class HeardHearing {
    String listedDate;
    String hearingNumber;
    String hearingType;
    String judge;
}
