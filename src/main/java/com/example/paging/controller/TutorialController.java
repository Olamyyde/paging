package com.example.paging.controller;


import com.example.paging.model.Tutorial;
import com.example.paging.repo.TutorialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static org.springframework.data.domain.Sort.*;


@RestController
@RequestMapping("/api")
public class TutorialController {
    @Autowired
    TutorialRepository tutorialRepository;

    private Sort.Direction getSortDirection(String direction) {
        if (direction.equals("asc")) {
            return Sort.Direction.ASC;
        } else if (direction.equals("desc")) {
            return Sort.Direction.DESC;
        }

        return Sort.Direction.ASC;
    }

    @GetMapping("/tutorials")
    public ResponseEntity<Map<String, Object>> getAllTutorialsPage(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "id, desc") String[] sort){

        try {
            List<Order> orders = new ArrayList<Order>();

            if ( sort[0].contains(",")) {
                // will sort more than 2 fields
                // sortOrder="field, direction"
                for (String sortOrder : sort) {
                    String[] _sort = sortOrder.split(",");
                    orders.add(new Order(getSortDirection(_sort[1]), _sort[0]));
                }
            } else {
                // sort=[field, direction]
                orders.add(new Order(getSortDirection(sort[1]), sort[0]));
            }

            List<Tutorial> tutorials = new ArrayList<Tutorial>();
            Pageable pagingSort = PageRequest.of(page, size, Sort.by(orders));

            Page<Tutorial> pageTuts;
            if (title == null)
                pageTuts = tutorialRepository.findAll(pagingSort);
            else
                pageTuts = tutorialRepository.findByTitleContaining(title, pagingSort);

            tutorials = pageTuts.getContent();

            Map<String, Object> response = new HashMap<>();
            response.put("tutorials", tutorials);
            response.put("currentPage", pageTuts.getNumber());
            response.put("totalItems", pageTuts.getTotalElements());
            response.put("totalPages", pageTuts.getTotalPages());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }

    @GetMapping("tutorials/published")
    public ResponseEntity<Map<String, Object>> findByPublished(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size){
        try {
            List<Tutorial> tutorials = new ArrayList<>();
            Pageable pageable = PageRequest.of(page, size);

            Page<Tutorial> page1 = tutorialRepository.findByPublished(true, pageable);
            tutorials = page1.getContent();

            Map<String, Object> response = new HashMap<>();
            response.put("tutorials", tutorials);
            response.put("currentPage", page1.getNumber());
            response.put("totalItems", page1.getTotalElements());
            response.put("totalPages", page1.getTotalPages());

            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }



    }



    @GetMapping("/tutorials/{id}")
    public ResponseEntity<Tutorial> getTutorialById(@PathVariable("id") long id){
        Optional<Tutorial> tutorialData = tutorialRepository.findById(id);

        if (tutorialData.isPresent())
            return new ResponseEntity<>(tutorialData.get(), HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


    @PostMapping("/tutorials")
    public ResponseEntity<Tutorial> createTutorial(@RequestBody Tutorial tutorial){
        try {
            Tutorial tutorial1 = tutorialRepository.save(new Tutorial(tutorial.getTitle(), tutorial.getDescription(), false));
            return new ResponseEntity<>(tutorial1, HttpStatus.CREATED);
        }catch (Exception e){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/tutorials/{id}")
    public ResponseEntity<Tutorial> updateTutorial(@PathVariable("id") long id, @RequestBody Tutorial tutorial){
        Optional<Tutorial> tutorialData = tutorialRepository.findById(id);

        if (tutorialData.isPresent()){
            Tutorial tut = tutorialData.get();
            tut.setTitle(tut.getTitle());
            tut.setDescription(tut.getDescription());
            tut.setPublished(tut.isPublished());
            return new ResponseEntity<>(tutorialRepository.save(tut), HttpStatus.OK);
        }else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }


    @DeleteMapping("/tutorials/{id}")
    public ResponseEntity<HttpStatus> deleteTutorial(@PathVariable("id") long id){
        try {
            tutorialRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/tutorials")
    public ResponseEntity<HttpStatus> deleteAllTutorials(long id){
        try {
            tutorialRepository.deleteAll();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }







}
