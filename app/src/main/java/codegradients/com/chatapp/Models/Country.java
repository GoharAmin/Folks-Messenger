package codegradients.com.chatapp.Models;

public class Country {
    private String code;
    private  String name;
    private  String dailCode;

    public Country() {
    }

    public Country(String code, String name, String dailCode) {
        this.code = code;
        this.name = name;
        this.dailCode = dailCode;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDailCode() {
        return dailCode;
    }

    @Override
    public String toString() {
        return getName() + " (" + getDailCode() + ")";
    }
}
