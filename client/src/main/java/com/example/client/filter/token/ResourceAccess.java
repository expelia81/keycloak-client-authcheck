package com.example.client.filter.token;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResourceAccess {
    @JsonProperty("account")
    private Account account;

    @JsonProperty("realm-management")
    private RealmManagement realmManagement;

    @Getter
    public static class RealmManagement {
        private Set<String> roles = new HashSet<>();
    }

    @Getter
    public static class Account {
        private Set<String> roles = new HashSet<>();
    }
}
