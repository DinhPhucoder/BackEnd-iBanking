package com.example.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 🚀 MAIN CLASS - ĐIỂM KHỞI ĐẦU CỦA ỨNG DỤNG
 * 
 * @SpringBootApplication: Annotation này bao gồm:
 * - @Configuration: Đánh dấu class này là cấu hình
 * - @EnableAutoConfiguration: Tự động cấu hình Spring Boot
 * - @ComponentScan: Tự động quét và tạo các bean
 */
@SpringBootApplication
public class UserServiceApplication {

    /**
     * 🎯 PHƯƠNG THỨC MAIN - NƠI BẮT ĐẦU CHẠY ỨNG DỤNG
     * 
     * @param args: Tham số dòng lệnh (không sử dụng trong trường hợp này)
     * 
     * SpringApplication.run(): Khởi động Spring Boot application
     * - Tự động cấu hình database, security, web server
     * - Quét tất cả các @Component, @Service, @Repository
     * - Khởi động Tomcat server trên port 8081
     */
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

}
