package jpabook.jpashop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JpashopApplication {

    public static void main(String[] args) {
        Hello hello = new Hello();
        hello.setName("name");
        String name = hello.getName();
        System.out.println(name);
        SpringApplication.run(JpashopApplication.class, args);
    }

}
