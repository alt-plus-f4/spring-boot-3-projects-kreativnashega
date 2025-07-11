package com.mnb.controller;

import com.mnb.entity.Author;
import com.mnb.service.AuthorService;
import com.mnb.service.BookService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/author")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthorController {

    public static final Logger logger = LoggerFactory.getLogger(AuthorController.class);

    final BookService bookService;
    final AuthorService authorService;

    public AuthorController(BookService bookService, AuthorService authorService) {
        this.bookService = bookService;
        this.authorService = authorService;
    }


    @GetMapping("/list")
    public String listAuthors(Model theModel) {
        // get author from db
        List<Author> theAuthors = authorService.findAll();
        // add to the spring model
        theModel.addAttribute("authors", theAuthors);
        return "list-authors";
    }

    @GetMapping("/showFormForAdd")
    public String showFormForAdd(Model theModel) {
        // create model attribute to bind form data
        Author theAuthor = new Author();
        theModel.addAttribute("authors", theAuthor);
        return "author-form";
    }


    @GetMapping("/showFormForUpdate")
    public String showFormForUpdate(@RequestParam("authorId") int theID, Model theModel) {
        try {
            Author theAuthor = authorService.findById(theID);
            theModel.addAttribute("authors", theAuthor);
            return "author-form";
        } catch (RuntimeException e) {
            theModel.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }
    @PostMapping("/save")
    public String saveAuthor(@ModelAttribute("authors") Author theAuthor) {
        // save the author
        authorService.save(theAuthor);
        // use a redirect to prevent duplicate submissions
        return "redirect:/author/list";
    }

    @GetMapping("/delete")
    public String delete(@RequestParam("authorId") int theId) {
        // delete the author
        authorService.deleteById(theId);
        // redirect to /author/list
        return "redirect:/author/list";
    }
}
