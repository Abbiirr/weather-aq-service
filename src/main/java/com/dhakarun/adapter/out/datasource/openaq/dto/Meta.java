package com.dhakarun.adapter.out.datasource.openaq.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = MetaDeserializer.class)
public record Meta(
    @JsonProperty("found") Integer found,
    @JsonProperty("limit") Object limit,
    @JsonProperty("page") Integer page
) {
    public Integer getLimitAsInteger() {
        if (limit instanceof Integer) {
            return (Integer) limit;
        } else if (limit instanceof String) {
            String limitStr = (String) limit;
            // Handle the ">1000" case by extracting the numeric part
            if (limitStr.startsWith(">")) {
                try {
                    return Integer.parseInt(limitStr.substring(1));
                } catch (NumberFormatException e) {
                    // Return null or a default value if parsing fails
                    return null;
                }
            }
            // Handle other string cases
            try {
                return Integer.parseInt(limitStr);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
