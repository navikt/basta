package no.nav.aura.basta.rest.dataobjects;

import java.util.HashMap;
import java.util.Map;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ResultDO implements Comparable<ResultDO> {

    private String resultName;
    private Map<String, String> details;
    private String description;

    public ResultDO() {

    }

    public ResultDO(String resultName) {
        this.resultName = resultName;
        this.details = new HashMap<>();
    }

    public String getResultName() {
        return resultName;
    }

    public void setResultName(String resultName) {
        this.resultName = resultName;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }

    @Override
    public int compareTo(ResultDO o) {
        return resultName.compareTo(o.getResultName());
    }

    public void addDetail(String key, String value) {
        details.put(key, value);
    }

    public String getDetail(String key) {
        return details.get(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ResultDO resultDO = (ResultDO) o;

        if (details != null ? !details.equals(resultDO.details) : resultDO.details != null)
            return false;
        if (!resultName.equals(resultDO.resultName))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = resultName.hashCode();
        result = 31 * result + (details != null ? details.hashCode() : 0);
        return result;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
