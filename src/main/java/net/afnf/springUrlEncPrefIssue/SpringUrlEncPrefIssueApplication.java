package net.afnf.springUrlEncPrefIssue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@SpringBootApplication
@Controller
public class SpringUrlEncPrefIssueApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringUrlEncPrefIssueApplication.class, args);
    }

    @RequestMapping(value = "/")
    public ModelAndView index() {

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < 5000; i++) {
            Entry e = new Entry();
            e.id = i;
            entries.add(e);
        }

        Map<String, Object> model = new HashMap<>();
        model.put("entries", entries);

        return new ModelAndView("index", model);
    }

    public class Entry {
        public int id;
    }
}
