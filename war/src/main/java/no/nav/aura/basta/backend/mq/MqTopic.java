package no.nav.aura.basta.backend.mq;

public class MqTopic {
    
    private String name;
    private String topicString;
    private String description;
    
    public MqTopic(String name, String topicString) {
        this.name = name;
        this.topicString = topicString;
    }

    public String getName() {
        return name;
    }

    public String getTopicString() {
        return topicString;
    }

    @Override
    public String toString() {
        return "MqTopic [name=" + name + ", topicString=" + topicString + "]";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
}
