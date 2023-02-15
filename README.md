# keycloak-client-authcheck

## 개요

- Security 없이 Webflux Filter와 Annotation을 사용해 권한을 확인한다.
- Keycloak에서 인증이 완료되었다는 확신 아래 수행된다.
    - JWT로 수행.
        - JWT 검증을 하고싶다면, Keycloak의 공개키만 yml에 설정하면 된다.
    - Keycloak은 role까지만 넘겨주기 때문에, permission 단위의 매핑을 원한다면 직접 DB를 조회해야한다.