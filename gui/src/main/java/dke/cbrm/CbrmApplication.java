package dke.cbrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
@EntityScan(value = { "dke.cbrm.persistence.model", "dke.cbrm.business", "dke.cbrm.business.statemachine" })
@EnableAutoConfiguration
public class CbrmApplication {

    public static void main(String[] args) {
	SpringApplication.run(CbrmApplication.class);
    }

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
