## Spring Boot 환경에서의 JWT 실습
## 참고
+ [inflearn 강의](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8-jwt)
+ [github code](https://github.com/SilverNine/spring-boot-jwt-tutorial)

## 프로젝트 구성
+ Project: Gradle Project
+ Language: Java11
+ Spring Boot: 2.7.1
+ Packaging: Jar
+ Dependencies : Spring Web, Spring Security, Spring Data JPA, H2 Database, Lombok, Validation

## IntelliJ에서 Lombok 사용시 설정 사항
+ Preferences > Annotation Processors 검색 > Enable annotation processing 활성화

## @EnableWebSecurity
+ config/SecurityConfig.class
+ 기본적인 Web 보안을 활성화하기 위한 어노테이션
+ 추가적인 설정이 필요한 경우 `WebSecurityConfigurer`를 implements 하거나 `WebSecurityConfigurerAdapter`를 extends 함
  + 강의에서는 `WebSecurityConfigurerAdapter`를 extends 했으나 Spring Boot 버전이 업그레이드 되면서 해당 클래스가 deprecated 됨
    + HttpSecurity에 대한 configure → `SecurityFilterChain`를 `@Bean`으로 등록
    + WebSecurity에 대한 configure → `WebSecurityCustomizer`를 `@Bean`으로 등록
+ 참고 
  + [Deprecated된 WebSecurityConfigurerAdapter, 어떻게 대처하지?](https://velog.io/@pjh612/Deprecated%EB%90%9C-WebSecurityConfigurerAdapter-%EC%96%B4%EB%96%BB%EA%B2%8C-%EB%8C%80%EC%B2%98%ED%95%98%EC%A7%80#httpsecurity-configure)
  + [[Spring Security] Config Refactoring](https://velog.io/@csh0034/Spring-Security-Config-Refactoring)

## @EnableGlobalMethodSecurity
+ config/SecurityConfig
+ MethodSecurity : 메소드 수준에서 권한을 제어할 수 있도록 하는 어노테이션
+ `@EnableGlobalMethodSecurity(prePostEnabled = true)` 와 같이 옵션 추가 가능
  + prePostEnabled : Spring Security의 @PreAuthorize, @PreFilter, @PostAuthorize, @PostFilter어노테이션 활성화 여부
  + securedEnabled : @Secured 어노테이션 활성화 여부
  + jsr250Enabled : @RoleAllowed 어노테이션 사용 활성화 여부
+ @Secured("ROLE_ADMIN") 는 @PreAuthorize("hasRole('ROLE_ADMIN')") 과 동일한 의미
  + hasRole, hasAnyRole은 기본적으로 제공하는 메서드로 관련 내용은 [여기를 참고](https://docs.spring.io/spring-security/site/docs/3.0.x/reference/el-access.html#el-common-built-in)
+ 참고
  + [Spring Security @PreAuthorize 사용하기](https://gaemi606.tistory.com/entry/Spring-Boot-Spring-Security-PreAuthorize%EC%82%AC%EC%9A%A9%ED%95%98%EA%B8%B0)

## JWT 와 Security 설정
+ `TokenProvider` : 유저 정보로 JWT 토큰을 만들거나 토큰을 바탕으로 유저 정보를 가져옴
+ `JwtFilter` : Spring Request 앞단에 붙일 Custom Filter
+ `SecurityConfig` : 스프링 시큐리티에 필요한 설정
+ `JwtSecurityConfig` : TokenProvider 클래스를 주입한 JwtFilter 클래스를 Security 로직에 등록
+ `JwtAuthenticationEntryPoint` : 인증 정보 없을 때 401 에러
+ `JwtAccessDeniedHandler` : 접근 권한 없을 때 403 에러
+ `SecurityUtil` : jwtFilter 에서 SecurityContext 에 세팅한 유저 정보를 꺼냄

+ 각 설정에 대한 설명은 [여기를 참고](https://bcp0109.tistory.com/301)

## 이슈 사항
### 1. DB table 생성이 정상적으로 되지 않음
+ 테이블명 user → h2 2.1.212 버전 이후 예약어로 지정됨
  1) h2 버전을 1.4.200 이하로 변경
  2) properties.yml → `spring.datasource.url`에 `NON_KEYWORDS=USER` 설정 추가
  3) 테이블 이름 user를 users로 바꾸기
     1) @Table(name = "users")
     2) data.sql의 user -> users로 변경
+ 참고 : https://www.inflearn.com/questions/546219
+ 추가로 h2 console 접속 주소 : http://localhost:8080/h2-console

### 2. DB table 생성 전 query insert 가 진행 됨
+ Spring Boot 2.5 이후 부터 `data.sql` 이 먼저 실행됨
  + `spring.jpa.defer-datasource-initialization: true` 옵션 추가
+ 참고 : https://zzang9ha.tistory.com/371
