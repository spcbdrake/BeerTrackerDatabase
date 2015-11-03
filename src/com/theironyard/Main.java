package com.theironyard;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    static ArrayList<Beer> selectBeers (Connection conn) throws SQLException {
        ArrayList<Beer> beers = new ArrayList();
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery("SELECT * FROM beers");
        int id = 1;
        while (results.next()) {
            Beer beer = new Beer();
            beer.id = results.getInt("id");
            beer.name = results.getString("name");
            beer.type = results.getString("type");
            beers.add(beer);
        }
        return beers;
    }

    static void editBeers (Connection conn, int id, String name, String type) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE beers SET name = ?, type = ? WHERE id = ?");
        stmt.setString(1, name);
        stmt.setString(2, type);
        stmt.setInt(3, id);
        stmt.execute();
    }

    static void insertBeers (Connection conn, Beer beer) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO beers VALUES (NULL, ?, ?)");
        stmt.setString(1, beer.name);
        stmt.setString(2, beer.type);
        stmt.execute();
    }

    static void deleteBeers (Connection conn, int selectBeer) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM beers WHERE id = ?");
        stmt.setInt(1, selectBeer);
        stmt.execute();
    }

    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");

        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS beers (id IDENTITY, name VARCHAR, type VARCHAR)");


        Spark.get(
                "/",
                ((request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    if (username == null) {
                        return new ModelAndView(new HashMap(), "not-logged-in.html");
                    }
                    HashMap m = new HashMap();
                    m.put("username", username);
                    m.put("beers", selectBeers(conn));
                    return new ModelAndView(m, "logged-in.html");
                }),
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/login",
                ((request, response) -> {
                    String username = request.queryParams("username");
                    Session session = request.session();
                    session.attribute("username", username);
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/create-beer",
                ((request, response) -> {
                    Beer beer = new Beer();
                    beer.name = request.queryParams("beername");
                    beer.type = request.queryParams("beertype");
                    insertBeers(conn, beer);
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/delete-beer",
                ((request, response) -> {
                    String id = request.queryParams("beerid");
                    try {
                        int idNum = Integer.valueOf(id);
                        deleteBeers(conn, idNum);
                    } catch (Exception e) {

                    }

                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/edit-beer",
                ((request, response) -> {
                    String id = request.queryParams("beerid");
                    String name = request.queryParams("beername");
                    String type = request.queryParams("beertype");
                    try {
                        int idNum = Integer.valueOf(id);
                        editBeers(conn, idNum, name, type);

                    }
                    catch (Exception e){

                    }

                    response.redirect("/");
                    return "";
                })
        );
    }
}
