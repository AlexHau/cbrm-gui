package dke.cbrm;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import dke.cbrm.business.statemachine.StateMachineJpaConfigurationInitializer;

@SpringBootApplication
@EnableJpaRepositories
@EntityScan(value = { "dke.cbrm.persistence.model", "dke.cbrm.business", "dke.cbrm.business.statemachine" })
@EnableAutoConfiguration
public class CbrmApplication {

    public static void main(String[] args) {
	// startH2Server();
	SpringApplication.run(CbrmApplication.class);
    }

    // @Bean(name = "OBJECT_MAPPER_BEAN")
    // public ObjectMapper jsonObjectMapper() {
    // return Jackson2ObjectMapperBuilder.json()
    // .serializationInclusion(JsonInclude.Include.NON_NULL) // Don’t
    // include null values
    // .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    // //ISODate
    // .modules(new JavaTimeModule())
    // .build();
    // }

    // private static void startH2Server() {
    // try {
    // Server h2Server = Server.createTcpServer()
    // .start();
    // if (h2Server.isRunning(true)) {
    // log.info(
    // "H2 server was started and is running.");
    // } else {
    // throw new RuntimeException(
    // "Could not start H2 server.");
    // }
    // } catch (SQLException e) {
    // throw new RuntimeException(
    // "Failed to start H2 server: ", e);
    // }
    // }

    // @Bean
    // public ServletRegistrationBean h2servletRegistration() {
    // ServletRegistrationBean registration = new ServletRegistrationBean(
    // new WebServlet());
    // registration.addUrlMappings("/console/*");
    // return registration;
    // }

    // @Bean
    // public CommandLineRunner parseFloraFiles(FloraFileParser
    // floraFileParser) {
    // return (args) -> {
    // floraFileParser.readFileContentsFromFolder();
    // };
    // }
    //
    // @Bean
    // @DependsOn("parseFloraFiles")
    // public CommandLineRunner parseFloraOutput(
    // FloraCommandOutputParser floraCommandOutputParser) {
    // return (args) -> {
    // floraCommandOutputParser.runOutputParser();
    // };
    // }
    //
    // @Bean
    // @DependsOn("parseFloraOutput")
    // public CommandLineRunner getDetParamValues(
    // FloraFileParser floraFileParser) {
    // return (args) -> {
    // floraFileParser.relateDetParamValuesWithParameters();
    // };
    // }
    //
    // @Bean
    // // @DependsOn("getDetParamValues")
    // public CommandLineRunner createUsers(
    // UserRolePrivilegeInitializer initializer) {
    // return (args) -> {
    // initializer.initializeUsers();
    // };
    // }
    //
    // @Bean
    // @DependsOn("createUsers")
    // public CommandLineRunner createStates(
    // StateMachineJpaConfigurationInitializer initializer) {
    // return (args) -> {
    // initializer.initializeJpaStateMachineRepository();
    // };
    // }
}
