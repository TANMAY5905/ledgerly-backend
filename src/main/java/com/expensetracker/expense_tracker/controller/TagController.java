package com.expensetracker.expense_tracker.controller;

import com.expensetracker.expense_tracker.entity.Tag;
import com.expensetracker.expense_tracker.service.TagService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService){
        this.tagService = tagService;
    }

    @PostMapping("/create")
    public Tag createTag(@RequestBody Tag tag){
        return tagService.createTag(tag);
    }

    @GetMapping("/all")
    public List<Tag> getTags(){
        return tagService.getAllTags();
    }
}
