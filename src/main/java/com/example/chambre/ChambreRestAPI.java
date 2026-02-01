package com.example.chambre;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;

@RestController
@RequestMapping("/chambres")
public class ChambreRestAPI {
    private String title="Hello, i'm the candidate Micro-Service";
    @RequestMapping("/hello")
    public String sayHello(){
        System.out.println(title);
        return title;
    }

    @Autowired
    DataSource dataSource;
    @GetMapping("/db-info")
    public String getDbInfo() throws Exception {
        return dataSource.getConnection().getMetaData().getDatabaseProductName();
    }
}