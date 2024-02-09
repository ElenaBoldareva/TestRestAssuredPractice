package api;

import color.ColorsData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import registration.Register;
import registration.RegisterSuccessful;
import registration.RegisterUnSuccessful;
import specification.Specifications;
import users.UserData;
import users.UserTime;
import users.UserTimeResponse;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReqresTest {
    private final static String URL = "https://reqres.in/";


    /**
     *  get list of users from second page
     *  make sure the user ID is included in their Avatar
     *  make sure that the user's email has an ending reqres.in
     **/
    @Test
    public void checkAvatarAndIdTest() {
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecOK200());
//        1 way
        List<UserData> users = given().
                when()
//                .contentType(ContentType.JSON)
//                .get(URL + "api/users?page=2")
                .get("api/users?page=2")
                .then().log().all()
                .extract().body().jsonPath().getList("data", UserData.class);

        users.forEach(x -> Assertions.assertTrue(x.getAvatar().contains(x.getId().toString())));
        Assertions.assertTrue(users.stream().allMatch(x -> x.getEmail().endsWith("@reqres.in")));
//          2 way
        List<String> avatars = users.stream().map(UserData::getAvatar).collect(Collectors.toList());
        List<String> ids = users.stream().map(x -> x.getId().toString()).collect(Collectors.toList());
        for (int i = 0; i < avatars.size(); i++) {
            Assertions.assertTrue(avatars.get(i).contains(ids.get(i)));
        }
    }

    @Test
    /**     Successful registration     **/

    public void successRegTest() {
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecOK200());
        Integer id = 4;
        String token = "QpwL5tke4Pnpja7X4";
        Register user = new Register("eve.holt@reqres.in", "pistol");
        RegisterSuccessful registerSuccessful = given()
                .body(user)
                .when()
                .post("api/register")
                .then().log().all()
                .extract().as(RegisterSuccessful.class);
        Assertions.assertNotNull(registerSuccessful.getId());
        Assertions.assertNotNull(registerSuccessful.getToken());

        assertEquals(id, registerSuccessful.getId());
        assertEquals(token, registerSuccessful.getToken());

    }

    @Test
    /**     Unsuccessful registration     **/

    public void unSuccessRegTest() {
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpec400());
        String message = "Missing password";
        Register user = new Register("eve.holt@reqres.in", "");
        RegisterUnSuccessful unSuccessful = given()
                .body(user)
                .when()
                .post("api/register")
                .then().log().all()
                .extract().as(RegisterUnSuccessful.class);
        assertEquals(message, unSuccessful.getError());

    }

    @Test
    /**Check if the list is sorted correctly**/

    public void checkSortList() {
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecOK200());
        List<ColorsData> colors = given()
                .when()
                .get("api/unknown")
                .then().log().all()
                .extract().body().jsonPath().getList("data", ColorsData.class);
        List<Integer> years = colors.stream().map(ColorsData::getYear).collect(Collectors.toList());
        List<Integer> sortYears = years.stream().sorted().collect(Collectors.toList());
        Assertions.assertEquals(sortYears, years);
    }

    @Test
    public void deleteUserTest() {
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecUnique(204));
        given()
                .when()
                .delete("api/users/2")
                .then().log().all();
    }

    @Test
    public void timeTest() {
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecUnique(200));
        UserTime userTime = new UserTime("morpheus", "zion resident");
        UserTimeResponse userTimeResponse = given()
                .body(userTime)
                .when()
                .put("api/users/2")
                .then().log().all()
                .extract().as(UserTimeResponse.class);

        String currentTime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss", Locale.getDefault()).format(LocalDateTime.now(Clock.systemUTC()));
        String regex = "(.{5})$";
        System.out.println(currentTime);
        assertEquals(currentTime, userTimeResponse.getUpdatedAt().replaceAll(regex, ""));
        System.out.println(userTimeResponse.getUpdatedAt().replaceAll(regex, ""));
    }
}
