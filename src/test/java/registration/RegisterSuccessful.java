package registration;

public class RegisterSuccessful {

    private Integer id;
    private String token;

    public Integer getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public RegisterSuccessful(Integer id, String token) {
        this.id = id;
        this.token = token;
    }

    public RegisterSuccessful() {
    }
}

