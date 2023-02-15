package com.example.client.filter.token;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class Token {
    @JsonProperty("realm_access")
    private RealmAccess realmAccess;

    @JsonProperty("resource_access")
    private ResourceAccess resourceAccess;

    @JsonProperty("preferred_username")
    private String userName;

    /**
     *  자신이 가진 모든 Role을 Set으로 넘겨준다.
     * @return set
     */
    public Set<String> getAllRoles(){
        Set<String> set = new HashSet<>();
        
        set.addAll(getRealmAccess().getRoles());
        set.addAll(getResourceAccess().getAccount().getRoles());
        set.addAll(getResourceAccess().getRealmManagement().getRoles());

        return set;
    }
}
