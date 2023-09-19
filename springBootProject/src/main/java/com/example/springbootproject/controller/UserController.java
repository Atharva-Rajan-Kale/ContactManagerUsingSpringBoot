package com.example.springbootproject.controller;

import com.example.springbootproject.dao.ContactRepository;
import com.example.springbootproject.dao.Repository;
import com.example.springbootproject.entities.Contact;
import com.example.springbootproject.entities.User;
import com.example.springbootproject.helper.Message;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private Repository repository;
    @Autowired
    private ContactRepository contactRepository;

    @ModelAttribute
    public void addCommonData(Model model,Principal principal){
        String username=principal.getName();
        User user=repository.findByEmail(username);
        model.addAttribute("user", user);
    }
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal, HttpSession session){
        session.removeAttribute("message");
        model.addAttribute("title","User Dashboard");
        return "normal/user_dashboard";
    }

    @GetMapping("/add-contact")
    public String openAddContactForm(Model m){
        m.addAttribute("title","Add Contact");
        m.addAttribute("contact",new Contact());
        return "normal/add_contact_form";
    }

    @PostMapping("/process-contact")
    public String processContact(@ModelAttribute Contact contact,
                                 @RequestParam("profileImage") MultipartFile file,
                                 Principal principal,HttpSession session){
        try{
            String name=principal.getName();
            User user=this.repository.findByEmail(name);

            if(file.isEmpty()){
                System.out.println("file is empty");
                contact.setImage("contact.png");
            }
            else {
                contact.setImage(file.getOriginalFilename());
                File file1=new ClassPathResource("static/img").getFile();
                Path path=Paths.get(file1.getAbsolutePath()+File.separator+file.getOriginalFilename());
                Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
            }
            contact.setUser(user);
            user.getContacts().add(contact);
            this.repository.save(user);
            session.setAttribute("message",new Message("Your contact is added!! You can add more","success"));
        }
        catch (Exception e){
            System.out.println("ERROR "+e.getMessage());
            e.printStackTrace();
            session.setAttribute("message",new Message("Something went wrong!! Try again","danger"));

        }
        return "normal/add_contact_form";
    }
    @GetMapping("/show-contacts/{page}")
    public String showContacts(@PathVariable("page") Integer page, Model m,Principal principal){
        m.addAttribute("title","User Contacts");
        String userName=principal.getName();
        User user=this.repository.findByEmail(userName);
        Pageable pageable=PageRequest.of(page,7);
        Page<Contact> contacts=this.contactRepository.findContactByUser(user.getId(),pageable);
        m.addAttribute("contacts",contacts);
        m.addAttribute("currentPage",page);

        m.addAttribute("totalPages",contacts.getTotalPages());
        return "normal/show_contacts";
    }
    @GetMapping("/{cid}/contact")
    public String showContactDetails(@PathVariable("cid") Integer cid,Model model,Principal principal){

        Optional<Contact> contactOptional=this.contactRepository.findById(cid);
        Contact contact=contactOptional.get();
        String userName=principal.getName();
        User user=this.repository.findByEmail(userName);
        if(user.getId()==contact.getUser().getId()){
            model.addAttribute("contact",contact);
        }
        return "normal/contact_detail";
    }
    @GetMapping("/delete/{cid}")
    public String deleteContact(@PathVariable("cid")Integer cid,HttpSession session,Principal principal){
        Optional<Contact> contactOptional=this.contactRepository.findById(cid);
        Contact contact=contactOptional.get();
        User user=this.repository.findByEmail(principal.getName());
        user.getContacts().remove(contact);
        this.repository.save(user);
        File delfile;
        try {
            delfile = new ClassPathResource("/static/img").getFile();
            File f1= new File(delfile,contactOptional.get().getImage());
            f1.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
        session.setAttribute("message",new Message("Contact deleted successfully...","success"));
        return "redirect:/user/show-contacts/0";

    }
    @PostMapping("/update-contact/{cid}")
    public String updateForm(@PathVariable("cid")Integer cid,Model m)
    {
        m.addAttribute("title","Update Contact");
        Contact contact=this.contactRepository.findById(cid).get();
        m.addAttribute("contact",contact);
        return "normal/update_form";
    }
    @PostMapping("/process-update")
    public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model m,HttpSession session,Principal principal)
    {
        try{
            Contact oldContact=this.contactRepository.findById(contact.getCid()).get();
            if(!file.isEmpty()){

                File deleteFile=new ClassPathResource("static/img").getFile();
                File file2=new File(deleteFile,oldContact.getImage());
                file2.delete();

                File file1=new ClassPathResource("static/img").getFile();
                Path path=Paths.get(file1.getAbsolutePath()+File.separator+file.getOriginalFilename());
                Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
                contact.setImage(file.getOriginalFilename());
            }
            else{
                contact.setImage(oldContact.getImage());
            }
            User user= this.repository.findByEmail(principal.getName());

            contact.setUser(user);
            this.contactRepository.save(contact);
            session.setAttribute("message",new Message("Your contact is updated...","success"));
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return "redirect:/user/"+contact.getCid()+"/contact";
    }
    @GetMapping("/profile")
    public String yourProfile(Model model){
        model.addAttribute("title","Profile Page");
        return "normal/profile";
    }
    @GetMapping("/settings")
    public String openSettings(){
        return "normal/settings";
    }
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword")String newPassword,Principal principal,HttpSession session){
        String userName=principal.getName();
        User currentUser=this.repository.findByEmail(userName);
        if(this.bCryptPasswordEncoder.matches(oldPassword,currentUser.getPassword())){
            currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
            this.repository.save(currentUser);
            session.setAttribute("message",new Message("Your password has been successfully changed...","success"));
        }
        else {
            session.setAttribute("message",new Message("Please enter the correct password...","danger"));
            return "redirect:/user/settings";
        }
        return "redirect:/user/dashboard";
    }


}
