package greenhouse.config;

/**
 * Global application settings.
 */
public class GreenhouseSettings {

    private String mvn = System.getProperty("os.name").startsWith("Windows") ? "mvn.bat" : "mvn";

    public String getMvn() {
        return mvn;
    }

    public void setMvn(String mvn) {
        this.mvn = mvn;
    }

}
