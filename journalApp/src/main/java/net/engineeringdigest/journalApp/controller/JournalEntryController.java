package net.engineeringdigest.journalApp.controller;

import net.engineeringdigest.journalApp.entity.JournalEntry;
import net.engineeringdigest.journalApp.entity.User;
import net.engineeringdigest.journalApp.service.JournalEntryService;
import net.engineeringdigest.journalApp.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/journal")
public class JournalEntryController {

    @Autowired
    private JournalEntryService journalEntryService;
    @Autowired
    private UserService userService;

    @GetMapping("{username}")
    public ResponseEntity<List<JournalEntry>> getAll(@PathVariable String username) {
        try {
            User userInDb = userService.findByUsername(username);
            List<JournalEntry> all = userInDb.getJournalEntries();
            if (all != null && !all.isEmpty()) {
                return new ResponseEntity<>(all, HttpStatus.OK);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("{username}")
    public ResponseEntity<JournalEntry> createEntry(@RequestBody JournalEntry myEntry, @PathVariable String username) {
        try {
            journalEntryService.saveEntry(myEntry, username);
            return new ResponseEntity<>(myEntry, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("id/{myId}")
    public ResponseEntity<?> findJournalById(@PathVariable ObjectId myId) {
        Optional<JournalEntry> journalEntry = journalEntryService.findById(myId);
        return journalEntry.isPresent() ?
                new ResponseEntity<>(journalEntry.get(), HttpStatus.OK) :
                new ResponseEntity<>("Journal not found", HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("id/{username}/{myId}")
    public ResponseEntity<?> deleteJournalById(@PathVariable ObjectId myId, @PathVariable String username) {
        if (!journalEntryService.existsById(myId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "Journal not found"));
        }
        journalEntryService.deleteById(myId,username);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("id/{username}/{myId}")
    public ResponseEntity<?> updateJournalById(
            @PathVariable ObjectId myId,
            @RequestBody JournalEntry newEntry,
            @PathVariable String username
    ) {
        JournalEntry oldEntry = journalEntryService.findById(myId).orElse(null);
        if (oldEntry != null) {
            oldEntry.setTitle(newEntry.getTitle() != null && !newEntry.getTitle().isEmpty() ? newEntry.getTitle() : oldEntry.getTitle());
            oldEntry.setContent(newEntry.getContent() != null && !newEntry.getContent().isEmpty() ? newEntry.getContent() : oldEntry.getContent());
            journalEntryService.updateEntry(oldEntry);
            return new ResponseEntity<>(oldEntry, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
