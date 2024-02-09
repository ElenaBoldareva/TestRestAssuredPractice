package api;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import specification.Specifications;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class ReqresNoPojoTest {
    private final static String URL = "https://reqres.in/";

    @Test
    public void checkAvatarNoPojoTest() {
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecOK200());
        Response response = given()
                .when()
                .get("api/users?page=2")
                .then().log().all()
                .body("page", equalTo(2))
                .body("data.id", notNullValue())
                .body("data.email", notNullValue())
                .body("data.last_name", notNullValue())
                .body("data.avatar", notNullValue())
                .extract().response();
        JsonPath jsonPath = response.jsonPath();
        List<String> emails = jsonPath.get("data.email");
        List<Integer> ids = jsonPath.get("data.id");
        List<String> avatars = jsonPath.get("data.avatar");
        for (int i = 0; i < avatars.size(); i++) {
            Assertions.assertTrue(avatars.get(i).contains(ids.get(i).toString()));
        }
        Assertions.assertTrue(emails.stream().allMatch(x -> x.endsWith("reqres.in")));
    }


    @Test
    public void successRegNoPojoTest() {
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecOK200());
        Map<String, String> users = new HashMap<>();
        users.put("email", "eve.holt@reqres.in");
        users.put("password", "pistol");
        //1 way
//        given()
//                .body(users)
//                .when()
//                .post("api/register")
//                .then().log().all()
//                .body("id", equalTo("4"))
//                .body("token", equalTo("QpwL5tke4Pnpja7X4"));
        // 2 way
        Response response = given()
                .body(users)
                .when()
                .post("api/register")
                .then().log().all()
                .extract().response();
        JsonPath jsonPath = response.jsonPath();
        int id = jsonPath.get("id");
        String token = jsonPath.get("token");
        Assertions.assertEquals(4, id);
        Assertions.assertEquals("QpwL5tke4Pnpja7X4", token);
    }

    @Test
    public void unSuccessRegNoPojoTest() {
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpec400());
        Map<String, String> users = new HashMap<>();
        users.put("email", "sydney@fife");
        Response response = given()
                .body(users)
                .when()
                .post("api/register")
                .then().log().all()
                .extract().response();
        JsonPath jsonPath = response.jsonPath();
        String error = jsonPath.get("error");
        Assertions.assertEquals("Missing password", error);

        //2 way
//        given()
//                .body(users)
//                .when()
//                .post("api/register")
//                .then().log().all()
//                .body("error", equalTo("Missing password"));
    }
}
