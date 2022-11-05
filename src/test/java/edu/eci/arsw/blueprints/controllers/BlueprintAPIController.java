/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blueprints.controllers;

import java.util.*;

import com.google.gson.Gson;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author hcadavid
 */
@RestController
@RequestMapping(value = "/blueprints")
public class BlueprintAPIController {
    @Autowired
    @Qualifier("blueprintsServices")
    BlueprintsServices blueprintsServices;

    /*GET*/
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> getBlueprints() {
        Set<Blueprint> blueprintSet = blueprintsServices.getAllBlueprints();
        String gsonString = this.makeStringForGson(blueprintSet);

        return new ResponseEntity<>(new Gson().toJson(gsonString), HttpStatus.ACCEPTED);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/authors/{author}")
    public ResponseEntity<?> getBlueprintsByAuthor(@PathVariable String author) {
        Set<Blueprint> blueprintSet = blueprintsServices.getBlueprintsByAuthor(author);
        String gsonString = this.makeStringForGson(blueprintSet);

        return new ResponseEntity<>(new Gson().toJson(gsonString), HttpStatus.ACCEPTED);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/authors/{author}/blueprints/{bpname}")
    public ResponseEntity<?> getBlueprintsByAuthor(@PathVariable String author, @PathVariable String bpname) {
        Blueprint blueprint = blueprintsServices.getBlueprint(author, bpname);
        Set<Blueprint> blueprintSet = new HashSet<Blueprint>();

        blueprintSet.add(blueprint);

        String gsonString = this.makeStringForGson(blueprintSet);

        return new ResponseEntity<>(new Gson().toJson(gsonString), HttpStatus.ACCEPTED);
    }

    /*POST*/
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ResponseEntity<?> manejadorPostRecursoPlanos(@RequestBody JSONObject blueprint) {
        try {
            Blueprint temp = new Blueprint(blueprint.get("Author").toString(), blueprint.get("Name").toString());
            String[] list = blueprint.get("Points").toString().split("-");
            for (String str : list) {
                List<String> templis = Arrays.asList(str.split(","));
                String corX = templis.get(0).substring(2);
                String corY = templis.get(1).substring(2, templis.get(0).length());
                Point po = new Point(Integer.parseInt(corX), Integer.parseInt(corY));
                temp.addPoint(po);
            }

            blueprintsServices.addNewBlueprint(temp);
            return new ResponseEntity<>(HttpStatus.CREATED.getReasonPhrase(), HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.FORBIDDEN.getReasonPhrase(), HttpStatus.FORBIDDEN);
        }
    }

    @RequestMapping(value = "/authors/{author}/blueprints/{bpname}", method = RequestMethod.PUT)
    public ResponseEntity<?> putBlueprintsByAuthor(@PathVariable String author, @PathVariable String bpname, @RequestBody JSONObject data) throws JSONException {
        Blueprint blueprint = blueprintsServices.getBlueprint(author, bpname);
        String[] list = data.get("Puntos").toString().split("-");
        blueprint.setAuthor(data.get("Autor").toString());
        blueprint.setName(data.get("Nombre").toString());

        for (String str : list) {
            List<String> templis = Arrays.asList(str.split(","));
            String corX = templis.get(0).substring(2);
            String corY = templis.get(1).substring(2, templis.get(0).length());
            Point po = new Point(Integer.parseInt(corX), Integer.parseInt(corY));
            blueprint.addPoint(po);
        }

        Set<Blueprint> blueprintSet = new HashSet<Blueprint>();
        blueprintSet.add(blueprint);
        String gsonString = this.makeStringForGson(blueprintSet);

        return new ResponseEntity<>(new Gson().toJson(gsonString), HttpStatus.CREATED);
    }

    private String makeStringForGson(Set<Blueprint> blueprints) {
        List<Blueprint> blueprintList = new ArrayList<>(blueprints);
        String blueprintsString = "{\"blueprints\" : ";

        for (Blueprint blueprint:blueprintList) {
            String author = blueprint.getAuthor();
            String name = blueprint.getName();
            String points = blueprint.getPointsString();
            blueprintsString += "{\"Author\": \"" + author + "\", \"Name\": \"" + name + "\", \"Points\": \"" + points + "\"}";
        }

        blueprintsString += "}";

        return blueprintsString;
    }
}
