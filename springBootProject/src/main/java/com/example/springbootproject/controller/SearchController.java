package com.example.springbootproject.controller;

import com.example.springbootproject.dao.ContactRepository;
import com.example.springbootproject.dao.Repository;
import com.example.springbootproject.entities.Contact;
import com.example.springbootproject.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
public class SearchController {
    @Autowired
    private Repository repository;
    @Autowired
    private ContactRepository contactRepository;

    @GetMapping("/search/{query}")
    public ResponseEntity<?> search(@PathVariable("query")String query,Principal principal){
        User user=this.repository.findByEmail(principal.getName());
        List<Contact> contacts=this.contactRepository.findByNameContainingAndUser(query,user);
        return ResponseEntity.ok(contacts);
    }
}
