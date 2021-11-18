package uk.gov.hmcts.ethos.replacement.functional.dataloader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.config.SSLConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.junit.runners.SerenityRunner;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.functional.ComponentTest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.useRelaxedHTTPSValidation;
import static uk.gov.hmcts.ethos.replacement.functional.util.ResponseUtil.getProperty;

@TestPropertySource(locations = "classpath:config.properties")
@RunWith(SerenityRunner.class)
public class DataLoader {

    private String AUTH_TOKEN = null;
    private String environment = "demo";
    private CCDReq ccdReq;

    private CaseDetails caseDetails1;

    @Before
    public void setUp() throws Exception {
        useRelaxedHTTPSValidation();
        baseURI = getProperty(environment + ".docmosis.api.url");
        ccdReq = new CCDReq();
//        caseDetails1 = generateCaseDetails("dataLoader.json");
    }

    @Test
    @Category(ComponentTest.class)
    public void createCase() throws IOException, URISyntaxException {
        CCDRequest ccdRequest = ccdReq.ccdRequest();
        AUTH_TOKEN = loadAuthToken();
        System.out.println(ccdRequest);
        for (int i = 0; i < 1; i++) {
            RestAssured.given()
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .body(ccdRequest)
                .post("/createCase")
                .then()
                .statusCode(HttpStatus.SC_OK);

        }
    }

    public String loadAuthToken() throws IOException {
        if (AUTH_TOKEN == null) {
            AUTH_TOKEN = getAuthToken();
        }
        if (!AUTH_TOKEN.startsWith("Bearer")) {
            AUTH_TOKEN = "Bearer " + AUTH_TOKEN;
        }
        return AUTH_TOKEN;
    }

    public String getAuthToken() throws IOException {
        RestAssured.config = RestAssuredConfig.config().sslConfig(SSLConfig.sslConfig().allowAllHostnames());
        RequestSpecification httpRequest = RestAssured.given();
        httpRequest.header("Accept", "*/*");
        httpRequest.header("Content-Type", "application/x-www-form-urlencoded");
        httpRequest.formParam("username", "employment_service@mailinator.com"); // ecmdev@mailinator.com employment_service@mailinator.com
        httpRequest.formParam("password",  "Nagoya0102"); // Pa55word11 Nagoya0102
        Response response = httpRequest.post(getProperty(environment + ".idam.auth.url"));
        return response.body().jsonPath().getString("access_token");
    }

    private CaseDetails generateCaseDetails(String jsonFileName) throws IOException, URISyntaxException {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader()
                .getResource(jsonFileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }
}
