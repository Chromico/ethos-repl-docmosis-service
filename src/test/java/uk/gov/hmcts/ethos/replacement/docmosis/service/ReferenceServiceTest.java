package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.Reference;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ReferenceRepository;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class ReferenceServiceTest {

    @InjectMocks
    private ReferenceService referenceService;
    @Mock
    private ReferenceRepository referenceRepository;

    private Reference reference;
    private String caseId;

    @Before
    public void setUp() {
        caseId = "1232132";
        reference = new Reference(caseId);
    }

    @Test
    public void createReference() {
        when(referenceRepository.save(isA(Reference.class))).thenReturn(reference);
        assertEquals(referenceService.createReference(caseId), reference);
        assertEquals(referenceService.createReference(caseId).getCaseId(), caseId);
    }

}