package nl._42.database.truncator.config;

import nl._42.database.truncator.TruncationStrategy;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "databaseTruncator", ignoreUnknownFields = false)
public class DatabaseTruncatorProperties {

    private List<String> exclude = new ArrayList<>();

    private TruncationStrategy strategy;

    private Boolean resetSequences = false;

    public List<String> getExclude() {
        return exclude;
    }

    public void setExclude(List<String> exclude) {
        this.exclude = exclude;
    }

    public TruncationStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(TruncationStrategy strategy) {
        this.strategy = strategy;
    }

    public Boolean getResetSequences() {
        return resetSequences;
    }

    public void setResetSequences(Boolean resetSequences) {
        this.resetSequences = resetSequences;
    }
}
