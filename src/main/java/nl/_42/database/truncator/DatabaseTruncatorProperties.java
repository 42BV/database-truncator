package nl._42.database.truncator;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "databaseTruncator", ignoreUnknownFields = false)
public class DatabaseTruncatorProperties {

    private List<String> exclude = new ArrayList<>();

    public List<String> getExclude() {
        return exclude;
    }

    public void setExclude(List<String> exclude) {
        this.exclude = exclude;
    }

}
